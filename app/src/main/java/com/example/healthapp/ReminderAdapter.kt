package com.example.healthapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ReminderAdapter(
    private var reminders: List<Reminder>,
    private val onReminderClick: (Reminder) -> Unit = {},
    private val onDeleteReminder: (Reminder) -> Unit = {}
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.tv_reminder_title)
        val messageTextView: TextView = itemView.findViewById(R.id.tv_reminder_message)
        val timeTextView: TextView = itemView.findViewById(R.id.tv_reminder_time)
        val statusImageView: ImageView = itemView.findViewById(R.id.iv_reminder_status)
        val deleteButton: ImageView = itemView.findViewById(R.id.iv_delete_reminder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = reminders[position]
        
        holder.titleTextView.text = reminder.title
        holder.messageTextView.text = reminder.message
        holder.timeTextView.text = reminder.getFormattedTime()
        
        // Set status icon - always show as active since we're only showing enabled reminders
        holder.statusImageView.setImageResource(R.drawable.ic_notifications_active)
        holder.statusImageView.setColorFilter(holder.itemView.context.getColor(android.R.color.holo_green_dark))
        
        // Set click listeners
        holder.itemView.setOnClickListener {
            onReminderClick(reminder)
        }
        
        holder.deleteButton.setOnClickListener {
            onDeleteReminder(reminder)
        }
    }

    override fun getItemCount(): Int = reminders.size

    fun updateReminders(newReminders: List<Reminder>) {
        reminders = newReminders
        notifyDataSetChanged()
    }
}
