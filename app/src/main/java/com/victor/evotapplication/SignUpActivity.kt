package com.victor.evotapplication

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
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

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener (this){
                task -> if(task.isSuccessful){
                    Log.d("TAGY", "createUserWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                    Toast.makeText(baseContext, "Authentication success.",Toast.LENGTH_SHORT).show()
            }else
            {
                Log.w("TAGY", "createUserWithEmail:failure",task.exception)
                Toast.makeText(baseContext, "Authentication failed.",Toast.LENGTH_SHORT).show()
                updateUI(null)
            }
            }
    }

    public override fun onStart(){
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser != null){
            reload();
        }
    }

    public fun reload(){

    }

    public fun updateUI(user: FirebaseUser?){

    }

    }


