package com.example.healthapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HydrationActivity : AppCompatActivity() {
    private var currentIntake = 1500
    private val dailyGoal = 2500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hydration)

        setupToolbar()
        updateWaterDisplay()
        setupButtons()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun updateWaterDisplay() {
        findViewById<TextView>(R.id.tv_current_amount).text = "${currentIntake}ml"
        findViewById<TextView>(R.id.tv_goal).text = "of ${dailyGoal}ml"

        val progress = (currentIntake * 100 / dailyGoal).coerceAtMost(100)
        findViewById<ProgressBar>(R.id.progress_bar).progress = progress
        findViewById<TextView>(R.id.tv_progress).text = "$progress% Complete"
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btn_add_250).setOnClickListener { addWater(250) }
        findViewById<Button>(R.id.btn_add_500).setOnClickListener { addWater(500) }
        findViewById<Button>(R.id.btn_add_750).setOnClickListener { addWater(750) }
        findViewById<Button>(R.id.btn_add_glass).setOnClickListener { addWater(250) }
    }

    private fun addWater(amount: Int) {
        currentIntake = (currentIntake + amount).coerceAtMost(dailyGoal)
        updateWaterDisplay()

        // Save to SharedPreferences
        val prefs = getSharedPreferences("hydration", MODE_PRIVATE)
        prefs.edit().putInt("current_intake", currentIntake).apply()
    }
}