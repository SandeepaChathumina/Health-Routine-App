package com.example.healthapp

import android.annotation.SuppressLint
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView

class Home : AppCompatActivity() {
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        //quick actions

        val workoutButton: Button = findViewById(R.id.card_bmi_calculator)
        workoutButton.setOnClickListener {
            // ✅ Navigate to OnBoarding3 instead of OnBoarding2
            val intent = Intent(this, BmiCalculatorActivity::class.java)
            startActivity(intent)
        }

        //next button

        val nextButton: Button = findViewById(R.id.card_daily_workout)
        nextButton.setOnClickListener {
            // ✅ Navigate to OnBoarding3 instead of OnBoarding2
            val intent = Intent(this, DailyWorkoutActivity::class.java)
            startActivity(intent)
        }

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
                    val intent = Intent(this, Hydration::class.java) // Replace with your Hydration activity
                    startActivity(intent)
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, Profile::class.java) // Replace with your Profile activity
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
}
