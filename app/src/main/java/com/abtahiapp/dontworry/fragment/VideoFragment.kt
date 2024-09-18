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
import com.abtahiapp.dontworry.RetrofitClient
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

class VideoFragment : Fragment() {

    private lateinit var videoAdapter: VideoAdapter
    private lateinit var account: GoogleSignInAccount
    private lateinit var database: DatabaseReference
    private lateinit var videoRecyclerView: RecyclerView
    private lateinit var progressBar: LottieAnimationView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

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

        showLoading(true)
        fetchLastMoodAndVideos()

        swipeRefreshLayout.setOnRefreshListener {
            fetchVideos(null)
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
        showLoading(true)
        swipeRefreshLayout.isRefreshing = false

        val apiKey = BuildConfig.GOOGLE_API_KEY

        val queries = ArticleVideoQuery.getQueries(mood)
        val query = queries.random()

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.youtubeInstance.getVideos(query = query, apiKey = apiKey)
                }
                if (isAdded) {
                    videoAdapter.updateVideos(response.items)
                }
            } catch (e: Exception) {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Failed to fetch videos", Toast.LENGTH_SHORT).show()
                }
            } finally {
                if (isAdded) {
                    showLoading(false)
                }
            }
        }
    }
}