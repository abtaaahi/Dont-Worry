package com.abtahiapp.dontworry.activity

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.Article
import com.abtahiapp.dontworry.Movie
import com.abtahiapp.dontworry.MovieResponse
import com.abtahiapp.dontworry.adapter.ArticleAdapter
import com.abtahiapp.dontworry.adapter.AudioAdapter
import com.abtahiapp.dontworry.NewsResponse
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.RetrofitClient
import com.abtahiapp.dontworry.TrailerResponse
import com.abtahiapp.dontworry.adapter.VideoAdapter
import com.abtahiapp.dontworry.VideoResponse
import com.abtahiapp.dontworry.adapter.MovieAdapter
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var videoAdapter: VideoAdapter
    private lateinit var audioAdapter: AudioAdapter
    private lateinit var movieAdapter: MovieAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val profileImageView: CircleImageView = findViewById(R.id.profile_image)
        val articleRecyclerView: RecyclerView = findViewById(R.id.article_recycler_view)
        val videoRecyclerView: RecyclerView = findViewById(R.id.video_recycler_view)
        val audioRecyclerView: RecyclerView = findViewById(R.id.audio_recycler_view)
        val movieRecyclerView: RecyclerView = findViewById(R.id.movie_recycler_view)

        val account = intent.getParcelableExtra<GoogleSignInAccount>("account")
        if (account != null) {

            Glide.with(this)
                .load(account.photoUrl)
                .placeholder(R.drawable.person)
                .error(R.drawable.person)
                .fallback(R.drawable.person)
                .into(profileImageView)

            profileImageView.setOnClickListener {
                val intent = Intent(this, MyProfile::class.java)
                intent.putExtra("userId", account.id)
                intent.putExtra("name", account.displayName)
                intent.putExtra("email", account.email)
                intent.putExtra("photoUrl", account.photoUrl.toString())

                startActivity(intent)
            }

            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val database = FirebaseDatabase.getInstance().getReference("user_information")
            val moodHistoryRef = database.child(account.id!!).child("mood_history")

            moodHistoryRef.orderByChild("date").equalTo(currentDate)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            showMoodDialog(account.id!!)
                        } else {
                            val lastMood = dataSnapshot.children.last().child("mood").getValue(String::class.java)
                            fetchArticles(lastMood)
                            fetchVideos(lastMood)
                            fetchAudios(lastMood)
                            fetchMovies(lastMood)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(this@MainActivity, "Failed to check mood history", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        articleRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        articleAdapter = ArticleAdapter(this, mutableListOf<Article>())
        articleRecyclerView.adapter = articleAdapter

        videoRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        videoAdapter = VideoAdapter(this, mutableListOf())
        videoRecyclerView.adapter = videoAdapter

        audioRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        audioAdapter = AudioAdapter(this, mutableListOf())
        audioRecyclerView.adapter = audioAdapter

        movieRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        movieAdapter = MovieAdapter(this, mutableListOf())
        movieRecyclerView.adapter = movieAdapter
    }
    private fun showMoodDialog(userId: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.how_was_the_day_dialog)

        val moodImages = mapOf(
            R.id.mood_1 to "Angry",
            R.id.mood_2 to "Very Sad",
            R.id.mood_3 to "Sad",
            R.id.mood_4 to "Fine",
            R.id.mood_5 to "Very Fine"
        )

        val etDetails: EditText = dialog.findViewById(R.id.et_details)

        var selectedMood: String? = null

        moodImages.forEach { (imageViewId, moodName) ->
            val imageView: ImageView = dialog.findViewById(imageViewId)
            imageView.setOnClickListener {
                if (selectedMood == moodName) {
                    selectedMood = null
                    moodImages.keys.forEach { id ->
                        dialog.findViewById<ImageView>(id).visibility = View.VISIBLE
                    }
                } else {
                    selectedMood = moodName
                    moodImages.keys.forEach { id ->
                        dialog.findViewById<ImageView>(id).visibility = if (id == imageViewId) View.VISIBLE else View.INVISIBLE
                    }
                }
            }
        }

        val button: Button = dialog.findViewById(R.id.submit)
        button.setOnClickListener {
            selectedMood?.let {
                storeMoodInDatabase(userId, it, etDetails.text.toString())
                fetchArticles(it)
                fetchVideos(it)
                fetchAudios(it)
                fetchMovies(it)
                dialog.dismiss()
            } ?: Toast.makeText(this, "Please select a mood", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    private fun storeMoodInDatabase(userId: String, moodName: String, details: String) {
        val database = FirebaseDatabase.getInstance().getReference("user_information")

        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentDateTime = SimpleDateFormat("hh:mm a dd MMM", Locale.getDefault()).format(Date())

        val moodData = mapOf(
            "date" to currentDate,
            "dateTime" to currentDateTime,
            "mood" to moodName,
            "details" to details
        )

        database.child(userId).child("mood_history").push().setValue(moodData).addOnSuccessListener {
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to save mood data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchArticles(mood: String?) {
        val apiKey = "d2bdc009335842078a30d4ba304212a0"
        val query = when (mood) {
            "Angry" -> "stress management"
            "Very Sad", "Sad" -> "mental health"
            "Fine", "Very Fine" -> "positive thinking"
            else -> "mindfulness"
        }

        RetrofitClient.instance.getTopHeadlines(query, apiKey)
            .enqueue(object : Callback<NewsResponse> {
                override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                    if (response.isSuccessful) {
                        val articles = response.body()?.articles ?: emptyList()
                        articleAdapter.updateArticles(articles)
                    }
                }

                override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Failed to fetch articles", Toast.LENGTH_SHORT).show()
                }
            })
    }
    private fun fetchVideos(mood: String?) {
        val apiKey = "AIzaSyBi_Bg1FYzX9R6yIfREZZH0_yatJ5hkerw"
        val query = when (mood) {
            "Angry" -> "stress management"
            "Very Sad", "Sad" -> "mental health"
            "Fine", "Very Fine" -> "positive thinking"
            else -> "mindfulness"
        }

        RetrofitClient.youtubeInstance.getVideos(query = query, apiKey = apiKey)
            .enqueue(object : Callback<VideoResponse> {
                override fun onResponse(call: Call<VideoResponse>, response: Response<VideoResponse>) {
                    if (response.isSuccessful) {
                        val videos = response.body()?.items ?: emptyList()
                        videoAdapter.updateVideos(videos)
                    }
                }

                override fun onFailure(call: Call<VideoResponse>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Failed to fetch videos", Toast.LENGTH_SHORT).show()
                }
            })
    }
    private fun fetchAudios(mood: String?) {
        val apiKey = "AIzaSyBi_Bg1FYzX9R6yIfREZZH0_yatJ5hkerw"
        val query = when (mood) {
            "Angry" -> "relaxing sound"
            "Very Sad", "Sad" -> "soothing sound"
            "Fine", "Very Fine" -> "cheerful sound"
            else -> "calmness sound"
        }

        RetrofitClient.youtubeInstance.getVideos(query = query, apiKey = apiKey)
            .enqueue(object : Callback<VideoResponse> {
                override fun onResponse(call: Call<VideoResponse>, response: Response<VideoResponse>) {
                    if (response.isSuccessful) {
                        val videos = response.body()?.items ?: emptyList()
                        audioAdapter.updateAudios(videos)
                    }
                }

                override fun onFailure(call: Call<VideoResponse>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Failed to fetch videos", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun fetchMovies(mood: String?) {
        val apiKey = "1d3c78106341dfe37711be964e190207"
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
                    } else {
                        Toast.makeText(this@MainActivity, "No movies found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Failed to fetch movies", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Failed to fetch movies", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchTrailersForMovies(movies: List<Movie>) {
        val apiKey = "1d3c78106341dfe37711be964e190207"

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