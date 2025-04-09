package com.victor.evotapplication.fragments

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.victor.evotapplication.R
import com.victor.evotapplication.databinding.FragmentManageUserBinding

class ManageUserFragment : Fragment() {

    private lateinit var binding: FragmentManageUserBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentManageUserBinding.inflate(inflater, container, false)

        // Găsim cardurile folosind ID-urile din <include>
        val addResidentCard = binding.root.findViewById<View>(R.id.cardAddResident)
        val removeResidentCard = binding.root.findViewById<View>(R.id.cardRemoveResident)
        val listUsersCard = binding.root.findViewById<View>(R.id.cardList_of_residents)

        setupCard(addResidentCard, R.drawable.person_add, "Add Resident") {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddResidentFragment())
                .addToBackStack(null)
                .commit()
        }

        setupCard(removeResidentCard, R.drawable.remove_user, "Remove Resident") {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RemoveFragment())
                .addToBackStack(null)
                .commit()
        }

        setupCard(listUsersCard, R.drawable.list_user, "List Residents") {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ListUserFragment())
                .addToBackStack(null)
                .commit()
        }

        return binding.root
    }

    // Funcție pentru a configura fiecare card
    private fun setupCard(view: View, iconRes: Int, labelText: String, onClick: () -> Unit) {
        val icon = view.findViewById<ImageView>(R.id.cardIcon)
        val label = view.findViewById<TextView>(R.id.cardLabel)
        icon.setImageResource(iconRes)
        label.text = labelText
        view.setOnClickListener { onClick() }
    }
}