package com.victor.evotapplication.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.victor.evotapplication.databinding.ItemInvoiceBinding
import com.victor.evotapplication.models.Invoice
import java.text.SimpleDateFormat
import java.util.*

class InvoiceAdapter(
    private val invoices: List<Invoice>,
    private val onDownloadClick: (String) -> Unit
) : RecyclerView.Adapter<InvoiceAdapter.InvoiceViewHolder>() {

    inner class InvoiceViewHolder(val binding: ItemInvoiceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceViewHolder {
        val binding = ItemInvoiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InvoiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InvoiceViewHolder, position: Int) {
        val invoice = invoices[position]
        val fileName = invoice.fileName

        holder.binding.invoiceName.text = fileName
        holder.binding.uploadedBy.text = "Uploaded by: ${invoice.uploadedBy}"

        val dateFormatted = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            .format(Date(invoice.timestamp))
        holder.binding.invoiceDate.text = "Uploaded on: $dateFormatted"

        holder.binding.downloadButton.setOnClickListener {
            onDownloadClick(invoice.url)
        }
    }

    override fun getItemCount(): Int = invoices.size
}