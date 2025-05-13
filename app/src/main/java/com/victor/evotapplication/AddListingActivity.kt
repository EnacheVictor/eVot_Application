package com.victor.evotapplication

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.victor.evotapplication.models.Listing
import com.victor.evotapplication.models.ListingType
import java.util.*

class AddListingActivity : AppCompatActivity() {

    private lateinit var imageViewSelected: ImageView
    private lateinit var editTextTitle: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var editTextPrice: EditText
    private lateinit var editTextContactInfo: EditText
    private lateinit var spinnerType: Spinner
    private lateinit var spinnerAssociation: Spinner
    private lateinit var buttonPublish: Button

    private var imageUri: Uri? = null
    private var associationList = listOf<Association>()

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var loadingDialog: MaterialDialog? = null

    data class Association(val id: String, val name: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_listing)

        imageViewSelected = findViewById(R.id.imageViewSelected)
        editTextTitle = findViewById(R.id.editTextTitle)
        editTextDescription = findViewById(R.id.editTextDescription)
        editTextPrice = findViewById(R.id.editTextPrice)
        editTextContactInfo = findViewById(R.id.editTextContactInfo)
        spinnerType = findViewById(R.id.spinnerType)
        spinnerAssociation = findViewById(R.id.spinnerAssociation)
        buttonPublish = findViewById(R.id.buttonPublish)

        setupSpinners()

        loadUserAssociations()

        imageViewSelected.setOnClickListener {
            pickImageFromGallery()
        }

        buttonPublish.setOnClickListener {
            if (validateInputs()) {
                uploadImageAndSaveListing()
            }
        }
    }

    private fun setupSpinners() {
        val typeAdapter = ArrayAdapter(
            this,
            R.layout.spinner_item_black,
            ListingType.values()
        )
        typeAdapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item)
        spinnerType.adapter = typeAdapter
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 1001)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            imageViewSelected.setImageURI(imageUri)
        }
    }

    private fun loadUserAssociations() {
        val currentUserId = auth.currentUser?.uid ?: return

        firestore.collection("user-type").document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                val associationsIds = document["associations"] as? List<String> ?: listOf()

                if (associationsIds.isEmpty()) {
                    Toast.makeText(this, "Nu faci parte din nicio asociație.", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }

                firestore.collection("associations")
                    .whereIn(FieldPath.documentId(), associationsIds)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        associationList = snapshot.documents.map {
                            Association(id = it.id, name = it.getString("name") ?: "Fără nume")
                        }

                        val associationAdapter = ArrayAdapter(
                            this,
                            R.layout.spinner_item_black,
                            associationList.map { it.name }
                        )
                        associationAdapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item)
                        spinnerAssociation.adapter = associationAdapter
                    }
            }
    }

    private fun validateInputs(): Boolean {
        if (editTextTitle.text.isEmpty() ||
            editTextDescription.text.isEmpty() ||
            editTextPrice.text.isEmpty() ||
            editTextContactInfo.text.isEmpty() ||
            imageUri == null
        ) {
            Toast.makeText(this, "Completează toate câmpurile și adaugă o imagine!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun uploadImageAndSaveListing() {
        showLoadingDialog()

        val fileName = UUID.randomUUID().toString()
        val ref = storage.reference.child("listings/$fileName")

        imageUri?.let { uri ->
            ref.putFile(uri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { downloadUrl ->
                        saveListing(downloadUrl.toString())
                    }
                }
                .addOnFailureListener {
                    hideLoadingDialog()
                    Toast.makeText(this, "Eroare la încărcarea imaginii.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveListing(imageUrl: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        val selectedAssociationPosition = spinnerAssociation.selectedItemPosition
        val selectedAssociationId = associationList[selectedAssociationPosition].id

        val contactInfo = editTextContactInfo.text.toString().trim()

        firestore.collection("user-type").document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                val username = document.getString("username") ?: "User necunoscut"

                val listing = Listing(
                    id = UUID.randomUUID().toString(),
                    associationId = selectedAssociationId,
                    ownerId = currentUserId,
                    ownerUsername = username,
                    contactInfo = contactInfo,
                    title = editTextTitle.text.toString(),
                    description = editTextDescription.text.toString(),
                    price = editTextPrice.text.toString().toDouble(),
                    type = spinnerType.selectedItem as ListingType,
                    imageUrl = imageUrl,
                    createdAt = System.currentTimeMillis()
                )

                firestore.collection("listings")
                    .document(listing.id)
                    .set(listing)
                    .addOnSuccessListener {
                        hideLoadingDialog()
                        Toast.makeText(this, "Anunț publicat cu succes!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        hideLoadingDialog()
                        Toast.makeText(this, "Eroare la salvare!", Toast.LENGTH_SHORT).show()
                    }
            }
    }

    private fun showLoadingDialog() {
        loadingDialog = MaterialDialog(this).show {
            title(text = "Se încarcă...")
            message(text = "Vă rugăm așteptați câteva momente.")
            cancelable(false)
        }
    }

    private fun hideLoadingDialog() {
        loadingDialog?.dismiss()
    }
}
