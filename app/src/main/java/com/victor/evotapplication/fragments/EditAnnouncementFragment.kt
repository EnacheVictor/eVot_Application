package com.victor.evotapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.victor.evotapplication.databinding.FragmentEditAnnouncementBinding

// Fragment for editing an existing announcement in Firestore

class EditAnnouncementFragment : Fragment() {

    private lateinit var binding: FragmentEditAnnouncementBinding
    private lateinit var db: FirebaseFirestore

    private var announcementId: String? = null
    private var initialTitle: String? = null
    private var initialContent: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditAnnouncementBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()

        announcementId = arguments?.getString("announcementId")
        initialTitle = arguments?.getString("title")
        initialContent = arguments?.getString("content")

        binding.editTitle.setText(initialTitle)
        binding.editContent.setText(initialContent)

        binding.saveEditedBtn.setOnClickListener {
            saveEditedAnnouncement()
        }

        return binding.root
    }

    // Validates and updates the announcement in Firestore

    private fun saveEditedAnnouncement() {
        val newTitle = binding.editTitle.text.toString().trim()
        val newContent = binding.editContent.text.toString().trim()

        if (newTitle.isEmpty() || newContent.isEmpty()) {
            Toast.makeText(requireContext(), "Complete all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (announcementId == null) return

        val updateMap = mapOf(
            "title" to newTitle,
            "content" to newContent
        )

        db.collection("announcements").document(announcementId!!)
            .update(updateMap)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Announcement updated!", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error updating", Toast.LENGTH_SHORT).show()
            }
    }
}
