package com.victor.evotapplication.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.victor.evotapplication.R
import com.victor.evotapplication.databinding.ItemUserBinding

class UserAdapter(private val users: List<String>) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val username = users[position]
        holder.binding.usernameText.text = username

        // Setăm culoarea textului ca să fie negru
        holder.binding.usernameText.setTextColor(
            ContextCompat.getColor(holder.itemView.context, R.color.black)
        )
    }

    override fun getItemCount(): Int = users.size
}

