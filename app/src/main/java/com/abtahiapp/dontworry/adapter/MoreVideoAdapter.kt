package com.abtahiapp.dontworry.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.utils.VideoItem
import com.bumptech.glide.Glide

class MoreVideoAdapter(private val context: Context, private var videos: MutableList<VideoItem>, private val onClick: (VideoItem) -> Unit) :
    RecyclerView.Adapter<MoreVideoAdapter.VideoViewHolder>() {

    inner class VideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val thumbnail: ImageView = view.findViewById(R.id.video_thumbnail)
        val title: TextView = view.findViewById(R.id.video_title)

        fun bind(video: VideoItem) {
            title.text = video.snippet.title
            Glide.with(context).load(video.snippet.thumbnails.high.url).into(thumbnail)
            itemView.setOnClickListener {
                onClick(video)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(videos[position])
    }

    override fun getItemCount(): Int = videos.size

    fun updateVideos(newVideos: List<VideoItem>) {
        videos.clear()
        videos.addAll(newVideos)
        notifyDataSetChanged()
    }
}