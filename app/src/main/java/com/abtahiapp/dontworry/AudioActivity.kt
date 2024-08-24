package com.abtahiapp.dontworry

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView

class AudioActivity : AppCompatActivity() {

    private lateinit var playerView: PlayerView
    private lateinit var audioAlbum: TextView
    private lateinit var audioArtist: TextView
    private lateinit var exoPlayer: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio)

        playerView = findViewById(R.id.player_view)
        audioAlbum = findViewById(R.id.audio_album)
        audioArtist = findViewById(R.id.audio_artist)

        val audioUrl = intent.getStringExtra("audioUrl")
        val album = intent.getStringExtra("album")
        val artist = intent.getStringExtra("artist")

        audioAlbum.text = album
        audioArtist.text = artist

        exoPlayer = ExoPlayer.Builder(this).build()
        playerView.player = exoPlayer

        audioUrl?.let {
            val mediaItem = MediaItem.fromUri(it)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
    }
}
