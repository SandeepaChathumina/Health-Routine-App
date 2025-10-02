package com.example.healthapp

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class StatsDataManager(private val context: Context) {
    
    private val habitsPrefs: SharedPreferences = context.getSharedPreferences("HabitsPrefs", Context.MODE_PRIVATE)
    private val hydrationPrefs: SharedPreferences = context.getSharedPreferences("hydration_data", Context.MODE_PRIVATE)
    private val moodPrefs: SharedPreferences = context.getSharedPreferences("MoodPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    data class StatsData(
        val healthScore: Int,
        val habitsScore: Int,
        val waterScore: Int,
        val moodScore: Int,
        val exerciseScore: Int,
        val currentStreak: Int,
        val bestStreak: Int,
        val totalDaysActive: Int,
        val totalHabits: Int,
        val avgHabitsPerDay: Float,
        val habitStreak: Int,
        val dailyWaterAvg: Float,
        val glassesPerDay: Float,
        val daysOnTrack: Int,
        val moodEntriesWeek: Int,
        val mostCommonMood: String,
        val moodImprovement: String
    )
    
    fun getStatsForPeriod(days: Int): StatsData {
        val endDate = Date()
        val startDate = Calendar.getInstance().apply {
            time = endDate
            add(Calendar.DAY_OF_YEAR, -days)
        }.time
        
        // Get habits data
        val habitsData = getHabitsStats(startDate, endDate)
        
        // Get hydration data
        val hydrationData = getHydrationStats(startDate, endDate)
        
        // Get mood data
        val moodData = getMoodStats(startDate, endDate)
        
        // Calculate overall health score
        val healthScore = calculateHealthScore(habitsData, hydrationData, moodData)
        
        return StatsData(
            healthScore = healthScore,
            habitsScore = habitsData.score,
            waterScore = hydrationData.score,
            moodScore = moodData.score,
            exerciseScore = 70, // Placeholder for now
            currentStreak = habitsData.currentStreak,
            bestStreak = habitsData.bestStreak,
            totalDaysActive = calculateTotalDaysActive(startDate, endDate),
            totalHabits = habitsData.totalHabits,
            avgHabitsPerDay = habitsData.avgPerDay,
            habitStreak = habitsData.currentStreak,
            dailyWaterAvg = hydrationData.dailyAvg,
            glassesPerDay = hydrationData.glassesPerDay,
            daysOnTrack = hydrationData.daysOnTrack,
            moodEntriesWeek = moodData.entriesThisWeek,
            mostCommonMood = moodData.mostCommonMood,
            moodImprovement = moodData.improvement
        )
    }
    
    private fun getHabitsStats(startDate: Date, endDate: Date): HabitsStats {
        val habitsJson = habitsPrefs.getString("saved_habits", null)
        val habits = if (habitsJson != null) {
            val type = object : TypeToken<MutableList<Habit>>() {}.type
            gson.fromJson<MutableList<Habit>>(habitsJson, type) ?: mutableListOf()
        } else {
            mutableListOf()
        }
        
        val totalHabits = habits.size
        val completedHabits = habits.count { it.isFullyCompleted() }
        val score = if (totalHabits > 0) {
            (completedHabits.toFloat() / totalHabits * 100).roundToInt()
        } else 0
        
        val currentStreak = habits.maxOfOrNull { it.currentStreak } ?: 0
        val bestStreak = habitsPrefs.getInt("best_habit_streak", currentStreak)
        
        // Save best streak if current is higher
        if (currentStreak > bestStreak) {
            habitsPrefs.edit().putInt("best_habit_streak", currentStreak).apply()
        }
        
        val avgPerDay = if (totalHabits > 0) {
            habits.sumOf { it.completedCount }.toFloat() / totalHabits
        } else 0f
        
        return HabitsStats(score, currentStreak, bestStreak, totalHabits, avgPerDay)
    }
    
    private fun getHydrationStats(startDate: Date, endDate: Date): HydrationStats {
        val currentIntake = hydrationPrefs.getInt("current_intake", 0)
        val dailyGoal = 6000 // 6L default goal
        val streakDays = hydrationPrefs.getInt("streak_days", 0)
        
        val score = ((currentIntake.toFloat() / dailyGoal) * 100).roundToInt().coerceAtMost(100)
        val dailyAvg = currentIntake / 1000f // Convert to liters
        val glassesPerDay = currentIntake / 250f // Assuming 250ml per glass
        val daysOnTrack = streakDays
        
        return HydrationStats(score, dailyAvg, glassesPerDay, daysOnTrack)
    }
    
    private fun getMoodStats(startDate: Date, endDate: Date): MoodStats {
        val moodEntriesJson = moodPrefs.getString("mood_entries", null)
        val moodEntries = if (moodEntriesJson != null) {
            val type = object : TypeToken<MutableList<MoodEntry>>() {}.type
            gson.fromJson<MutableList<MoodEntry>>(moodEntriesJson, type) ?: mutableListOf()
        } else {
            mutableListOf()
        }
        
        // Filter entries for the time period
        val filteredEntries = moodEntries.filter { entry ->
            entry.date.after(startDate) && entry.date.before(endDate)
        }
        
        val entriesThisWeek = filteredEntries.size
        val mostCommonMood = getMostCommonMoodFromEntries(filteredEntries)
        val score = calculateMoodScore(filteredEntries)
        val improvement = calculateMoodImprovement(filteredEntries)
        
        return MoodStats(score, entriesThisWeek, mostCommonMood, improvement)
    }
    
    private fun getMostCommonMoodFromEntries(entries: List<MoodEntry>): String {
        if (entries.isEmpty()) return "😊"
        
        val moodCounts = entries.groupingBy { it.moodEmoji }.eachCount()
        return moodCounts.maxByOrNull { it.value }?.key ?: "😊"
    }
    
    private fun calculateMoodScore(entries: List<MoodEntry>): Int {
        if (entries.isEmpty()) return 75
        
        val moodValues = mapOf(
            "🥳" to 5, "😊" to 4, "😌" to 3, "😐" to 2,
            "😴" to 2, "😤" to 1, "😔" to 1, "😠" to 0
        )
        
        val avgMoodValue = entries.mapNotNull { moodValues[it.moodEmoji] }
            .average()
        
        return ((avgMoodValue / 5.0) * 100).roundToInt()
    }
    
    private fun calculateMoodImprovement(entries: List<MoodEntry>): String {
        if (entries.size < 2) return "+0%"
        
        val recentEntries = entries.take(entries.size / 2)
        val olderEntries = entries.drop(entries.size / 2)
        
        val recentAvg = calculateMoodScore(recentEntries)
        val olderAvg = calculateMoodScore(olderEntries)
        
        val improvement = recentAvg - olderAvg
        return if (improvement >= 0) "+$improvement%" else "$improvement%"
    }
    
    private fun calculateHealthScore(habits: HabitsStats, hydration: HydrationStats, mood: MoodStats): Int {
        return ((habits.score * 0.4 + hydration.score * 0.3 + mood.score * 0.3)).roundToInt()
    }
    
    private fun calculateTotalDaysActive(startDate: Date, endDate: Date): Int {
        // Calculate days between start and end date
        val diffInMillis = endDate.time - startDate.time
        val diffInDays = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
        
        // For now, assume user has been active for most days in the period
        return (diffInDays * 0.8).roundToInt().coerceAtLeast(1)
    }
    
    // Data classes for organized stats
    data class HabitsStats(
        val score: Int,
        val currentStreak: Int,
        val bestStreak: Int,
        val totalHabits: Int,
        val avgPerDay: Float
    )
    
    data class HydrationStats(
        val score: Int,
        val dailyAvg: Float,
        val glassesPerDay: Float,
        val daysOnTrack: Int
    )
    
    data class MoodStats(
        val score: Int,
        val entriesThisWeek: Int,
        val mostCommonMood: String,
        val improvement: String
    )
}
