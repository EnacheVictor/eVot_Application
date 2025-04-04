package com.victor.evotapplication.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.victor.evotapplication.R
import com.victor.evotapplication.databinding.FragmentAssociationsBinding
// Fragment that allows admins to create associations and users to join one using an invite code

class AssociationsFragment : Fragment() {

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

        checkUserRole()

        return binding.root
    }

    // Get the current user's role from Firestore

    private fun checkUserRole() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("user-type").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    userRole = document.getString("role")
                    updateUIForRole()
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Eroare la obținerea rolului", e)
            }
    }

    private fun updateUIForRole() {
        if (userRole == "Admin") {
            showAdminUI()
        } else {
            showLocatarUI()
        }
    }

    // Show admin-specific UI and handle association creation

    private fun showAdminUI() {
        binding.adminLayout.visibility = View.VISIBLE
        binding.createAssociationBtn.setOnClickListener {
            val assocName = binding.assocNameInput.text.toString()
            val assocLocation = binding.assocLocationInput.text.toString()
            if (assocName.isNotEmpty()) {
                saveAssociationsToFirestore(assocName, assocLocation)
            } else {
                Toast.makeText(requireContext(), "Insert Name for Association!", Toast.LENGTH_SHORT)
                    .show()
            }
            if (assocLocation.isEmpty()) {
            Toast.makeText(requireContext(), "Insert Location for Association!", Toast.LENGTH_SHORT)
                .show()
        }
    }
    }

    // Save the new association in Firestore

    private fun saveAssociationsToFirestore(assocName: String, assocLocation: String) {
        val adminId = auth.currentUser?.uid ?: return
        val assocData = hashMapOf(
            "name" to assocName,
            "adminId" to adminId,
            "location" to assocLocation,
            "members" to listOf(adminId)
        )

        db.collection("associations").add(assocData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Association created!", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment())
                    .commit()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error creating!", Toast.LENGTH_SHORT).show()
                Log.e("Firestore", "Eroare la salvare", e)
            }
    }

    private fun showLocatarUI() {
        binding.locatarLayout.visibility = View.VISIBLE
        binding.joinAssociationBtn.setOnClickListener {
            val code = binding.joinCodeInput.text.toString()
            if (code.isNotEmpty()) {
                joinAssociation(code)
            } else {
                Toast.makeText(requireContext(), "Insert code!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Find the association by invite code and join it

    private fun joinAssociation(code: String) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("invites").document(code)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val associationId = document.getString("associationId") ?: ""
                    val used = document.getBoolean("used") ?: false
                    val timestamp = document.getLong("timestamp") ?: 0L
                    val isExpired = System.currentTimeMillis() - timestamp > 24 * 60 * 60 * 1000

                    if (!used && !isExpired) {
                        addUserToAssociation(associationId)
                        document.reference.delete()
                    } else {
                        Toast.makeText(requireContext(), "Code expired or already used.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Invalid code!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error fetching code.", Toast.LENGTH_SHORT).show()
            }
    }

    // Add current user to the selected association

    private fun addUserToAssociation(assocId: String) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("associations").document(assocId)
            .update("members", FieldValue.arrayUnion(userId))
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Joining successful!", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment())
                    .commit()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Joining error!", Toast.LENGTH_SHORT).show()
            }

    }
}

