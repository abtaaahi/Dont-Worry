package com.abtahiapp.dontworry

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class VideoAdapter(private val context: Context, private var videos: MutableList<VideoItem>) :
    RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    inner class VideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val thumbnail: ImageView = view.findViewById(R.id.video_thumbnail)
        val title: TextView = view.findViewById(R.id.video_title)

        fun bind(video: VideoItem) {
            title.text = video.snippet.title
            Glide.with(context).load(video.snippet.thumbnails.high.url).into(thumbnail)
            itemView.setOnClickListener {
                val intent = Intent(context, VideoActivity::class.java)
                intent.putExtra("videoUrl", video.id.videoId)
                intent.putExtra("title", video.snippet.title)
                context.startActivity(intent)
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
