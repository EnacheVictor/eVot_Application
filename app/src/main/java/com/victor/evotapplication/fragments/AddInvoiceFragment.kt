package com.victor.evotapplication.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.victor.evotapplication.databinding.FragmentAddInvoiceBinding
import com.victor.evotapplication.models.Association
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.util.*

class AddInvoiceFragment : Fragment() {

    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var binding: FragmentAddInvoiceBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var adminAssociations = listOf<Association>()
    private var selectedAssociationId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddInvoiceBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        fetchAdminAssociations()
        setupFilePicker()

        binding.uploadExcelButton.setOnClickListener {
            if (adminAssociations.isNotEmpty()) {
                val selectedIndex = binding.spinner.selectedItemPosition
                val selectedAssociation = adminAssociations[selectedIndex]
                uploadInvoiceFile(selectedAssociation.id)
            } else {
                Toast.makeText(requireContext(), "Select an association.", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    private fun setupFilePicker() {
        filePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                uri?.let {
                    val input = EditText(requireContext())
                    input.hint = "Ex: February Invoice"
                    input.setPadding(40, 30, 40, 30)

                    AlertDialog.Builder(requireContext())
                        .setTitle("Name of the invoice")
                        .setMessage("Insert the name of the invoice you want to upload.")
                        .setView(input)
                        .setPositiveButton("Upload") { _, _ ->
                            val fileName = input.text.toString().ifEmpty { "No name provided" }
                            selectedAssociationId?.let { id ->
                                performUpload(id, uri, fileName)
                            }
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }
        }
    }

    private fun fetchAdminAssociations() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("associations")
            .whereEqualTo("adminId", userId)
            .get()
            .addOnSuccessListener { documents ->
                adminAssociations = documents.map {
                    Association(
                        it.id,
                        it.getString("name") ?: "No name",
                        it.getString("location") ?: "No location"
                    )
                }

                val adapter = object : ArrayAdapter<String>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    adminAssociations.map { it.name }
                ) {
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view = super.getView(position, convertView, parent)
                        (view as? TextView)?.setTextColor(resources.getColor(android.R.color.black, null))
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
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error fetching associations", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadInvoiceFile(associationId: String) {
        selectedAssociationId = associationId
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        filePickerLauncher.launch(intent)
    }

    private fun performUpload(associationId: String, fileUri: Uri, fileName: String) {
        val inputStream = requireContext().contentResolver.openInputStream(fileUri)
        val workbook = WorkbookFactory.create(inputStream)
        val sheet = workbook.getSheetAt(0)

        val headerRow = sheet.getRow(0)
        val headers = mutableListOf<String>()
        for (i in 0 until headerRow.lastCellNum) {
            headers.add(headerRow.getCell(i)?.toString()?.trim() ?: "")
        }

        val apartments = mutableMapOf<String, Any>()
        for (i in 1..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue
            val apt = row.getCell(0)?.toString()?.trim() ?: continue

            var total = 0.0
            val components = mutableMapOf<String, Double>()
            for (j in 1 until headers.size) {
                val rubric = headers[j].lowercase()
                if (rubric.contains("parcare")) continue
                val value = row.getCell(j)?.numericCellValue ?: 0.0
                total += value
                components[headers[j]] = value
            }

            apartments[apt] = mapOf(
                "total" to total,
                "paid" to false,
                "components" to components
            )
        }

        val storageRef = FirebaseStorage.getInstance().reference
        val path = "invoices/$associationId/${System.currentTimeMillis()}.pdf"
        val fileRef = storageRef.child(path)

        binding.uploadStatusText.text = "⏳ Upload în progres..."

        fileRef.putFile(fileUri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val userId = auth.currentUser?.uid ?: return@addOnSuccessListener
                    db.collection("user-type").document(userId).get()
                        .addOnSuccessListener { userDoc ->
                            val uploaderName = userDoc.getString("username") ?: "Unknown"

                            val invoice = mapOf(
                                "url" to downloadUri.toString(),
                                "fileName" to fileName,
                                "month" to fileName,
                                "timestamp" to System.currentTimeMillis(),
                                "dueDate" to getDueDateMillis(),
                                "uploadedBy" to uploaderName,
                                "apartments" to apartments
                            )

                            db.collection("associations")
                                .document(associationId)
                                .collection("invoices")
                                .add(invoice)
                                .addOnSuccessListener {
                                    binding.uploadStatusText.text = "✅ Factura încărcată!"
                                    Toast.makeText(requireContext(), "Factura a fost salvată.", Toast.LENGTH_SHORT).show()
                                }
                        }
                }
            }
            .addOnFailureListener {
                binding.uploadStatusText.text = "❌ Upload eșuat"
                Toast.makeText(requireContext(), "Eroare: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getDueDateMillis(): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, 1)
        cal.set(Calendar.DAY_OF_MONTH, 15)
        return cal.timeInMillis
    }
}