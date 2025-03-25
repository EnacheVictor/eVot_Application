package com.victor.evotapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AssociationAdapter(private val associations: List<Association>) :
    RecyclerView.Adapter<AssociationAdapter.AssociationViewHolder>() {

    class AssociationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val assocName: TextView = view.findViewById(R.id.associationName)
        val inviteCode: TextView = view.findViewById(R.id.inviteCode)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssociationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_association, parent, false)
        return AssociationViewHolder(view)
    }

    override fun onBindViewHolder(holder: AssociationViewHolder, position: Int) {
        val association = associations[position]
        holder.assocName.text = association.name
        holder.inviteCode.text = "Cod: ${association.inviteCode}"
        if (association.inviteCode.isNotEmpty()) {
            holder.inviteCode.text = "Cod: ${association.inviteCode}"
            holder.inviteCode.visibility = View.VISIBLE
        } else {
            holder.inviteCode.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = associations.size

}