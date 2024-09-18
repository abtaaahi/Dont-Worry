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
import com.abtahiapp.dontworry.adapter.AudioAdapter
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.abtahiapp.dontworry.query.ArticleVideoQuery
import com.airbnb.lottie.LottieAnimationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MusicFragment : Fragment() {

    private lateinit var audioAdapter: AudioAdapter
    private lateinit var account: GoogleSignInAccount
    private lateinit var database: DatabaseReference
    private lateinit var audioRecyclerView: RecyclerView
    private lateinit var progressBar: LottieAnimationView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_music, container, false)
        audioRecyclerView = view.findViewById(R.id.audio_recycler_view)
        progressBar = view.findViewById(R.id.progress_bar)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)

        audioRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        audioAdapter = AudioAdapter(requireContext(), mutableListOf())
        audioRecyclerView.adapter = audioAdapter

        account = activity?.intent?.getParcelableExtra("account") ?: return view

        database = FirebaseDatabase.getInstance().getReference("user_information")

        showLoading(true)
        fetchLastMoodAndAudios()

        swipeRefreshLayout.setOnRefreshListener {
            fetchAudios(null)
        }

        return view
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            audioRecyclerView.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
            audioRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun fetchLastMoodAndAudios() {
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
                    fetchAudios(lastMood)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Failed to check mood history", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun fetchAudios(mood: String?) {
        showLoading(true)
        swipeRefreshLayout.isRefreshing = false
        val apiKey = BuildConfig.GOOGLE_API_KEY
        val queries = ArticleVideoQuery.getQueries(mood).map { "$it music" }
        val query = queries.random()

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.youtubeInstance.getVideos(query = query, apiKey = apiKey)
                }
                audioAdapter.updateAudios(response.items)
            } catch (e: Exception) {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Failed to fetch music", Toast.LENGTH_SHORT).show()
                } else {

                }
            } finally {
                if (isAdded) {
                    showLoading(false)
                }
            }
        }
    }
}