package com.victor.evotapplication.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
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
                    updateUIForRole()
                    fetchUserAssociations()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error fetching role", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUIForRole() {
        if (userRole == "Admin") {
            showAdminUI()
        } else {
            showLocatarUI()
        }
    }

    private fun showAdminUI() {
        binding.adminLayout.visibility = View.VISIBLE

        binding.createAssociationBtn.setOnClickListener {
            val name = binding.assocNameInput.text.toString()
            val location = binding.assocLocationInput.text.toString()

            if (name.isEmpty() || location.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val adminId = auth.currentUser?.uid ?: return@setOnClickListener
            val data = hashMapOf(
                "name" to name,
                "location" to location,
                "adminId" to adminId,
                "members" to listOf(adminId)
            )

            db.collection("associations").add(data)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Association created!", Toast.LENGTH_SHORT).show()
                    fetchUserAssociations()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error creating association", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showLocatarUI() {
        binding.locatarLayout.visibility = View.VISIBLE

        binding.joinAssociationBtn.setOnClickListener {
            val code = binding.joinCodeInput.text.toString()
            if (code.isEmpty()) {
                Toast.makeText(requireContext(), "Insert invite code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.collection("invites").document(code)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val associationId = document.getString("associationId") ?: ""
                        val used = document.getBoolean("used") ?: false
                        val timestamp = document.getLong("timestamp") ?: 0L
                        val isExpired = System.currentTimeMillis() - timestamp > 24 * 60 * 60 * 1000

                        if (!used && !isExpired) {
                            joinAssociation(associationId)
                            document.reference.delete()
                        } else {
                            Toast.makeText(requireContext(), "Code expired or already used.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Invalid code.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error verifying code.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun joinAssociation(associationId: String) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = db.collection("user-type").document(userId)

        db.collection("associations").document(associationId)
            .update("members", FieldValue.arrayUnion(userId))
            .addOnSuccessListener {
                userRef.update("associations", FieldValue.arrayUnion(associationId))
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Joined successfully!", Toast.LENGTH_SHORT).show()
                        fetchUserAssociations()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Joined association but failed to update user.", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error joining association", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchUserAssociations() {
        val userId = auth.currentUser?.uid ?: return
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
                Toast.makeText(requireContext(), "Error loading associations", Toast.LENGTH_SHORT).show()
            }
    }
}

