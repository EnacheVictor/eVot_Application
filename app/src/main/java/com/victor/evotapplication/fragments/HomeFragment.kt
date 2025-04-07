package com.victor.evotapplication.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.victor.evotapplication.R
import com.victor.evotapplication.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        checkUserRole()

        binding.adminDashboardBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AdminPanelFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.associationBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AssociationsFragment())
                .addToBackStack(null)
                .commit()
        }

        return binding.root
    }

    private fun checkUserRole() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("user-type").document(uid).get()
            .addOnSuccessListener { doc ->
                val role = doc.getString("role") ?: ""
                val username = doc.getString("username") ?: "User"

                binding.welcomeText.text = "Welcome, $username!"

                if (role.equals("admin", ignoreCase = true)) {
                    binding.adminDashboardBtn.visibility = View.VISIBLE
                } else {
                    binding.associationBtn.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error loading user info", Toast.LENGTH_SHORT).show()
            }
    }
}