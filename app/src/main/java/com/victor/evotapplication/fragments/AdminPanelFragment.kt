package com.victor.evotapplication.fragments

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.victor.evotapplication.R
import com.victor.evotapplication.databinding.FragmentAdminPanelBinding

class AdminPanelFragment : Fragment() {

    private lateinit var binding: FragmentAdminPanelBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminPanelBinding.inflate(inflater, container, false)

        setupCard(binding.root.findViewById(R.id.cardManageResident), R.drawable.manage, "Manage Residents") {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ManageUserFragment())
                .addToBackStack(null)
                .commit()
        }

        setupCard(binding.root.findViewById(R.id.cardAddInvoice), R.drawable.invoice_add, "Add Invoice") {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddInvoiceFragment())
                .addToBackStack(null)
                .commit()
        }

        setupCard(binding.root.findViewById(R.id.cardAddAnnouncement), R.drawable.ann_add, "Add Announcement") {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddAnnouncementFragment())
                .addToBackStack(null)
                .commit()
        }

        setupCard(binding.root.findViewById(R.id.cardCreateVote), R.drawable.add_vote, "Create Vote") {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddVoteFragment())
                .addToBackStack(null)
                .commit()
        }

        setupCard(binding.root.findViewById(R.id.createAssociation), R.drawable.group_add, "Create Association") {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddAssociationFragment())
                .addToBackStack(null)
                .commit()
        }

        return binding.root
    }

    private fun setupCard(card: View, iconRes: Int, label: String, onClick: () -> Unit) {
        val icon = card.findViewById<ImageView>(R.id.cardIcon)
        val labelView = card.findViewById<TextView>(R.id.cardLabel)
        icon.setImageResource(iconRes)
        labelView.text = label
        card.setOnClickListener { onClick() }
    }
}
