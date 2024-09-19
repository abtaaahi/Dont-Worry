package com.abtahiapp.dontworry.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.BuildConfig
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.utils.RetrofitClient
import com.abtahiapp.dontworry.adapter.VideoAdapter
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import com.abtahiapp.dontworry.query.ArticleVideoQuery
import com.airbnb.lottie.LottieAnimationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.abtahiapp.dontworry.utils.NetworkUtil.isOnline
import com.abtahiapp.dontworry.utils.Snippet
import com.abtahiapp.dontworry.utils.Thumbnail
import com.abtahiapp.dontworry.utils.Thumbnails
import com.abtahiapp.dontworry.utils.VideoId
import com.abtahiapp.dontworry.utils.VideoItem
import com.abtahiapp.dontworry.room.VideoDatabase
import com.abtahiapp.dontworry.room.VideoEntity

class VideoFragment : Fragment() {

    private lateinit var videoAdapter: VideoAdapter
    private lateinit var account: GoogleSignInAccount
    private lateinit var database: DatabaseReference
    private lateinit var videoRecyclerView: RecyclerView
    private lateinit var progressBar: LottieAnimationView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var databaseRoom: VideoDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_video, container, false)
        videoRecyclerView= view.findViewById(R.id.video_recycler_view)
        progressBar = view.findViewById(R.id.progress_bar)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)

        videoRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        videoAdapter = VideoAdapter(requireContext(), mutableListOf())
        videoRecyclerView.adapter = videoAdapter

        account = activity?.intent?.getParcelableExtra("account") ?: return view

        database = FirebaseDatabase.getInstance().getReference("user_information")
        databaseRoom = VideoDatabase.getDatabase(requireContext())

        showLoading(true)
        fetchLastMoodAndVideos()
        if (isOnline(requireContext())) {
            fetchLastMoodAndVideos()
        } else {
            loadVideosFromLocal()
        }

        swipeRefreshLayout.setOnRefreshListener {
            if (isOnline(requireContext())) {
                fetchVideos(null)
            } else {
                swipeRefreshLayout.isRefreshing = false
                Toast.makeText(requireContext(), "You are offline. Cannot refresh.", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            videoRecyclerView.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
            videoRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun fetchLastMoodAndVideos() {
        showLoading(true)
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val moodHistoryRef = database.child(account.id!!).child("mood_history")

        moodHistoryRef.orderByChild("date").equalTo(currentDate)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val lastMood = if (dataSnapshot.exists()) {
                        dataSnapshot.children.last().child("mood").getValue(String::class.java)
                    } else {
                        null
                    }
                    fetchVideos(lastMood)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Failed to check mood history", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun fetchVideos(mood: String?) {
        val apiKey = BuildConfig.GOOGLE_API_KEY

        val queries = ArticleVideoQuery.getQueries(mood)
        val query = queries.random()

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.youtubeInstance.getVideos(query = query, apiKey = apiKey)
                }
                saveVideosToLocal(response.items)
                videoAdapter.updateVideos(response.items)
            } catch (e: Exception) {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Failed to fetch videos", Toast.LENGTH_SHORT).show()
                    loadVideosFromLocal()
                }
            } finally {
                if (isAdded) {
                    showLoading(false)
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    private suspend fun saveVideosToLocal(videos: List<VideoItem>) {
        withContext(Dispatchers.IO) {
            val videoEntities = videos.map {
                VideoEntity(
                    videoId = it.id.videoId,
                    title = it.snippet.title,
                    thumbnailUrl = it.snippet.thumbnails.high.url
                )
            }
            databaseRoom.videoDao().clearVideos()
            databaseRoom.videoDao().insertVideos(videoEntities)
        }
    }

    private fun loadVideosFromLocal() {
        lifecycleScope.launch {
            val savedVideos = withContext(Dispatchers.IO) {
                databaseRoom.videoDao().getAllVideos()
            }

            val videoItems = savedVideos.map {
                VideoItem(
                    id = VideoId(it.videoId),
                    snippet = Snippet(
                        title = it.title,
                        thumbnails = Thumbnails(
                            high = Thumbnail(it.thumbnailUrl)
                        )
                    )
                )
            }

            videoAdapter.updateVideos(videoItems)
            showLoading(false)
        }
    }
}