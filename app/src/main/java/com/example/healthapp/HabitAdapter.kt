package com.example.healthapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class HabitAdapter(
    private var habits: List<Habit>,
    private val onHabitChecked: (Habit, Boolean) -> Unit,
    private val onMoreClicked: (Habit) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cbHabit: CheckBox = itemView.findViewById(R.id.cb_habit)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_habit_title)
        val tvCategory: TextView = itemView.findViewById(R.id.tv_category)
        val tvStreak: TextView = itemView.findViewById(R.id.tv_streak)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progress_habit)
        val tvProgressText: TextView = itemView.findViewById(R.id.tv_progress_text)
        val btnMore: ImageButton = itemView.findViewById(R.id.btn_more)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_habit_item, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]

        holder.tvTitle.text = habit.title
        holder.tvCategory.text = habit.category
        holder.tvStreak.text = "${habit.currentStreak} days"

        // Set category color
        val (bgRes, textColor) = getCategoryStyle(habit.category)
        holder.tvCategory.setBackgroundResource(bgRes)
        holder.tvCategory.setTextColor(ContextCompat.getColor(holder.itemView.context, textColor))

        // Set progress
        val progress = habit.getProgress()
        holder.progressBar.progress = progress

        // Set progress text and color based on completion status
        if (habit.isFullyCompleted()) {
            holder.tvProgressText.text = "Completed"
            holder.tvProgressText.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.cyan_500))
            holder.progressBar.progressTintList = ContextCompat.getColorStateList(holder.itemView.context, R.color.cyan_500)
        } else {
            holder.tvProgressText.text = "${habit.completedCount}/${habit.targetCount}"
            holder.tvProgressText.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.slate_500))
            holder.progressBar.progressTintList = ContextCompat.getColorStateList(holder.itemView.context, getProgressColor(habit.category))
        }

        // Remove previous listener to avoid multiple callbacks
        holder.cbHabit.setOnCheckedChangeListener(null)

        // Set checkbox state - checked only if fully completed
        holder.cbHabit.isChecked = habit.isFullyCompleted()

        // Enable/disable checkbox based on completion status
        holder.cbHabit.isEnabled = !habit.isFullyCompleted()

        // Set checkbox listener
        holder.cbHabit.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !habit.isFullyCompleted()) {
                onHabitChecked(habit, isChecked)
            } else if (!isChecked && habit.isFullyCompleted()) {
                // If user tries to uncheck a completed habit, revert it
                holder.cbHabit.isChecked = true
            }
        }

        // Set more button listener
        holder.btnMore.setOnClickListener {
            onMoreClicked(habit)
        }
    }

    override fun getItemCount(): Int = habits.size

    fun updateHabits(newHabits: List<Habit>) {
        habits = newHabits
        notifyDataSetChanged()
    }

    private fun getCategoryStyle(category: String): Pair<Int, Int> {
        return when (category.lowercase()) {
            "health" -> Pair(R.drawable.bg_rounded_tag_blue, R.color.blue_600)
            "fitness" -> Pair(R.drawable.bg_rounded_tag_green, R.color.green_500)
            "mindfulness" -> Pair(R.drawable.bg_rounded_tag_purple, R.color.purple_500)
            "work" -> Pair(R.drawable.bg_rounded_tag_orange, R.color.orange_500)
            else -> Pair(R.drawable.bg_rounded_tag_blue, R.color.blue_600)
        }
    }

    private fun getProgressColor(category: String): Int {
        return when (category.lowercase()) {
            "health" -> R.color.cyan_500
            "fitness" -> R.color.emerald_500
            "mindfulness" -> R.color.purple_500
            "work" -> R.color.orange_500
            else -> R.color.cyan_500
        }
    }
}