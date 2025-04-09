package com.victor.evotapplication.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.victor.evotapplication.R
import com.victor.evotapplication.adapters.UserAdapter
import com.victor.evotapplication.databinding.FragmentListUserBinding
import com.victor.evotapplication.models.Association

class ListUserFragment : Fragment() {

    private lateinit var binding: FragmentListUserBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private val associationList = mutableListOf<Association>()
    private val userList = mutableListOf<String>()
    private lateinit var adapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListUserBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupRecyclerView()
        loadAssociations()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = UserAdapter(userList)
        binding.userRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.userRecyclerView.adapter = adapter
    }

    private fun loadAssociations() {
        val userId = auth.currentUser?.uid ?: return
        Log.d("ListUserFragment", "Fetching user document for $userId")

        db.collection("user-type").document(userId).get()
            .addOnSuccessListener { userDoc ->
                val userAssociations = userDoc.get("associations") as? List<String> ?: listOf()
                if (userAssociations.isEmpty()) {
                    Toast.makeText(context, "No associations found!", Toast.LENGTH_SHORT).show()
                    Log.w("ListUserFragment", "No associations in user document.")
                    return@addOnSuccessListener
                }

                Log.d("ListUserFragment", "User associations: $userAssociations")

                db.collection("associations")
                    .whereIn(FieldPath.documentId(), userAssociations)
                    .get()
                    .addOnSuccessListener { docs ->
                        associationList.clear()
                        for (doc in docs) {
                            val association = Association(
                                doc.id,
                                doc.getString("name") ?: "No name",
                                doc.getString("location") ?: "No location"
                            )
                            associationList.add(association)
                        }

                        if (associationList.isEmpty()) {
                            Toast.makeText(context, "No associations found!", Toast.LENGTH_SHORT).show()
                            Log.w("ListUserFragment", "Association list empty after fetch.")
                            return@addOnSuccessListener
                        }

                        val spinnerItems = associationList.map { "${it.name} - ${it.location}" }
                        val spinnerAdapter = object : ArrayAdapter<String>(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            spinnerItems
                        ) {
                            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                                val view = super.getView(position, convertView, parent)
                                (view as? TextView)?.setTextColor(resources.getColor(R.color.text_dark, null))
                                return view
                            }

                            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                                val view = super.getDropDownView(position, convertView, parent)
                                (view as? TextView)?.setTextColor(resources.getColor(R.color.white, null))
                                return view
                            }
                        }

                        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.associationSpinner.adapter = spinnerAdapter

                        binding.associationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                                val selectedId = associationList[position].id
                                Log.d("ListUserFragment", "Selected association: $selectedId")
                                loadUsersInAssociation(selectedId)
                            }

                            override fun onNothingSelected(parent: AdapterView<*>) {}
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error loading associations", Toast.LENGTH_SHORT).show()
                        Log.e("ListUserFragment", "Failed to load associations: ${it.message}")
                    }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error loading user data", Toast.LENGTH_SHORT).show()
                Log.e("ListUserFragment", "Failed to fetch user doc: ${it.message}")
            }
    }

    private fun loadUsersInAssociation(associationId: String) {
        Log.d("ListUserFragment", "Loading users in association $associationId")

        db.collection("user-type")
            .whereArrayContains("associations", associationId)
            .get()
            .addOnSuccessListener { docs ->
                userList.clear()
                for (doc in docs) {
                    if (doc.getString("role")?.lowercase() != "admin") {
                        val username = doc.getString("username") ?: "Unknown"
                        userList.add(username)
                    }
                }

                Log.d("ListUserFragment", "Users loaded: ${userList.size}")
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Could not load users", Toast.LENGTH_SHORT).show()
                Log.e("ListUserFragment", "Error loading users: ${it.message}")
            }
    }
}
