package com.example.healthapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class WorkoutCompleteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_workout_complete)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        val backButton: ImageButton = findViewById(R.id.btn_back)
        val doneButton: MaterialButton = findViewById(R.id.btn_done)

        backButton.setOnClickListener {
            finish()
        }

        doneButton.setOnClickListener {
            navigateToDailyWorkout()
        }
    }

    private fun setupClickListeners() {
        val backButton: ImageButton = findViewById(R.id.btn_back)
        val doneButton: MaterialButton = findViewById(R.id.btn_done)

        backButton.setOnClickListener {
            finish()
        }

        doneButton.setOnClickListener {
            navigateToDailyWorkout()
        }
    }

    private fun navigateToDailyWorkout() {
        val intent = Intent(this, DailyWorkoutActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }
}