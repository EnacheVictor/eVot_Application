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
import com.google.firebase.ktx.Firebase
import com.victor.evotapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // Entry activity of the app; handles login and navigation to sign-up

    private lateinit var auth: FirebaseAuth
    lateinit var  binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        auth = Firebase.auth

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.createAccBTN.setOnClickListener {
            var intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
        binding.emailSignInButton.setOnClickListener {
            signInWithEmailAndPassword(
                binding.email.text.toString().trim(),
                binding.password.text.toString().trim()
            )
        }
        }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
           goToLogInActivity() // te pastreaza conectat
        }
    }

    fun goToLogInActivity() {
        var intent = Intent(this, LogInActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Handles email/password authentication using Firebase

    fun signInWithEmailAndPassword(email: String , password:String)
    {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("TAGY", "signInWithEmail:success")
                    Toast.makeText(
                        baseContext,
                        "Authentication success.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    goToLogInActivity()
                } else {
                    Log.w("TAGY", "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }
}

