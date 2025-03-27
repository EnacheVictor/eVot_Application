package com.victor.evotapplication.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.victor.evotapplication.databinding.FragmentAddVoteBinding
import java.text.SimpleDateFormat
import java.util.*

class AddVoteFragment : Fragment() {

    private lateinit var binding: FragmentAddVoteBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var associationId: String? = null
    private var selectedDeadline: Date? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddVoteBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        associationId = arguments?.getString("associationId")

        binding.selectDeadlineBtn.setOnClickListener {
            showDateTimePicker()
        }

        binding.createVoteBtn.setOnClickListener {
            createVote()
        }

        return binding.root
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(requireContext(), { _, year, month, day ->
            TimePickerDialog(requireContext(), { _, hour, minute ->
                calendar.set(year, month, day, hour, minute)
                selectedDeadline = calendar.time

                val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                binding.selectedDeadline.text = "Limit: ${format.format(selectedDeadline!!)}"
            }, 12, 0, true).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun createVote() {
        val question = binding.voteQuestionInput.text.toString().trim()
        val userId = auth.currentUser?.uid

        if (question.isEmpty() || selectedDeadline == null || userId == null || associationId == null) {
            Toast.makeText(requireContext(), "Complete all fields!", Toast.LENGTH_SHORT).show()
            return
        }

        val voteData = hashMapOf(
            "question" to question,
            "createdBy" to userId,
            "associationId" to associationId,
            "active" to true,
            "deadline" to selectedDeadline
        )

        db.collection("votes")
            .add(voteData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Vote created successfully!", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack() // înapoi
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error creating the vote", Toast.LENGTH_SHORT).show()
            }
    }
}