package com.example.healthapp

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import java.text.DecimalFormat

class Home : AppCompatActivity() {
    
    private lateinit var homeDataManager: HomeDataManager
    private lateinit var profilePrefs: SharedPreferences
    
    // UI Components
    private lateinit var tvGreeting: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var tvProgressPercent: TextView
    private lateinit var tvHabitsCount: TextView
    private lateinit var tvStreakDays: TextView
    private lateinit var tvWaterIntake: TextView
    
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        homeDataManager = HomeDataManager(this)
        profilePrefs = getSharedPreferences("user_profile", MODE_PRIVATE)
        
        initializeViews()
        setupBottomNavigation()
        setupQuickActions()
        loadTodayProgress()
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh data when returning to home page
        loadTodayProgress()
    }
    
    private fun initializeViews() {
        tvGreeting = findViewById(R.id.tv_greeting)
        tvSubtitle = findViewById(R.id.tv_subtitle)
        tvProgressPercent = findViewById(R.id.tv_progress_percent)
        tvHabitsCount = findViewById(R.id.tv_habits_count)
        tvStreakDays = findViewById(R.id.tv_streak_days)
        tvWaterIntake = findViewById(R.id.tv_water_intake)
        
        // Setup "View All" click handler
        findViewById<TextView>(R.id.tv_view_all).setOnClickListener {
            val intent = Intent(this, Stats::class.java)
            startActivity(intent)
        }
    }
    
    private fun loadTodayProgress() {
        val progressData = homeDataManager.getTodayProgress()
        updateUI(progressData)
    }
    
    private fun updateUI(progressData: HomeDataManager.TodayProgressData) {
        val decimalFormat = DecimalFormat("#.#")
        
        // Update greeting with user's name
        val userName = profilePrefs.getString("user_name", "Friend") ?: "Friend"
        val greeting = homeDataManager.getGreetingMessage()
        tvGreeting.text = "$greeting, $userName!"
        
        // Update motivational subtitle
        val motivationalMessage = homeDataManager.getMotivationalMessage(progressData)
        tvSubtitle.text = motivationalMessage
        
        // Update overall progress
        tvProgressPercent.text = "${progressData.overallProgress}%"
        updateProgressColor(progressData.overallProgress)
        
        // Update habits count
        tvHabitsCount.text = "${progressData.habitsCompleted}/${progressData.totalHabits}"
        
        // Update streak
        val streakText = if (progressData.currentStreak == 1) {
            "${progressData.currentStreak} day"
        } else {
            "${progressData.currentStreak} days"
        }
        tvStreakDays.text = streakText
        
        // Update water intake
        val waterInLiters = progressData.waterIntake / 1000f
        tvWaterIntake.text = "${decimalFormat.format(waterInLiters)}L"
        
        // Update water intake color based on progress
        val waterProgress = (progressData.waterIntake.toFloat() / progressData.waterGoal * 100).toInt()
        updateWaterIntakeColor(waterProgress)
    }
    
    private fun updateProgressColor(progress: Int) {
        val color = when {
            progress >= 80 -> ContextCompat.getColor(this, android.R.color.holo_green_dark)
            progress >= 60 -> ContextCompat.getColor(this, android.R.color.holo_blue_dark)
            progress >= 40 -> ContextCompat.getColor(this, android.R.color.holo_orange_dark)
            else -> ContextCompat.getColor(this, android.R.color.holo_red_dark)
        }
        tvProgressPercent.setTextColor(color)
    }
    
    private fun updateWaterIntakeColor(waterProgress: Int) {
        val color = when {
            waterProgress >= 100 -> ContextCompat.getColor(this, android.R.color.holo_green_dark)
            waterProgress >= 75 -> ContextCompat.getColor(this, android.R.color.holo_blue_dark)
            waterProgress >= 50 -> ContextCompat.getColor(this, android.R.color.holo_orange_dark)
            else -> ContextCompat.getColor(this, android.R.color.holo_red_dark)
        }
        tvWaterIntake.setTextColor(color)
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
