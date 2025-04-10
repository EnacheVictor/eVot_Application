package com.victor.evotapplication.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.victor.evotapplication.adapters.AssociationAdapter
import com.victor.evotapplication.databinding.FragmentAssociationsBinding
import com.victor.evotapplication.models.Association

// Fragment that allows admins to create associations and users to join one using an invite code

class AssociationsFragment : Fragment() {

    private lateinit var adapter: AssociationAdapter
    private val associationList = mutableListOf<Association>()
    private lateinit var binding: FragmentAssociationsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var userRole: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAssociationsBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        checkUserRole()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = AssociationAdapter(associationList)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun checkUserRole() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("user-type").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    userRole = document.getString("role")
                    fetchUserAssociations()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error fetching role", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchUserAssociations() {
        val userId = auth.currentUser?.uid ?: return

        if (userRole == "Admin") {
            db.collection("associations")
                .whereEqualTo("adminId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    associationList.clear()
                    for (doc in documents) {
                        val assoc = Association(
                            id = doc.id,
                            name = doc.getString("name") ?: "No name",
                            location = doc.getString("location") ?: "No location"
                        )
                        associationList.add(assoc)
                    }
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error loading admin associations", Toast.LENGTH_SHORT).show()
                }
        } else {
            db.collection("associations")
                .whereArrayContains("members", userId)
                .get()
                .addOnSuccessListener { documents ->
                    associationList.clear()
                    for (doc in documents) {
                        val assoc = Association(
                            id = doc.id,
                            name = doc.getString("name") ?: "No name",
                            location = doc.getString("location") ?: "No location"
                        )
                        associationList.add(assoc)
                    }
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error loading user associations", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

