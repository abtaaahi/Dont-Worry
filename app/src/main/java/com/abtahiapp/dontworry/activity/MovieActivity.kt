package com.abtahiapp.dontworry.activity

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.abtahiapp.dontworry.R
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class MovieActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie)

        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")
        val trailerUrl = intent.getStringExtra("trailerUrl")

        findViewById<TextView>(R.id.movietitle).text = title
        findViewById<TextView>(R.id.moviedescription).text = description

        val youTubePlayerView: YouTubePlayerView = findViewById(R.id.youtube_player_view)
        lifecycle.addObserver(youTubePlayerView)

        youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                trailerUrl?.let {
                    youTubePlayer.loadVideo(it, 0f)
                }
            }
        })
    }
}