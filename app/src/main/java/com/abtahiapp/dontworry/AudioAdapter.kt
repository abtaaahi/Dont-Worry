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

class AudioAdapter(private val context: Context, private var audios: List<AudioItem>) :
    RecyclerView.Adapter<AudioAdapter.AudioViewHolder>() {

    inner class AudioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val thumbnail: ImageView = view.findViewById(R.id.audio_thumbnail)
        val title: TextView = view.findViewById(R.id.audio_title)

        fun bind(audio: AudioItem) {
            title.text = audio.strAlbum
            Glide.with(context)
                .load(audio.strAlbumThumb)
                .placeholder(R.drawable.defaultnews)
                .error(R.drawable.defaultnews)
                .into(thumbnail)

            itemView.setOnClickListener {
                val intent = Intent(context, AudioActivity::class.java)
                intent.putExtra("audioUrl", "https://www.theaudiodb.com/audio/${audio.idAlbum}")
                intent.putExtra("album", audio.strAlbum)
                intent.putExtra("artist", audio.strArtist)
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_audio, parent, false)
        return AudioViewHolder(view)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        holder.bind(audios[position])
    }

    override fun getItemCount(): Int = audios.size

    fun updateAudios(newAudios: List<AudioItem>) {
        audios = newAudios
        notifyDataSetChanged()
    }
}
