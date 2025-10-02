package com.example.healthapp

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.ImageButton
import android.widget.SeekBar
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
    private lateinit var btnMusicToggle: MaterialButton
    private lateinit var seekbarVolume: SeekBar
    private lateinit var tvVolumePercentage: TextView
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var tvCurrentExercise: TextView
    private lateinit var tvTotalExercises: TextView

    private var countDownTimer: CountDownTimer? = null
    private var isTimerRunning = false
    private var timeLeftInMillis: Long = 0L
    private var totalTime: Long = 0L
    private var isMusicEnabled = true
    private var currentVolume = 80

    private lateinit var selectedExercises: List<Exercise>
    private var currentExerciseIndex = 0

    companion object {
        const val EXTRA_EXERCISES = "extra_exercises"
        const val VOLUME_MAX = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_workout_timer)

        initViews()
        setupClickListeners()
        loadExercises()
        startWorkout()

        // Start music when workout begins
        startMusic()
    }

    private fun initViews() {
        tvTimer = findViewById(R.id.tv_timer)
        tvExerciseName = findViewById(R.id.tv_exercise_name)
        tvExerciseDetails = findViewById(R.id.tv_exercise_details)
        btnPauseResume = findViewById(R.id.btn_pause_resume)
        btnNext = findViewById(R.id.btn_next)
        btnMusicToggle = findViewById(R.id.btn_music_toggle)
        seekbarVolume = findViewById(R.id.seekbar_volume)
        tvVolumePercentage = findViewById(R.id.tv_volume_percentage)
        progressIndicator = findViewById(R.id.progress_indicator)
        tvCurrentExercise = findViewById(R.id.tv_current_exercise)
        tvTotalExercises = findViewById(R.id.tv_total_exercises)

        val backButton: ImageButton = findViewById(R.id.btn_back)
        backButton.setOnClickListener {
            showExitConfirmation()
        }

        // Set up volume seekbar
        seekbarVolume.max = VOLUME_MAX
        seekbarVolume.progress = currentVolume
        updateVolumeText()
    }

    private fun setupClickListeners() {
        btnPauseResume.setOnClickListener {
            if (isTimerRunning) {
                pauseTimer()
                pauseMusic()
            } else {
                resumeTimer()
                if (isMusicEnabled) {
                    resumeMusic()
                }
            }
        }

        btnNext.setOnClickListener {
            nextExercise()
        }

        btnMusicToggle.setOnClickListener {
            toggleMusic()
        }

        seekbarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    currentVolume = progress
                    updateVolumeText()
                    updateMusicVolume()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Not needed
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Not needed
            }
        })
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
        stopMusic()
        val intent = Intent(this, WorkoutCompleteActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Music Control Methods
    private fun startMusic() {
        if (isMusicEnabled) {
            val intent = Intent(this, MusicPlayerService::class.java)
            intent.action = MusicPlayerService.ACTION_PLAY
            intent.putExtra(MusicPlayerService.EXTRA_VOLUME, currentVolume)
            startService(intent)
        }
    }

    private fun pauseMusic() {
        val intent = Intent(this, MusicPlayerService::class.java)
        intent.action = MusicPlayerService.ACTION_PAUSE
        startService(intent)
    }

    private fun resumeMusic() {
        if (isMusicEnabled) {
            val intent = Intent(this, MusicPlayerService::class.java)
            intent.action = MusicPlayerService.ACTION_PLAY
            intent.putExtra(MusicPlayerService.EXTRA_VOLUME, currentVolume)
            startService(intent)
        }
    }

    private fun stopMusic() {
        val intent = Intent(this, MusicPlayerService::class.java)
        intent.action = MusicPlayerService.ACTION_STOP
        startService(intent)
    }

    private fun updateMusicVolume() {
        if (isMusicEnabled) {
            val intent = Intent(this, MusicPlayerService::class.java)
            intent.action = MusicPlayerService.ACTION_SET_VOLUME
            intent.putExtra(MusicPlayerService.EXTRA_VOLUME, currentVolume)
            startService(intent)
        }
    }

    private fun toggleMusic() {
        isMusicEnabled = !isMusicEnabled
        if (isMusicEnabled) {
            btnMusicToggle.text = "ON"
            btnMusicToggle.setBackgroundColor(getColor(android.R.color.holo_green_dark))
            if (isTimerRunning) {
                resumeMusic()
            }
        } else {
            btnMusicToggle.text = "OFF"
            btnMusicToggle.setBackgroundColor(getColor(android.R.color.darker_gray))
            pauseMusic()
        }
    }

    private fun updateVolumeText() {
        tvVolumePercentage.text = "$currentVolume%"
    }

    private fun showExitConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Exit Workout")
            .setMessage("Are you sure you want to exit the workout? Your progress will be lost.")
            .setPositiveButton("Exit") { dialog, which ->
                stopMusic()
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        // Don't stop music here to allow it to continue if user rotates screen
    }

    override fun onPause() {
        super.onPause()
        // Don't stop music when activity pauses (screen off, etc.)
    }
}