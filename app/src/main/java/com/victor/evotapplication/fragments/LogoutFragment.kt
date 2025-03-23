package com.victor.evotapplication.fragments
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.victor.evotapplication.MainActivity

class LogoutFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Deloghează utilizatorul imediat când fragmentul este deschis
        FirebaseAuth.getInstance().signOut()

        // Navighează înapoi la ecranul de autentificare
        val intent = Intent(activity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        return null // Nu avem nevoie de un layout, fragmentul doar deloghează și schimbă ecranul
    }
}