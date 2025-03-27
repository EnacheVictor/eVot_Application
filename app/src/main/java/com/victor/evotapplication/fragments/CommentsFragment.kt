package com.victor.evotapplication.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.victor.evotapplication.CommentsAdapter
import com.victor.evotapplication.databinding.FragmentCommentsBinding
import com.victor.evotapplication.models.Comment

class CommentsFragment : Fragment() {

    private lateinit var binding: FragmentCommentsBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: CommentsAdapter
    private val commentList = mutableListOf<Comment>()

    private var announcementId: String? = null
    private var currentUserId: String? = null
    private var currentUsername: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommentsBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        announcementId = arguments?.getString("announcementId")
        currentUserId = auth.currentUser?.uid

        if (announcementId == null || currentUserId == null) {
            Toast.makeText(requireContext(), "Error: Missing data", Toast.LENGTH_SHORT).show()
            return binding.root
        }

        getCurrentUsername()
        setupRecyclerView()
        loadComments()

        binding.sendCommentBtn.setOnClickListener {
            addComment()
        }

        return binding.root
    }

    private fun getCurrentUsername() {
        db.collection("user-type").document(currentUserId!!)
            .get()
            .addOnSuccessListener { doc ->
                currentUsername = doc.getString("username")
            }
    }

    private fun setupRecyclerView() {
        adapter = CommentsAdapter(commentList, currentUserId!!) { comment ->
            deleteComment(comment)
        }
        binding.commentsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.commentsRecycler.adapter = adapter
    }

    private fun loadComments() {
        db.collection("comments")
            .whereEqualTo("announcementId", announcementId)
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Comments", "Error fetching comments", error)
                    return@addSnapshotListener
                }

                commentList.clear()
                for (doc in snapshot!!) {
                    val comment = Comment(
                        id = doc.id,
                        announcementId = doc.getString("announcementId") ?: "",
                        userId = doc.getString("userId") ?: "",
                        username = doc.getString("username") ?: "",
                        text = doc.getString("text") ?: "",
                        timestamp = doc.getTimestamp("timestamp")?.toDate()
                    )
                    commentList.add(comment)
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun addComment() {
        val text = binding.commentInput.text.toString().trim()
        if (text.isEmpty() || currentUsername == null) {
            Toast.makeText(requireContext(), "Write a comment", Toast.LENGTH_SHORT).show()
            return
        }

        val commentData = hashMapOf(
            "announcementId" to announcementId,
            "userId" to currentUserId,
            "username" to currentUsername,
            "text" to text,
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("comments")
            .add(commentData)
            .addOnSuccessListener {
                binding.commentInput.text.clear()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error with comment", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteComment(comment: Comment) {
        db.collection("comments").document(comment.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Comment deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error when deleting the comment", Toast.LENGTH_SHORT).show()
            }
    }
}