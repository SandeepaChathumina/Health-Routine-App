package com.example.healthapp

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WorkoutTimerActivity : AppCompatActivity() {

    private lateinit var tvTimer: TextView
    private lateinit var tvExerciseName: TextView
    private lateinit var tvExerciseDetails: TextView
    private lateinit var btnPauseResume: MaterialButton
    private lateinit var btnNext: MaterialButton
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var tvCurrentExercise: TextView
    private lateinit var tvTotalExercises: TextView

    private var countDownTimer: CountDownTimer? = null
    private var isTimerRunning = false
    private var timeLeftInMillis: Long = 0L
    private var totalTime: Long = 0L

    private lateinit var selectedExercises: List<Exercise>
    private var currentExerciseIndex = 0

    companion object {
        const val EXTRA_EXERCISES = "extra_exercises"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_workout_timer)

        initViews()
        setupClickListeners()
        loadExercises()
        startWorkout()
    }

    private fun initViews() {
        tvTimer = findViewById(R.id.tv_timer)
        tvExerciseName = findViewById(R.id.tv_exercise_name)
        tvExerciseDetails = findViewById(R.id.tv_exercise_details)
        btnPauseResume = findViewById(R.id.btn_pause_resume)
        btnNext = findViewById(R.id.btn_next)
        progressIndicator = findViewById(R.id.progress_indicator)
        tvCurrentExercise = findViewById(R.id.tv_current_exercise)
        tvTotalExercises = findViewById(R.id.tv_total_exercises)

        val backButton: ImageButton = findViewById(R.id.btn_back)
        backButton.setOnClickListener {
            showExitConfirmation()
        }
    }

    private fun setupClickListeners() {
        btnPauseResume.setOnClickListener {
            if (isTimerRunning) {
                pauseTimer()
            } else {
                resumeTimer()
            }
        }

        btnNext.setOnClickListener {
            nextExercise()
        }
    }

    private fun loadExercises() {
        val exercisesJson = intent.getStringExtra(EXTRA_EXERCISES)
        selectedExercises = if (exercisesJson != null) {
            Gson().fromJson(exercisesJson, object : TypeToken<List<Exercise>>() {}.type)
        } else {
            emptyList()
        }

        tvTotalExercises.text = selectedExercises.size.toString()
    }

    private fun startWorkout() {
        if (selectedExercises.isNotEmpty()) {
            showCurrentExercise()
        } else {
            // No exercises selected, go back
            finish()
        }
    }

    private fun showCurrentExercise() {
        if (currentExerciseIndex < selectedExercises.size) {
            val currentExercise = selectedExercises[currentExerciseIndex]

            tvExerciseName.text = currentExercise.name
            tvExerciseDetails.text = "${currentExercise.duration} • ${currentExercise.category}"
            tvCurrentExercise.text = (currentExerciseIndex + 1).toString()

            // Parse duration (e.g., "30 seconds" or "12 reps")
            totalTime = parseDurationToMillis(currentExercise.duration)
            timeLeftInMillis = totalTime

            startTimer(totalTime)
        } else {
            // Workout completed
            workoutCompleted()
        }
    }

    private fun parseDurationToMillis(duration: String): Long {
        return when {
            duration.contains("second") -> {
                val seconds = duration.replace(Regex("[^0-9]"), "").toLong()
                seconds * 1000
            }
            duration.contains("minute") -> {
                val minutes = duration.replace(Regex("[^0-9]"), "").toLong()
                minutes * 60 * 1000
            }
            else -> 30000L // Default 30 seconds for reps
        }
    }

    private fun startTimer(totalTimeMillis: Long) {
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(totalTimeMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerText()
                updateProgress()
            }

            override fun onFinish() {
                timeLeftInMillis = 0
                updateTimerText()
                updateProgress()
                autoNextExercise()
            }
        }.start()

        isTimerRunning = true
        btnPauseResume.text = "Pause"
        updateProgress()
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        btnPauseResume.text = "Resume"
    }

    private fun resumeTimer() {
        startTimer(timeLeftInMillis)
    }

    private fun nextExercise() {
        countDownTimer?.cancel()
        currentExerciseIndex++
        showCurrentExercise()
    }

    private fun autoNextExercise() {
        // Auto-advance to next exercise after 2 seconds
        android.os.Handler().postDelayed({
            nextExercise()
        }, 2000)
    }

    private fun updateTimerText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        tvTimer.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun updateProgress() {
        if (totalTime > 0) {
            val progress = ((totalTime - timeLeftInMillis) * 100 / totalTime).toInt()
            progressIndicator.progress = progress
        }
    }

    private fun workoutCompleted() {
        val intent = Intent(this, WorkoutCompleteActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showExitConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Exit Workout")
            .setMessage("Are you sure you want to exit the workout? Your progress will be lost.")
            .setPositiveButton("Exit") { dialog, which ->
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}