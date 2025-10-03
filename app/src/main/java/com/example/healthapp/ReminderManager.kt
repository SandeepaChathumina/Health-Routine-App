package com.example.healthapp

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ReminderManager(private val context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("ReminderPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val REMINDERS_KEY = "reminders"
        private const val REMINDER_COUNT_KEY = "reminder_count"
    }
    
    fun saveReminder(reminder: Reminder) {
        val reminders = getAllReminders().toMutableList()
        
        // Check if reminder with same ID exists, update it
        val existingIndex = reminders.indexOfFirst { it.id == reminder.id }
        if (existingIndex != -1) {
            reminders[existingIndex] = reminder
        } else {
            reminders.add(reminder)
        }
        
        saveRemindersToStorage(reminders)
    }
    
    fun getAllReminders(): List<Reminder> {
        val remindersJson = sharedPreferences.getString(REMINDERS_KEY, "[]")
        return try {
            val type = object : TypeToken<List<Reminder>>() {}.type
            gson.fromJson<List<Reminder>>(remindersJson, type) ?: emptyList()
        } catch (e: Exception) {
            android.util.Log.e("ReminderManager", "Error loading reminders: ${e.message}")
            emptyList()
        }
    }
    
    fun getActiveReminders(): List<Reminder> {
        return getAllReminders().filter { it.isEnabled }
    }
    
    fun deleteReminder(reminderId: Int) {
        val reminders = getAllReminders().toMutableList()
        reminders.removeAll { it.id == reminderId }
        saveRemindersToStorage(reminders)
    }
    
    fun updateReminderStatus(reminderId: Int, isEnabled: Boolean) {
        val reminders = getAllReminders().toMutableList()
        val reminderIndex = reminders.indexOfFirst { it.id == reminderId }
        if (reminderIndex != -1) {
            val updatedReminder = reminders[reminderIndex].copy(isEnabled = isEnabled)
            reminders[reminderIndex] = updatedReminder
            saveRemindersToStorage(reminders)
        }
    }
    
    fun getTodaysReminders(): List<Reminder> {
        val activeReminders = getActiveReminders()
        val today = java.util.Calendar.getInstance()
        val currentHour = today.get(java.util.Calendar.HOUR_OF_DAY)
        val currentMinute = today.get(java.util.Calendar.MINUTE)
        
        return activeReminders.filter { reminder ->
            val reminderTime = reminder.time.split(":")
            val reminderHour = reminderTime[0].toInt()
            val reminderMinute = reminderTime[1].toInt()
            
            // Show reminders that are due or upcoming (within next 2 hours)
            val reminderMinutes = reminderHour * 60 + reminderMinute
            val currentMinutes = currentHour * 60 + currentMinute
            val timeDiff = reminderMinutes - currentMinutes
            
            timeDiff >= 0 && timeDiff <= 120 // Within next 2 hours
        }
    }
    
    private fun saveRemindersToStorage(reminders: List<Reminder>) {
        val remindersJson = gson.toJson(reminders)
        val editor = sharedPreferences.edit()
        editor.putString(REMINDERS_KEY, remindersJson)
        editor.putInt(REMINDER_COUNT_KEY, reminders.size)
        editor.commit() // Use commit() instead of apply() for immediate persistence
    }
    
    
    // Alternative simple storage method (backup)
    fun saveReminderSimple(reminder: Reminder) {
        val editor = sharedPreferences.edit()
        editor.putString("reminder_${reminder.id}_title", reminder.title)
        editor.putString("reminder_${reminder.id}_message", reminder.message)
        editor.putString("reminder_${reminder.id}_time", reminder.time)
        editor.putBoolean("reminder_${reminder.id}_enabled", reminder.isEnabled)
        editor.putInt("reminder_${reminder.id}_id", reminder.id)
        editor.commit()
    }
    
    fun getAllRemindersSimple(): List<Reminder> {
        val reminders = mutableListOf<Reminder>()
        val reminderCount = sharedPreferences.getInt(REMINDER_COUNT_KEY, 0)
        
        for (i in 1..reminderCount) {
            val id = sharedPreferences.getInt("reminder_${i}_id", -1)
            if (id != -1) {
                val title = sharedPreferences.getString("reminder_${i}_title", "") ?: ""
                val message = sharedPreferences.getString("reminder_${i}_message", "") ?: ""
                val time = sharedPreferences.getString("reminder_${i}_time", "") ?: ""
                val enabled = sharedPreferences.getBoolean("reminder_${i}_enabled", true)
                
                if (title.isNotEmpty() && time.isNotEmpty()) {
                    reminders.add(Reminder(id, title, message, time, enabled))
                }
            }
        }
        
        return reminders
    }
}
