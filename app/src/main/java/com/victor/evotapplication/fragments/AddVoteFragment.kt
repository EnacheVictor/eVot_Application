package com.victor.evotapplication.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.victor.evotapplication.databinding.FragmentAddVoteBinding
import com.victor.evotapplication.models.Association
import java.text.SimpleDateFormat
import java.util.*

class AddVoteFragment : Fragment() {

    private lateinit var binding: FragmentAddVoteBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var selectedAssociationId: String? = null
    private var selectedDeadline: Date? = null
    private var userAssociations = listOf<Association>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddVoteBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        loadUserAssociations()

        binding.selectDeadlineBtn.setOnClickListener {
            showDateTimePicker()
        }

        binding.createVoteBtn.setOnClickListener {
            createVote()
        }

        return binding.root
    }

    private fun loadUserAssociations() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("user-type").document(userId).get()
            .addOnSuccessListener { userDoc ->
                val role = userDoc.getString("role") ?: "locatar"

                val query = if (role.lowercase() == "admin") {
                    db.collection("associations").whereEqualTo("adminId", userId)
                } else {
                    db.collection("associations").whereArrayContains("members", userId)
                }

                query.get()
                    .addOnSuccessListener { result ->
                        userAssociations = result.map {
                            Association(
                                id = it.id,
                                name = it.getString("name") ?: "Unnamed",
                                location = it.getString("location") ?: "Unknown"
                            )
                        }

                        val adapter = object : ArrayAdapter<String>(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            userAssociations.map { it.name }
                        ) {
                            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                                val view = super.getView(position, convertView, parent)
                                (view as? TextView)?.setTextColor(
                                    resources.getColor(android.R.color.black, null)
                                )
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

                        if (userAssociations.isNotEmpty()) {
                            selectedAssociationId = userAssociations[0].id
                            binding.spinner.setSelection(0)
                        }

                        binding.spinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: android.widget.AdapterView<*>,
                                view: View,
                                position: Int,
                                id: Long
                            ) {
                                selectedAssociationId = userAssociations[position].id
                            }

                            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {
                                selectedAssociationId = null
                            }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Error loading associations", Toast.LENGTH_SHORT).show()
                    }
            }
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(requireContext(), { _, year, month, day ->
            TimePickerDialog(requireContext(), { _, hour, minute ->
                calendar.set(year, month, day, hour, minute)
                selectedDeadline = calendar.time

                val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                binding.selectedDeadline.text = "Deadline: ${format.format(selectedDeadline!!)}"
            }, 12, 0, true).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun createVote() {
        val question = binding.voteQuestionInput.text.toString().trim()
        val userId = auth.currentUser?.uid

        if (question.isEmpty() || selectedDeadline == null || selectedAssociationId == null || userId == null) {
            Toast.makeText(requireContext(), "Please fill in all fields!", Toast.LENGTH_SHORT).show()
            return
        }

        val voteData = hashMapOf(
            "question" to question,
            "createdBy" to userId,
            "associationId" to selectedAssociationId,
            "active" to true,
            "deadline" to selectedDeadline
        )

        db.collection("votes")
            .add(voteData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Vote created!", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error creating vote", Toast.LENGTH_SHORT).show()
            }
    }
}
