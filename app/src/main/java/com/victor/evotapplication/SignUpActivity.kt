package com.victor.evotapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.victor.evotapplication.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    lateinit var binding: ActivitySignUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)

        auth = Firebase.auth

        binding.newAccountBtn.setOnClickListener {
            createUser()
        }
    }

    private fun createUser() {
        val email = binding.createAccEmail.text.toString()
        val password = binding.createPassword.text.toString()
        val username = binding.username.text.toString()
        val role = binding.roleSpinner.selectedItem.toString()

        if (role == "Selectează un rol") {
            Toast.makeText(this, "Te rog să selectezi un rol!", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val uid = user.uid  // Obține UID-ul utilizatorului
                        saveUserToFirestore(uid, username, email,role) // Salvează în Firestore
                    } else {
                        Log.e("Auth", "User is null after sign-up")
                    }
                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                    goToLogInActivity()
                } else {
                    Toast.makeText(
                        this,
                        "Sign-up failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun saveUserToFirestore(uid: String, username: String, email: String, role: String) {
        val db = Firebase.firestore
        val user = hashMapOf(
            "username" to username,
            "email" to email,
            "role" to role
        )

        db.collection("user-type").document(uid)
            .set(user)
            .addOnSuccessListener {
                Log.d("Firestore", "User added successfully")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error adding user", e)
            }
    }

    fun goToLogInActivity() {
        var intent = Intent(this, LogInActivity::class.java)
        startActivity(intent)
        finish()
    }
}


