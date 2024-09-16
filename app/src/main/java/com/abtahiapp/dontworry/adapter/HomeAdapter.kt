package com.abtahiapp.dontworry.adapter

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.HomeFeedItem
import com.abtahiapp.dontworry.activity.MovieActivity
import com.abtahiapp.dontworry.HomeItem
import com.abtahiapp.dontworry.HomeItemType
import com.abtahiapp.dontworry.Post
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.activity.ArticleActivity
import com.abtahiapp.dontworry.activity.AudioActivity
import com.abtahiapp.dontworry.activity.VideoActivity
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class HomeAdapter(private val context: Context, private var items: List<HomeFeedItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object{
        private const val VIEW_TYPE_POST = 0
        private const val VIEW_TYPE_HOME_ITEM = 1
    }

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
                    else -> {}
                }
            }
        }

    }

    inner class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val profileImageView: ImageView = view.findViewById(R.id.item_profile_image)
        private val userNameTextView: TextView = view.findViewById(R.id.user_name)
        private val postTimeTextView: TextView = view.findViewById(R.id.post_time)
        private val postContentTextView: TextView = view.findViewById(R.id.post_text)

        private var isTextExpanded = false

        fun bind(post: Post) {
            Glide.with(context).load(post.userPhotoUrl).placeholder(R.drawable.person).into(profileImageView)
            val firstName = post.userName.split(" ").first()
            userNameTextView.text = firstName
            postTimeTextView.text = getRelativeTime(post.postTime)
            postContentTextView.text = post.content

            postContentTextView.maxLines = 5
            postContentTextView.ellipsize = TextUtils.TruncateAt.END

            postContentTextView.setOnClickListener {
                if (isTextExpanded) {
                    postContentTextView.maxLines = 5
                    postContentTextView.ellipsize = TextUtils.TruncateAt.END
                } else {
                    postContentTextView.maxLines = Int.MAX_VALUE
                    postContentTextView.ellipsize = null
                }
                isTextExpanded = !isTextExpanded
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is HomeFeedItem.PostItem -> VIEW_TYPE_POST
            is HomeFeedItem.HomeItemItem -> VIEW_TYPE_HOME_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_POST -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
                PostViewHolder(view)
            }
            VIEW_TYPE_HOME_ITEM -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_home_redesigned, parent, false)
                HomeViewHolder(view)}
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is HomeFeedItem.PostItem -> (holder as PostViewHolder).bind(item.post)
            is HomeFeedItem.HomeItemItem -> (holder as HomeViewHolder).bind(item.homeItem)
        }
    }

    fun updateItems(newItems: List<HomeFeedItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    private fun getRelativeTime(postTime: String): String {
        return try {
            val sdf = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.US)
            val postDate: Date = sdf.parse(postTime) ?: return "Just now"

            val now = Date()
            val diffInMillis = now.time - postDate.time

            when {
                TimeUnit.MILLISECONDS.toMinutes(diffInMillis) < 1 -> "Just now"
                TimeUnit.MILLISECONDS.toMinutes(diffInMillis) < 60 -> {
                    "${TimeUnit.MILLISECONDS.toMinutes(diffInMillis)}m ago"
                }
                TimeUnit.MILLISECONDS.toHours(diffInMillis) < 24 -> {
                    "${TimeUnit.MILLISECONDS.toHours(diffInMillis)}h ago"
                }
                TimeUnit.MILLISECONDS.toDays(diffInMillis) < 7 -> {
                    "${TimeUnit.MILLISECONDS.toDays(diffInMillis)}d ago"
                }
                TimeUnit.MILLISECONDS.toDays(diffInMillis) < 30 -> {
                    "${TimeUnit.MILLISECONDS.toDays(diffInMillis) / 7}w ago"
                }
                else -> {
                    "${TimeUnit.MILLISECONDS.toDays(diffInMillis) / 30}m ago"
                }
            }
        } catch (e: Exception) {
            "Unknown time"
        }
    }
}