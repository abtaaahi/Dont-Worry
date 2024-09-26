package com.abtahiapp.dontworry.adapter

import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
        val btnPlay: Button = itemView.findViewById(R.id.btn_play)
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

        holder.btnPlay.setOnClickListener {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
                mediaPlayer?.release()
            }
            mediaPlayer = MediaPlayer().apply {
                setDataSource(currentItem.voiceUrl)
                prepare()
                start()
            }
        }
    }

    override fun getItemCount() = personalItems.size
}