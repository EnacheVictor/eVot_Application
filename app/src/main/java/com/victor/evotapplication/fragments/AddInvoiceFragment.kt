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
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error fetching associations", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadInvoiceFile(associationId: String) {
        selectedAssociationId = associationId
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        filePickerLauncher.launch(intent)
    }

    private fun performUpload(associationId: String, fileUri: Uri, fileName: String) {
        val storageRef = FirebaseStorage.getInstance().reference
        val path = "invoices/$associationId/${System.currentTimeMillis()}.pdf"
        val fileRef = storageRef.child(path)

        binding.uploadStatusText.text = "⏳ Upload in progres..."

        fileRef.putFile(fileUri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    saveInvoiceRecord(associationId, downloadUri.toString(), fileName)
                }
            }
            .addOnFailureListener {
                binding.uploadStatusText.text = "❌ Upload failed"
                Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveInvoiceRecord(associationId: String, fileUrl: String, fileName: String) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("user-type").document(userId).get()
            .addOnSuccessListener { userDoc ->
                val uploaderName = userDoc.getString("username") ?: "Unknown"

                val invoice = mapOf(
                    "url" to fileUrl,
                    "fileName" to fileName,
                    "timestamp" to System.currentTimeMillis(),
                    "uploadedBy" to uploaderName
                )

                db.collection("associations")
                    .document(associationId)
                    .collection("invoices")
                    .add(invoice)
                    .addOnSuccessListener {
                        binding.uploadStatusText.text = "✅ Invoice uploaded!"
                        Toast.makeText(requireContext(), "Invoice uploaded!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "❌ Error uploading invoice", Toast.LENGTH_SHORT).show()
                    }
            }
    }
}
