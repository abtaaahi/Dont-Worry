package com.abtahiapp.dontworry.activity

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.BuildConfig
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.RetrofitClient
import com.abtahiapp.dontworry.VideoItem
import com.abtahiapp.dontworry.adapter.MoreVideoAdapter
import com.abtahiapp.dontworry.query.ArticleVideoQuery
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideoActivity : AppCompatActivity() {

    private lateinit var videoAdapter: MoreVideoAdapter
    private lateinit var videoRecyclerView: RecyclerView
    private lateinit var youTubePlayer: YouTubePlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        videoRecyclerView = findViewById(R.id.recycler_view)
        videoAdapter = MoreVideoAdapter(this, mutableListOf()){ video ->
            updateVideoDetails(video)
        }
        videoRecyclerView.layoutManager = LinearLayoutManager(this)
        videoRecyclerView.adapter = videoAdapter

        val youtubePlayerView: YouTubePlayerView = findViewById(R.id.youtube_player_view)
        lifecycle.addObserver(youtubePlayerView)

        val title = intent.getStringExtra("title")

        youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                this@VideoActivity.youTubePlayer = youTubePlayer
                val videoUrl = intent.getStringExtra("videoUrl")
                videoUrl?.let {
                    youTubePlayer.loadVideo(it, 0f)
                }
            }
        })

        findViewById<TextView>(R.id.textView7).text = title

        fetchVideos("")
    }

    private fun updateVideoDetails(video: VideoItem) {
        video.id.videoId.let {
            youTubePlayer.loadVideo(it, 0f)
        }

        findViewById<TextView>(R.id.textView7).text = video.snippet.title
        fetchVideos(video.snippet.title)
    }

    private fun fetchVideos(mood: String?) {
        val apiKey = BuildConfig.GOOGLE_API_KEY
        val queries = ArticleVideoQuery.getQueries(mood)
        val query = queries.random()

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.youtubeInstance.getVideos(query = query, apiKey = apiKey)
                }
                videoAdapter.updateVideos(response.items)
            } catch (e: Exception) {
                Toast.makeText(this@VideoActivity, "Failed to fetch videos", Toast.LENGTH_SHORT).show()
            } finally {
            }
        }
    }
}