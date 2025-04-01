package com.victor.evotapplication.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.victor.evotapplication.R
import com.victor.evotapplication.VoteAdapter
import com.victor.evotapplication.databinding.FragmentVotesBinding
import com.victor.evotapplication.models.Vote
import java.util.*

// Fragment for displaying and managing votes in an association

class VotesFragment : Fragment() {

    private lateinit var binding: FragmentVotesBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val voteList = mutableListOf<Vote>()
    private lateinit var adapter: VoteAdapter

    private var isAdmin: Boolean = false
    private var associationId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVotesBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        associationId = arguments?.getString("associationId")

        // Navigate to AddVoteFragment

        binding.addVoteBtn.setOnClickListener {
            val fragment = AddVoteFragment()
            fragment.arguments = Bundle().apply {
                putString("associationId", associationId)
            }
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        checkIfAdmin()

        return binding.root
    }

    private fun checkIfAdmin() {
        val userId = auth.currentUser?.uid ?: return
        if (associationId == null) return

        db.collection("associations").document(associationId!!)
            .get()
            .addOnSuccessListener { document ->
                val adminId = document.getString("adminId")
                isAdmin = adminId == userId

                if (isAdmin) {
                    binding.addVoteBtn.visibility = View.VISIBLE
                }

                setupRecyclerView()
                loadVotes()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error checking role", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupRecyclerView() {
        adapter = VoteAdapter(voteList, isAdmin, associationId ?: "")
        binding.votesRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.votesRecycler.adapter = adapter
    }

    private fun loadVotes() {
        if (associationId == null) return

        db.collection("votes")
            .whereEqualTo("associationId", associationId)
            .orderBy("deadline")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Votes", "Error fetching vote", error)
                    return@addSnapshotListener
                }

                voteList.clear()
                snapshot?.forEach { doc ->
                    val vote = Vote(
                        id = doc.id,
                        question = doc.getString("question") ?: "",
                        createdBy = doc.getString("createdBy") ?: "",
                        associationId = doc.getString("associationId") ?: "",
                        active = doc.getBoolean("active") ?: false,
                        deadline = doc.getTimestamp("deadline")?.toDate()
                    )
                    voteList.add(vote)
                }

                adapter.notifyDataSetChanged()
            }
    }
}