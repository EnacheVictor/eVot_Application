package com.victor.evotapplication.fragments

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.victor.evotapplication.databinding.FragmentAddAnnouncementBinding
import com.victor.evotapplication.models.Association

class AddAnnouncementFragment : Fragment() {

    private lateinit var binding: FragmentAddAnnouncementBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var currentUsername: String? = null
    private var userAssociations: List<Association> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddAnnouncementBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        getCurrentUsername()
        loadUserAssociations()

        binding.saveAnnouncementBtn.setOnClickListener {
            addAnnouncement()
        }

        return binding.root
    }

    private fun getCurrentUsername() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("user-type").document(userId).get()
            .addOnSuccessListener { doc ->
                currentUsername = doc.getString("username")
            }
    }

    private fun loadUserAssociations() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("user-type").document(userId).get()
            .addOnSuccessListener { userDoc ->
                val role = userDoc.getString("role") ?: "locatar"

                val query = if (role.lowercase() == "admin") {
                    db.collection("associations").whereEqualTo("adminId", userId)
                } else {
                    db.collection("associations").whereArrayContains("members", userId)
                }

                query.get()
                    .addOnSuccessListener { result ->
                        userAssociations = result.map {
                            Association(
                                id = it.id,
                                name = it.getString("name") ?: "Unnamed",
                                location = it.getString("location") ?: "Unknown"
                            )
                        }

                        val adapter = object : ArrayAdapter<String>(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            userAssociations.map { it.name }
                        ) {
                            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                                val view = super.getView(position, convertView, parent)
                                (view as? TextView)?.setTextColor(resources.getColor(android.R.color.black, null))
                                return view
                            }

                            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                                val view = super.getDropDownView(position, convertView, parent)
                                (view as? TextView)?.apply {
                                    setTextColor(resources.getColor(android.R.color.white, null))
                                    setPadding(32, 24, 32, 24)
                                }
                                return view
                            }
                        }

                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.spinner.adapter = adapter
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Error loading associations", Toast.LENGTH_SHORT).show()
                    }
            }
    }

    private fun addAnnouncement() {
        val title = binding.titleInput.text.toString().trim()
        val content = binding.contentInput.text.toString().trim()
        val userId = auth.currentUser?.uid ?: return

        if (title.isEmpty() || content.isEmpty() || currentUsername == null || userAssociations.isEmpty()) {
            Toast.makeText(requireContext(), "Complete all fields!", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedIndex = binding.spinner.selectedItemPosition
        val selectedAssociationId = userAssociations[selectedIndex].id

        val announcement = hashMapOf(
            "title" to title,
            "content" to content,
            "createdBy" to userId,
            "createdByUsername" to currentUsername,
            "associationId" to selectedAssociationId,
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
