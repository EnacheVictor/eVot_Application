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
            goToLogInActivity()
        }
    }

    private fun createUser() {
        val email = binding.createAccEmail.text.toString()
        val password = binding.createPassword.text.toString()
        val username = binding.createUsername.text.toString()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("TAGY", "createUserWithEmail:success")
                    val user = auth.currentUser
                    Toast.makeText(baseContext, "Authentication success.", Toast.LENGTH_SHORT)
                        .show()
                    user?.let {
                        saveUserToFirestore(it.uid, username, email)
                    }
                } else {
                    Log.w("TAGY", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserToFirestore(uid: String, username: String, email: String) {
        val db = Firebase.firestore
        val user = hashMapOf(
            "username" to username,
            "email" to email
        )

        db.collection("users").document(uid)
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


