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
import com.google.firebase.firestore.FirebaseFirestore
import com.victor.evotapplication.Association
import com.victor.evotapplication.AssociationAdapter
import com.victor.evotapplication.databinding.FragmentHomeBinding

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

    private fun fetchUserAssociations() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("associations")
            .whereArrayContains("members", userId)
            .get()
            .addOnSuccessListener { documents ->
                associationList.clear()
                for (document in documents) {
                    val isAdmin = document.getString("adminId") == userId
                    val inviteCode = if (isAdmin) document.getString("inviteCode") ?: "Fără cod"
                    else ""

                    val association = Association(
                        id = document.id,
                        name = document.getString("name") ?: "Fără nume",
                        inviteCode = inviteCode
                    )
                    associationList.add(association)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Eroare la încărcare!", Toast.LENGTH_SHORT).show()
                Log.e("Firestore", "Eroare la obținerea asociațiilor", e)
            }
    }
}