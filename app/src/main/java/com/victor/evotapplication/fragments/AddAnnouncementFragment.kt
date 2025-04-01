package com.victor.evotapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.victor.evotapplication.databinding.FragmentAddAnnouncementBinding

// Fragment for creating and saving a new announcement to Firestore

class AddAnnouncementFragment : Fragment() {

    private lateinit var binding: FragmentAddAnnouncementBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var associationId: String? = null
    private var currentUsername: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddAnnouncementBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        associationId = arguments?.getString("associationId")
        getCurrentUsername()

        binding.saveAnnouncementBtn.setOnClickListener {
            addAnnouncement()
        }

        return binding.root
    }

    // Retrieves the current user's username from Firestore

    private fun getCurrentUsername() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("user-type").document(userId).get()
            .addOnSuccessListener { document ->
                currentUsername = document.getString("username")
            }
    }

    // Validates and sends announcement data to Firestore

    private fun addAnnouncement() {
        val title = binding.titleInput.text.toString().trim()
        val content = binding.contentInput.text.toString().trim()
        val userId = auth.currentUser?.uid ?: return

        if (title.isEmpty() || content.isEmpty() || currentUsername == null || associationId == null) {
            Toast.makeText(requireContext(), "Complete all fields!", Toast.LENGTH_SHORT).show()
            return
        }

        // Construct announcement object

        val announcement = hashMapOf(
            "title" to title,
            "content" to content,
            "createdBy" to userId,
            "createdByUsername" to currentUsername,
            "associationId" to associationId,
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("announcements")
            .add(announcement)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Announcement saved!", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Saving error!", Toast.LENGTH_SHORT).show()
            }
    }
}
