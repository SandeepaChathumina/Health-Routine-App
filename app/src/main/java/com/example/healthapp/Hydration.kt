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
        loadHydrationData()
        setupHydrationButtons()
        updateWaterDisplay()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_hydration

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
                R.id.nav_hydration -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, Profile::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun loadHydrationData() {
        val prefs = getSharedPreferences("hydration_data", MODE_PRIVATE)
        currentIntake = prefs.getInt("current_intake", 1500)
    }

    private fun setupHydrationButtons() {
        // Quick add buttons
        findViewById<Button>(R.id.btn_add_250).setOnClickListener { addWater(250) }
        findViewById<Button>(R.id.btn_add_500).setOnClickListener { addWater(500) }
        findViewById<Button>(R.id.btn_add_750).setOnClickListener { addWater(750) }

        // Manual controls
        findViewById<ImageButton>(R.id.btn_subtract).setOnClickListener { subtractWater(250) }
        findViewById<Button>(R.id.btn_add_glass).setOnClickListener { addWater(250) }
        findViewById<ImageButton>(R.id.btn_add).setOnClickListener { addWater(250) }

        // Settings
        findViewById<ImageButton>(R.id.btn_settings).setOnClickListener {
            // Open hydration settings
        }
    }

    private fun addWater(amount: Int) {
        currentIntake = (currentIntake + amount).coerceAtMost(dailyGoal)
        saveHydrationData()
        updateWaterDisplay()
        animateWaterLevel()
    }

    private fun subtractWater(amount: Int) {
        currentIntake = (currentIntake - amount).coerceAtLeast(0)
        saveHydrationData()
        updateWaterDisplay()
        animateWaterLevel()
    }

    private fun saveHydrationData() {
        val prefs = getSharedPreferences("hydration_data", MODE_PRIVATE)
        prefs.edit().putInt("current_intake", currentIntake).apply()
    }

    private fun updateWaterDisplay() {
        val progressPercentage = (currentIntake * 100 / dailyGoal).coerceAtMost(100)
        val cupsCompleted = currentIntake / 250
        val totalCups = dailyGoal / 250

        findViewById<TextView>(R.id.tv_current_amount).text = "${currentIntake}ml"
        findViewById<TextView>(R.id.tv_goal_amount).text = "of ${dailyGoal}ml"
        findViewById<TextView>(R.id.tv_progress_percent).text = "$progressPercentage% Complete"

        findViewById<TextView>(R.id.tv_cups_today).text = "$cupsCompleted/$totalCups"
        findViewById<TextView>(R.id.tv_progress).text = "$progressPercentage%"
    }

    private fun animateWaterLevel() {
        val waterLevel = findViewById<View>(R.id.water_level)
        val progressPercentage = (currentIntake * 100 / dailyGoal).coerceAtMost(100)
        val targetHeight = (150 * progressPercentage / 100).toFloat()

        val animator = ValueAnimator.ofInt(waterLevel.layoutParams.height, targetHeight.toInt())
        animator.addUpdateListener { valueAnimator ->
            val height = valueAnimator.animatedValue as Int
            waterLevel.layoutParams.height = height
            waterLevel.requestLayout()
        }
        animator.duration = 500
        animator.start()
    }
}