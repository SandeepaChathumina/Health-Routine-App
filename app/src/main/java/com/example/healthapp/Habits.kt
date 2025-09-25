package com.example.healthapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class Habits : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_habits)

        setupBottomNavigation()
        setupCategoryFilters()
        setupHabitInteractions()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_habits

        bottomNav.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, Home::class.java))
                    finish()
                    true
                }
                R.id.nav_habits -> true
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
                R.id.nav_profile -> {
                    startActivity(Intent(this, Profile::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupCategoryFilters() {
        val categories = listOf(
            findViewById<Button>(R.id.btn_all),
            findViewById<Button>(R.id.btn_health),
            findViewById<Button>(R.id.btn_fitness),
            findViewById<Button>(R.id.btn_mindfulness)
        )

        categories.forEach { button ->
            button.setOnClickListener {
                // Update active state
                categories.forEach { it.setTextColor(Color.parseColor("#64748B")) }
                categories.forEach { it.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded_white) }

                button.setTextColor(Color.WHITE)
                button.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded_primary)

                // Filter habits based on category
                filterHabits(button.text.toString())
            }
        }
    }

    private fun setupHabitInteractions() {
        findViewById<ImageButton>(R.id.btn_add_habit).setOnClickListener {
            // Open add habit dialog/activity
        }

        findViewById<CheckBox>(R.id.cb_water).setOnCheckedChangeListener { _, isChecked ->
            updateHabitProgress("water", isChecked)
        }

        findViewById<CheckBox>(R.id.cb_meditation).setOnCheckedChangeListener { _, isChecked ->
            updateHabitProgress("meditation", isChecked)
        }
    }

    private fun filterHabits(category: String) {
        // Implement habit filtering logic
    }

    private fun updateHabitProgress(habitId: String, completed: Boolean) {
        // Update progress bar and save to SharedPreferences
    }
}