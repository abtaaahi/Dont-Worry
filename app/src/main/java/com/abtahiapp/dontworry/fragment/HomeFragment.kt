package com.abtahiapp.dontworry.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.Article
import com.abtahiapp.dontworry.Movie
import com.abtahiapp.dontworry.MovieResponse
import com.abtahiapp.dontworry.VideoItem
import com.abtahiapp.dontworry.adapter.HomeAdapter
import android.widget.Toast
import com.abtahiapp.dontworry.HomeItem
import com.abtahiapp.dontworry.HomeItemType
import com.abtahiapp.dontworry.NewsResponse
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val homeRecyclerView: RecyclerView = view.findViewById(R.id.home_recycler_view)

        homeRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        homeAdapter = HomeAdapter(requireContext(), mutableListOf())
        homeRecyclerView.adapter = homeAdapter

        account = activity?.intent?.getParcelableExtra("account") ?: return view

        database = FirebaseDatabase.getInstance().getReference("user_information")

        fetchItems()

        return view
    }

    private fun fetchItems() {
        fetchVideos { videos ->
            fetchArticles { articles ->
                fetchAudios{ audios ->

                    val allItems = mutableListOf<HomeItem>()

                    allItems.addAll(videos)
                    allItems.addAll(articles)
                    allItems.addAll(audios)

                    allItems.shuffle()

                    homeAdapter.updateItems(allItems)
                }
            }
        }
    }

    private fun fetchVideos(callback: (List<HomeItem>) -> Unit) {
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


                    val apiKey = "AIzaSyBi_Bg1FYzX9R6yIfREZZH0_yatJ5hkerw"
                    val query = when (lastMood) {
                        "Angry" -> "stress management"
                        "Very Sad", "Sad" -> "mental health"
                        "Fine", "Very Fine" -> "positive thinking"
                        else -> "mindfulness"
                    }


                    RetrofitClient.youtubeInstance.getVideos(query = query, apiKey = apiKey)
                        .enqueue(object : Callback<VideoResponse> {
                            override fun onResponse(call: Call<VideoResponse>, response: Response<VideoResponse>) {
                                if (response.isSuccessful) {
                                    val videoItems = response.body()?.items ?: emptyList()

                                    val videoList = videoItems.map { video ->
                                        HomeItem(
                                            id = video.id.videoId,
                                            title = video.snippet.title,
                                            description = null,
                                            imageUrl = video.snippet.thumbnails.high.url,
                                            type = HomeItemType.VIDEO
                                        )
                                    }
                                    callback(videoList)
                                } else {
                                    callback(emptyList())
                                }
                            }

                            override fun onFailure(call: Call<VideoResponse>, t: Throwable) {
                                callback(emptyList())
                            }
                        })
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    callback(emptyList())
                }
            })
    }

    private fun fetchAudios(callback: (List<HomeItem>) -> Unit) {
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

                    val apiKey = "AIzaSyBi_Bg1FYzX9R6yIfREZZH0_yatJ5hkerw"
                    val query = when (lastMood) {
                        "Angry" -> "relaxing music"
                        "Very Sad", "Sad" -> "soothing music"
                        "Fine", "Very Fine" -> "cheerful music"
                        else -> "calmness music"
                    }

                    RetrofitClient.youtubeInstance.getVideos(query = query, apiKey = apiKey)
                        .enqueue(object : Callback<VideoResponse> {
                            override fun onResponse(call: Call<VideoResponse>, response: Response<VideoResponse>) {
                                if (response.isSuccessful) {
                                    val videoItems = response.body()?.items ?: emptyList()

                                    val videoList = videoItems.map { video ->
                                        HomeItem(
                                            id = video.id.videoId,
                                            title = video.snippet.title,
                                            description = null,
                                            imageUrl = video.snippet.thumbnails.high.url,
                                            type = HomeItemType.AUDIO
                                        )
                                    }
                                    callback(videoList)
                                } else {
                                    callback(emptyList())
                                }
                            }

                            override fun onFailure(call: Call<VideoResponse>, t: Throwable) {
                                callback(emptyList())
                            }
                        })
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    callback(emptyList())
                }
            })
    }

    private fun fetchArticles(callback: (List<HomeItem>) -> Unit) {
        val apiKey = "d2bdc009335842078a30d4ba304212a0"
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

                    val query = when (lastMood) {
                        "Angry" -> "stress management"
                        "Very Sad", "Sad" -> "mental health"
                        "Fine", "Very Fine" -> "positive thinking"
                        else -> "mindfulness"
                    }

                    RetrofitClient.instance.getTopHeadlines(query, apiKey)
                        .enqueue(object : Callback<NewsResponse> {
                            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                                if (response.isSuccessful) {
                                    val articleItems = response.body()?.articles ?: emptyList()
                                    val articleList = articleItems.map { article ->
                                        HomeItem(
                                            id = article.url,
                                            title = article.title,
                                            description = article.description,
                                            imageUrl = article.urlToImage ?: "",
                                            type = HomeItemType.ARTICLE
                                        )
                                    }
                                    callback(articleList)
                                } else {
                                    callback(emptyList())
                                }
                            }

                            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                                callback(emptyList())
                                Toast.makeText(requireContext(), "Failed to fetch articles", Toast.LENGTH_SHORT).show()
                            }
                        })
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to check mood history", Toast.LENGTH_SHORT).show()
                    callback(emptyList())
                }
            })
    }
}