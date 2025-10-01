package com.example.healthapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DailyWorkoutActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    private lateinit var exercisesContainer: LinearLayout
    private val selectedExercises = mutableListOf<Exercise>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_daily_workout)

        Log.d("Navigation", "DailyWorkoutActivity started")

        sharedPreferences = getSharedPreferences("workout_prefs", MODE_PRIVATE)
        exercisesContainer = findViewById(R.id.exercises_container)

        val backButton: ImageButton = findViewById(R.id.btn_back)
        val resetButton: MaterialButton = findViewById(R.id.btn_reset_workout)
        val startWorkoutButton: MaterialButton = findViewById(R.id.btn_start_workout)

        // DEBUG: Check if we can find the CardView
        val workoutInfoCard: CardView? = findViewById(R.id.card_workout_info)
        Log.d("Navigation", "Workout info card found: ${workoutInfoCard != null}")

        backButton.setOnClickListener {
            Log.d("Navigation", "Back button clicked")
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish()
        }

        // SIMPLE APPROACH: Add click listener directly to the CardView
        workoutInfoCard?.setOnClickListener {
            Log.d("Navigation", "Workout info card clicked - navigating to WorkoutDetails")
            navigateToWorkoutDetails()
        }

        // ALTERNATIVE: Also make the title and description clickable
        val tvWorkoutTitle: TextView? = findViewById(R.id.tv_workout_title)
        val tvWorkoutDescription: TextView? = findViewById(R.id.tv_workout_description)

        tvWorkoutTitle?.setOnClickListener {
            Log.d("Navigation", "Workout title clicked")
            navigateToWorkoutDetails()
        }

        tvWorkoutDescription?.setOnClickListener {
            Log.d("Navigation", "Workout description clicked")
            navigateToWorkoutDetails()
        }

        resetButton.setOnClickListener {
            resetSelectedExercises()
        }

        // Start Workout Button
        startWorkoutButton.setOnClickListener {
            if (selectedExercises.isNotEmpty()) {
                startWorkoutTimer()
            } else {
                Toast.makeText(this, "Please select exercises first", Toast.LENGTH_SHORT).show()
            }
        }

        // Load and display selected exercises
        loadAndDisplaySelectedExercises()
    }

    override fun onResume() {
        super.onResume()
        Log.d("Navigation", "DailyWorkoutActivity resumed")
        // Refresh exercises when returning from WorkoutDetails
        loadAndDisplaySelectedExercises()
    }

    private fun navigateToWorkoutDetails() {
        Log.d("Navigation", "Starting WorkoutDetails activity")
        val intent = Intent(this, WorkoutDetails::class.java)
        startActivity(intent)
        // Don't call finish() so user can come back with back button
    }

    private fun startWorkoutTimer() {
        val intent = Intent(this, WorkoutTimerActivity::class.java)
        val exercisesJson = gson.toJson(ArrayList(selectedExercises))
        intent.putExtra(WorkoutTimerActivity.EXTRA_EXERCISES, exercisesJson)
        startActivity(intent)
    }

    private fun updateStartButtonVisibility() {
        val startWorkoutButton: MaterialButton = findViewById(R.id.btn_start_workout)
        startWorkoutButton.visibility = if (selectedExercises.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun loadAndDisplaySelectedExercises() {
        val json = sharedPreferences.getString("selected_exercises", null)
        exercisesContainer.removeAllViews()
        selectedExercises.clear()

        if (json != null) {
            val type = object : TypeToken<ArrayList<Exercise>>() {}.type
            val savedExercises: ArrayList<Exercise> = gson.fromJson(json, type)
            selectedExercises.addAll(savedExercises)

            updateExerciseCount(selectedExercises.size)
            updateStartButtonVisibility()

            if (selectedExercises.isEmpty()) {
                showEmptyState()
            } else {
                selectedExercises.forEachIndexed { index, exercise ->
                    addExerciseToContainer(exercise, index + 1)
                }
            }
        } else {
            updateExerciseCount(0)
            updateStartButtonVisibility()
            showEmptyState()
        }
    }

    private fun addExerciseToContainer(exercise: Exercise, position: Int) {
        val exerciseCard = layoutInflater.inflate(R.layout.item_exercise, exercisesContainer, false)

        // Set exercise data
        val tvPosition = exerciseCard.findViewById<TextView>(R.id.tv_position)
        val tvExerciseName = exerciseCard.findViewById<TextView>(R.id.tv_exercise_name)
        val tvExerciseDetails = exerciseCard.findViewById<TextView>(R.id.tv_exercise_details)
        val btnRemove = exerciseCard.findViewById<MaterialButton>(R.id.btn_remove_exercise)
        val positionBackground = exerciseCard.findViewById<LinearLayout>(R.id.position_background)

        tvPosition.text = position.toString()
        tvExerciseName.text = exercise.name
        tvExerciseDetails.text = "${exercise.duration} • ${exercise.category}"

        // Set position background color based on exercise color
        when (exercise.color) {
            "#10B981" -> {
                positionBackground.setBackgroundResource(R.drawable.bg_circle_light_green)
                tvPosition.setTextColor(ContextCompat.getColor(this, R.color.green_500))
            }
            "#2563EB" -> {
                positionBackground.setBackgroundResource(R.drawable.bg_circle_light_blue)
                tvPosition.setTextColor(ContextCompat.getColor(this, R.color.blue_500))
            }
            "#F59E0B" -> {
                positionBackground.setBackgroundResource(R.drawable.bg_circle_light_orange)
                tvPosition.setTextColor(ContextCompat.getColor(this, R.color.orange_500))
            }
            "#EF4444" -> {
                positionBackground.setBackgroundResource(R.drawable.bg_circle_light_red)
                tvPosition.setTextColor(ContextCompat.getColor(this, R.color.red_500))
            }
            else -> {
                positionBackground.setBackgroundResource(R.drawable.bg_circle_light_gray)
                tvPosition.setTextColor(ContextCompat.getColor(this, R.color.gray_500))
            }
        }

        // Handle trash icon click to remove exercise
        btnRemove.setOnClickListener {
            showRemoveConfirmation(exercise)
        }

        exercisesContainer.addView(exerciseCard)
    }

    private fun showRemoveConfirmation(exercise: Exercise) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Remove Exercise")
            .setMessage("Are you sure you want to remove \"${exercise.name}\" from your workout?")
            .setPositiveButton("Remove") { dialog, which ->
                removeExercise(exercise)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun removeExercise(exerciseToRemove: Exercise) {
        // Remove from local list
        selectedExercises.removeAll { it.id == exerciseToRemove.id }

        // Save updated list
        saveSelectedExercises()

        // Refresh display
        loadAndDisplaySelectedExercises()
    }

    private fun saveSelectedExercises() {
        val json = gson.toJson(ArrayList(selectedExercises))
        sharedPreferences.edit().putString("selected_exercises", json).apply()
    }

    private fun showEmptyState() {
        val emptyView = layoutInflater.inflate(R.layout.empty_exercises_state, exercisesContainer, false)
        val tvEmptyMessage = emptyView.findViewById<TextView>(R.id.tv_empty_message)
        tvEmptyMessage.text = "No exercises selected yet.\nTap 'Quick Morning Workout' to choose exercises."
        exercisesContainer.addView(emptyView)
    }

    private fun resetSelectedExercises() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Reset All Exercises")
            .setMessage("Are you sure you want to remove all exercises from your workout?")
            .setPositiveButton("Reset All") { dialog, which ->
                sharedPreferences.edit().remove("selected_exercises").apply()
                loadAndDisplaySelectedExercises()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateExerciseCount(count: Int) {
        val tvExerciseCount = findViewById<TextView>(R.id.tv_exercise_count)
        tvExerciseCount.text = "$count ${if (count == 1) "exercise" else "exercises"}"
    }
}