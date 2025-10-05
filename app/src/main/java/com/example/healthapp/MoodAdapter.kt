package com.example.healthapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class MoodAdapter(
    private var moodEntries: List<MoodEntry>,
    private val onEditClicked: (MoodEntry) -> Unit,
    private val onDeleteClicked: (MoodEntry) -> Unit
) : RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMoodEmoji: TextView = itemView.findViewById(R.id.tv_mood_emoji)
        val tvMoodType: TextView = itemView.findViewById(R.id.tv_mood_type)
        val tvMoodNote: TextView = itemView.findViewById(R.id.tv_mood_note)
        val tvMoodTime: TextView = itemView.findViewById(R.id.tv_mood_time)
        val btnOptions: ImageButton = itemView.findViewById(R.id.btn_mood_options)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_mood_item, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val moodEntry = moodEntries[position]

        // Enhanced emoji display with better support and clarity
        holder.tvMoodEmoji.text = moodEntry.moodEmoji
        holder.tvMoodEmoji.typeface = android.graphics.Typeface.DEFAULT
        holder.tvMoodEmoji.paint.isAntiAlias = true
        holder.tvMoodEmoji.paint.isSubpixelText = true
        holder.tvMoodEmoji.paint.isFakeBoldText = false
        holder.tvMoodEmoji.paint.textSize = 36f
        holder.tvMoodEmoji.setShadowLayer(1f, 0f, 1f, android.graphics.Color.parseColor("#20000000"))
        holder.tvMoodType.text = moodEntry.moodType

        // Handle note display
        if (moodEntry.note.isNotEmpty()) {
            holder.tvMoodNote.text = moodEntry.note
            holder.tvMoodNote.visibility = View.VISIBLE
        } else {
            holder.tvMoodNote.visibility = View.GONE
        }

        // Format time
        val timeFormat = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
        holder.tvMoodTime.text = timeFormat.format(moodEntry.date)

        // Set options button click listener
        holder.btnOptions.setOnClickListener {
            showOptionsDialog(holder.itemView.context, moodEntry, holder.adapterPosition)
        }

        // Set item click listener for quick edit
        holder.itemView.setOnClickListener {
            onEditClicked(moodEntry)
        }
    }

    override fun getItemCount(): Int = moodEntries.size

    fun updateMoodEntries(newEntries: List<MoodEntry>) {
        moodEntries = newEntries.sortedByDescending { it.date }
        notifyDataSetChanged()
    }

    private fun showOptionsDialog(context: android.content.Context, moodEntry: MoodEntry, position: Int) {
        val options = arrayOf("Edit", "Delete")

        android.app.AlertDialog.Builder(context)
            .setTitle("Mood Options")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> onEditClicked(moodEntry) // Edit
                    1 -> onDeleteClicked(moodEntry) // Delete
                }
            }
            .show()
    }
}