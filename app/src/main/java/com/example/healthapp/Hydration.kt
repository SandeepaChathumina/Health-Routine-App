package com.example.healthapp

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

class Hydration : AppCompatActivity() {
    private var currentIntake = 0
    private var dailyGoal = 6000
    private var glassSize = 500
    private var currentGlasses = 0
    private var goalGlasses = 12
    private var isGoalAchieved = false
    private var streakDays = 0

    // UI Components
    private lateinit var tvCurrentAmount: TextView
    private lateinit var tvGoalAmount: TextView
    private lateinit var tvProgressPercent: TextView
    private lateinit var tvCupsToday: TextView
    private lateinit var tvProgress: TextView
    private lateinit var tvStreak: TextView
    private lateinit var waterLevel: View
    private lateinit var btnAddGlass: MaterialButton
    private lateinit var btnAdd: MaterialButton
    private lateinit var btnSubtract: MaterialButton
    private lateinit var btnResetBottle: MaterialButton
    private lateinit var btnAddReminder: MaterialButton
    private lateinit var remindersContainer: LinearLayout
    private lateinit var hydrationChart: HydrationChartView

    // SharedPreferences
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var alarmManager: AlarmManager
    
    // Sensor components for shake detection
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var shakeDetector: ShakeDetector
    private lateinit var vibrator: Vibrator

    // Bottle dimensions
    private val maxWaterHeight = 180f
    private val waterWidth = 80f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hydration)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("hydration_data", MODE_PRIVATE)
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Initialize sensor components
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        
        // Initialize shake detector
        shakeDetector = ShakeDetector { waterAmount ->
            onShakeDetected(waterAmount)
        }

        initializeViews()
        setupClickListeners()
        setupBottomNavigation()
        loadHydrationData()
        loadReminders()
        rescheduleAllReminders()
        checkNotificationPermission()
        loadChartData()
        updateUI()
    }
    
    override fun onResume() {
        super.onResume()
        // Register sensor listener when activity is active
        if (accelerometer != null) {
            sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
    }
    
    override fun onPause() {
        super.onPause()
        // Unregister sensor listener when activity is paused
        sensorManager.unregisterListener(shakeDetector)
        // Save data when leaving the activity
        saveHydrationData()
    }

    private fun initializeViews() {
        tvCurrentAmount = findViewById(R.id.tv_current_amount)
        tvGoalAmount = findViewById(R.id.tv_goal_amount)
        tvProgressPercent = findViewById(R.id.tv_progress_percent)
        tvCupsToday = findViewById(R.id.tv_cups_today)
        tvProgress = findViewById(R.id.tv_progress)
        tvStreak = findViewById(R.id.tv_streak)
        waterLevel = findViewById(R.id.water_level)
        btnAddGlass = findViewById(R.id.btn_add_glass)
        btnAdd = findViewById(R.id.btn_add)
        btnSubtract = findViewById(R.id.btn_subtract)
        btnResetBottle = findViewById(R.id.btn_reset_bottle)
        btnAddReminder = findViewById(R.id.btn_add_reminder)
        remindersContainer = findViewById(R.id.reminders_container)
        hydrationChart = findViewById(R.id.hydration_chart)

        // Quick add buttons
        findViewById<MaterialButton>(R.id.btn_add_250).setOnClickListener { addWater(250) }
        findViewById<MaterialButton>(R.id.btn_add_500).setOnClickListener { addWater(500) }
        findViewById<MaterialButton>(R.id.btn_add_750).setOnClickListener { addWater(750) }

        // Initialize reset button as hidden
        btnResetBottle.visibility = View.GONE
        
        updateWaterLevel(0f)
    }

    private fun setupClickListeners() {
        btnAddGlass.setOnClickListener { addWater(glassSize) }
        btnAdd.setOnClickListener { addWater(glassSize) }
        btnSubtract.setOnClickListener { removeWater(glassSize) }
        btnResetBottle.setOnClickListener { resetBottle() }
        btnAddReminder.setOnClickListener { showAddReminderDialog() }
        findViewById<ImageButton>(R.id.btn_settings).setOnClickListener { showSettingsDialog() }
    }

    private fun loadHydrationData() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastSavedDate = sharedPreferences.getString("last_saved_date", "")

        // Reset data if it's a new day
        if (lastSavedDate != today) {
            resetDailyIntake()

            // Check if goal was achieved yesterday to maintain streak
            val yesterdayGoalAchieved = sharedPreferences.getBoolean("yesterday_goal_achieved", false)
            if (yesterdayGoalAchieved) {
                streakDays = sharedPreferences.getInt("streak_days", 0) + 1
            } else {
                streakDays = 0
            }

            // Save new date and reset yesterday's achievement flag
            sharedPreferences.edit().apply {
                putString("last_saved_date", today)
                putBoolean("yesterday_goal_achieved", false)
                putInt("streak_days", streakDays)
                apply()
            }
        } else {
            // Load today's data
            currentIntake = sharedPreferences.getInt("current_intake", 0)
            currentGlasses = sharedPreferences.getInt("current_glasses", 0)
            isGoalAchieved = sharedPreferences.getBoolean("is_goal_achieved", false)
            streakDays = sharedPreferences.getInt("streak_days", 0)
        }
    }

    private fun saveHydrationData() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        sharedPreferences.edit().apply {
            putInt("current_intake", currentIntake)
            putInt("current_glasses", currentGlasses)
            putBoolean("is_goal_achieved", isGoalAchieved)
            putString("last_saved_date", today)
            putInt("streak_days", streakDays)

            // If goal is achieved today, mark it for tomorrow's streak calculation
            if (isGoalAchieved) {
                putBoolean("yesterday_goal_achieved", true)
            }

            apply()
        }
    }

    private fun addWater(amount: Int) {
        if (isGoalAchieved) return // Prevent adding more water after goal is achieved

        val previousIntake = currentIntake
        currentIntake += amount
        if (currentIntake > dailyGoal) currentIntake = dailyGoal

        currentGlasses = (currentIntake / glassSize).coerceAtMost(goalGlasses)

        animateWaterChange(previousIntake, currentIntake)
        
        // Add data point to chart
        addChartDataPoint(currentIntake)
        
        // Check if goal is reached BEFORE updating UI
        if (currentIntake >= dailyGoal && !isGoalAchieved) {
            isGoalAchieved = true
            saveHydrationData() // Save immediately when goal is achieved
            updateUI() // Update UI after setting isGoalAchieved to true
            showTrophyCelebration()
        } else {
            updateUI()
            saveHydrationData()
        }
    }

    private fun removeWater(amount: Int) {
        if (isGoalAchieved) {
            isGoalAchieved = false // Reset achievement flag if user subtracts water
        }

        val previousIntake = currentIntake
        currentIntake -= amount
        if (currentIntake < 0) currentIntake = 0

        currentGlasses = (currentIntake / glassSize).coerceAtLeast(0)

        animateWaterChange(previousIntake, currentIntake)
        
        // Update chart data
        addChartDataPoint(currentIntake)
        
        updateUI()
        saveHydrationData()
    }

    private fun animateWaterChange(from: Int, to: Int) {
        val progressFrom = (from.toFloat() / dailyGoal).coerceIn(0f, 1f)
        val progressTo = (to.toFloat() / dailyGoal).coerceIn(0f, 1f)

        val animator = ValueAnimator.ofFloat(progressFrom, progressTo)
        animator.duration = 500
        animator.interpolator = DecelerateInterpolator()

        animator.addUpdateListener { animation ->
            val progress = animation.animatedValue as Float
            updateWaterLevel(progress)
        }

        animator.start()
    }

    private fun updateWaterLevel(progress: Float) {
        val targetHeight = maxWaterHeight * progress.coerceIn(0f, 1f)
        val density = resources.displayMetrics.density
        val targetHeightPx = (targetHeight * density).toInt()

        val layoutParams = waterLevel.layoutParams
        layoutParams.height = targetHeightPx
        waterLevel.layoutParams = layoutParams
        waterLevel.requestLayout()
    }

    private fun updateUI() {
        tvCurrentAmount.text = "${currentIntake}ml"
        tvGoalAmount.text = "of ${dailyGoal}ml"

        val progressPercentage = (currentIntake.toFloat() / dailyGoal * 100).toInt()
        tvProgressPercent.text = "$progressPercentage% Complete"
        tvProgress.text = "$progressPercentage%"
        tvCupsToday.text = "$currentGlasses/$goalGlasses"
        tvStreak.text = "$streakDays days"

        val progress = currentIntake.toFloat() / dailyGoal
        updateWaterLevel(progress)
        
        // Show/hide reset button based on goal achievement
        btnResetBottle.visibility = if (isGoalAchieved) View.VISIBLE else View.GONE
    }

    private fun showTrophyCelebration() {
        // Get the root view of the activity
        val rootView = window.decorView.rootView as ViewGroup

        // Create overlay background
        val overlay = View(this).apply {
            setBackgroundColor(0xCC000000.toInt())
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            id = View.generateViewId()
        }

        // Create main celebration container
        val celebrationContainer = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                dpToPx(320),
                dpToPx(450),
                Gravity.CENTER
            )
            background = createRoundedBackground()
            elevation = dpToPx(16).toFloat()
            id = View.generateViewId()
        }

        // Create trophy image using PNG
        val trophyImage = ImageView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                dpToPx(150),
                dpToPx(150),
                Gravity.CENTER
            ).apply {
                topMargin = dpToPx(40)
            }
            // Use PNG image - make sure trophy.png is in your drawable folder
            setImageResource(R.drawable.trophy)
            scaleType = ImageView.ScaleType.FIT_CENTER
            contentDescription = "Achievement Trophy"
        }

        // Create congratulations text
        val congratsText = TextView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_HORIZONTAL
            ).apply {
                topMargin = dpToPx(210)
            }
            text = "🎉 Goal Achieved! 🎉"
            textSize = 22f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setShadowLayer(dpToPx(2).toFloat(), 0f, 0f, Color.BLACK)
        }

        // Create message text
        val messageText = TextView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_HORIZONTAL
            ).apply {
                topMargin = dpToPx(260)
            }
            text = "You've successfully consumed\n6 liters of water today!"
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setShadowLayer(dpToPx(1).toFloat(), 0f, 0f, Color.BLACK)
        }

        // Add views to container
        celebrationContainer.addView(trophyImage)
        celebrationContainer.addView(congratsText)
        celebrationContainer.addView(messageText)

        // Add to root view
        rootView.addView(overlay)
        rootView.addView(celebrationContainer)

        // Bring to front
        celebrationContainer.bringToFront()

        // Create celebration animations
        val scaleX = ObjectAnimator.ofFloat(celebrationContainer, View.SCALE_X, 0f, 1.1f, 1f)
        val scaleY = ObjectAnimator.ofFloat(celebrationContainer, View.SCALE_Y, 0f, 1.1f, 1f)
        val alpha = ObjectAnimator.ofFloat(celebrationContainer, View.ALPHA, 0f, 1f)

        val containerAnim = AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            duration = 1000
            interpolator = BounceInterpolator()
        }

        // Text animations - fade in text separately
        val congratsAlpha = ObjectAnimator.ofFloat(congratsText, View.ALPHA, 0f, 1f)
        val messageAlpha = ObjectAnimator.ofFloat(messageText, View.ALPHA, 0f, 1f)

        val textAnim = AnimatorSet().apply {
            playTogether(congratsAlpha, messageAlpha)
            duration = 800
            startDelay = 500
        }

        // Trophy bounce animation
        val trophyBounce = ObjectAnimator.ofFloat(trophyImage, View.SCALE_X, 1f, 1.2f, 1f)
        val trophyBounceY = ObjectAnimator.ofFloat(trophyImage, View.SCALE_Y, 1f, 1.2f, 1f)

        val trophyAnim = AnimatorSet().apply {
            playTogether(trophyBounce, trophyBounceY)
            duration = 600
            startDelay = 300
            interpolator = BounceInterpolator()
        }

        // Start animations
        containerAnim.start()
        textAnim.start()
        trophyAnim.start()

        // Add confetti effect
        addConfettiEffect(rootView)

        // Set click listener to close
        overlay.setOnClickListener {
            removeCelebrationViews(rootView, overlay, celebrationContainer)
        }
        celebrationContainer.setOnClickListener {
            removeCelebrationViews(rootView, overlay, celebrationContainer)
        }

        // Auto close after 6 seconds
        overlay.postDelayed({
            removeCelebrationViews(rootView, overlay, celebrationContainer)
        }, 6000)
    }

    private fun addConfettiEffect(rootView: ViewGroup) {
        val confettiColors = intArrayOf(
            Color.parseColor("#FF6B6B"), // Red
            Color.parseColor("#4ECDC4"), // Teal
            Color.parseColor("#FFD166"), // Yellow
            Color.parseColor("#06D6A0"), // Green
            Color.parseColor("#118AB2"), // Blue
            Color.parseColor("#EF476F"), // Pink
            Color.parseColor("#073B4C")  // Dark Blue
        )

        repeat(25) {
            val confetti = View(this).apply {
                layoutParams = FrameLayout.LayoutParams(dpToPx(12), dpToPx(12)).apply {
                    leftMargin = (0 until resources.displayMetrics.widthPixels).random()
                    topMargin = -dpToPx(50)
                }
                setBackgroundColor(confettiColors.random())
                rotation = (0..360).random().toFloat()
            }

            rootView.addView(confetti)

            // Animate confetti falling
            val fallAnim = ObjectAnimator.ofFloat(confetti, View.TRANSLATION_Y, 0f, resources.displayMetrics.heightPixels + dpToPx(100).toFloat())
            val rotateAnim = ObjectAnimator.ofFloat(confetti, View.ROTATION, confetti.rotation, confetti.rotation + 1080f)
            val fadeAnim = ObjectAnimator.ofFloat(confetti, View.ALPHA, 1f, 0f)

            AnimatorSet().apply {
                playTogether(fallAnim, rotateAnim, fadeAnim)
                duration = (3000..6000).random().toLong()
                interpolator = AccelerateDecelerateInterpolator()
                start()

                // Remove confetti after animation
                doOnEnd {
                    rootView.removeView(confetti)
                }
            }
        }
    }

    private fun removeCelebrationViews(rootView: ViewGroup, vararg views: View) {
        views.forEach { view ->
            if (view.parent != null) {
                rootView.removeView(view)
            }
        }
    }

    private fun createRoundedBackground(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dpToPx(28).toFloat()
            // Use a beautiful gradient background
            colors = intArrayOf(
                Color.parseColor("#2196F3"),  // Blue
                Color.parseColor("#1976D2")   // Darker Blue
            )
            gradientType = GradientDrawable.LINEAR_GRADIENT
            orientation = GradientDrawable.Orientation.TOP_BOTTOM
            setStroke(dpToPx(4), Color.WHITE)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun showSettingsDialog() {
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Hydration Settings")
            .setMessage("Daily Goal: 6000ml (6L)\nGlass Size: 500ml\n\n6 liters is recommended for active individuals.")
            .setPositiveButton("OK") { dialog, which -> dialog.dismiss() }
            .setNegativeButton("Reset Data") { dialog, which ->
                resetAllData()
            }
            .create()
        dialog.show()
    }

    private fun resetAllData() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
        resetDailyIntake()
        Toast.makeText(this, "All hydration data reset", Toast.LENGTH_SHORT).show()
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_hydration -> true
                R.id.nav_habits -> { startActivity(Intent(this, Habits::class.java)); true }
                R.id.nav_mood -> { startActivity(Intent(this, Mood::class.java)); true }
                R.id.nav_home -> { startActivity(Intent(this, Home::class.java)); true }
                R.id.nav_profile -> { startActivity(Intent(this, Profile::class.java)); true }
                else -> false
            }
        }
        bottomNavigationView.selectedItemId = R.id.nav_hydration
    }

    fun resetDailyIntake() {
        currentIntake = 0
        currentGlasses = 0
        isGoalAchieved = false
        clearChartData() // Clear chart data for new day
        updateUI()
        saveHydrationData()
    }

    private fun resetBottle() {
        if (isGoalAchieved) {
            // Animate the water level going down to empty
            animateWaterChange(currentIntake, 0)
            
            // Add final data point showing reset to zero
            addChartDataPoint(0)
            
            // Reset all values
            currentIntake = 0
            currentGlasses = 0
            isGoalAchieved = false
            
            // Update UI and save data
            updateUI()
            saveHydrationData()
            
            // Show confirmation message
            Toast.makeText(this, "Bottle reset! Start fresh!", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        // Save data when closing the app
        saveHydrationData()
    }

    // Reminder Management Functions
    private fun loadReminders() {
        remindersContainer.removeAllViews()
        val reminders = getReminders()
        
        if (reminders.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "No reminders set. Tap + to add one!"
                textSize = 14f
                setTextColor(Color.parseColor("#666666"))
                gravity = Gravity.CENTER
                setPadding(0, 32, 0, 32)
            }
            remindersContainer.addView(emptyText)
        } else {
            reminders.forEach { reminder ->
                addReminderCard(reminder)
            }
        }
    }

    private fun getReminders(): List<ReminderData> {
        return try {
            val reminders = mutableListOf<ReminderData>()
            val reminderCount = sharedPreferences.getInt("reminder_count", 0)
            for (i in 0 until reminderCount) {
                val time = sharedPreferences.getString("reminder_$i", "")
                if (time != null && time.isNotEmpty()) {
                    reminders.add(ReminderData(i, time))
                }
            }
            reminders
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveReminders(reminders: List<ReminderData>) {
        sharedPreferences.edit().apply {
            putInt("reminder_count", reminders.size)
            reminders.forEachIndexed { index, reminder ->
                putString("reminder_$index", reminder.time)
            }
            apply()
        }
    }

    private fun addReminderCard(reminder: ReminderData) {
        val card = MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(8)
            }
            radius = dpToPx(12).toFloat()
            elevation = dpToPx(2).toFloat()
            setCardBackgroundColor(Color.WHITE)
        }

        val contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12))
        }

        val timeText = TextView(this).apply {
            text = "⏰ ${reminder.time}"
            textSize = 16f
            setTextColor(Color.parseColor("#1A1A1A"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val deleteButton = MaterialButton(this).apply {
            text = "Delete"
            textSize = 12f
            setTextColor(Color.parseColor("#FF5722"))
            setBackgroundColor(Color.TRANSPARENT)
            strokeColor = ContextCompat.getColorStateList(this@Hydration, android.R.color.transparent)
            setOnClickListener {
                removeReminder(reminder.id)
            }
        }

        contentLayout.addView(timeText)
        contentLayout.addView(deleteButton)
        card.addView(contentLayout)
        remindersContainer.addView(card)
    }

    private fun showAddReminderDialog() {
        val timePicker = TimePicker(this).apply {
            setIs24HourView(true)
        }

        AlertDialog.Builder(this)
            .setTitle("Add Water Reminder")
            .setMessage("Set a time to be reminded to drink water")
            .setView(timePicker)
            .setPositiveButton("Add") { _, _ ->
                val hour = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    timePicker.hour
                } else {
                    @Suppress("DEPRECATION")
                    timePicker.currentHour
                }
                val minute = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    timePicker.minute
                } else {
                    @Suppress("DEPRECATION")
                    timePicker.currentMinute
                }
                
                val timeString = String.format("%02d:%02d", hour, minute)
                addReminder(timeString)
            }
            .setNeutralButton("Test (30s)") { _, _ ->
                testReminder()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun testReminder() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.SECOND, 30) // 30 seconds from now
        
        val testTime = String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
        val testReminder = ReminderData(999, testTime)
        
        android.util.Log.d("Hydration", "Creating test reminder for 30 seconds from now")
        
        // Schedule test alarm for 30 seconds from now
        val intent = Intent(this, WaterReminderReceiver::class.java).apply {
            putExtra("reminder_id", 999)
            putExtra("time", testTime)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
            Toast.makeText(this, "Test reminder set for 30 seconds from now", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Test reminder failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun addReminder(time: String) {
        val reminders = getReminders().toMutableList()
        val newId = reminders.size
        val newReminder = ReminderData(newId, time)
        reminders.add(newReminder)
        
        saveReminders(reminders)
        scheduleReminder(newReminder)
        loadReminders()
        
        Toast.makeText(this, "Reminder added for $time", Toast.LENGTH_SHORT).show()
    }

    private fun removeReminder(reminderId: Int) {
        val reminders = getReminders().toMutableList()
        reminders.removeAll { it.id == reminderId }
        
        saveReminders(reminders)
        cancelReminder(reminderId)
        loadReminders()
        
        Toast.makeText(this, "Reminder removed", Toast.LENGTH_SHORT).show()
    }

    private fun scheduleReminder(reminder: ReminderData) {
        android.util.Log.d("Hydration", "Scheduling reminder: ${reminder.time}")
        
        val intent = Intent(this, WaterReminderReceiver::class.java).apply {
            putExtra("reminder_id", reminder.id)
            putExtra("time", reminder.time)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Parse time string (HH:MM format)
        val timeParts = reminder.time.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // If the time has passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val timeUntilAlarm = calendar.timeInMillis - System.currentTimeMillis()
        android.util.Log.d("Hydration", "Alarm scheduled for: ${calendar.time}")
        android.util.Log.d("Hydration", "Time until alarm: ${timeUntilAlarm / 1000} seconds")

        // Schedule the alarm
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                android.util.Log.d("Hydration", "Alarm set with setExactAndAllowWhileIdle")
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                android.util.Log.d("Hydration", "Alarm set with setExact")
            }
            
            Toast.makeText(this, "Reminder set for ${reminder.time}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            android.util.Log.e("Hydration", "Error setting alarm: ${e.message}")
            Toast.makeText(this, "Could not set reminder: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cancelReminder(reminderId: Int) {
        val intent = Intent(this, WaterReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun rescheduleAllReminders() {
        val reminders = getReminders()
        reminders.forEach { reminder ->
            scheduleReminder(reminder)
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notification permission denied. Reminders won't work.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Chart Data Management
    private fun loadChartData() {
        val chartData = getChartData()
        hydrationChart.setData(chartData)
    }
    
    private fun getChartData(): List<HydrationChartView.ChartDataPoint> {
        val chartData = mutableListOf<HydrationChartView.ChartDataPoint>()
        val chartDataJson = sharedPreferences.getString("chart_data", "") ?: ""
        
        if (chartDataJson.isNotEmpty()) {
            try {
                val dataPoints = chartDataJson.split("|")
                for (dataPoint in dataPoints) {
                    if (dataPoint.isNotEmpty()) {
                        val parts = dataPoint.split(",")
                        if (parts.size >= 3) {
                            chartData.add(
                                HydrationChartView.ChartDataPoint(
                                    time = parts[0],
                                    value = parts[1].toFloat(),
                                    label = parts[2]
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                // If parsing fails, return empty list
            }
        }
        
        return chartData
    }
    
    private fun saveChartData(chartData: List<HydrationChartView.ChartDataPoint>) {
        val chartDataJson = chartData.joinToString("|") { "${it.time},${it.value},${it.label}" }
        sharedPreferences.edit().putString("chart_data", chartDataJson).apply()
    }
    
    private fun addChartDataPoint(currentIntake: Int) {
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val chartData = getChartData().toMutableList()
        
        // Add new data point
        chartData.add(
            HydrationChartView.ChartDataPoint(
                time = currentTime,
                value = currentIntake.toFloat(),
                label = if (currentIntake == 0) "Reset" else "${currentIntake}ml"
            )
        )
        
        // Keep only last 10 data points to avoid clutter
        if (chartData.size > 10) {
            chartData.removeAt(0)
        }
        
        // Save and update chart
        saveChartData(chartData)
        hydrationChart.setData(chartData)
    }
    
    private fun clearChartData() {
        sharedPreferences.edit().remove("chart_data").apply()
        hydrationChart.clearData()
    }
    
    // Shake detection handler
    private fun onShakeDetected(waterAmount: Int) {
        // Add water with shake detection
        addWater(waterAmount)
        
        // Provide haptic feedback
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(android.os.VibrationEffect.createOneShot(200, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(200)
        }
        
        // Show simple toast feedback
        Toast.makeText(this, "Shake detected - Added 250ml water!", Toast.LENGTH_SHORT).show()
        
        // Animate water bottle for visual feedback
        animateShakeFeedback()
    }
    
    private fun animateShakeFeedback() {
        val waterBottle = findViewById<View>(R.id.bottle_background)
        
        // Create shake animation
        val shakeAnimator = ObjectAnimator.ofFloat(waterBottle, "translationX", 0f, 10f, -10f, 8f, -8f, 6f, -6f, 4f, -4f, 2f, -2f, 0f)
        shakeAnimator.duration = 500
        shakeAnimator.start()
        
        // Add scale animation for emphasis
        val scaleAnimator = ObjectAnimator.ofFloat(waterBottle, "scaleX", 1f, 1.1f, 1f)
        val scaleYAnimator = ObjectAnimator.ofFloat(waterBottle, "scaleY", 1f, 1.1f, 1f)
        scaleAnimator.duration = 300
        scaleYAnimator.duration = 300
        
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(shakeAnimator, scaleAnimator, scaleYAnimator)
        animatorSet.start()
    }

    // Data class for reminders
    data class ReminderData(
        val id: Int,
        val time: String
    )
}