package com.example.healthapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupBottomNavigation()
        setupQuickActions()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_home -> true
                R.id.nav_habits -> {
                    startActivity(Intent(this, Habits::class.java))
                    true
                }
                R.id.nav_mood -> {
                    startActivity(Intent(this, MoodActivity::class.java))
                    true
                }
                R.id.nav_hydration -> {
                    startActivity(Intent(this, HydrationActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupQuickActions() {
        findViewById<Button>(R.id.btn_add_habit).setOnClickListener {
            // Open add habit activity
        }
        findViewById<Button>(R.id.btn_log_mood).setOnClickListener {
            startActivity(Intent(this, MoodActivity::class.java))
        }
        findViewById<Button>(R.id.btn_drink_water).setOnClickListener {
            startActivity(Intent(this, HydrationActivity::class.java))
        }
    }
}