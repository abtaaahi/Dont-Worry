package com.abtahiapp.dontworry.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.BuildConfig
import com.abtahiapp.dontworry.adapter.HomeAdapter
import com.abtahiapp.dontworry.GoogleCustomSearchResponse
import com.abtahiapp.dontworry.HomeItem
import com.abtahiapp.dontworry.HomeItemType
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.RetrofitClient
import com.abtahiapp.dontworry.VideoResponse
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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

        fetchVideos(videoQuery) { videos ->
            fetchAudios(audioQuery) { audios ->
                fetchArticles(articleQuery) { articles ->
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
        }
    }

    private fun fetchVideos(query: String, callback: (List<HomeItem>) -> Unit) {
        RetrofitClient.youtubeInstance.getVideos(query = query, apiKey = BuildConfig.GOOGLE_API_KEY)
            .enqueue(createResponseCallback(HomeItemType.VIDEO, callback))
    }

    private fun fetchAudios(query: String, callback: (List<HomeItem>) -> Unit) {
        RetrofitClient.youtubeInstance.getVideos(query = query, apiKey = BuildConfig.GOOGLE_API_KEY)
            .enqueue(createResponseCallback(HomeItemType.AUDIO, callback))
    }

    private fun fetchArticles(query: String, callback: (List<HomeItem>) -> Unit) {
        RetrofitClient.instance.getSearchResults(query, BuildConfig.CUSTOM_SEARCH_ENGINE_ID, BuildConfig.GOOGLE_API_KEY)
            .enqueue(object : Callback<GoogleCustomSearchResponse> {
                override fun onResponse(call: Call<GoogleCustomSearchResponse>, response: Response<GoogleCustomSearchResponse>) {
                    if (response.isSuccessful) {
                        val articles = response.body()?.items?.map { item ->
                            HomeItem(
                                id = item.link,
                                title = item.title,
                                description = item.snippet,
                                imageUrl = item.pagemap?.cse_image?.firstOrNull()?.src ?: "",
                                type = HomeItemType.ARTICLE
                            )
                        } ?: emptyList()
                        callback(articles)
                    } else callback(emptyList())
                }

                override fun onFailure(call: Call<GoogleCustomSearchResponse>, t: Throwable) {
                    callback(emptyList())
                }
            })
    }

    private fun createResponseCallback(type: HomeItemType, callback: (List<HomeItem>) -> Unit): Callback<VideoResponse> {
        return object : Callback<VideoResponse> {
            override fun onResponse(call: Call<VideoResponse>, response: Response<VideoResponse>) {
                if (response.isSuccessful) {
                    val items = response.body()?.items?.map { video ->
                        HomeItem(
                            id = video.id.videoId,
                            title = video.snippet.title,
                            description = null,
                            imageUrl = video.snippet.thumbnails.high.url,
                            type = type
                        )
                    } ?: emptyList()
                    callback(items)
                } else callback(emptyList())
            }

            override fun onFailure(call: Call<VideoResponse>, t: Throwable) {
                callback(emptyList())
            }
        }
    }

    private fun updateUI(items: List<HomeItem>) {
        homeAdapter.updateItems(items)
    }
}
