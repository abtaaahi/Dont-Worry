package com.abtahiapp.dontworry.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.utils.Mood
import com.abtahiapp.dontworry.R

class MoodAdapter(private val moodList: List<Mood>) : RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mood, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val mood = moodList[position]
        holder.bind(mood)
    }

    override fun getItemCount(): Int {
        return moodList.size
    }

    class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val moodImage: ImageView = itemView.findViewById(R.id.mood_image)
        private val moodDateTime: TextView = itemView.findViewById(R.id.mood_date_time)
        private val moodDetails: TextView = itemView.findViewById(R.id.mood_details)

        fun bind(mood: Mood) {
            moodImage.setImageResource(mood.moodImage)
            moodDateTime.text = mood.dateTime
            moodDetails.text = mood.details
        }
    }
}
