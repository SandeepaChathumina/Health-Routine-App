package com.example.healthapp

import java.io.Serializable
import java.util.*

data class MoodEntry(
    val id: Int,
    val moodType: String,
    val moodEmoji: String,
    val note: String,
    val date: Date,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable