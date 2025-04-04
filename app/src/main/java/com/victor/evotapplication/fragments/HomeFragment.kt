package com.victor.evotapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.victor.evotapplication.R
import com.victor.evotapplication.models.Association
import com.victor.evotapplication.adapters.AssociationAdapter
import com.victor.evotapplication.databinding.FragmentHomeBinding

// Home screen fragment displaying the list of associations the user is part of

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: AssociationAdapter
    private val associationList = mutableListOf<Association>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        fetchUserAssociations()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = AssociationAdapter(associationList)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    // Fetches all associations where the current user is a member

    private fun fetchUserAssociations() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("associations")
            .whereArrayContains("members", userId)
            .get()
            .addOnSuccessListener { documents ->
                associationList.clear()
                var isUserAdmin = false

                for (document in documents) {
                    val isAdmin = document.getString("adminId") == userId
                    if (isAdmin) isUserAdmin = true

                    val association = Association(
                        id = document.id,
                        name = document.getString("name") ?: "No name"
                    )
                    associationList.add(association)
                }
                binding.addResident.visibility = if (isUserAdmin) View.VISIBLE else View.GONE
                adapter.notifyDataSetChanged()
                binding.addResident.setOnClickListener {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, AddResidentFragment())
                        .addToBackStack(null)
                        .commit()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error when uploading!", Toast.LENGTH_SHORT).show()
            }
    }
}