package com.victor.evotapplication.fragments

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.victor.evotapplication.databinding.FragmentRemoveBinding
import com.victor.evotapplication.models.Association

class RemoveFragment : Fragment() {

    private lateinit var binding: FragmentRemoveBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private val associations = mutableListOf<Association>()
    private val residents = mutableListOf<Pair<String, String>>() // Pair<username, uid>

    private var selectedAssociationId: String? = null
    private var selectedResidentUid: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRemoveBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        loadAdminAssociations()

        binding.removeButton.setOnClickListener {
            selectedAssociationId?.let { assocId ->
                selectedResidentUid?.let { userId ->
                    removeUserFromAssociation(userId, assocId)
                }
            }
        }

        return binding.root
    }

    private fun loadAdminAssociations() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("associations")
            .whereEqualTo("adminId", userId)
            .get()
            .addOnSuccessListener { documents ->
                associations.clear()
                associations.addAll(documents.map {
                    Association(
                        id = it.id,
                        name = it.getString("name") ?: "Unnamed",
                        location = it.getString("location") ?: "Unknown"
                    )
                })

                val adapter = object : ArrayAdapter<String>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    associations.map { "${it.name} - ${it.location}" }
                ) {
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view = super.getView(position, convertView, parent) as TextView
                        view.setTextColor(resources.getColor(android.R.color.black, null))
                        return view
                    }

                    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view = super.getDropDownView(position, convertView, parent) as TextView
                        view.setTextColor(resources.getColor(android.R.color.white, null))
                        return view
                    }
                }

                binding.associationSpinner.adapter = adapter

                binding.associationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        selectedAssociationId = associations[position].id
                        loadResidents(selectedAssociationId!!)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error loading associations", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadResidents(associationId: String) {
        db.collection("user-type")
            .whereArrayContains("associations", associationId)
            .get()
            .addOnSuccessListener { documents ->
                residents.clear()
                for (doc in documents) {
                    if (doc.getString("role")?.lowercase() != "admin") {
                        val username = doc.getString("username") ?: "Unknown"
                        val uid = doc.id
                        residents.add(Pair(username, uid))
                    }
                }

                val adapter = object : ArrayAdapter<String>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    residents.map { it.first }
                ) {
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view = super.getView(position, convertView, parent) as TextView
                        view.setTextColor(resources.getColor(android.R.color.black, null))
                        return view
                    }

                    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view = super.getDropDownView(position, convertView, parent) as TextView
                        view.setTextColor(resources.getColor(android.R.color.white, null))
                        return view
                    }
                }

                binding.residentSpinner.adapter = adapter

                binding.residentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        selectedResidentUid = residents[position].second
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error loading residents", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeUserFromAssociation(userId: String, associationId: String) {
        val userRef = db.collection("user-type").document(userId)
        val assocRef = db.collection("associations").document(associationId)

        assocRef.update("members", FieldValue.arrayRemove(userId)).addOnSuccessListener {
            userRef.update("associations", FieldValue.arrayRemove(associationId)).addOnSuccessListener {
                Toast.makeText(requireContext(), "Resident removed successfully!", Toast.LENGTH_SHORT).show()
                loadResidents(associationId)
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to remove resident", Toast.LENGTH_SHORT).show()
        }
    }
}
