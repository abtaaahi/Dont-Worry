package com.abtahiapp.dontworry.adapter

import com.abtahiapp.dontworry.utils.Item
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
import com.abtahiapp.dontworry.activity.ArticleActivity
import com.bumptech.glide.Glide

class ArticleAdapter(
    private val context: Context,
    private val articles: MutableList<Item>
) : RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_article, parent, false)
        return ArticleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = articles[position]
        holder.titleTextView.text = article.title
        holder.descriptionTextView.text = article.snippet

        val imageUrl = article.pagemap?.cse_image?.firstOrNull()?.src
        Glide.with(context)
            .load(imageUrl)
            .placeholder(R.drawable.defaultnews)
            .error(R.drawable.defaultnews)
            .into(holder.imageView)

        holder.itemView.setOnClickListener {
            if (isOnline(context)) {
                val intent = Intent(context, ArticleActivity::class.java).apply {
                    putExtra("url", article.link)
                }
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "You are offline. Cannot open article.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = articles.size

    class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.article_image)
        val titleTextView: TextView = itemView.findViewById(R.id.article_title)
        val descriptionTextView: TextView = itemView.findViewById(R.id.article_description)
    }

    fun updateArticles(newArticles: List<Item>) {
        articles.clear()
        articles.addAll(newArticles)
        notifyDataSetChanged()
    }
}

//News API

//class ArticleAdapter(
//    private val context: Context,
//    private val articles: MutableList<Article>
//) : RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
//        val view = LayoutInflater.from(context).inflate(R.layout.item_article, parent, false)
//        return ArticleViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
//        val article = articles[position]
//        holder.titleTextView.text = article.title
//        holder.descriptionTextView.text = article.description
//
//        Glide.with(context)
//            .load(article.urlToImage)
//            .placeholder(R.drawable.defaultnews)
//            .error(R.drawable.defaultnews)
//            .into(holder.imageView)
//
//        holder.itemView.setOnClickListener {
//            val intent = Intent(context, ArticleActivity::class.java).apply {
//                putExtra("url", article.url)
//            }
//            context.startActivity(intent)
//        }
//    }
//
//    override fun getItemCount(): Int = articles.size
//
//    class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val imageView: ImageView = itemView.findViewById(R.id.article_image)
//        val titleTextView: TextView = itemView.findViewById(R.id.article_title)
//        val descriptionTextView: TextView = itemView.findViewById(R.id.article_description)
//    }
//
//    fun updateArticles(newArticles: List<Article>) {
//        articles.clear()
//        articles.addAll(newArticles)
//        notifyDataSetChanged()
//    }
//}