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
import com.victor.evotapplication.databinding.FragmentAssociationsBinding
import java.util.UUID


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

        checkUserRole() // Verifică rolul utilizatorului curent

        return binding.root
    }


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

    private fun showAdminUI() {
            binding.adminLayout.visibility = View.VISIBLE
            binding.createAssociationBtn.setOnClickListener {
                val assocName = binding.assocNameInput.text.toString()
                if (assocName.isNotEmpty()) {
                    val inviteCode = UUID.randomUUID().toString().substring(0, 6) // Cod unic de 6 caractere
                    saveAssociationsToFirestore(assocName, inviteCode)
                } else {
                    Toast.makeText(requireContext(), "Introdu un nume pentru asociație!", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun saveAssociationsToFirestore(assocName: String, inviteCode: String) {
        val adminId = auth.currentUser?.uid ?: return
        val assocData = hashMapOf(
            "name" to assocName,
            "adminId" to adminId,
            "inviteCode" to inviteCode
        )

        db.collection("associations").add(assocData)
            .addOnSuccessListener {
                binding.inviteCodeText.text = "Cod invitație: $inviteCode"
                Toast.makeText(requireContext(), "Asociație creată!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Eroare la creare!", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(requireContext(), "Introdu un cod!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun joinAssociation(code: String) {
        db.collection("associations")
            .whereEqualTo("inviteCode", code)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val assocId = documents.documents[0].id
                    addUserToAssociation(assocId)
                } else {
                    Toast.makeText(requireContext(), "Cod invalid!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Eroare Firestore!", Toast.LENGTH_SHORT).show()
                Log.e("Firestore", "Eroare la căutarea asociației", e)
            }
    }

    private fun addUserToAssociation(assocId: String) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("associations").document(assocId)
            .update("members", FieldValue.arrayUnion(userId))
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Alăturare reușită!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Eroare la alăturare!", Toast.LENGTH_SHORT).show()
                Log.e("Firestore", "Eroare la update", e)
            }
    }
}
