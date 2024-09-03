package com.abtahiapp.dontworry.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.BuildConfig
import com.abtahiapp.dontworry.adapter.HomeAdapter
import com.abtahiapp.dontworry.HomeItem
import com.abtahiapp.dontworry.HomeItemType
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.RetrofitClient
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
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        progressBar = view.findViewById(R.id.progress_bar)
        recyclerView = view.findViewById(R.id.home_recycler_view)

        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        homeAdapter = HomeAdapter(requireContext(), mutableListOf())
        recyclerView.adapter = homeAdapter

        account = activity?.intent?.getParcelableExtra("account") ?: return view
        database = FirebaseDatabase.getInstance().getReference("user_information")

        showLoading(true)
        fetchItems()
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

    private fun fetchItems() {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val moodHistoryRef = database.child(account.id!!).child("mood_history")
        moodHistoryRef.orderByChild("date").equalTo(currentDate)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val lastMood = dataSnapshot.children.lastOrNull()?.child("mood")?.getValue(String::class.java)
                    val queryType = when (lastMood) {
                        "Angry" -> Triple("stress management", "relaxing music", "stress management")
                        "Very Sad", "Sad" -> Triple("mental health", "soothing music", "mental health")
                        "Fine", "Very Fine" -> Triple("positive thinking", "cheerful music", "positive thinking")
                        else -> Triple("mindfulness", "calmness music", "mindfulness")
                    }
                    fetchData(queryType)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    updateUI(emptyList())
                    showLoading(false)
                }
            })
    }

    private fun fetchData(queryType: Triple<String, String, String>) {
        val (videoQuery, audioQuery, articleQuery) = queryType

        lifecycleScope.launch {
            val videos = fetchVideos(videoQuery)
            val audios = fetchAudios(audioQuery)
            val articles = fetchArticles(articleQuery)

            val allItems = mutableListOf<HomeItem>().apply {
                addAll(videos)
                addAll(audios)
                addAll(articles)
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

    private fun updateUI(items: List<HomeItem>) {
        homeAdapter.updateItems(items)
    }
}