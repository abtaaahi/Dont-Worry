package com.abtahiapp.dontworry.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.utils.NetworkUtil.isOnline
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.utils.VideoItem
import com.abtahiapp.dontworry.activity.AudioActivity
import com.bumptech.glide.Glide

class AudioAdapter(private val context: Context, private var videos: MutableList<VideoItem>) :
    RecyclerView.Adapter<AudioAdapter.VideoViewHolder>() {

    inner class VideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val thumbnail: ImageView = view.findViewById(R.id.audio_thumbnail)
        val title: TextView = view.findViewById(R.id.audio_title)

        fun bind(video: VideoItem) {
            title.text = video.snippet.title
            Glide.with(context).load(video.snippet.thumbnails.high.url).into(thumbnail)
            itemView.setOnClickListener {
                if (isOnline(context)) {
                    val intent = Intent(context, AudioActivity::class.java)
                    intent.putExtra("videoUrl", video.id.videoId)
                    intent.putExtra("title", video.snippet.title)
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "You are offline.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_audio, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(videos[position])
    }

    override fun getItemCount(): Int = videos.size

    fun updateAudios(newVideos: List<VideoItem>) {
        videos.clear()
        videos.addAll(newVideos)
        notifyDataSetChanged()
    }
}
