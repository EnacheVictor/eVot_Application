package com.victor.evotapplication

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.victor.evotapplication.fragments.AboutUsFragment
import com.victor.evotapplication.fragments.AssociationsFragment
import com.victor.evotapplication.fragments.HomeFragment
import com.victor.evotapplication.fragments.InvoiceFragment
import com.victor.evotapplication.fragments.LogoutFragment
import com.victor.evotapplication.fragments.SettingsFragment
import com.victor.evotapplication.fragments.ShareFragment

// Main activity that handles navigation drawer and fragment switching

class LogInActivity : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.open,
            R.string.close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment()).commit()
            navigationView.setCheckedItem(R.id.nav_home)
        }
        updateNavHeader()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, HomeFragment()).commit()
            R.id.nav_settings -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, SettingsFragment()).commit()
            R.id.nav_share -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, ShareFragment()).commit()
            R.id.nav_about -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, AboutUsFragment()).commit()
            R.id.nav_logout -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, LogoutFragment()).commit()
            R.id.nav_associations -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, AssociationsFragment()).commit()
            R.id.invoice -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, InvoiceFragment()).commit()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    // Updates the Navigation Drawer header with user data from Firestore

    fun updateNavHeader() {
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val headerView = navView.getHeaderView(0)
        val navUsername = headerView.findViewById<TextView>(R.id.username)
        val navEmail = headerView.findViewById<TextView>(R.id.email)
        val navRole = headerView.findViewById<TextView>(R.id.role)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val uid = user.uid

            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("user-type").document(uid)

            // Get user data from Firestore and set text fields

            userRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val username = document.getString("username") ?: "Unknown"
                    val email = document.getString("email") ?: "No Email"
                    val role = document.getString("role") ?: "Unknown"

                    navUsername.text = username
                    navEmail.text = email
                    navRole.text = role
                }
            }.addOnFailureListener {
                Log.e("Firestore", "Error getting user data")
            }
        }
    }

}
