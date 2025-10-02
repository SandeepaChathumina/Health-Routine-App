package com.example.healthapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WorkoutDetails : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    private val selectedExercises = mutableListOf<Exercise>()

    // Define all available exercises
    private val availableExercises = listOf(
        Exercise(1, "Jumping Jacks", "30 seconds", "Cardio Full Body", "jumpingjack", "#10B981"),
        Exercise(2, "Push Ups", "12 reps", "Strength Chest", "pushups", "#2563EB"),
        Exercise(3, "Squats", "15 reps", "Legs Lower Body", "squats", "#F59E0B"),
        Exercise(4, "Plank", "30 seconds", "Core Abs", "plank", "#EF4444"),
        Exercise(5, "Mountain Climbers", "45 seconds", "Cardio Core", "mountainclimbers", "#10B981"),
        Exercise(6, "Bicycle Crunches", "20 reps", "Core Abs", "bicyclecrunches", "#EF4444"),
        Exercise(7, "Lunges", "12 reps each leg", "Legs Strength", "lunges", "#2563EB"),
        Exercise(8, "Glute Bridges", "15 reps", "Glutes Lower Body", "glutebridges", "#F59E0B")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_workout_details)

        sharedPreferences = getSharedPreferences("workout_prefs", MODE_PRIVATE)




        val backButton: ImageButton = findViewById(R.id.btn_back)
        val startWorkoutButton: MaterialButton = findViewById(R.id.btn_start_workout)

        // Load previously selected exercises
        loadSelectedExercises()

        backButton.setOnClickListener {
            finish() // Just go back without saving
        }

        startWorkoutButton.setOnClickListener {
            // Save selected exercises and go back to daily workout
            saveSelectedExercises()
            finish()
        }

        // Set up exercise selection with checkboxes
        setupExerciseSelection()
    }

    private fun setupExerciseSelection() {
        // Set up checkboxes for each exercise
        availableExercises.forEach { exercise ->
            val checkBox = findExerciseCheckbox(exercise.id)
            checkBox?.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // Add to selection
                    if (!selectedExercises.any { it.id == exercise.id }) {
                        selectedExercises.add(exercise)
                    }
                } else {
                    // Remove from selection
                    selectedExercises.removeAll { it.id == exercise.id }
                }
            }
        }
    }

    private fun findExerciseCheckbox(exerciseId: Int): MaterialCheckBox? {
        // Since we need to add checkboxes, we'll find the parent layout and add checkboxes programmatically
        // First, let's update the XML to include checkboxes
        return when (exerciseId) {
            1 -> findViewById(R.id.checkbox_exercise_1)
            2 -> findViewById(R.id.checkbox_exercise_2)
            3 -> findViewById(R.id.checkbox_exercise_3)
            4 -> findViewById(R.id.checkbox_exercise_4)
            5 -> findViewById(R.id.checkbox_exercise_5)
            6 -> findViewById(R.id.checkbox_exercise_6)
            7 -> findViewById(R.id.checkbox_exercise_7)
            8 -> findViewById(R.id.checkbox_exercise_8)
            else -> null
        }
    }

    private fun saveSelectedExercises() {
        val json = gson.toJson(ArrayList(selectedExercises))
        sharedPreferences.edit().putString("selected_exercises", json).apply()
    }

    private fun loadSelectedExercises() {
        val json = sharedPreferences.getString("selected_exercises", null)
        if (json != null) {
            val type = object : TypeToken<ArrayList<Exercise>>() {}.type
            val savedExercises: ArrayList<Exercise> = gson.fromJson(json, type)
            selectedExercises.clear()
            selectedExercises.addAll(savedExercises)

            // Update checkboxes to show selected exercises
            savedExercises.forEach { exercise ->
                val checkBox = findExerciseCheckbox(exercise.id)
                checkBox?.isChecked = true
            }
        }
    }
}