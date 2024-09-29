package com.abtahiapp.dontworry.adapter

import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.utils.PersonalItem

class PersonalSpaceAdapter(private val personalItems: List<PersonalItem>) :
    RecyclerView.Adapter<PersonalSpaceAdapter.PersonalSpaceViewHolder>() {

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false

    inner class PersonalSpaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvText: TextView = itemView.findViewById(R.id.tv_text)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tv_timestamp)
        val btnPlayPause: ImageButton = itemView.findViewById(R.id.btn_play_pause)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonalSpaceViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.personal_space_item, parent, false)
        return PersonalSpaceViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PersonalSpaceViewHolder, position: Int) {
        val currentItem = personalItems[position]
        holder.tvText.text = currentItem.text
        holder.tvTimestamp.text = currentItem.timestamp

        holder.btnPlayPause.setBackgroundResource(R.drawable.play)  // Start with play icon

        holder.btnPlayPause.setOnClickListener {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(currentItem.voiceUrl)
                    prepare()
                    start()
                }
                isPlaying = true
                holder.btnPlayPause.setBackgroundResource(R.drawable.pause)  // Set to pause icon
            } else {
                if (mediaPlayer!!.isPlaying) {
                    mediaPlayer!!.pause()
                    isPlaying = false
                    holder.btnPlayPause.setBackgroundResource(R.drawable.play)  // Switch back to play icon
                } else {
                    mediaPlayer!!.start()
                    isPlaying = true
                    holder.btnPlayPause.setBackgroundResource(R.drawable.pause)  // Switch to pause icon
                }
            }
        }

        holder.itemView.setOnClickListener {
            releaseMediaPlayer(holder)
        }
    }

    override fun getItemCount() = personalItems.size

    private fun releaseMediaPlayer(holder: PersonalSpaceViewHolder) {
        mediaPlayer?.let {
            if (it.isPlaying || it.isLooping) {
                it.stop()
                it.release()
                mediaPlayer = null
                holder.btnPlayPause.setBackgroundResource(R.drawable.play)
                isPlaying = false
            }
        }
    }

    fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}