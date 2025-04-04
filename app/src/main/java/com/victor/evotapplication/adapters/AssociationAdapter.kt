package com.victor.evotapplication.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.victor.evotapplication.R
import com.victor.evotapplication.fragments.AssociationDetailsFragment
import com.victor.evotapplication.models.Association

// Adapter for displaying a list of associations in a RecyclerView

class AssociationAdapter(private val associations: List<Association>) :
    RecyclerView.Adapter<AssociationAdapter.AssociationViewHolder>() {

    class AssociationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val assocName: TextView = view.findViewById(R.id.associationName)
        val assocLocation: TextView = view.findViewById(R.id.associationLocation)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssociationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_association, parent, false)
        return AssociationViewHolder(view)
    }

    // Binds data from the association object to the views

    override fun onBindViewHolder(holder: AssociationViewHolder, position: Int) {
        val association = associations[position]
        holder.assocName.text = association.name
        holder.assocLocation.text = association.location

       // Open the AssociationDetailsFragment and pass association data

        holder.itemView.setOnClickListener {
            val fragment = AssociationDetailsFragment()
            val bundle = Bundle()
            bundle.putString("associationId", association.id)
            bundle.putString("associationName", association.name)
            bundle.putString("associationLocation", association.location)
            fragment.arguments = bundle

            (holder.itemView.context as AppCompatActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun getItemCount(): Int = associations.size

}