package com.victor.evotapplication.fragments

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.victor.evotapplication.R
import com.victor.evotapplication.databinding.FragmentLinkUserBinding
import com.victor.evotapplication.models.Association

class LinkUserFragment : Fragment() {

    private lateinit var binding: FragmentLinkUserBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var associations = listOf<Association>()
    private var users = listOf<Pair<String, String>>() // Pair<UID, Username>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLinkUserBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        fetchAssociations()

        binding.linkButton.setOnClickListener {
            linkUserToApartment()
        }

        binding.selfAssignCheckbox.setOnCheckedChangeListener { _, isChecked ->
            binding.userSpinner.isEnabled = !isChecked
        }

        return binding.root
    }

    private fun fetchAssociations() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("associations")
            .whereEqualTo("adminId", uid)
            .get()
            .addOnSuccessListener { result ->
                associations = result.map {
                    Association(it.id, it.getString("name") ?: "", it.getString("location") ?: "")
                }
                val adapter = ArrayAdapter(
                    requireContext(),
                    R.layout.spinner2,
                    associations.map { "${it.name} - ${it.location}" }
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.associationSpinner.adapter = adapter

                binding.associationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        fetchUsersForAssociation(associations[position].id)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
            }
    }

    private fun fetchUsersForAssociation(associationId: String) {
        db.collection("user-type")
            .whereArrayContains("associations", associationId)
            .get()
            .addOnSuccessListener { docs ->
                users = docs
                    .filter { it.getString("role")?.lowercase() != "admin" }
                    .map { it.id to (it.getString("username") ?: "Unknown") }

                val adapter = ArrayAdapter(
                    requireContext(),
                    R.layout.spinner2,
                    users.map { it.second }
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.userSpinner.adapter = adapter
            }
    }

    private fun linkUserToApartment() {
        val associationIndex = binding.associationSpinner.selectedItemPosition
        val userIndex = binding.userSpinner.selectedItemPosition

        if (associationIndex < 0 || (!binding.selfAssignCheckbox.isChecked && userIndex < 0)) {
            Toast.makeText(context, "Select an association and user", Toast.LENGTH_SHORT).show()
            return
        }

        val associationId = associations[associationIndex].id
        val userId = if (binding.selfAssignCheckbox.isChecked) {
            auth.currentUser!!.uid
        } else {
            users.getOrNull(userIndex)?.first
        }

        if (userId == null) {
            Toast.makeText(context, "User ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        val apartment = binding.apartmentEditText.text.toString().trim()
        val parking = binding.parkingEditText.text.toString().trim()

        val updates = mutableMapOf<String, Any>()
        if (apartment.isNotEmpty()) updates["apartment"] = apartment
        if (parking.isNotEmpty()) updates["parking"] = parking

        db.collection("user-type").document(userId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(context, "✅ Proprietatea a fost salvată", Toast.LENGTH_SHORT).show()
                binding.apartmentEditText.text.clear()
                binding.parkingEditText.text.clear()
            }
            .addOnFailureListener {
                Toast.makeText(context, "❌ Eroare la salvare", Toast.LENGTH_SHORT).show()
            }
    }
}