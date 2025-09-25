package com.example.healthapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class Mood : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood)

        setupToolbar()
        setupMoodGrid()
        setupRecentEntries()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        findViewById<FloatingActionButton>(R.id.fab_add).setOnClickListener {
            // Open add mood activity
        }
    }

    private fun setupMoodGrid() {
        val moods = listOf(
            Mood("😊", "Happy", 15, "#10B981"),
            Mood("😴", "Tired", 8, "#6B7280"),
            Mood("😌", "Calm", 12, "#8B5CF6"),
            Mood("😤", "Stressed", 5, "#EF4444"),
            Mood("🥳", "Excited", 7, "#F59E0B")
        )

        val gridLayout = findViewById<GridLayout>(R.id.grid_moods)
        moods.forEach { mood ->
            val view = LayoutInflater.from(this).inflate(R.layout.item_mood, gridLayout, false)
            view.findViewById<TextView>(R.id.tv_emoji).text = mood.emoji
            view.findViewById<TextView>(R.id.tv_label).text = mood.label
            view.findViewById<TextView>(R.id.tv_count).text = "${mood.count} times"

            val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
            progressBar.progress = (mood.count * 100 / 20) // Assuming 20 is max

            gridLayout.addView(view)
        }
    }

    private fun setupRecentEntries() {
        val entries = listOf(
            MoodEntry("Today, 2:30 PM", "😊", "Happy", "Had a great workout this morning!"),
            MoodEntry("Yesterday, 8:45 PM", "😌", "Calm", "Finished all my tasks for the day"),
            MoodEntry("Nov 15, 11:20 AM", "😤", "Stressed", "Lots of deadlines coming up")
        )

        val container = findViewById<LinearLayout>(R.id.container_recent_entries)
        entries.forEach { entry ->
            val view = LayoutInflater.from(this).inflate(R.layout.item_mood_entry, container, false)
            view.findViewById<TextView>(R.id.tv_date).text = entry.date
            view.findViewById<TextView>(R.id.tv_emoji).text = entry.emoji
            view.findViewById<TextView>(R.id.tv_mood).text = entry.mood
            view.findViewById<TextView>(R.id.tv_note).text = entry.note

            container.addView(view)
        }
    }

    data class Mood(val emoji: String, val label: String, val count: Int, val color: String)
    data class MoodEntry(val date: String, val emoji: String, val mood: String, val note: String)
}