package com.abtahiapp.dontworry.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.abtahiapp.dontworry.BuildConfig
import com.abtahiapp.dontworry.utils.HomeFeedItem
import com.abtahiapp.dontworry.adapter.HomeAdapter
import com.abtahiapp.dontworry.utils.HomeItem
import com.abtahiapp.dontworry.utils.HomeItemType
import com.abtahiapp.dontworry.utils.Post
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.utils.RetrofitClient
import com.abtahiapp.dontworry.query.ArticleVideoQuery
import com.abtahiapp.dontworry.room.HomeDatabase
import com.abtahiapp.dontworry.room.HomeItemEntity
import com.abtahiapp.dontworry.room.HomePostEntity
import com.abtahiapp.dontworry.utils.NetworkUtil.isOnline
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume

class HomeFragment : Fragment() {

    private lateinit var homeAdapter: HomeAdapter
    private lateinit var database: DatabaseReference
    private lateinit var account: GoogleSignInAccount
    //private lateinit var progressBar: ProgressBar
    private lateinit var progressBar: LottieAnimationView
    private lateinit var recyclerView: RecyclerView
    private lateinit var postList: MutableList<Post>
    private lateinit var homeItemList: MutableList<HomeItem>
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var databaseRoom: HomeDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        progressBar = view.findViewById(R.id.progress_bar)
        recyclerView = view.findViewById(R.id.home_recycler_view)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)

        postList = mutableListOf()
        homeItemList = mutableListOf()

        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        homeAdapter = HomeAdapter(requireContext(), emptyList())

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                swipeRefreshLayout.isEnabled = !recyclerView.canScrollVertically(-1)
            }
        })

        recyclerView.adapter = homeAdapter

        account = activity?.intent?.getParcelableExtra("account") ?: return view
        database = FirebaseDatabase.getInstance().getReference("user_information")
        databaseRoom = HomeDatabase.getDatabase(requireContext())

        showLoading(true)

        if (isOnline(requireContext())) {
            fetchItemsFromApi()
        } else {
            loadFromLocalDatabase()
            showLoading(false)
        }

        swipeRefreshLayout.setOnRefreshListener {
            if (isOnline(requireContext())) {
                fetchItemsFromApi()
            } else {
                swipeRefreshLayout.isRefreshing = false
                Toast.makeText(requireContext(), "You are offline.", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun fetchItemsFromApi() {
        lifecycleScope.launch {
            try {
                val posts = loadPostsFromFirebase()
                val items = fetchDataFromApi()

                if (isAdded) {
                    saveToLocalDatabase(posts, items)
                    updateUI(posts, items)
                }
            } catch (e: Exception) {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Error fetchingdata.", Toast.LENGTH_SHORT).show()
                    loadFromLocalDatabase()
                }
            } finally {
                if (isAdded) {
                    swipeRefreshLayout.isRefreshing = false
                    showLoading(false)
                }
            }
        }
    }

    private fun loadFromLocalDatabase() {
        lifecycleScope.launch {
            val localPosts = databaseRoom.homePostDao().getAllPosts()
            val localItems = databaseRoom.homeItemDao().getAllHomeItems()

            val posts = localPosts.map { it.toPost() }
            val items = localItems.map { it.toHomeItem() }
            Log.d("RoomPosts", "Loaded Post from Room: $localPosts")
            updateUI(posts, items)
            showLoading(false)
        }
    }

    private suspend fun loadPostsFromFirebase(): List<Post> {
        return withContext(Dispatchers.IO) {
            val posts = mutableListOf<Post>()
            val databaseReference = FirebaseDatabase.getInstance().getReference("social_posts")

            val task = suspendCancellableCoroutine<List<Post>> { continuation ->
                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (postSnapshot in snapshot.children) {
                            val post = postSnapshot.getValue(Post::class.java)
                            if (post != null) {
                                post.id = postSnapshot.key ?: "Unknown ID"
                                posts.add(post)
                            }
                        }
                        Log.d("FirebasePosts", "Saving Post: $posts")

                        continuation.resume(posts.sortedByDescending { it.postTime })
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(emptyList())
                    }
                })
            }

            return@withContext task
        }
    }

    private suspend fun fetchDataFromApi(): List<HomeItem> {
        return withContext(Dispatchers.IO) {
            val moodHistoryRef = FirebaseDatabase.getInstance()
                .getReference("user_information")
                .child(account.id!!).child("mood_history")

            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val queryTask = suspendCancellableCoroutine<Triple<String, String, String>> { continuation ->
                moodHistoryRef.orderByChild("date").equalTo(currentDate)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val lastMood = dataSnapshot.children.lastOrNull()?.child("mood")?.getValue(String::class.java)
                            val queries = ArticleVideoQuery.getQueries(lastMood)
                            val query = queries.random()
                            val musicQueries = queries.map { "$it music" }
                            val musicQuery = musicQueries.random()

                            continuation.resume(Triple(query, musicQuery, query))
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            continuation.resume(Triple("", "", ""))
                        }
                    })
            }

            val (videoQuery, audioQuery, articleQuery) = queryTask
            val videos = fetchVideos(videoQuery)
            val audios = fetchAudios(audioQuery)
            val articles = fetchArticles(articleQuery)

            return@withContext mutableListOf<HomeItem>().apply {
                addAll(videos)
                addAll(audios)
                addAll(articles)
            }
        }
    }

    private suspend fun fetchVideos(query: String): List<HomeItem> {
        return try {
            val response = RetrofitClient.youtubeInstance.getVideos(query = query, apiKey = BuildConfig.GOOGLE_API_KEY)
            response.items.map { video ->
                HomeItem(
                    id = video.id.videoId,
                    title = video.snippet.title,
                    description = null,
                    imageUrl = video.snippet.thumbnails.high.url,
                    type = HomeItemType.VIDEO
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun fetchAudios(query: String): List<HomeItem> {
        return try {
            val response = RetrofitClient.youtubeInstance.getVideos(query = query, apiKey = BuildConfig.GOOGLE_API_KEY)
            response.items.map { video ->
                HomeItem(
                    id = video.id.videoId,
                    title = video.snippet.title,
                    description = null,
                    imageUrl = video.snippet.thumbnails.high.url,
                    type = HomeItemType.AUDIO
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun fetchArticles(query: String): List<HomeItem> {
        return try {
            val response = RetrofitClient.instance.getSearchResults(query, BuildConfig.CUSTOM_SEARCH_ENGINE_ID, BuildConfig.GOOGLE_API_KEY)
            response.items.map { item ->
                HomeItem(
                    id = item.link,
                    title = item.title,
                    description = item.snippet,
                    imageUrl = item.pagemap?.cse_image?.firstOrNull()?.src ?: "",
                    type = HomeItemType.ARTICLE
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun updateUI(posts: List<Post>, items: List<HomeItem>) {
        val allItems = mutableListOf<HomeFeedItem>().apply {
            addAll(posts.map { HomeFeedItem.PostItem(it) })
            addAll(items.map { HomeFeedItem.HomeItemItem(it) })
            shuffle()
        }
        homeAdapter.updateItems(allItems)
    }

    private suspend fun saveToLocalDatabase(posts: List<Post>, items: List<HomeItem>) {
        withContext(Dispatchers.IO) {
            val postEntities = posts.map { it.toHomePostEntity() }
            val itemEntities = items.map { it.toHomeItemEntity() }

            databaseRoom.homePostDao().clearPosts()
            databaseRoom.homeItemDao().clearHomeItems()

            databaseRoom.homePostDao().insertPosts(postEntities)
            databaseRoom.homeItemDao().insertHomeItems(itemEntities)
        }
    }
}

private fun Post.toHomePostEntity() = HomePostEntity(
    id = this.id,
    userName = this.userName ?: "Unknown User",
    userPhotoUrl = this.userPhotoUrl ?: "",
    content = this.content ?: "No content",
    postTime = this.postTime ?: "Unknown time"
)
private fun HomeItem.toHomeItemEntity() = HomeItemEntity(id, title, description, imageUrl, type)

private fun HomePostEntity.toPost() = Post(
    id = this.id,
    userName = this.userName,
    userPhotoUrl = this.userPhotoUrl,
    content = this.content,
    postTime = this.postTime
    )
private fun HomeItemEntity.toHomeItem() = HomeItem(id, title, description, imageUrl, type)