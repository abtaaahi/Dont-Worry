package com.abtahiapp.dontworry.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.BuildConfig
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.adapter.MovieAdapter
import com.abtahiapp.dontworry.Movie
import com.abtahiapp.dontworry.RetrofitClient
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MovieFragment : Fragment() {

    private lateinit var movieAdapter: MovieAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var movieRecyclerView: RecyclerView
    private lateinit var account: GoogleSignInAccount
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_movie, container, false)
        movieRecyclerView = view.findViewById(R.id.movie_recycler_view)
        progressBar = view.findViewById(R.id.progress_bar)

        movieRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        movieAdapter = MovieAdapter(requireContext(), mutableListOf())
        movieRecyclerView.adapter = movieAdapter

        account = activity?.intent?.getParcelableExtra("account") ?: return view

        database = FirebaseDatabase.getInstance().getReference("user_information")

        showLoading(true)
        fetchLastMoodAndMovies()

        return view
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            movieRecyclerView.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
            movieRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun fetchLastMoodAndMovies() {
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
                    fetchMovies(lastMood)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Failed to check mood history", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun fetchMovies(mood: String?) {
        CoroutineScope(Dispatchers.Main).launch {
            showLoading(true)
            val apiKey = BuildConfig.TMDB_API_KEY
            val genreId = when (mood) {
                "Angry" -> 35 // Comedy
                "Very Sad", "Sad" -> 18 // Drama
                "Fine", "Very Fine" -> 28 // Action
                else -> null // If no mood, fetch popular movies
            }

            try {
                val movieResponse = withContext(Dispatchers.IO) {
                    if (genreId != null) {
                        RetrofitClient.movieInstance.getMoviesByGenre(apiKey, genreId)
                    } else {
                        RetrofitClient.movieInstance.getMovies(apiKey)
                    }
                }
                fetchTrailersForMovies(movieResponse.results)
                showLoading(false)
            } catch (e: Exception) {
                showLoading(false)
                Toast.makeText(requireContext(), "Failed to fetch movies", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchTrailersForMovies(movies: List<Movie>) {
        CoroutineScope(Dispatchers.Main).launch {
            val apiKey = BuildConfig.TMDB_API_KEY

            movies.forEach { movie ->
                try {
                    val trailerResponse = withContext(Dispatchers.IO) {
                        RetrofitClient.movieInstance.getMovieTrailers(movie.id, apiKey)
                    }
                    val trailer = trailerResponse.results.find { it.type == "Trailer" }
                    trailer?.let {
                        movie.trailerUrl = it.key
                    }
                } catch (e: Exception) {
                }
            }
            movieAdapter.updateMovies(movies.toMutableList())
        }
    }
}
