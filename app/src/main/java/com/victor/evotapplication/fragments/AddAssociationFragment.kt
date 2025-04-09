package com.victor.evotapplication.fragments

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.victor.evotapplication.R

class AddAssociationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_add_association, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val nameInput = view.findViewById<EditText>(R.id.assocNameInput)
        val locationInput = view.findViewById<EditText>(R.id.assocLocationInput)
        val createBtn = view.findViewById<Button>(R.id.createAssociationBtn)

        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()

        createBtn.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val location = locationInput.text.toString().trim()
            val adminId = auth.currentUser?.uid

            if (name.isEmpty() || location.isEmpty()) {
                Toast.makeText(requireContext(), "Complete all fields!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val association = mapOf(
                "name" to name,
                "location" to location,
                "adminId" to adminId
            )

            db.collection("associations")
                .add(association)
                .addOnSuccessListener { documentRef ->
                    // 👇 Adaugă asociația și în lista adminului
                    val assocId = documentRef.id
                    db.collection("user-type").document(adminId!!).update(
                        "associations", FieldValue.arrayUnion(assocId)
                    ).addOnSuccessListener {
                        Toast.makeText(requireContext(), "✅ Association created!", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    }.addOnFailureListener {
                        Toast.makeText(requireContext(), "Association added, but failed to update admin record.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "❌ Error creating association", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
