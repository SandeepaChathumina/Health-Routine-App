package com.example.healthapp

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class Mood : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var recyclerView: RecyclerView
    private lateinit var moodAdapter: MoodAdapter
    private lateinit var emptyState: LinearLayout
    private val gson = Gson()
    private val MOOD_ENTRIES_KEY = "mood_entries"
    private val moodEntries = mutableListOf<MoodEntry>()

    // Mood types with emojis and colors
    private val moodTypes = listOf(
        MoodType("Excited", "🥳", "#F59E0B"),
        MoodType("Happy", "😊", "#10B981"),
        MoodType("Calm", "😌", "#8B5CF6"),
        MoodType("Neutral", "😐", "#64748B"),
        MoodType("Tired", "😴", "#6B7280"),
        MoodType("Stressed", "😤", "#EF4444"),
        MoodType("Sad", "😔", "#3B82F6"),
        MoodType("Angry", "😠", "#DC2626")
    )

    // Mood distribution data class
    data class MoodDistribution(
        val moodType: String,
        val count: Int,
        val percentage: Int
    )

    data class MoodType(val name: String, val emoji: String, val color: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MoodPrefs", MODE_PRIVATE)

        setupRecyclerView()
        setupBottomNavigation()
        setupClickListeners()
        loadMoodEntries()
        updateStats()
        updateEmptyState()
        updateMoodDistribution()
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.rv_mood_history)
        emptyState = findViewById(R.id.empty_state)

        recyclerView.layoutManager = LinearLayoutManager(this)

        moodAdapter = MoodAdapter(
            moodEntries = moodEntries,
            onEditClicked = { moodEntry ->
                showEditMoodDialog(moodEntry)
            },
            onDeleteClicked = { moodEntry ->
                showDeleteConfirmation(moodEntry)
            }
        )

        recyclerView.adapter = moodAdapter
    }

    private fun setupClickListeners() {
        val btnAddMood = findViewById<ImageButton>(R.id.btn_add_mood)
        btnAddMood.setOnClickListener {
            showMoodSelectionDialog()
        }

        // Setup quick actions
        setupQuickActions()
    }

    private fun showMoodSelectionDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("How are you feeling?")
            .setCancelable(true)
            .create()

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(32, 32, 32, 32)

        // Create mood buttons
        moodTypes.forEach { moodType ->
            val button = Button(this)
            button.text = "${moodType.emoji} ${moodType.name}"
            button.textSize = 16f
            button.setPadding(32, 32, 32, 32)
            button.gravity = Gravity.START or Gravity.CENTER_VERTICAL
            button.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded_outline)

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.bottomMargin = 16
            button.layoutParams = layoutParams

            button.setOnClickListener {
                showNoteDialog(moodType)
                dialog.dismiss()
            }

            layout.addView(button)
        }

        dialog.setView(layout)
        dialog.show()
    }

    private fun showNoteDialog(moodType: MoodType, existingNote: String = "") {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Add a note (optional)")
            .setMessage("How are you feeling about ${moodType.emoji} ${moodType.name}?")
            .setView(R.layout.dialog_mood_note)
            .setPositiveButton("Save") { dialogInterface, _ ->
                val noteEditText = (dialogInterface as AlertDialog).findViewById<TextView>(R.id.et_mood_note)
                val note = noteEditText?.text?.toString()?.trim() ?: ""

                saveMoodEntry(moodType, note)
                dialogInterface.dismiss()
            }
            .setNegativeButton("Skip") { dialogInterface, _ ->
                saveMoodEntry(moodType, existingNote)
                dialogInterface.dismiss()
            }
            .setCancelable(false)
            .create()

        // Pre-fill note if editing
        dialog.setOnShowListener {
            val noteEditText = dialog.findViewById<TextView>(R.id.et_mood_note)
            noteEditText?.setText(existingNote)
        }

        dialog.show()
    }

    private fun showEditMoodDialog(moodEntry: MoodEntry) {
        val currentMoodType = moodTypes.find { it.name == moodEntry.moodType }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Mood")
            .setMessage("Update your mood and note")
            .setView(R.layout.dialog_mood_note)
            .setPositiveButton("Update") { dialogInterface, _ ->
                val noteEditText = (dialogInterface as AlertDialog).findViewById<TextView>(R.id.et_mood_note)
                val updatedNote = noteEditText?.text?.toString()?.trim() ?: ""

                updateMoodEntry(moodEntry, updatedNote)
                dialogInterface.dismiss()
            }
            .setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .setNeutralButton("Change Mood") { dialogInterface, _ ->
                dialogInterface.dismiss()
                showMoodSelectionForEdit(moodEntry)
            }
            .create()

        // Pre-fill with existing data
        dialog.setOnShowListener {
            val noteEditText = dialog.findViewById<TextView>(R.id.et_mood_note)
            noteEditText?.setText(moodEntry.note)
        }

        dialog.show()
    }

    private fun showMoodSelectionForEdit(oldMoodEntry: MoodEntry) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Select New Mood")
            .setCancelable(true)
            .create()

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(32, 32, 32, 32)

        moodTypes.forEach { moodType ->
            val button = Button(this)
            button.text = "${moodType.emoji} ${moodType.name}"
            button.textSize = 16f
            button.setPadding(32, 32, 32, 32)
            button.gravity = Gravity.START or Gravity.CENTER_VERTICAL
            button.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded_outline)

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.bottomMargin = 16
            button.layoutParams = layoutParams

            button.setOnClickListener {
                updateMoodEntryType(oldMoodEntry, moodType)
                dialog.dismiss()
            }

            layout.addView(button)
        }

        dialog.setView(layout)
        dialog.show()
    }

    private fun saveMoodEntry(moodType: MoodType, note: String) {
        val newEntry = MoodEntry(
            id = System.currentTimeMillis().toInt(),
            moodType = moodType.name,
            moodEmoji = moodType.emoji,
            note = note,
            date = Date()
        )

        moodEntries.add(0, newEntry) // Add to beginning for newest first
        saveMoodEntriesToStorage()
        updateStats()
        updateMoodDistribution()
        updateMoodList()

        // Show confirmation
        Toast.makeText(this, "Mood recorded! ${moodType.emoji}", Toast.LENGTH_SHORT).show()

        // Show celebration for positive moods
        if (moodType.name in listOf("Excited", "Happy", "Calm")) {
            showMoodCelebration(moodType)
        }
    }

    private fun updateMoodEntry(oldEntry: MoodEntry, updatedNote: String) {
        val index = moodEntries.indexOfFirst { it.id == oldEntry.id }
        if (index != -1) {
            val updatedEntry = oldEntry.copy(note = updatedNote)
            moodEntries[index] = updatedEntry
            saveMoodEntriesToStorage()
            updateMoodList()
            updateMoodDistribution()
            Toast.makeText(this, "Mood updated! ✅", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateMoodEntryType(oldEntry: MoodEntry, newMoodType: MoodType) {
        val index = moodEntries.indexOfFirst { it.id == oldEntry.id }
        if (index != -1) {
            val updatedEntry = oldEntry.copy(
                moodType = newMoodType.name,
                moodEmoji = newMoodType.emoji
            )
            moodEntries[index] = updatedEntry
            saveMoodEntriesToStorage()
            updateMoodList()
            updateStats()
            updateMoodDistribution()
            Toast.makeText(this, "Mood changed to ${newMoodType.emoji}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteConfirmation(moodEntry: MoodEntry) {
        AlertDialog.Builder(this)
            .setTitle("Delete Mood Entry")
            .setMessage("Are you sure you want to delete this mood entry?")
            .setPositiveButton("Delete") { dialog, _ ->
                deleteMoodEntry(moodEntry)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteMoodEntry(moodEntry: MoodEntry) {
        val index = moodEntries.indexOfFirst { it.id == moodEntry.id }
        if (index != -1) {
            moodEntries.removeAt(index)
            saveMoodEntriesToStorage()
            updateMoodList()
            updateStats()
            updateMoodDistribution()
            Toast.makeText(this, "Mood entry deleted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateMoodList() {
        moodAdapter.updateMoodEntries(moodEntries)
        updateEmptyState()
    }

    private fun updateEmptyState() {
        if (moodEntries.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
            findViewById<TextView>(R.id.tv_mood_history).visibility = View.GONE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
            findViewById<TextView>(R.id.tv_mood_history).visibility = View.VISIBLE
        }
    }

    private fun updateMoodDistribution() {
        if (moodEntries.isEmpty()) {
            // Reset to default values if no entries
            resetProgressBarsToDefault()
            return
        }

        val distribution = calculateMoodDistribution()
        updateProgressBars(distribution)
    }

    private fun calculateMoodDistribution(): List<MoodDistribution> {
        val totalEntries = moodEntries.size

        // Count occurrences of each mood type
        val moodCounts = moodEntries.groupingBy { it.moodType }
            .eachCount()
            .toMutableMap()

        // Ensure all mood types are represented (even with 0 count)
        moodTypes.forEach { moodType ->
            moodCounts.putIfAbsent(moodType.name, 0)
        }

        // Calculate percentages and create distribution list
        return moodCounts.map { (moodType, count) ->
            val percentage = if (totalEntries > 0) {
                (count.toFloat() / totalEntries.toFloat() * 100).toInt()
            } else {
                0
            }
            MoodDistribution(moodType, count, percentage)
        }
    }

    private fun updateProgressBars(distribution: List<MoodDistribution>) {
        // Update each mood type's progress bar
        distribution.forEach { moodDist ->
            updateProgressBarForMood(moodDist)
        }
    }

    private fun updateProgressBarForMood(moodDist: MoodDistribution) {
        val progressBar = when (moodDist.moodType) {
            "Happy" -> findViewById<ProgressBar>(R.id.progress_happy)
            "Calm" -> findViewById<ProgressBar>(R.id.progress_calm)
            "Stressed" -> findViewById<ProgressBar>(R.id.progress_stressed)
            "Excited" -> findViewById<ProgressBar>(R.id.progress_excited)
            else -> null
        }

        val countTextView = when (moodDist.moodType) {
            "Happy" -> findViewById<TextView>(R.id.tv_happy_count)
            "Calm" -> findViewById<TextView>(R.id.tv_calm_count)
            "Stressed" -> findViewById<TextView>(R.id.tv_stressed_count)
            "Excited" -> findViewById<TextView>(R.id.tv_excited_count)
            else -> null
        }

        progressBar?.progress = moodDist.percentage
        countTextView?.text = "${moodDist.count} times"
    }

    private fun resetProgressBarsToDefault() {
        // Reset to default values when no data
        val defaultValues = mapOf(
            "Happy" to Pair(0, "0 times"),
            "Calm" to Pair(0, "0 times"),
            "Stressed" to Pair(0, "0 times"),
            "Excited" to Pair(0, "0 times")
        )

        defaultValues.forEach { (moodType, values) ->
            val progressBar = when (moodType) {
                "Happy" -> findViewById<ProgressBar>(R.id.progress_happy)
                "Calm" -> findViewById<ProgressBar>(R.id.progress_calm)
                "Stressed" -> findViewById<ProgressBar>(R.id.progress_stressed)
                "Excited" -> findViewById<ProgressBar>(R.id.progress_excited)
                else -> null
            }

            val countTextView = when (moodType) {
                "Happy" -> findViewById<TextView>(R.id.tv_happy_count)
                "Calm" -> findViewById<TextView>(R.id.tv_calm_count)
                "Stressed" -> findViewById<TextView>(R.id.tv_stressed_count)
                "Excited" -> findViewById<TextView>(R.id.tv_excited_count)
                else -> null
            }

            progressBar?.progress = values.first
            countTextView?.text = values.second
        }
    }

    private fun showMoodCelebration(moodType: MoodType) {
        val messages = listOf(
            "Great to see you're feeling ${moodType.name.lowercase()}! 🌟",
            "Awesome! ${moodType.emoji} Keep up the positive vibes!",
            "Wonderful! ${moodType.emoji} Your positivity is inspiring!"
        )
        val randomMessage = messages.random()
        Toast.makeText(this, randomMessage, Toast.LENGTH_LONG).show()
    }

    private fun setupQuickActions() {
        // Mood Patterns button
        val moodPatternsCard = findViewById<CardView>(R.id.card_mood_patterns)
        moodPatternsCard?.setOnClickListener {
            showMoodPatterns()
        }

        // Set Reminders button
        val remindersCard = findViewById<CardView>(R.id.card_reminders)
        remindersCard?.setOnClickListener {
            showReminderDialog()
        }

        // View Insights button in Mood Distribution
        val viewInsightsText = findViewById<TextView>(R.id.tv_view_insights)
        viewInsightsText?.setOnClickListener {
            showMoodInsights()
        }
    }

    private fun showMoodPatterns() {
        if (moodEntries.isEmpty()) {
            Toast.makeText(this, "Record some moods first to see patterns!", Toast.LENGTH_SHORT).show()
            return
        }

        val mostCommonMood = getMostCommonMood()
        val patternMessage = when (mostCommonMood) {
            "Happy", "Excited" -> "You're mostly positive! 🌈\nKeep spreading those good vibes!"
            "Calm", "Neutral" -> "You maintain great balance! ⚖️\nYour emotional stability is impressive!"
            else -> "You're working through various emotions.\nRemember, all feelings are valid! 💪"
        }

        AlertDialog.Builder(this)
            .setTitle("Your Mood Patterns")
            .setMessage("Your most common mood: $mostCommonMood ${getEmojiForMood(mostCommonMood)}\n\n$patternMessage")
            .setPositiveButton("Got it", null)
            .show()
    }

    private fun showReminderDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Set Mood Reminder")
            .setView(R.layout.dialog_reminder)
            .setPositiveButton("Save Reminder") { dialogInterface, _ ->
                val titleEditText = (dialogInterface as AlertDialog).findViewById<TextView>(R.id.et_reminder_title)
                val messageEditText = (dialogInterface as AlertDialog).findViewById<TextView>(R.id.et_reminder_message)
                val timePicker = (dialogInterface as AlertDialog).findViewById<TimePicker>(R.id.time_picker)
                
                val title = titleEditText?.text?.toString()?.trim() ?: "Mood Check-in"
                val message = messageEditText?.text?.toString()?.trim() ?: "How are you feeling today?"
                val hour = timePicker?.hour ?: 9
                val minute = timePicker?.minute ?: 0
                
                val timeString = String.format("%02d:%02d", hour, minute)
                
                val reminder = Reminder(
                    id = System.currentTimeMillis().toInt(),
                    title = title,
                    message = message,
                    time = timeString
                )
                
                val reminderManager = ReminderManager(this)
                reminderManager.saveReminder(reminder)
                
                // Also try the simple method as backup
                reminderManager.saveReminderSimple(reminder)
                
                Toast.makeText(this, "Reminder set for $timeString! 📅", Toast.LENGTH_SHORT).show()
                dialogInterface.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.show()
    }

    private fun showMoodInsights() {
        if (moodEntries.isEmpty()) {
            Toast.makeText(this, "No mood data yet. Start recording your moods!", Toast.LENGTH_SHORT).show()
            return
        }

        val insights = buildString {
            append("📊 Your Mood Insights\n\n")
            append("Total entries: ${moodEntries.size}\n")
            append("Most common mood: ${getMostCommonMood()} ${getEmojiForMood(getMostCommonMood())}\n")
            append("Recent trend: ${getRecentTrend()}\n\n")
            append("💡 Tip: ${getMoodTip()}")
        }

        AlertDialog.Builder(this)
            .setTitle("Mood Insights")
            .setMessage(insights)
            .setPositiveButton("Thanks!", null)
            .show()
    }

    private fun getMostCommonMood(): String {
        return moodEntries.groupBy { it.moodType }
            .maxByOrNull { it.value.size }?.key ?: "No data"
    }

    private fun getEmojiForMood(mood: String): String {
        return moodTypes.find { it.name == mood }?.emoji ?: "😊"
    }

    private fun getRecentTrend(): String {
        if (moodEntries.size < 2) return "Not enough data"

        val recentMoods = moodEntries.takeLast(5).map { it.moodType }
        val positiveMoods = listOf("Happy", "Excited", "Calm")
        val positiveCount = recentMoods.count { it in positiveMoods }

        return when {
            positiveCount >= 4 -> "Very positive 📈"
            positiveCount >= 2 -> "Mostly positive ↗️"
            else -> "Mixed emotions 🔄"
        }
    }

    private fun getMoodTip(): String {
        val mostCommon = getMostCommonMood()
        return when (mostCommon) {
            "Happy", "Excited" -> "Your positivity is contagious! Share it with others."
            "Calm", "Neutral" -> "Your emotional balance is a superpower. Maintain it with mindfulness."
            "Stressed", "Angry" -> "Try deep breathing exercises when feeling overwhelmed."
            "Sad", "Tired" -> "Remember to practice self-care and reach out to loved ones."
            else -> "Regular mood tracking helps understand your emotional patterns."
        }
    }

    private fun saveMoodEntriesToStorage() {
        try {
            val moodEntriesJson = gson.toJson(moodEntries)
            sharedPreferences.edit().putString(MOOD_ENTRIES_KEY, moodEntriesJson).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadMoodEntries() {
        try {
            val moodEntriesJson = sharedPreferences.getString(MOOD_ENTRIES_KEY, null)
            if (moodEntriesJson != null) {
                val type = object : TypeToken<MutableList<MoodEntry>>() {}.type
                val savedEntries = gson.fromJson<MutableList<MoodEntry>>(moodEntriesJson, type)
                moodEntries.clear()
                moodEntries.addAll(savedEntries.sortedByDescending { it.date })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateStats() {
        // Update entries this month
        val entriesThisMonth = moodEntries.count {
            isSameMonth(it.date, Date())
        }
        findViewById<TextView>(R.id.tv_entries_month)?.text = entriesThisMonth.toString()

        // Update most common mood
        val mostCommonMood = getMostCommonMood()
        findViewById<TextView>(R.id.tv_most_common_mood)?.text =
            getEmojiForMood(mostCommonMood)

        // Update day streak
        val dayStreak = calculateDayStreak()
        findViewById<TextView>(R.id.tv_day_streak)?.text = dayStreak.toString()
    }

    private fun isSameMonth(date1: Date, date2: Date): Boolean {
        val calendar1 = Calendar.getInstance().apply { time = date1 }
        val calendar2 = Calendar.getInstance().apply { time = date2 }
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
                calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH)
    }

    private fun calculateDayStreak(): Int {
        if (moodEntries.isEmpty()) return 0

        val sortedEntries = moodEntries.sortedByDescending { it.date }
        var streak = 1
        val calendar = Calendar.getInstance()

        for (i in 1 until sortedEntries.size) {
            calendar.time = sortedEntries[i-1].date
            val previousDay = calendar.get(Calendar.DAY_OF_YEAR)

            calendar.time = sortedEntries[i].date
            val currentDay = calendar.get(Calendar.DAY_OF_YEAR)

            if (previousDay - currentDay == 1) {
                streak++
            } else {
                break
            }
        }

        return streak
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_mood

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_mood -> true
                R.id.nav_habits -> {
                    startActivity(Intent(this, Habits::class.java))
                    finish()
                    true
                }
                R.id.nav_home -> {
                    startActivity(Intent(this, Home::class.java))
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
}