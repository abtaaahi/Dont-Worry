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
import com.abtahiapp.dontworry.adapter.ArticleAdapter
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ArticleFragment : Fragment() {

    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var account: GoogleSignInAccount
    private lateinit var database: DatabaseReference
    private lateinit var articleRecyclerView: RecyclerView
    private lateinit var progressBar: LottieAnimationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_article, container, false)
        articleRecyclerView = view.findViewById(R.id.article_recycler_view)
        progressBar = view.findViewById(R.id.progress_bar)

        articleRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        articleAdapter = ArticleAdapter(requireContext(), mutableListOf())
        articleRecyclerView.adapter = articleAdapter

        account = activity?.intent?.getParcelableExtra("account") ?: return view

        database = FirebaseDatabase.getInstance().getReference("user_information")

        showLoading(true)
        fetchLastMoodAndArticles()

        return view
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            articleRecyclerView.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
            articleRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun fetchLastMoodAndArticles() {
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
                    fetchArticles(lastMood)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Failed to check mood history", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun fetchArticles(mood: String?) {
        val apiKey = BuildConfig.GOOGLE_API_KEY
        val customSearchEngineId = BuildConfig.CUSTOM_SEARCH_ENGINE_ID
        // https://programmablesearchengine.google.com/controlpanel/all

        val query = when (mood) {
            "Angry" -> "stress management"
            "Very Sad", "Sad" -> "mental health"
            "Fine", "Very Fine" -> "positive thinking"
            else -> "mindfulness"
        }

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.getSearchResults(query, customSearchEngineId, apiKey)
                }
                articleAdapter.updateArticles(response.items)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to fetch articles", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }
}

// NewsAPI

//        private fun fetchArticles(mood: String?) {
//        val apiKey = "d2bdc009335842078a30d4ba304212a0"
//        val query = when (mood) {
//            "Angry" -> "stress management"
//            "Very Sad", "Sad" -> "mental health"
//            "Fine", "Very Fine" -> "positive thinking"
//            else -> "mindfulness"
//        }
//
//        RetrofitClient.instance.getTopHeadlines(query, apiKey)
//            .enqueue(object : Callback<NewsResponse> {
//                override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
//                    if (response.isSuccessful) {
//                        val articles = response.body()?.articles ?: emptyList()
//                        articleAdapter.updateArticles(articles)
//                    }
//                }
//
//                override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
//                    Toast.makeText(requireContext(), "Failed to fetch articles", Toast.LENGTH_SHORT).show()
//                }
//            })
//    }