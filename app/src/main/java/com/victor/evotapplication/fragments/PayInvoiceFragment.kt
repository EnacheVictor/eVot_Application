package com.victor.evotapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.victor.evotapplication.databinding.FragmentPayInvoiceBinding
import java.text.SimpleDateFormat
import java.util.*

class PayInvoiceFragment : Fragment() {

    private lateinit var binding: FragmentPayInvoiceBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPayInvoiceBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        loadInvoiceForUser()

        return binding.root
    }

    private fun loadInvoiceForUser() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("user-type").document(userId).get()
            .addOnSuccessListener { userDoc ->
                val apartment = userDoc.getString("apartment") ?: return@addOnSuccessListener
                val associations = userDoc.get("associations") as? List<String> ?: return@addOnSuccessListener
                if (associations.isEmpty()) return@addOnSuccessListener

                val associationId = associations[0]
                db.collection("associations")
                    .document(associationId)
                    .collection("invoices")
                    .orderBy("timestamp")
                    .get()
                    .addOnSuccessListener { invoices ->
                        if (invoices.isEmpty) {
                            binding.paymentStatusText.text = "Nu există facturi disponibile."
                            return@addOnSuccessListener
                        }

                        val latestInvoice = invoices.last()
                        val invoiceMonth = latestInvoice.getString("month") ?: "Luna curentă"
                        val dueDateMillis = latestInvoice.getLong("dueDate") ?: System.currentTimeMillis()
                        val apartmentData = latestInvoice.get("apartments.$apartment") as? Map<*, *> ?: return@addOnSuccessListener

                        val total = apartmentData["total"] as? Double ?: 0.0
                        val paid = apartmentData["paid"] as? Boolean ?: false
                        val components = apartmentData["components"] as? Map<*, *> ?: emptyMap<String, Double>()

                        binding.invoiceTitle.text = "\uD83D\uDCC4 Factura - $invoiceMonth"
                        binding.totalAmountText.text = "Total de plată: %.2f lei".format(total)

                        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        binding.dueDateText.text = "Scadență: ${dateFormat.format(Date(dueDateMillis))}"

                        binding.componentsContainer.removeAllViews()
                        for ((name, value) in components) {
                            val textView = TextView(requireContext())
                            textView.text = "• ${name.toString().replace("_", " ").replaceFirstChar { it.uppercase() }}: ${"%.2f".format(value)} lei"
                            binding.componentsContainer.addView(textView)
                        }

                        binding.paymentStatusText.text = if (paid) "✅ Factură plătită" else "❌ Factură neplătită"
                        binding.payButton.isEnabled = !paid

                        binding.payButton.setOnClickListener {
                            markInvoiceAsPaid(associationId, latestInvoice.id, apartment)
                        }
                    }
            }
    }

    private fun markInvoiceAsPaid(associationId: String, invoiceId: String, apartment: String) {
        val path = "apartments.$apartment.paid"
        db.collection("associations")
            .document(associationId)
            .collection("invoices")
            .document(invoiceId)
            .update(path, true)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "✅ Factura a fost marcată ca plătită", Toast.LENGTH_SHORT).show()
                binding.paymentStatusText.text = "✅ Factură plătită"
                binding.payButton.isEnabled = false
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "❌ Eroare la actualizarea facturii", Toast.LENGTH_SHORT).show()
            }
    }
}
