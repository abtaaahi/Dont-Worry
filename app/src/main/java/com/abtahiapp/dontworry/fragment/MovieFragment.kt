package com.abtahiapp.dontworry.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.BuildConfig
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.adapter.MovieAdapter
import com.abtahiapp.dontworry.Movie
import com.abtahiapp.dontworry.RetrofitClient
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.abtahiapp.dontworry.TrailerResponse
import com.abtahiapp.dontworry.MovieResponse
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MovieFragment : Fragment() {

    private lateinit var movieAdapter: MovieAdapter
    private lateinit var progressBar: LottieAnimationView
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
        val apiKey = BuildConfig.TMDB_API_KEY
        val genreId = when (mood) {
            "Angry" -> 35 // Comedy
            "Very Sad", "Sad" -> 18 // Drama
            "Fine", "Very Fine" -> 28 // Action
            else -> null // If no mood, fetch popular movies
        }

        val call = if (genreId != null) {
            RetrofitClient.movieInstance.getMoviesByGenre(apiKey, genreId)
        } else {
            RetrofitClient.movieInstance.getMovies(apiKey)
        }

        call.enqueue(object : Callback<MovieResponse> {
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                if (response.isSuccessful) {
                    val movieResponse = response.body()
                    if (movieResponse != null) {
                        val movies = movieResponse.results
                        fetchTrailersForMovies(movies)
                        showLoading(false)
                    } else {
                        Toast.makeText(requireContext(), "No movies found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch movies", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                showLoading(false)
                Toast.makeText(requireContext(), "Failed to fetch movies", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchTrailersForMovies(movies: List<Movie>) {
        val apiKey = BuildConfig.TMDB_API_KEY

        movies.forEach { movie ->
            RetrofitClient.movieInstance.getMovieTrailers(movie.id, apiKey)
                .enqueue(object : Callback<TrailerResponse> {
                    override fun onResponse(call: Call<TrailerResponse>, response: Response<TrailerResponse>) {
                        if (response.isSuccessful) {
                            val trailers = response.body()?.results ?: emptyList()
                            val trailer = trailers.find { it.type == "Trailer" }
                            trailer?.let {
                                movie.trailerUrl = it.key
                            }
                        }
                        movieAdapter.updateMovies(movies.toMutableList())
                    }

                    override fun onFailure(call: Call<TrailerResponse>, t: Throwable) {
                    }
                })
        }
    }
}