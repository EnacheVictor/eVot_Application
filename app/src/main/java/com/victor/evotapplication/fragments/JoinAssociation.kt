package com.victor.evotapplication.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.victor.evotapplication.databinding.FragmentJoinAssociationBinding

class JoinAssociation : Fragment() {

    private lateinit var binding: FragmentJoinAssociationBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentJoinAssociationBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupJoinLogic()

        return binding.root
    }

    private fun setupJoinLogic() {

        binding.joinAssociationBtn.setOnClickListener {
            val code = binding.joinCodeInput.text.toString().trim()
            if (code.isEmpty()) {
                Toast.makeText(requireContext(), "Insert invite code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.collection("invites").document(code)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val associationId = document.getString("associationId") ?: ""
                        val used = document.getBoolean("used") ?: false
                        val timestamp = document.getLong("timestamp") ?: 0L
                        val isExpired = System.currentTimeMillis() - timestamp > 24 * 60 * 60 * 1000

                        if (!used && !isExpired) {
                            saveAssociation(associationId)
                            document.reference.delete()
                        } else {
                            Toast.makeText(requireContext(), "Code expired or already used.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Invalid code.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error verifying code.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveAssociation(associationId: String) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = db.collection("user-type").document(userId)

        db.collection("associations").document(associationId)
            .update("members", FieldValue.arrayUnion(userId))
            .addOnSuccessListener {
                userRef.update("associations", FieldValue.arrayUnion(associationId))
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "✅ Joined successfully!", Toast.LENGTH_SHORT).show()
                        // Optionally: go back or update UI
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Joined association but failed to update user.", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "❌ Error joining association", Toast.LENGTH_SHORT).show()
            }
    }
}
