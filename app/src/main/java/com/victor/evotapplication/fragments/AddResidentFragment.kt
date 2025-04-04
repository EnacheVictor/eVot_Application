package com.victor.evotapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.victor.evotapplication.databinding.FragmentAddResidentBinding
import com.victor.evotapplication.models.Association

class AddResidentFragment : Fragment() {

    private lateinit var binding: FragmentAddResidentBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var adminAssociations = listOf<Association>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddResidentBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        fetchAdminAssociations()

        binding.generateButton.setOnClickListener {
            if (adminAssociations.isNotEmpty()) {
                val selectedIndex = binding.spinner.selectedItemPosition
                val selectedAssociation = adminAssociations[selectedIndex]
                generateInviteCode(selectedAssociation.id)
            } else {
                Toast.makeText(requireContext(), "Please select an association first.", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    private fun fetchAdminAssociations() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("associations")
            .whereEqualTo("adminId", userId)
            .get()
            .addOnSuccessListener { documents ->
                adminAssociations = documents.map {
                    Association(it.id, it.getString("name") ?: "No name", it.getString("location") ?: "No location")
                }

                val adapter = object : ArrayAdapter<String>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    adminAssociations.map { it.name }
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
                Toast.makeText(requireContext(), "Error fetching associations", Toast.LENGTH_SHORT).show()
            }
    }

    private fun generateInviteCode(associationId: String) {
        val code = generateUniqueCode()
        val inviteData = hashMapOf(
            "code" to code,
            "associationId" to associationId,
            "timestamp" to System.currentTimeMillis(),
            "used" to false
        )

        db.collection("invites").document(code)
            .set(inviteData)
            .addOnSuccessListener {
                binding.generatedCode.text = "Generated code: $code"
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error generating code", Toast.LENGTH_SHORT).show()
            }
    }

    private fun generateUniqueCode(): String {
        val letters = ('A'..'Z') + ('a'..'z')
        val numbers = ('0'..'9')
        return (1..6).map { letters.random() }.joinToString("") +
                (1..3).map { numbers.random() }.joinToString("")
    }
}
