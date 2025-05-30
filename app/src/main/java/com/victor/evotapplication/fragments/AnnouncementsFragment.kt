package com.victor.evotapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.victor.evotapplication.R
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.victor.evotapplication.adapters.AnnouncementsAdapter
import com.victor.evotapplication.databinding.FragmentAnnouncementsBinding
import java.util.*

// Data model for announcements

data class Announcement(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val createdByUsername: String = "",
    val timestamp: Date? = null
)

// Fragment for displaying a list of announcements in an association

class AnnouncementsFragment : Fragment() {

    private lateinit var binding: FragmentAnnouncementsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var associationId: String? = null
    private val announcementsList = ArrayList<Announcement>()
    private lateinit var adapter: AnnouncementsAdapter
    private var isAdmin: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAnnouncementsBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        associationId = arguments?.getString("associationId")

        if (associationId == null) {
            Toast.makeText(requireContext(), "Association not found", Toast.LENGTH_SHORT).show()
            return binding.root
        }

        checkIfAdminAndLoad()

        return binding.root
    }

    private fun checkIfAdminAndLoad() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("associations").document(associationId!!)
            .get()
            .addOnSuccessListener { document ->
                val adminId = document.getString("adminId")
                isAdmin = adminId == userId

                setupRecyclerView()
                loadAnnouncements()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Checking admin role failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupRecyclerView() {
        adapter = AnnouncementsAdapter(
            announcements = announcementsList,
            isAdmin = isAdmin,
            onDelete = { announcement -> deleteAnnouncement(announcement) },
            onEdit = { announcement -> editAnnouncement(announcement) }
        )
        binding.announcementsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.announcementsRecycler.adapter = adapter
    }

    private fun loadAnnouncements() {
        db.collection("announcements")
            .whereEqualTo("associationId", associationId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                announcementsList.clear()
                for (doc in documents) {
                    announcementsList.add(
                        Announcement(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            content = doc.getString("content") ?: "",
                            createdByUsername = doc.getString("createdByUsername") ?: "Anonim",
                            timestamp = doc.getTimestamp("timestamp")?.toDate()
                        )
                    )
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error loading announcements", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteAnnouncement(announcement: Announcement) {
        db.collection("announcements").document(announcement.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Announcement deleted!", Toast.LENGTH_SHORT).show()
                loadAnnouncements()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error deleting announcement", Toast.LENGTH_SHORT).show()
            }
    }

    private fun editAnnouncement(announcement: Announcement) {
        val fragment = EditAnnouncementFragment()
        fragment.arguments = Bundle().apply {
            putString("announcementId", announcement.id)
            putString("title", announcement.title)
            putString("content", announcement.content)
        }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}