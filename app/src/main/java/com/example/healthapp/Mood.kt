package com.example.healthapp

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView

class Mood : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood)

        setupBottomNavigation()
        setupMoodInteractions()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_mood

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
                R.id.nav_mood -> true
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

    private fun setupMoodInteractions() {
        findViewById<ImageButton>(R.id.btn_add_mood).setOnClickListener {
            showAddMoodDialog()
        }

        // View Insights click
        findViewById<TextView>(R.id.tv_view_insights).setOnClickListener {
            // Open mood insights activity
        }

        // Quick actions
        findViewById<CardView>(R.id.card_mood_patterns).setOnClickListener {
            // Open mood patterns activity
        }

        findViewById<CardView>(R.id.card_set_reminders).setOnClickListener {
            // Open mood reminders activity
        }
    }

    private fun showAddMoodDialog() {
        val moods = listOf(
            MoodEntry("😊", "Happy", "#10B981"),
            MoodEntry("😌", "Calm", "#8B5CF6"),
            MoodEntry("😤", "Stressed", "#EF4444"),
            MoodEntry("🥳", "Excited", "#F59E0B"),
            MoodEntry("😴", "Tired", "#6B7280")
        )

        val builder = AlertDialog.Builder(this)
        builder.setTitle("How are you feeling?")

        val moodView = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        moods.forEach { mood ->
            val moodButton = Button(this).apply {
                text = mood.emoji
                textSize = 20f
                setBackgroundColor(Color.TRANSPARENT)
                setOnClickListener {
                    // Save mood entry
                    saveMoodEntry(mood)
                    // Dismiss dialog
                }
            }
            moodView.addView(moodButton)
        }

        builder.setView(moodView)
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun saveMoodEntry(mood: MoodEntry) {
        // Save mood to SharedPreferences
        val prefs = getSharedPreferences("mood_data", MODE_PRIVATE)
        val editor = prefs.edit()

        // Increment mood count
        val currentCount = prefs.getInt(mood.label, 0)
        editor.putInt(mood.label, currentCount + 1)

        // Save current entry
        editor.putString("last_mood", mood.emoji)
        editor.putLong("last_mood_time", System.currentTimeMillis())
        editor.apply()

        Toast.makeText(this, "Mood recorded: ${mood.label}", Toast.LENGTH_SHORT).show()
    }

    data class MoodEntry(val emoji: String, val label: String, val color: String)
}