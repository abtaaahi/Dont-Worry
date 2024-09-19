package com.abtahiapp.dontworry.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.utils.Post
import com.abtahiapp.dontworry.R
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class PostAdapter(private var posts: MutableList<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private var onItemClickListener: ((Post) -> Unit)? = null
    private var onProfileImageClickListener: ((Post) -> Unit)? = null
    private var onPostLongClickListener: ((Post) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post)
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(post)
        }
        holder.profileImageView.setOnClickListener {
            onProfileImageClickListener?.invoke(post)
        }
        holder.postContentTextView.setOnLongClickListener {
            onPostLongClickListener?.invoke(post)
            true
        }
    }

    override fun getItemCount(): Int = posts.size

    fun updatePosts(newPosts: List<Post>) {
        posts.clear()
        posts.addAll(newPosts)
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: (Post) -> Unit) {
        onItemClickListener = listener
    }

    fun setOnProfileImageClickListener(listener: (Post) -> Unit) {
        onProfileImageClickListener = listener
    }

    fun setOnPostLongClickListener(listener: (Post) -> Unit) {
        onPostLongClickListener = listener
    }

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImageView: ImageView = itemView.findViewById(R.id.item_profile_image)
        private val userNameTextView: TextView = itemView.findViewById(R.id.user_name)
        private val postTimeTextView: TextView = itemView.findViewById(R.id.post_time)
        val postContentTextView: TextView = itemView.findViewById(R.id.post_text)

        private var isTextExpanded = false

        fun bind(post: Post) {
            Glide.with(itemView.context)
                .load(post.userPhotoUrl)
                .placeholder(R.drawable.person)
                .into(profileImageView)

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
}