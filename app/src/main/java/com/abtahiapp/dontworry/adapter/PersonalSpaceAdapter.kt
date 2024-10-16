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

class PersonalSpaceAdapter(private val personalItems: List<PersonalItem>, private val onItemLongClick: (Int) -> Unit) :
    RecyclerView.Adapter<PersonalSpaceAdapter.PersonalSpaceViewHolder>() {

    private var currentMediaPlayer: MediaPlayer? = null
    private var currentPlayingPosition: Int = -1
    var selectedPosition: Int = -1

    inner class PersonalSpaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvText: TextView = itemView.findViewById(R.id.tv_text)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tv_timestamp)
        val btnPlayPause: ImageButton = itemView.findViewById(R.id.btn_play_pause)
        val dividerView: View = itemView.findViewById(R.id.divider)
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

        if (position == selectedPosition) {
            holder.itemView.setBackgroundResource(R.drawable.rounded_corners_selected)
            holder.dividerView.visibility = View.GONE
        } else {
            holder.itemView.setBackgroundResource(R.drawable.rounded_corners)
            holder.dividerView.visibility = View.VISIBLE
        }

        if (position == currentPlayingPosition && currentMediaPlayer?.isPlaying == true) {
            holder.btnPlayPause.setBackgroundResource(R.drawable.pause)
        } else {
            holder.btnPlayPause.setBackgroundResource(R.drawable.play)
        }

        holder.btnPlayPause.setOnClickListener {
            if (position == currentPlayingPosition) {
                togglePlayPause(holder)
            } else {
                stopCurrentPlaying()
                startPlaying(currentItem, holder, position)
            }
        }

        holder.itemView.setOnLongClickListener {
            selectedPosition = if (selectedPosition == position) -1 else position
            onItemLongClick(position)
            notifyDataSetChanged()
            true
        }
    }

    override fun getItemCount() = personalItems.size

    private fun startPlaying(item: PersonalItem, holder: PersonalSpaceViewHolder, position: Int) {
        currentPlayingPosition = position
        currentMediaPlayer = MediaPlayer().apply {
            setDataSource(item.voiceUrl)
            prepare()
            start()
        }
        holder.btnPlayPause.setBackgroundResource(R.drawable.pause)

        currentMediaPlayer?.setOnCompletionListener {
            holder.btnPlayPause.setBackgroundResource(R.drawable.play)
            currentPlayingPosition = -1
        }
    }

    private fun togglePlayPause(holder: PersonalSpaceViewHolder) {
        currentMediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                holder.btnPlayPause.setBackgroundResource(R.drawable.play)
            } else {
                it.start()
                holder.btnPlayPause.setBackgroundResource(R.drawable.pause)
            }
        }
    }

    private fun stopCurrentPlaying() {
        if (currentMediaPlayer != null) {
            currentMediaPlayer?.stop()
            currentMediaPlayer?.release()
            currentMediaPlayer = null
            notifyItemChanged(currentPlayingPosition)
            currentPlayingPosition = -1
        }
    }

    fun releaseMediaPlayer() {
        currentMediaPlayer?.release()
        currentMediaPlayer = null
    }
}