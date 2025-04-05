package com.victor.evotapplication.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.victor.evotapplication.R
import com.victor.evotapplication.databinding.ItemChatBinding
import com.victor.evotapplication.models.Chat

class ChatAdapter(private val messages: List<Chat>) :
    RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    inner class MessageViewHolder(val binding: ItemChatBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun getItemCount() = messages.size

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val msg = messages[position]
        with(holder.binding) {
            senderNameText.text = msg.senderName

            messageText.visibility = if (!msg.text.isNullOrEmpty()) View.VISIBLE else View.GONE
            messageText.text = msg.text

            imageView.visibility = if (msg.mediaType == "image") View.VISIBLE else View.GONE
            if (msg.mediaType == "image") {
                Glide.with(imageView.context).load(msg.mediaUrl).into(imageView)
            }

            videoView.visibility = if (msg.mediaType == "video") View.VISIBLE else View.GONE
            if (msg.mediaType == "video") {
                videoView.setVideoURI(Uri.parse(msg.mediaUrl))
                videoView.setOnPreparedListener { it.isLooping = true; videoView.start() }
            }

            if (msg.senderId == currentUserId) {
                root.gravity = android.view.Gravity.END
                bubbleContainer.setBackgroundResource(R.drawable.chat_bubble_sent)
            } else {
                root.gravity = android.view.Gravity.START
                bubbleContainer.setBackgroundResource(R.drawable.chat_bubble_received)
            }
        }
    }
}