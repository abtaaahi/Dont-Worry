package com.abtahiapp.dontworry.activity

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.BuildConfig
import com.abtahiapp.dontworry.Movie
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.RetrofitClient
import com.abtahiapp.dontworry.adapter.MoreMovieAdapter
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MovieActivity : AppCompatActivity() {

    private lateinit var movieAdapter: MoreMovieAdapter
    private lateinit var movieRecyclerView: RecyclerView
    private lateinit var youTubePlayer: YouTubePlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie)

        movieRecyclerView = findViewById(R.id.movie_recycler_view)

        movieRecyclerView.layoutManager = GridLayoutManager(this, 3)
        movieAdapter = MoreMovieAdapter(this, mutableListOf()){ movie ->
            updateMovieDetails(movie)
        }
        movieRecyclerView.adapter = movieAdapter

        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")

        findViewById<TextView>(R.id.movietitle).text = title
        findViewById<TextView>(R.id.moviedescription).text = description

        val youTubePlayerView: YouTubePlayerView = findViewById(R.id.youtube_player_view)
        lifecycle.addObserver(youTubePlayerView)

        youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                this@MovieActivity.youTubePlayer = youTubePlayer
                val initialTrailerUrl = intent.getStringExtra("trailerUrl")
                initialTrailerUrl?.let {
                    youTubePlayer.loadVideo(it, 0f)
                }
            }
        })
        fetchMovies("")
    }

    private fun updateMovieDetails(movie: Movie) {
        movie.trailerUrl?.let {
            youTubePlayer.loadVideo(it, 0f)
        }

        findViewById<TextView>(R.id.movietitle).text = movie.title
        findViewById<TextView>(R.id.moviedescription).text = movie.description
    }

    private fun fetchMovies(mood: String?) {
        CoroutineScope(Dispatchers.Main).launch {
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
            } catch (e: Exception) {
                Toast.makeText(this@MovieActivity, "Failed to fetch movies", Toast.LENGTH_SHORT).show()
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