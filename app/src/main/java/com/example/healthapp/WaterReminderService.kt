package com.example.healthapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class WaterReminderService {
    companion object {
        private const val CHANNEL_ID = "water_reminder_channel"
        private const val CHANNEL_NAME = "Water Reminders"
        private const val CHANNEL_DESCRIPTION = "Notifications to remind you to drink water"
        private const val NOTIFICATION_ID_BASE = 1000

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = CHANNEL_DESCRIPTION
                    enableVibration(true)
                    enableLights(true)
                    setShowBadge(true)
                    setSound(
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                        null
                    )
                }

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }

        fun showWaterReminder(context: Context, reminderId: Int, time: String) {
            createNotificationChannel(context)

            val intent = Intent(context, Hydration::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                reminderId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_droplets)
                .setContentTitle("💧 Time to Drink Water!")
                .setContentText("It's $time - Remember to stay hydrated!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(longArrayOf(0, 1000, 500, 1000))
                .setLights(0xFF00BCD4.toInt(), 1000, 1000)
                .build()

            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID_BASE + reminderId, notification)
            }
            
            // Remove the reminder after notification is sent
            removeReminderAfterNotification(context, reminderId)
        }
        
        private fun removeReminderAfterNotification(context: Context, reminderId: Int) {
            val sharedPreferences = context.getSharedPreferences("hydration_data", Context.MODE_PRIVATE)
            val reminders = mutableListOf<Pair<Int, String>>()
            val reminderCount = sharedPreferences.getInt("reminder_count", 0)
            
            // Load all reminders
            for (i in 0 until reminderCount) {
                val time = sharedPreferences.getString("reminder_$i", "")
                if (time != null && time.isNotEmpty()) {
                    reminders.add(Pair(i, time))
                }
            }
            
            // Remove the specific reminder
            val updatedReminders = reminders.filter { it.first != reminderId }
            
            // Save updated reminders
            sharedPreferences.edit().apply {
                putInt("reminder_count", updatedReminders.size)
                updatedReminders.forEachIndexed { index, reminder ->
                    putString("reminder_$index", reminder.second)
                }
                // Clear any extra entries
                for (i in updatedReminders.size until reminderCount) {
                    remove("reminder_$i")
                }
                apply()
            }
        }

        fun cancelReminder(context: Context, reminderId: Int) {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.cancel(NOTIFICATION_ID_BASE + reminderId)
        }
    }
}
