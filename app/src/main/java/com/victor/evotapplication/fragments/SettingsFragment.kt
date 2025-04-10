package com.victor.evotapplication.fragments

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.victor.evotapplication.R
import com.victor.evotapplication.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var imageUri: Uri? = null

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imageUri = it
                binding.profileImageView.setImageURI(it)
                uploadProfileImage(it)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        loadUserInfo()

        binding.changeImageBtn.setOnClickListener {
            imagePicker.launch("image/*")
        }

        binding.saveUsernameBtn.setOnClickListener {
            val newUsername = binding.usernameInput.text.toString().trim()
            if (newUsername.isNotEmpty()) {
                saveUsername(newUsername)
            }
        }

        binding.changePasswordBtn.setOnClickListener {
            val email = auth.currentUser?.email
            email?.let {
                auth.sendPasswordResetEmail(it)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Password reset email sent!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Error sending email", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        binding.deleteAccountBtn.setOnClickListener {
            confirmDelete()
        }

        return binding.root
    }

    private fun loadUserInfo() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("user-type").document(uid).get()
            .addOnSuccessListener { doc ->
                val username = doc.getString("username") ?: ""
                binding.usernameInput.setText(username)

                val imageUrl = doc.getString("profileImageUrl")
                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(requireContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_user_placeholder)
                        .circleCrop()
                        .into(binding.profileImageView)
                }
            }
    }

    private fun uploadProfileImage(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        val ref = storage.reference.child("profile_images/$uid.jpg")

        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUrl ->
                    db.collection("user-type").document(uid)
                        .update("profileImageUrl", downloadUrl.toString())
                    Toast.makeText(requireContext(), "✅ Image updated , please restart the app to see the changes", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "❌ Failed to upload image", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUsername(newUsername: String) {
        val uid = auth.currentUser?.uid ?: return

        db.collection("user-type").document(uid)
            .update("username", newUsername)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "✅ Username updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "❌ Error updating username", Toast.LENGTH_SHORT).show()
            }
    }

    private fun confirmDelete() {
        AlertDialog.Builder(requireContext())
            .setTitle("Are you sure?")
            .setMessage("This will permanently delete your account.")
            .setPositiveButton("Delete") { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteAccount() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("user-type").document(uid)
            .delete()
            .addOnSuccessListener {
                auth.currentUser?.delete()
                    ?.addOnSuccessListener {
                        Toast.makeText(requireContext(), "✅ Account deleted", Toast.LENGTH_SHORT).show()
                        requireActivity().finish()
                    }
                    ?.addOnFailureListener {
                        Toast.makeText(requireContext(), "❌ Error deleting account", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "❌ Failed to delete user data", Toast.LENGTH_SHORT).show()
            }
    }
}
