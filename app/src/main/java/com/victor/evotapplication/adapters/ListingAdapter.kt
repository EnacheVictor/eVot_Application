package com.victor.evotapplication.adapters

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.victor.evotapplication.R
import com.victor.evotapplication.models.Listing
import com.victor.evotapplication.models.ListingType
import java.text.SimpleDateFormat
import java.util.*

class ListingAdapter(private val listings: MutableList<Listing>) : RecyclerView.Adapter<ListingAdapter.ListingViewHolder>() {

    inner class ListingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewListing: ImageView = itemView.findViewById(R.id.imageViewListing)
        val textViewTitle: TextView = itemView.findViewById(R.id.textViewTitle)
        val textViewPrice: TextView = itemView.findViewById(R.id.textViewPrice)
        val textViewType: TextView = itemView.findViewById(R.id.textViewType)
        val textViewOwner: TextView = itemView.findViewById(R.id.textViewOwner)
        val textViewDate: TextView = itemView.findViewById(R.id.textViewDate)
        val textViewContact: TextView = itemView.findViewById(R.id.textViewContact)
        val buttonDelete: ImageButton = itemView.findViewById(R.id.buttonDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_listing, parent, false)
        return ListingViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListingViewHolder, position: Int) {
        val listing = listings[position]
        holder.textViewTitle.text = listing.title
        holder.textViewPrice.text = "${listing.price} €"
        holder.textViewType.text = when (listing.type) {
            ListingType.RENT -> "De închiriat"
            ListingType.SELL -> "De vânzare"
        }
        holder.textViewOwner.text = "Publicat de: ${listing.ownerUsername}"
        holder.textViewDate.text = "Data: ${formatTimestamp(listing.createdAt)}"
        holder.textViewContact.text = "Contact: ${listing.contactInfo}"

        Glide.with(holder.itemView.context)
            .load(listing.imageUrl)
            .into(holder.imageViewListing)

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        holder.buttonDelete.visibility = if (currentUserId == listing.ownerId) View.VISIBLE else View.GONE

        holder.buttonDelete.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Confirmă ștergerea")
                .setMessage("Ești sigur că vrei să ștergi acest anunț?")
                .setPositiveButton("Șterge") { _, _ ->
                    FirebaseFirestore.getInstance()
                        .collection("listings")
                        .document(listing.id)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(holder.itemView.context, "Anunț șters.", Toast.LENGTH_SHORT).show()
                            listings.removeAt(position)
                            notifyItemRemoved(position)
                        }
                        .addOnFailureListener {
                            Toast.makeText(holder.itemView.context, "Eroare la ștergere.", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Anulează", null)
                .show()
        }
    }

    override fun getItemCount(): Int = listings.size

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
