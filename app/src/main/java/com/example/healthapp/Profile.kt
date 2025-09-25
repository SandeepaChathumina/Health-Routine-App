package com.example.healthapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class Profile : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        setupBottomNavigation()
        setupProfileInteractions()
        loadUserData()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_profile

        bottomNav.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, Home::class.java))
                    finish()
                    true
                }
                R.id.nav_habits -> {
                    startActivity(Intent(this, Habits::class.java))
                    finish()
                    true
                }
                R.id.nav_mood -> {
                    startActivity(Intent(this, Mood::class.java))
                    finish()
                    true
                }
                R.id.nav_hydration -> {
                    startActivity(Intent(this, Hydration::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }

    private fun setupProfileInteractions() {
        findViewById<Button>(R.id.btn_edit_profile).setOnClickListener {
            startActivity(Intent(this, Account::class.java))
        }

        findViewById<CardView>(R.id.card_account_settings).setOnClickListener {
            startActivity(Intent(this, Account::class.java))
        }

        findViewById<Button>(R.id.btn_sign_out).setOnClickListener {
            showSignOutConfirmation()
        }
    }

    private fun loadUserData() {
        val prefs = getSharedPreferences("user_data", MODE_PRIVATE)

        findViewById<TextView>(R.id.tv_user_name).text = prefs.getString("user_name", "Sarah Johnson")
        findViewById<TextView>(R.id.tv_user_email).text = prefs.getString("user_email", "sarah.johnson@email.com")
        findViewById<TextView>(R.id.tv_join_date).text = "Member since ${prefs.getString("join_date", "January 2024")}"

        findViewById<TextView>(R.id.tv_total_habits).text = prefs.getInt("total_habits", 12).toString()
        findViewById<TextView>(R.id.tv_current_streak).text = "${prefs.getInt("current_streak", 15)} days"
        findViewById<TextView>(R.id.tv_success_rate).text = "${prefs.getInt("success_rate", 83)}%"
    }

    private fun showSignOutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setPositiveButton("Sign Out") { _, _ ->
                // Clear user data and go to login/splash
                val prefs = getSharedPreferences("user_data", MODE_PRIVATE)
                prefs.edit().clear().apply()

                startActivity(Intent(this, Splash_Screen::class.java))
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}