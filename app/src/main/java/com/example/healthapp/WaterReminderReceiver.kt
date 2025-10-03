package com.example.healthapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class WaterReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("WaterReminder", "Broadcast received!")
        
        val reminderId = intent.getIntExtra("reminder_id", -1)
        val time = intent.getStringExtra("time") ?: "now"
        
        Log.d("WaterReminder", "Reminder ID: $reminderId, Time: $time")
        
        if (reminderId != -1) {
            try {
                WaterReminderService.showWaterReminder(context, reminderId, time)
                Log.d("WaterReminder", "Notification sent successfully")
            } catch (e: Exception) {
                Log.e("WaterReminder", "Error showing notification: ${e.message}")
                Toast.makeText(context, "Error showing notification: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Log.e("WaterReminder", "Invalid reminder ID")
        }
    }
}
