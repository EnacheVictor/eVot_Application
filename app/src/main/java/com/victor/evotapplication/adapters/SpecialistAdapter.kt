package com.victor.evotapplication.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.victor.evotapplication.R
import com.victor.evotapplication.models.Specialist

class SpecialistAdapter(private val specialists: List<Specialist>) : RecyclerView.Adapter<SpecialistAdapter.SpecialistViewHolder>() {

    inner class SpecialistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewSpecialist: ImageView = itemView.findViewById(R.id.imageViewSpecialist)
        val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        val textViewDescription: TextView = itemView.findViewById(R.id.textViewDescription)
        val textViewContact: TextView = itemView.findViewById(R.id.textViewContact)
        val buttonCall: ImageButton = itemView.findViewById(R.id.buttonCall)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpecialistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_specialists, parent, false)
        return SpecialistViewHolder(view)
    }

    override fun onBindViewHolder(holder: SpecialistViewHolder, position: Int) {
        val specialist = specialists[position]

        holder.textViewName.text = specialist.name
        holder.textViewDescription.text = specialist.description
        holder.textViewContact.text = specialist.contactInfo

        // Încărcăm imaginea cu Glide
        Glide.with(holder.itemView.context)
            .load(specialist.imageUrl)
            .placeholder(R.drawable.placeholder_specialist)
            .into(holder.imageViewSpecialist)

        // Buton de apelare 📞
        holder.buttonCall.setOnClickListener {
            val number = specialist.contactInfo.trim()
            if (number.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:$number")
                holder.itemView.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = specialists.size
}
