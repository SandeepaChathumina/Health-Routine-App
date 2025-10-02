package com.example.healthapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import java.text.DecimalFormat

class Stats : AppCompatActivity() {
    
    private lateinit var statsDataManager: StatsDataManager
    private var currentPeriodDays = 7 // Default to 7 days
    
    // UI Components
    private lateinit var tvHealthScore: TextView
    private lateinit var tvCurrentStreak: TextView
    private lateinit var tvBestStreak: TextView
    private lateinit var tvTotalDays: TextView
    private lateinit var btnWeek: MaterialButton
    private lateinit var btnMonth: MaterialButton
    private lateinit var btnYear: MaterialButton
    
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_stats)

        statsDataManager = StatsDataManager(this)
        
        initializeViews()
        setupClickListeners()
        loadStatsData()
    }
    
    private fun initializeViews() {
        tvHealthScore = findViewById(R.id.tv_health_score)
        tvCurrentStreak = findViewById(R.id.tv_current_streak)
        tvBestStreak = findViewById(R.id.tv_best_streak)
        tvTotalDays = findViewById(R.id.tv_total_days)
        btnWeek = findViewById(R.id.btn_week)
        btnMonth = findViewById(R.id.btn_month)
        btnYear = findViewById(R.id.btn_year)
    }
    
    private fun setupClickListeners() {
        val backButton: ImageButton = findViewById(R.id.btn_back)
        backButton.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }
        
        // Time period buttons
        btnWeek.setOnClickListener { 
            updateTimePeriod(7, btnWeek)
        }
        
        btnMonth.setOnClickListener { 
            updateTimePeriod(30, btnMonth)
        }
        
        btnYear.setOnClickListener { 
            updateTimePeriod(365, btnYear)
        }
    }
    
    private fun updateTimePeriod(days: Int, selectedButton: MaterialButton) {
        currentPeriodDays = days
        
        // Update button states
        resetButtonStates()
        selectedButton.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.holo_blue_bright)
        selectedButton.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        
        // Reload data for new period
        loadStatsData()
    }
    
    private fun resetButtonStates() {
        val buttons = listOf(btnWeek, btnMonth, btnYear)
        buttons.forEach { button ->
            button.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.white)
            button.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        }
    }
    
    private fun loadStatsData() {
        val stats = statsDataManager.getStatsForPeriod(currentPeriodDays)
        updateUI(stats)
    }
    
    private fun updateUI(stats: StatsDataManager.StatsData) {
        val decimalFormat = DecimalFormat("#.#")
        
        // Update main stats
        tvHealthScore.text = stats.healthScore.toString()
        tvCurrentStreak.text = stats.currentStreak.toString()
        tvBestStreak.text = stats.bestStreak.toString()
        tvTotalDays.text = stats.totalDaysActive.toString()
        
        // Update progress scores in mini progress bars
        findViewById<TextView>(R.id.tv_habits_score)?.text = stats.habitsScore.toString()
        findViewById<TextView>(R.id.tv_water_score)?.text = stats.waterScore.toString()
        findViewById<TextView>(R.id.tv_mood_score)?.text = stats.moodScore.toString()
        findViewById<TextView>(R.id.tv_exercise_score)?.text = stats.exerciseScore.toString()
        
        // Update detailed habit stats
        findViewById<TextView>(R.id.tv_avg_habits_per_day)?.text = decimalFormat.format(stats.avgHabitsPerDay)
        findViewById<TextView>(R.id.tv_total_habits_count)?.text = stats.totalHabits.toString()
        findViewById<TextView>(R.id.tv_habit_streak)?.text = stats.habitStreak.toString()
        
        // Update hydration stats
        findViewById<TextView>(R.id.tv_daily_water_avg)?.text = "${decimalFormat.format(stats.dailyWaterAvg)}L"
        findViewById<TextView>(R.id.tv_glasses_per_day)?.text = decimalFormat.format(stats.glassesPerDay)
        findViewById<TextView>(R.id.tv_hydration_days_on_track)?.text = stats.daysOnTrack.toString()
        
        // Update mood stats
        findViewById<TextView>(R.id.tv_mood_entries_week)?.text = stats.moodEntriesWeek.toString()
        findViewById<TextView>(R.id.tv_most_common_mood_emoji)?.text = stats.mostCommonMood
        findViewById<TextView>(R.id.tv_mood_improvement)?.text = stats.moodImprovement
        
        // Update health score color based on value
        updateHealthScoreColor(stats.healthScore)
    }
    
    private fun updateHealthScoreColor(score: Int) {
        val color = when {
            score >= 80 -> ContextCompat.getColor(this, android.R.color.holo_green_dark)
            score >= 60 -> ContextCompat.getColor(this, android.R.color.holo_orange_dark)
            else -> ContextCompat.getColor(this, android.R.color.holo_red_dark)
        }
        tvHealthScore.setTextColor(color)
    }
}