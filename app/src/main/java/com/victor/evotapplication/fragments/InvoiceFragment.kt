package com.victor.evotapplication.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.victor.evotapplication.R
import com.victor.evotapplication.adapters.InvoiceAdapter
import com.victor.evotapplication.databinding.FragmentInvoiceBinding
import com.victor.evotapplication.models.Association
import com.victor.evotapplication.models.Invoice

class InvoiceFragment : Fragment() {

    private lateinit var binding: FragmentInvoiceBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: InvoiceAdapter

    private val invoiceList = mutableListOf<Invoice>()
    private val associationList = mutableListOf<Association>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInvoiceBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupRecyclerView()
        loadUserAssociations()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = InvoiceAdapter(invoiceList) { url ->
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
        binding.invoiceRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.invoiceRecyclerView.adapter = adapter
    }

    private fun loadUserAssociations() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("user-type").document(userId)
            .get()
            .addOnSuccessListener { userDoc ->
                val role = userDoc.getString("role") ?: "locatar"

                if (role.lowercase() == "admin") {
                    db.collection("associations")
                        .whereEqualTo("adminId", userId)
                        .get()
                        .addOnSuccessListener { docs ->
                            processAssociationDocuments(docs)
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Error loading associations", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    val userAssociations = userDoc.get("associations") as? List<String> ?: listOf()
                    if (userAssociations.isEmpty()) {
                        Toast.makeText(context, "You are not a member of any association.", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    db.collection("associations")
                        .whereIn(FieldPath.documentId(), userAssociations)
                        .get()
                        .addOnSuccessListener { docs ->
                            processAssociationDocuments(docs)
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Error loading associations", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error loading user data", Toast.LENGTH_SHORT).show()
            }
    }
    private fun processAssociationDocuments(docs: Iterable<com.google.firebase.firestore.DocumentSnapshot>) {
        associationList.clear()

        for (doc in docs) {
            associationList.add(
                Association(
                    id = doc.id,
                    name = doc.getString("name") ?: "Fără nume",
                    location = doc.getString("location") ?: "Fără locație"
                )
            )
        }

        val names = associationList.map { it.name }
        val spinnerAdapter = ArrayAdapter(requireContext(), R.layout.spinner, names)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.associationSpinner.adapter = spinnerAdapter

        binding.associationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedAssociationId = associationList[position].id
                loadInvoicesForAssociation(selectedAssociationId)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }


    private fun loadInvoicesForAssociation(associationId: String) {
        db.collection("associations")
            .document(associationId)
            .collection("invoices")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { docs ->
                invoiceList.clear()
                for (doc in docs) {
                    val url = doc.getString("url") ?: continue
                    val fileName = doc.getString("fileName") ?: continue
                    val timestamp = doc.getLong("timestamp") ?: 0L
                    val uploadedBy = doc.getString("uploadedBy") ?: "Necunoscut"
                    invoiceList.add(Invoice(url, fileName , timestamp, uploadedBy))
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(context, "❌ Could not load invoices.", Toast.LENGTH_SHORT).show()
            }
    }
}