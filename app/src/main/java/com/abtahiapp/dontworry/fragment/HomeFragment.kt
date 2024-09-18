package com.abtahiapp.dontworry.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.abtahiapp.dontworry.BuildConfig
import com.abtahiapp.dontworry.HomeFeedItem
import com.abtahiapp.dontworry.adapter.HomeAdapter
import com.abtahiapp.dontworry.HomeItem
import com.abtahiapp.dontworry.HomeItemType
import com.abtahiapp.dontworry.Post
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.RetrofitClient
import com.abtahiapp.dontworry.query.ArticleVideoQuery
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

        showLoading(true)
        loadPostsFromDatabase()
        fetchItems()

        swipeRefreshLayout.setOnRefreshListener {
            fetchItems()
            loadPostsFromDatabase()
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

    private fun loadPostsFromDatabase() {
        val databaseReference = FirebaseDatabase.getInstance().getReference("social_posts")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val posts = mutableListOf<Post>()
                for (postSnapshot in snapshot.children) {
                    val post = postSnapshot.getValue(Post::class.java)
                    if (post != null) {
                        posts.add(post)
                    }
                }
                posts.sortByDescending { it.postTime }
                postList.clear()
                postList.addAll(posts)
                homeAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                swipeRefreshLayout.isRefreshing = false
            }
        })
    }

    private fun fetchItems() {
        swipeRefreshLayout.isRefreshing = false
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val moodHistoryRef = database.child(account.id!!).child("mood_history")
        moodHistoryRef.orderByChild("date").equalTo(currentDate)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val lastMood = dataSnapshot.children.lastOrNull()?.child("mood")?.getValue(String::class.java)

                    val queries = ArticleVideoQuery.getQueries(lastMood)
                    val query = queries.random()
                    val musicQueries = queries.map { "$it music" }
                    val musicQuery = musicQueries.random()

                    val queryType = Triple(query, musicQuery, query)

                    fetchData(queryType)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    updateUI(emptyList())
                    showLoading(false)
                    swipeRefreshLayout.isRefreshing = false
                }
            })
    }

    private fun fetchData(queryType: Triple<String, String, String>) {
        val (videoQuery, audioQuery, articleQuery) = queryType

        lifecycleScope.launch {
            val videos = fetchVideos(videoQuery)
            val audios = fetchAudios(audioQuery)
            val articles = fetchArticles(articleQuery)

            val allItems= mutableListOf<HomeFeedItem>().apply {
                postList.forEach { add(HomeFeedItem.PostItem(it)) }
                addAll(videos.map { HomeFeedItem.HomeItemItem(it) })
                addAll(audios.map { HomeFeedItem.HomeItemItem(it) })
                addAll(articles.map { HomeFeedItem.HomeItemItem(it) })
                shuffle()
            }
            updateUI(allItems)
            showLoading(false)
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

    private fun updateUI(items: List<HomeFeedItem>) {
        homeAdapter.updateItems(items)
    }
}