package com.victor.evotapplication

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.victor.evotapplication.fragments.Announcement
import java.text.SimpleDateFormat
import java.util.*

class AnnouncementsAdapter(
    private val announcements: List<Announcement>,
    private val isAdmin: Boolean,
    private val onDelete: (Announcement) -> Unit,
    private val onEdit: (Announcement) -> Unit
) : RecyclerView.Adapter<AnnouncementsAdapter.AnnouncementViewHolder>() {

    inner class AnnouncementViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.ann_title)
        val content: TextView = view.findViewById(R.id.ann_content)
        val author: TextView = view.findViewById(R.id.ann_author)
        val date: TextView = view.findViewById(R.id.ann_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_announcements, parent, false)
        return AnnouncementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnnouncementViewHolder, position: Int) {
        val announcement = announcements[position]

        holder.title.text = announcement.title
        holder.content.text = announcement.content
        holder.author.text = "Posted by: ${announcement.createdByUsername}"

        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        announcement.timestamp?.let {
            holder.date.text = formatter.format(it)
        } ?: run {
            holder.date.text = ""
        }

        holder.itemView.setOnClickListener {
            if (isAdmin) {
                onEdit(announcement)
            }
        }

        holder.itemView.setOnLongClickListener {
            if (isAdmin) {
                AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Erase announcement?")
                    .setMessage("Are you sure you want to erase the announcement?")
                    .setPositiveButton("Yes") { _, _ -> onDelete(announcement) }
                    .setNegativeButton("No", null)
                    .show()
            }
            true
        }
    }

    override fun getItemCount(): Int = announcements.size
}
