package com.victor.evotapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.victor.evotapplication.fragments.AssociationDetailsFragment

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
        holder.inviteCode.text = "Code: ${association.inviteCode}"
        if (association.inviteCode.isNotEmpty()) {
            holder.inviteCode.text = "Code: ${association.inviteCode}"
            holder.inviteCode.visibility = View.VISIBLE
        } else {
            holder.inviteCode.visibility = View.GONE
        }
        holder.itemView.setOnClickListener {
            val fragment = AssociationDetailsFragment()
            val bundle = Bundle()
            bundle.putString("associationId", association.id)
            bundle.putString("associationName", association.name)
            fragment.arguments = bundle

            (holder.itemView.context as AppCompatActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun getItemCount(): Int = associations.size

}
