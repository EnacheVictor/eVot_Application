package com.victor.evotapplication

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.victor.evotapplication.models.Vote
import java.text.SimpleDateFormat
import java.util.*

class VoteAdapter(
    private val votes: List<Vote>,
    private val isAdmin: Boolean,
    private val associationId: String
) : RecyclerView.Adapter<VoteAdapter.VoteViewHolder>() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    inner class VoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cancelledText: TextView = view.findViewById(R.id.vote_cancelled)
        val questionText: TextView = view.findViewById(R.id.vote_question)
        val deadlineText: TextView = view.findViewById(R.id.vote_deadline)
        val statusText: TextView = view.findViewById(R.id.vote_status)

        val daBtn: Button = view.findViewById(R.id.vote_yes_btn)
        val nuBtn: Button = view.findViewById(R.id.vote_no_btn)
        val abstainBtn: Button = view.findViewById(R.id.vote_abstain_btn)

        val resultText: TextView = view.findViewById(R.id.vote_results)
        val cancelVoteBtn: Button = view.findViewById(R.id.cancel_vote_btn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vote, parent, false)
        return VoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: VoteViewHolder, position: Int) {
        val vote = votes[position]
        val userId = auth.currentUser?.uid ?: return

        holder.questionText.text = vote.question
        if (!vote.active) {
            holder.cancelledText.visibility = View.VISIBLE
        } else {
            holder.cancelledText.visibility = View.GONE
        }
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        holder.deadlineText.text = "Limit: ${vote.deadline?.let { dateFormat.format(it) } ?: "-"}"
        holder.statusText.text = if (vote.active) "Active" else "Canceled"

        holder.cancelVoteBtn.visibility = if (isAdmin && vote.active) View.VISIBLE else View.GONE

        val now = Date()
        val expired = vote.deadline != null && now.after(vote.deadline)

        if (!vote.active || expired) {
            holder.statusText.text = if (!vote.active) "Canceled" else "Ended"
            holder.daBtn.visibility = View.GONE
            holder.nuBtn.visibility = View.GONE
            holder.abstainBtn.visibility = View.GONE
        }

        db.collection("votes").document(vote.id)
            .collection("responses").document(userId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val raspuns = snapshot.getString("answer") ?: ""
                    holder.daBtn.visibility = View.GONE
                    holder.nuBtn.visibility = View.GONE
                    holder.abstainBtn.visibility = View.GONE
                    holder.statusText.text = "You voted: $raspuns"
                } else if (vote.active && !expired) {
                    holder.daBtn.setOnClickListener { vote(userId, vote.id, "YES") }
                    holder.nuBtn.setOnClickListener { vote(userId, vote.id, "NO") }
                    holder.abstainBtn.setOnClickListener { vote(userId, vote.id, "I abstain") }
                }
            }

        // Calcul voturi
        db.collection("votes").document(vote.id)
            .collection("responses")
            .get()
            .addOnSuccessListener { docs ->
                var yes = 0
                var no = 0
                var abstin = 0
                for (doc in docs) {
                    when (doc.getString("answer")) {
                        "YES" -> yes++
                        "NO" -> no++
                        "I abstain" -> abstin++
                    }
                }
                holder.resultText.text = "Yes: $yes | No: $no | Mă abțin: $abstin"
            }

        holder.cancelVoteBtn.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Cancel the vote?")
                .setMessage("Are you sure you want to cancel the vote?")
                .setPositiveButton("Yes") { _, _ ->
                    db.collection("votes").document(vote.id)
                        .update("active", false)
                        .addOnSuccessListener {
                            Toast.makeText(holder.itemView.context, "Vote canceled", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("No", null)
                .show()
        }

    }

    override fun getItemCount(): Int = votes.size

    private fun vote(userId: String, voteId: String, answer: String) {
        val response = hashMapOf(
            "userId" to userId,
            "answer" to answer,
            "timestamp" to Timestamp.now()
        )
        db.collection("votes").document(voteId)
            .collection("responses").document(userId)
            .set(response)
    }
}