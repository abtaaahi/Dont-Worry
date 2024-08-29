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
import com.abtahiapp.dontworry.activity.MovieActivity
import com.abtahiapp.dontworry.HomeItem
import com.abtahiapp.dontworry.HomeItemType
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.activity.ArticleActivity
import com.abtahiapp.dontworry.activity.AudioActivity
import com.abtahiapp.dontworry.activity.VideoActivity
import com.bumptech.glide.Glide

class HomeAdapter(private val context: Context, private var items: MutableList<HomeItem>) :
    RecyclerView.Adapter<HomeAdapter.HomeViewHolder>() {

    inner class HomeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.image)
        val title: TextView = view.findViewById(R.id.title)
        val description: TextView = view.findViewById(R.id.description)

        fun bind(item: HomeItem) {
            title.text = item.title
            description.text = item.description
            Glide.with(context).load(item.imageUrl).into(image)

            itemView.setOnClickListener {
                when (item.type) {
                    HomeItemType.VIDEO -> {
                        val intent = Intent(context, VideoActivity::class.java)
                        intent.putExtra("videoUrl", item.id)
                        intent.putExtra("title", item.title)
                        context.startActivity(intent)
                    }
                    HomeItemType.ARTICLE -> {
                        val intent = Intent(context, ArticleActivity::class.java)
                        intent.putExtra("url", item.id)
                        context.startActivity(intent)
                    }
                    HomeItemType.AUDIO -> {
                        val intent = Intent(context, AudioActivity::class.java)
                        intent.putExtra("videoUrl", item.id)
                        intent.putExtra("title", item.title)
                        context.startActivity(intent)
                    }
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_home, parent, false)
        return HomeViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<HomeItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}

