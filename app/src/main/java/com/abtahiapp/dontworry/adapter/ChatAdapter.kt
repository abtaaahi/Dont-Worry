package com.abtahiapp.dontworry.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abtahiapp.dontworry.R
import com.abtahiapp.dontworry.utils.ChatMessage
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView

class ChatAdapter(
    private val chatMessages: List<ChatMessage>,
    private val userPhotoUrl: String
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = chatMessages[position]

        holder.userMessageTextView.visibility = View.GONE
        holder.botMessageTextView.visibility = View.GONE
        holder.userProfileImage.visibility = View.GONE
        holder.botProfileImage.visibility = View.GONE
        holder.typingIndicator.visibility = View.GONE

        if (message.sender == "user") {
            holder.userMessageTextView.text = message.content
            holder.userMessageTextView.visibility = View.VISIBLE
            holder.userProfileImage.visibility = View.VISIBLE

            if (userPhotoUrl.isNotEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(userPhotoUrl)
                    .placeholder(R.drawable.person)
                    .into(holder.userProfileImage)
            } else {
                holder.userProfileImage.setImageResource(R.drawable.person)
            }
        } else if (message.isTyping) {
            holder.typingIndicator.visibility = View.VISIBLE
            holder.typingIndicator.playAnimation()
            holder.botProfileImage.visibility = View.VISIBLE
        } else {
            holder.botMessageTextView.text = message.content
            holder.botMessageTextView.visibility = View.VISIBLE
            holder.botProfileImage.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int = chatMessages.size

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userMessageTextView: TextView = itemView.findViewById(R.id.userMessageTextView)
        val botMessageTextView: TextView = itemView.findViewById(R.id.botMessageTextView)
        val userProfileImage: CircleImageView = itemView.findViewById(R.id.userProfileImage)
        val botProfileImage: CircleImageView = itemView.findViewById(R.id.botProfileImage)
        val typingIndicator: LottieAnimationView = itemView.findViewById(R.id.typingIndicator)
    }
}