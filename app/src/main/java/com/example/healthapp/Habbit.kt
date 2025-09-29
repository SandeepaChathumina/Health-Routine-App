package com.example.healthapp
import java.io.Serializable

data class Habit(
    val id: Int,
    val title: String,
    val category: String,
    val currentStreak: Int,
    val targetCount: Int,
    val completedCount: Int,
    val isCompleted: Boolean = false
) : Serializable {
    fun getProgress(): Int {
        return if (targetCount > 0) {
            (completedCount.toFloat() / targetCount.toFloat() * 100).toInt()
        } else {
            0
        }
    }
}
