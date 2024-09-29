package com.abtahiapp.dontworry.adapter

import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.utils.PersonalItem

class PersonalSpaceAdapter(private val personalItems: List<PersonalItem>) :
    RecyclerView.Adapter<PersonalSpaceAdapter.PersonalSpaceViewHolder>() {

    private var mediaPlayer: MediaPlayer? = null

    inner class PersonalSpaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvText: TextView = itemView.findViewById(R.id.tv_text)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tv_timestamp)
        val btnPlay: ImageButton = itemView.findViewById(R.id.btn_play)
        val btnPause: ImageButton = itemView.findViewById(R.id.btn_pause)
        val btnStop: ImageButton = itemView.findViewById(R.id.btn_stop)
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

        holder.btnPause.visibility = View.INVISIBLE
        holder.btnStop.visibility = View.INVISIBLE

        holder.btnPlay.setOnClickListener {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(currentItem.voiceUrl)
                    prepare()
                    start()
                }

                holder.btnPause.visibility = View.VISIBLE
                holder.btnStop.visibility = View.VISIBLE
                holder.btnPlay.isEnabled = false
            } else if (!mediaPlayer!!.isPlaying) {
                mediaPlayer!!.start()
                holder.btnPlay.isEnabled = false
            }
        }

        holder.btnPause.setOnClickListener {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                    // Re-enable the play button when paused
                    holder.btnPlay.isEnabled = true
                }
            }
        }

        holder.btnStop.setOnClickListener {
            mediaPlayer?.let {
                if (it.isPlaying || it.isLooping) {
                    it.stop()
                    it.reset()
                    mediaPlayer = null

                    // Hide pause and stop buttons again
                    holder.btnPause.visibility = View.INVISIBLE
                    holder.btnStop.visibility = View.INVISIBLE

                    // Re-enable the play button after stopping
                    holder.btnPlay.isEnabled = true
                }
            }
        }
    }

    override fun getItemCount() = personalItems.size

    fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}