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
            .setTitle("Log Your Mood")
            .setView(R.layout.dialog_mood_entry)
            .setPositiveButton("Save Mood") { dialogInterface, _ ->
                val noteEditText = (dialogInterface as AlertDialog).findViewById<TextView>(R.id.et_mood_note)
                val note = noteEditText?.text?.toString()?.trim() ?: ""

                // Get selected mood from the enhanced dialog
                val selectedMood = getSelectedMoodFromDialog(dialogInterface as AlertDialog)
                
                if (selectedMood != null) {
                    saveMoodEntry(selectedMood, note)
                    Toast.makeText(this, "Mood logged successfully! ${selectedMood.emoji}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please select a mood first", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        // Set up emoji button listeners
        setupEmojiButtons(dialog)
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
    
    private fun showMoodSelectionForEditWithRefresh(oldMoodEntry: MoodEntry, calendarDialog: AlertDialog?) {
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
                
                // Refresh calendar after mood is actually changed
                if (calendarDialog != null) {
                    android.util.Log.d("Calendar", "Refreshing calendar after mood edit...")
                    refreshCalendarDisplay(calendarDialog)
                }
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
        android.util.Log.d("Calendar", "updateMoodEntryType called: ${oldEntry.moodType} -> ${newMoodType.name}")
        
        val index = moodEntries.indexOfFirst { it.id == oldEntry.id }
        android.util.Log.d("Calendar", "Found entry at index: $index")
        
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
            android.util.Log.d("Calendar", "Mood updated successfully")
        } else {
            android.util.Log.e("Calendar", "Entry not found for update")
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

        // Calendar View button
        val calendarCard = findViewById<CardView>(R.id.card_calendar)
        calendarCard?.setOnClickListener {
            showCalendarView()
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

    private fun showCalendarView() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Mood Calendar")
            .setView(R.layout.dialog_mood_calendar)
            .setPositiveButton("Close", null)
            .create()

        // Setup calendar functionality BEFORE showing
        setupCalendarView(dialog)
        
        dialog.show()
    }

    private fun setupEmojiButtons(dialog: AlertDialog) {
        dialog.setOnShowListener {
            val emojiButtons = listOf(
                R.id.btn_emoji_excited to moodTypes[0],
                R.id.btn_emoji_happy to moodTypes[1],
                R.id.btn_emoji_calm to moodTypes[2],
                R.id.btn_emoji_neutral to moodTypes[3],
                R.id.btn_emoji_tired to moodTypes[4],
                R.id.btn_emoji_stressed to moodTypes[5],
                R.id.btn_emoji_sad to moodTypes[6],
                R.id.btn_emoji_angry to moodTypes[7]
            )

            emojiButtons.forEach { (buttonId, moodType) ->
                val button = dialog.findViewById<Button>(buttonId)
                button?.setOnClickListener {
                    selectEmoji(dialog, buttonId, moodType)
                }
            }
        }
    }

    private fun selectEmoji(dialog: AlertDialog, selectedButtonId: Int, selectedMood: MoodType) {
        // Reset all buttons
        val allButtonIds = listOf(
            R.id.btn_emoji_excited, R.id.btn_emoji_happy, R.id.btn_emoji_calm, R.id.btn_emoji_neutral,
            R.id.btn_emoji_tired, R.id.btn_emoji_stressed, R.id.btn_emoji_sad, R.id.btn_emoji_angry
        )

        allButtonIds.forEach { buttonId ->
            val button = dialog.findViewById<Button>(buttonId)
            button?.background = ContextCompat.getDrawable(this, R.drawable.bg_emoji_button)
        }

        // Highlight selected button
        val selectedButton = dialog.findViewById<Button>(selectedButtonId)
        selectedButton?.background = ContextCompat.getDrawable(this, R.drawable.bg_emoji_button_selected)

        // Show selected mood
        val selectedMoodContainer = dialog.findViewById<LinearLayout>(R.id.selected_mood_container)
        val selectedMoodText = dialog.findViewById<TextView>(R.id.tv_selected_mood)
        
        selectedMoodContainer?.visibility = View.VISIBLE
        selectedMoodText?.text = "${selectedMood.emoji} ${selectedMood.name}"

        // Store selected mood for later retrieval
        dialog.findViewById<View>(R.id.selected_mood_container)?.tag = selectedMood
    }

    private fun getSelectedMoodFromDialog(dialog: AlertDialog): MoodType? {
        val selectedMoodContainer = dialog.findViewById<LinearLayout>(R.id.selected_mood_container)
        return selectedMoodContainer?.tag as? MoodType
    }

    private var currentCalendar: Calendar = Calendar.getInstance()
    
    private fun setupCalendarView(dialog: AlertDialog) {
        dialog.setOnShowListener {
            android.util.Log.d("Calendar", "Dialog shown, setting up calendar...")
            
            // First, let's try to add content directly to the dialog
            val dialogView = dialog.findViewById<View>(android.R.id.content)
            android.util.Log.d("Calendar", "Dialog view found: ${dialogView != null}")
            
            currentCalendar = Calendar.getInstance()
            updateCalendarDisplay(dialog, currentCalendar)
            
            // Setup navigation buttons
            val prevButton = dialog.findViewById<ImageButton>(R.id.btn_prev_month)
            val nextButton = dialog.findViewById<ImageButton>(R.id.btn_next_month)
            
            android.util.Log.d("Calendar", "Prev button found: ${prevButton != null}")
            android.util.Log.d("Calendar", "Next button found: ${nextButton != null}")
            
            prevButton?.setOnClickListener {
                currentCalendar.add(Calendar.MONTH, -1)
                updateCalendarDisplay(dialog, currentCalendar)
            }
            
            nextButton?.setOnClickListener {
                currentCalendar.add(Calendar.MONTH, 1)
                updateCalendarDisplay(dialog, currentCalendar)
            }
        }
    }

    private fun updateCalendarDisplay(dialog: AlertDialog, calendar: Calendar) {
        android.util.Log.d("Calendar", "updateCalendarDisplay called")
        
        val monthYearText = dialog.findViewById<TextView>(R.id.tv_month_year)
        val calendarGrid = dialog.findViewById<LinearLayout>(R.id.calendar_grid)
        
        android.util.Log.d("Calendar", "monthYearText found: ${monthYearText != null}")
        android.util.Log.d("Calendar", "calendarGrid found: ${calendarGrid != null}")
        
        // Update month/year display
        val monthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
        monthYearText?.text = monthYear
        android.util.Log.d("Calendar", "Month year set to: $monthYear")
        
        // Clear existing calendar
        calendarGrid?.removeAllViews()
        android.util.Log.d("Calendar", "Cleared existing views")
        
        // Create a very simple calendar with just text views
        createVerySimpleCalendar(calendarGrid, dialog)
    }
    
    private fun createVerySimpleCalendar(calendarGrid: LinearLayout?, calendarDialog: AlertDialog? = null) {
        if (calendarGrid == null) {
            android.util.Log.e("Calendar", "Calendar grid is null!")
            return
        }
        
        android.util.Log.d("Calendar", "Creating real calendar for ${currentCalendar.get(Calendar.MONTH) + 1}/${currentCalendar.get(Calendar.YEAR)}")
        
        // Get the first day of the month and number of days
        val calendar = currentCalendar.clone() as Calendar
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday, etc.
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        android.util.Log.d("Calendar", "First day of week: $firstDayOfWeek, Days in month: $daysInMonth")
        
        // Create 6 weeks (42 days total)
        for (week in 0..5) {
            val weekLayout = LinearLayout(this)
            weekLayout.orientation = LinearLayout.HORIZONTAL
            weekLayout.gravity = Gravity.CENTER
            
            val weekParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            weekParams.setMargins(0, 2, 0, 2)
            weekLayout.layoutParams = weekParams
            
            // Create 7 days for this week
            for (dayOfWeek in 0..6) {
                val dayPosition = week * 7 + dayOfWeek
                val dayView = createDayView(dayPosition, firstDayOfWeek, daysInMonth, calendar, calendarDialog)
                
                val dayParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                dayParams.setMargins(2, 2, 2, 2)
                dayView.layoutParams = dayParams
                
                weekLayout.addView(dayView)
            }
            
            calendarGrid.addView(weekLayout)
            android.util.Log.d("Calendar", "Added week $week with 7 days")
        }
        
        android.util.Log.d("Calendar", "Real calendar created with ${calendarGrid.childCount} weeks")
    }
    
    private fun createDayView(dayPosition: Int, firstDayOfWeek: Int, daysInMonth: Int, calendar: Calendar, calendarDialog: AlertDialog? = null): TextView {
        val dayView = TextView(this)
        
        // Calculate if this position should show a day of the current month
        val dayNumber = dayPosition - firstDayOfWeek + 2 // Adjust for Sunday = 1
        
        if (dayPosition < firstDayOfWeek - 1 || dayNumber > daysInMonth) {
            // Empty cell (previous month or next month)
            dayView.text = ""
            dayView.setBackgroundColor(ContextCompat.getColor(this, R.color.background_light))
        } else {
            // Current month day
            dayView.text = dayNumber.toString()
            dayView.textSize = 14f
            dayView.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
            dayView.gravity = Gravity.CENTER
            dayView.setPadding(8, 8, 8, 8)
            dayView.setBackgroundColor(ContextCompat.getColor(this, R.color.card_background_light))
            
            // Check if there's a mood entry for this day
            val moodForDay = getMoodForDay(dayNumber, calendar)
            if (moodForDay != null) {
                // Show mood emoji
                val emoji = getEmojiForMood(moodForDay.name)
                dayView.text = "$dayNumber\n$emoji"
                dayView.textSize = 12f
                dayView.setBackgroundColor(ContextCompat.getColor(this, R.color.accent_light_blue))
            }
            
            // Add click listener
            dayView.setOnClickListener {
                showDayDetails(dayNumber, moodForDay, calendar, calendarDialog)
            }
        }
        
        return dayView
    }
    
    private fun showDayDetails(dayNumber: Int, moodForDay: MoodType?, calendar: Calendar, calendarDialog: AlertDialog? = null) {
        // Create the correct date for this specific day
        val dayCalendar = calendar.clone() as Calendar
        dayCalendar.set(Calendar.DAY_OF_MONTH, dayNumber)
        val date = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(dayCalendar.time)
        
        if (moodForDay != null) {
            val emoji = getEmojiForMood(moodForDay.name)
            AlertDialog.Builder(this)
                .setTitle("$date")
                .setMessage("Mood: ${moodForDay.name} $emoji\n\nTap to add a note or edit mood.")
                .setPositiveButton("OK", null)
                .setNegativeButton("Edit Mood") { _, _ ->
                    editMoodForDay(dayNumber, dayCalendar, moodForDay, calendarDialog)
                }
                .show()
        } else {
            AlertDialog.Builder(this)
                .setTitle("$date")
                .setMessage("No mood recorded for this day.\n\nTap the + button to add a mood entry.")
                .setPositiveButton("OK", null)
                .setNegativeButton("Add Mood") { _, _ ->
                    addMoodForDay(dayNumber, dayCalendar, calendarDialog)
                }
                .show()
        }
    }
    
    private fun getMoodForDay(dayNumber: Int, calendar: Calendar): MoodType? {
        // Create a date for this specific day
        val dayCalendar = calendar.clone() as Calendar
        dayCalendar.set(Calendar.DAY_OF_MONTH, dayNumber)
        val targetDate = dayCalendar.time
        
        // Find mood entry for this date from the class variable
        val moodEntry = moodEntries.find { entry ->
            val entryCalendar = Calendar.getInstance()
            entryCalendar.time = entry.date
            entryCalendar.get(Calendar.YEAR) == dayCalendar.get(Calendar.YEAR) &&
            entryCalendar.get(Calendar.MONTH) == dayCalendar.get(Calendar.MONTH) &&
            entryCalendar.get(Calendar.DAY_OF_MONTH) == dayCalendar.get(Calendar.DAY_OF_MONTH)
        }
        
        // Convert string moodType to MoodType enum
        return moodTypes.find { it.name == moodEntry?.moodType }
    }
    
    private fun editMoodForDay(dayNumber: Int, dayCalendar: Calendar, currentMood: MoodType, calendarDialog: AlertDialog? = null) {
        android.util.Log.d("Calendar", "editMoodForDay called for day $dayNumber")
        
        // Find the existing mood entry for this day
        val existingEntry = moodEntries.find { entry ->
            val entryCalendar = Calendar.getInstance()
            entryCalendar.time = entry.date
            entryCalendar.get(Calendar.YEAR) == dayCalendar.get(Calendar.YEAR) &&
            entryCalendar.get(Calendar.MONTH) == dayCalendar.get(Calendar.MONTH) &&
            entryCalendar.get(Calendar.DAY_OF_MONTH) == dayCalendar.get(Calendar.DAY_OF_MONTH)
        }
        
        android.util.Log.d("Calendar", "Found existing entry: ${existingEntry != null}")
        
        if (existingEntry != null) {
            android.util.Log.d("Calendar", "Current mood: ${existingEntry.moodType}")
            // Show mood selection dialog for editing with calendar refresh callback
            showMoodSelectionForEditWithRefresh(existingEntry, calendarDialog)
        } else {
            Toast.makeText(this, "No mood entry found for this day", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun addMoodForDay(dayNumber: Int, dayCalendar: Calendar, calendarDialog: AlertDialog? = null) {
        // Show mood selection dialog for this specific date
        val dialog = AlertDialog.Builder(this)
            .setTitle("Select Mood for ${SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(dayCalendar.time)}")
            .setPositiveButton("Save") { _, _ ->
                // This will be handled in the dialog setup
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        val dialogView = layoutInflater.inflate(R.layout.dialog_mood_entry, null)
        dialog.setView(dialogView)
        
        // Setup emoji buttons
        setupEmojiButtons(dialog)
        
        dialog.show()
        
        // Override the save button to use the specific date
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
            val selectedMood = getSelectedMoodFromDialog(dialog)
            val note = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_mood_note)?.text?.toString()?.trim() ?: ""
            
            if (selectedMood != null) {
                // Create mood entry for the specific date
                val newEntry = MoodEntry(
                    id = System.currentTimeMillis().toInt(),
                    moodType = selectedMood.name,
                    moodEmoji = selectedMood.emoji,
                    note = note,
                    date = dayCalendar.time
                )
                
                moodEntries.add(0, newEntry)
                saveMoodEntriesToStorage()
                updateStats()
                updateEmptyState()
                updateMoodDistribution()
                updateMoodList()
                
                Toast.makeText(this, "Mood logged for ${SimpleDateFormat("MMM dd", Locale.getDefault()).format(dayCalendar.time)}! ${selectedMood.emoji}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                
                // Refresh calendar after adding
                if (calendarDialog != null) {
                    refreshCalendarDisplay(calendarDialog)
                }
            } else {
                Toast.makeText(this, "Please select a mood", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun refreshCalendarDisplay(calendarDialog: AlertDialog) {
        android.util.Log.d("Calendar", "Refreshing calendar display...")
        updateCalendarDisplay(calendarDialog, currentCalendar)
    }

    private fun createDayLayout(dialog: AlertDialog, day: Int, daysInMonth: Int, calendar: Calendar): LinearLayout {
        val dayLayout = LinearLayout(this)
        dayLayout.orientation = LinearLayout.VERTICAL
        dayLayout.gravity = Gravity.CENTER
        
        val layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        layoutParams.setMargins(2, 2, 2, 2)
        dayLayout.layoutParams = layoutParams
        
        // Set background
        dayLayout.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded_white)
        dayLayout.setPadding(8, 8, 8, 8)
        dayLayout.minimumHeight = 60
        
        // Always create a text view
        val dayText = TextView(this)
        dayText.textSize = 14f
        dayText.gravity = Gravity.CENTER
        dayText.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        
        if (day > 0 && day <= daysInMonth) {
            // Valid day of current month
            dayText.text = day.toString()
            dayText.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
            dayLayout.addView(dayText)
            
            // Check if there's a mood entry for this day
            val moodForDay = getMoodForDay(calendar, day)
            if (moodForDay != null) {
                val moodText = TextView(this)
                moodText.text = moodForDay.emoji
                moodText.textSize = 18f
                moodText.gravity = Gravity.CENTER
                moodText.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                dayLayout.addView(moodText)
                
                // Make clickable
                dayLayout.setOnClickListener {
                    showDayDetails(dialog, calendar, day, moodForDay)
                }
            }
        } else {
            // Empty day (from previous/next month)
            dayText.text = ""
            dayText.setTextColor(ContextCompat.getColor(this, R.color.text_tertiary))
            dayLayout.addView(dayText)
        }
        
        return dayLayout
    }

    private fun getMoodForDay(calendar: Calendar, day: Int): MoodType? {
        val targetDate = Calendar.getInstance()
        targetDate.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), day)
        val targetTimestamp = targetDate.timeInMillis
        
        // Find mood entry for this day
        for (entry in moodEntries) {
            val entryDate = Calendar.getInstance()
            entryDate.timeInMillis = entry.timestamp
            
            if (entryDate.get(Calendar.YEAR) == targetDate.get(Calendar.YEAR) &&
                entryDate.get(Calendar.MONTH) == targetDate.get(Calendar.MONTH) &&
                entryDate.get(Calendar.DAY_OF_MONTH) == targetDate.get(Calendar.DAY_OF_MONTH)) {
                
                return moodTypes.find { it.name == entry.moodType }
            }
        }
        return null
    }

    private fun showDayDetails(dialog: AlertDialog, calendar: Calendar, day: Int, mood: MoodType) {
        val selectedDayInfo = dialog.findViewById<LinearLayout>(R.id.selected_day_info)
        val selectedDate = dialog.findViewById<TextView>(R.id.tv_selected_date)
        val selectedMood = dialog.findViewById<TextView>(R.id.tv_selected_mood)
        val selectedNote = dialog.findViewById<TextView>(R.id.tv_selected_note)
        
        // Format date
        val date = Calendar.getInstance()
        date.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), day)
        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        
        selectedDate?.text = dateFormat.format(date.time)
        selectedMood?.text = "${mood.emoji} ${mood.name}"
        
        // Find the mood entry for this day
        val moodEntry = moodEntries.find { entry ->
            val entryDate = Calendar.getInstance()
            entryDate.timeInMillis = entry.timestamp
            entryDate.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
            entryDate.get(Calendar.MONTH) == date.get(Calendar.MONTH) &&
            entryDate.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH)
        }
        
        selectedNote?.text = moodEntry?.note ?: "No note added"
        selectedDayInfo?.visibility = View.VISIBLE
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