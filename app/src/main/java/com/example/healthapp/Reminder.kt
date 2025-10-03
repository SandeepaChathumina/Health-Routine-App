package com.example.healthapp

import java.io.Serializable
import java.util.*

data class Reminder(
    val id: Int,
    val title: String,
    val message: String,
    val time: String, // Format: "HH:mm"
    val isEnabled: Boolean = true,
    val createdAt: Date = Date(),
    val lastTriggered: Date? = null
) : Serializable {
    
    fun getFormattedTime(): String {
        return time
    }
    
    fun isDue(): Boolean {
        if (!isEnabled) return false
        
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        
        val reminderTime = time.split(":")
        val reminderHour = reminderTime[0].toInt()
        val reminderMinute = reminderTime[1].toInt()
        
        return currentHour == reminderHour && currentMinute == reminderMinute
    }
}
