package com.example.healthapp

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class Hydration : AppCompatActivity() {
    private var currentIntake = 1500
    private val dailyGoal = 2500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hydration)

        setupBottomNavigation()

    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Already on home, do nothing or refresh
                    true
                }

                R.id.nav_habits -> {
                    val intent = Intent(this, Habits::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_mood -> {
                    val intent = Intent(this, Mood::class.java) // Replace with your Mood activity
                    startActivity(intent)
                    true
                }

                R.id.nav_hydration -> {
                    val intent =
                        Intent(this, Hydration::class.java) // Replace with your Hydration activity
                    startActivity(intent)
                    true
                }

                R.id.nav_profile -> {
                    val intent =
                        Intent(this, Profile::class.java) // Replace with your Profile activity
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }
    }
}