package com.example.healthapp

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

class Profile : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvJoinDate: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        sharedPreferences = getSharedPreferences("user_profile", MODE_PRIVATE)

        // Initialize views
        tvUserName = findViewById(R.id.tv_user_name)
        tvUserEmail = findViewById(R.id.tv_user_email)
        tvJoinDate = findViewById(R.id.tv_join_date)

        // Load and display profile data
        loadProfileData()

        setupBottomNavigation()

        // Set up edit profile button
        val btnEditProfile = findViewById<Button>(R.id.btn_edit_profile)
        btnEditProfile.setOnClickListener {
            val intent = Intent(this, CreateProfile::class.java)
            startActivity(intent)
        }
    }

    private fun loadProfileData() {
        val userName = sharedPreferences.getString("user_name", "Sarah Johnson") ?: "Sarah Johnson"
        val userEmail = sharedPreferences.getString("user_email", "sarah.johnson@email.com") ?: "sarah.johnson@email.com"

        tvUserName.text = userName
        tvUserEmail.text = userEmail

        // Set join date to current date if not set
        if (!sharedPreferences.contains("join_date")) {
            val currentDate = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
            val joinDate = "Member since $currentDate"
            tvJoinDate.text = joinDate

            // Save join date
            sharedPreferences.edit().putString("join_date", joinDate).apply()
        } else {
            tvJoinDate.text = sharedPreferences.getString("join_date", "Member since January 2024")
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning from CreateProfile activity
        loadProfileData()
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    // Already on home, do nothing or refresh
                    true
                }
                R.id.nav_habits -> {
                    val intent = Intent(this, Habits::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_mood -> {
                    val intent = Intent(this, Mood::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_hydration -> {
                    val intent = Intent(this, Hydration::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_home -> {
                    val intent = Intent(this, Home::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
}