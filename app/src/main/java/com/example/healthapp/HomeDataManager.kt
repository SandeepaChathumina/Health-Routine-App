package com.example.healthapp

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class HomeDataManager(private val context: Context) {
    
    private val habitsPrefs: SharedPreferences = context.getSharedPreferences("HabitsPrefs", Context.MODE_PRIVATE)
    private val hydrationPrefs: SharedPreferences = context.getSharedPreferences("hydration_data", Context.MODE_PRIVATE)
    private val moodPrefs: SharedPreferences = context.getSharedPreferences("MoodPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    data class TodayProgressData(
        val overallProgress: Int,
        val habitsCompleted: Int,
        val totalHabits: Int,
        val currentStreak: Int,
        val waterIntake: Int, // in ml
        val waterGoal: Int, // in ml
        val hasLoggedMoodToday: Boolean,
        val todayMoodEmoji: String
    )
    
    fun getTodayProgress(): TodayProgressData {
        val habitsData = getTodayHabitsData()
        val hydrationData = getTodayHydrationData()
        val moodData = getTodayMoodData()
        
        // Calculate overall progress (weighted average)
        val habitProgress = if (habitsData.totalHabits > 0) {
            (habitsData.completed.toFloat() / habitsData.totalHabits * 100).roundToInt()
        } else 0
        
        val hydrationProgress = ((hydrationData.intake.toFloat() / hydrationData.goal) * 100).roundToInt().coerceAtMost(100)
        
        val moodProgress = if (moodData.hasEntry) 100 else 0
        
        // Weighted calculation: 50% habits, 30% hydration, 20% mood
        val overallProgress = ((habitProgress * 0.5 + hydrationProgress * 0.3 + moodProgress * 0.2)).roundToInt()
        
        return TodayProgressData(
            overallProgress = overallProgress,
            habitsCompleted = habitsData.completed,
            totalHabits = habitsData.totalHabits,
            currentStreak = habitsData.currentStreak,
            waterIntake = hydrationData.intake,
            waterGoal = hydrationData.goal,
            hasLoggedMoodToday = moodData.hasEntry,
            todayMoodEmoji = moodData.emoji
        )
    }
    
    private fun getTodayHabitsData(): HabitsData {
        val habitsJson = habitsPrefs.getString("saved_habits", null)
        val habits = if (habitsJson != null) {
            val type = object : TypeToken<MutableList<Habit>>() {}.type
            gson.fromJson<MutableList<Habit>>(habitsJson, type) ?: mutableListOf()
        } else {
            mutableListOf()
        }
        
        val totalHabits = habits.size
        val completedHabits = habits.count { it.isFullyCompleted() }
        val currentStreak = habits.maxOfOrNull { it.currentStreak } ?: 0
        
        return HabitsData(completedHabits, totalHabits, currentStreak)
    }
    
    private fun getTodayHydrationData(): HydrationData {
        val currentIntake = hydrationPrefs.getInt("current_intake", 0)
        val dailyGoal = 6000 // 6L default goal
        
        // Check if data is from today
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastSavedDate = hydrationPrefs.getString("last_saved_date", "")
        
        val todayIntake = if (lastSavedDate == today) currentIntake else 0
        
        return HydrationData(todayIntake, dailyGoal)
    }
    
    private fun getTodayMoodData(): MoodData {
        val moodEntriesJson = moodPrefs.getString("mood_entries", null)
        val moodEntries = if (moodEntriesJson != null) {
            val type = object : TypeToken<MutableList<MoodEntry>>() {}.type
            gson.fromJson<MutableList<MoodEntry>>(moodEntriesJson, type) ?: mutableListOf()
        } else {
            mutableListOf()
        }
        
        // Check if there's an entry for today
        val today = Calendar.getInstance()
        val todayEntries = moodEntries.filter { entry ->
            val entryCalendar = Calendar.getInstance().apply { time = entry.date }
            isSameDay(today, entryCalendar)
        }
        
        val hasEntry = todayEntries.isNotEmpty()
        val emoji = if (hasEntry) todayEntries.first().moodEmoji else "😊"
        
        return MoodData(hasEntry, emoji)
    }
    
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
    
    fun getGreetingMessage(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        return when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else -> "Good Night"
        }
    }
    
    fun getMotivationalMessage(progressData: TodayProgressData): String {
        return when {
            progressData.overallProgress >= 80 -> "Amazing work today! 🎉"
            progressData.overallProgress >= 60 -> "Great progress! Keep it up! 💪"
            progressData.overallProgress >= 40 -> "You're doing well! 👍"
            progressData.overallProgress >= 20 -> "Good start! Let's do more! 🌟"
            else -> "Let's make today amazing! ✨"
        }
    }
    
    // Data classes for organized data
    private data class HabitsData(
        val completed: Int,
        val totalHabits: Int,
        val currentStreak: Int
    )
    
    private data class HydrationData(
        val intake: Int,
        val goal: Int
    )
    
    private data class MoodData(
        val hasEntry: Boolean,
        val emoji: String
    )
}
