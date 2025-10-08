package com.example.healthapp
import java.io.Serializable

data class Habit(
    val id: Int,
    val title: String,
    val category: String,
    val currentStreak: Int,
    val targetCount: Int, // How many times per day
    val completedCount: Int, // How many times completed today
    val isCompleted: Boolean = false
) : Serializable {
    //Calculate progress percentage
    fun getProgress(): Int {
        return if (targetCount > 0) {
            (completedCount.toFloat() / targetCount.toFloat() * 100).toInt()
        } else {
            0
        }
    }

    // Check if habit is fully completed for today
    fun isFullyCompleted(): Boolean {
        return completedCount >= targetCount
    }

    // Mark one completion
    fun markOneCompletion(): Habit {
        val newCompletedCount = (completedCount + 1).coerceAtMost(targetCount)
        val fullyCompleted = newCompletedCount >= targetCount

        return this.copy(
            completedCount = newCompletedCount,
            isCompleted = fullyCompleted,
            // Increase streak only when fully completed for the first time today
            currentStreak = if (fullyCompleted && !isCompleted) currentStreak + 1 else currentStreak
        )
    }
}