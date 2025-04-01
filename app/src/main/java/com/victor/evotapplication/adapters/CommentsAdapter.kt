package com.victor.evotapplication.adapters

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.victor.evotapplication.R
import com.victor.evotapplication.models.Comment
import java.text.SimpleDateFormat
import java.util.Locale

// Adapter for displaying a list of comments in a RecyclerView

class CommentsAdapter(
    private val comments: List<Comment>,
    private val currentUserId: String,
    private val onDelete: (Comment) -> Unit
) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val username: TextView = view.findViewById(R.id.comment_username)
        val text: TextView = view.findViewById(R.id.comment_text)
        val timestamp: TextView = view.findViewById(R.id.comment_timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]

        holder.username.text = comment.username
        holder.text.text = comment.text

        comment.timestamp?.let {
            val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            holder.timestamp.text = formatter.format(it)
        } ?: run {
            holder.timestamp.text = ""
        }

        // Own commentary delete

        holder.itemView.setOnLongClickListener {
            if (comment.userId == currentUserId) {
                AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Delete commentary?")
                    .setMessage("Are you sure you want to delete the commentary?")
                    .setPositiveButton("YES") { _, _ -> onDelete(comment) }
                    .setNegativeButton("NO", null)
                    .show()
            }
            true
        }
    }

    override fun getItemCount(): Int = comments.size
}