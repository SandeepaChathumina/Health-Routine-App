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
import com.google.android.material.card.MaterialCardView

class Home : AppCompatActivity() {
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        setupBottomNavigation()
        setupQuickActions()

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

    private fun setupQuickActions() {
        // BMI Calculator
        val bmiCard: MaterialCardView = findViewById(R.id.card_bmi_calculator)
        bmiCard.setOnClickListener {
            val intent = Intent(this, BmiCalculatorActivity::class.java)
            startActivity(intent)
        }

        // Daily Workout
        val workoutCard: MaterialCardView = findViewById(R.id.card_daily_workout)
        workoutCard.setOnClickListener {
            val intent = Intent(this, DailyWorkoutActivity::class.java)
            startActivity(intent)
        }

        // Add Habit
        val addHabitCard: MaterialCardView = findViewById(R.id.card_add_habit)
        addHabitCard.setOnClickListener {
            val intent = Intent(this, Habits::class.java) // Replace with your AddHabit activity
            startActivity(intent)
        }

        // Log Mood
        val logMoodCard: MaterialCardView = findViewById(R.id.card_log_mood)
        logMoodCard.setOnClickListener {
            val intent = Intent(this, Mood::class.java) // Replace with your LogMood activity
            startActivity(intent)
        }

        // Drink Water
        val drinkWaterCard: MaterialCardView = findViewById(R.id.card_drink_water)
        drinkWaterCard.setOnClickListener {
            val intent = Intent(this, Hydration::class.java) // Replace with your DrinkWater activity
            startActivity(intent)
        }

        // View Stats
        val viewStatsCard: MaterialCardView = findViewById(R.id.card_view_stats)
        viewStatsCard.setOnClickListener {
            val intent = Intent(this, Stats::class.java) // Replace with your ViewStats activity
            startActivity(intent)
        }
    }
}
