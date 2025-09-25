package com.example.healthapp

import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView

class Home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        setupBottomNavigation()
        setupQuickActions()
        updateGreeting()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_home // Highlight home icon

        bottomNav.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_home -> {
                    // Already on home, do nothing
                    true
                }
                R.id.nav_habits -> {
                    startActivity(Intent(this, Habits::class.java))
                    true
                }
                R.id.nav_mood -> {
                    startActivity(Intent(this, Mood::class.java))
                    true
                }
                R.id.nav_hydration -> {
                    startActivity(Intent(this, Hydration::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, Profile::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupQuickActions() {
        findViewById<CardView>(R.id.card_add_habit).setOnClickListener {
            // Open add habit activity
        }

        findViewById<CardView>(R.id.card_log_mood).setOnClickListener {
            startActivity(Intent(this, Mood::class.java))
        }

        findViewById<CardView>(R.id.card_drink_water).setOnClickListener {
            startActivity(Intent(this, Hydration::class.java))
        }

        findViewById<CardView>(R.id.card_view_stats).setOnClickListener {
            // Open stats activity
        }
    }

    private fun updateGreeting() {
        val greetingText = findViewById<TextView>(R.id.tv_greeting)
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val greeting = when {
            hour < 12 -> "Good morning!"
            hour < 17 -> "Good afternoon!"
            else -> "Good evening!"
        }

        greetingText.text = greeting
    }
}
