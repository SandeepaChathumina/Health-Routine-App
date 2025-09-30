package com.example.healthapp

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

class Hydration : AppCompatActivity() {
    private var currentIntake = 0
    private var dailyGoal = 6000
    private var glassSize = 500
    private var currentGlasses = 0
    private var goalGlasses = 12
    private var isGoalAchieved = false

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

    // Bottle dimensions
    private val maxWaterHeight = 180f
    private val waterWidth = 80f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hydration)

        initializeViews()
        setupClickListeners()
        setupBottomNavigation()
        updateUI()
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

        // Quick add buttons
        findViewById<MaterialButton>(R.id.btn_add_250).setOnClickListener { addWater(250) }
        findViewById<MaterialButton>(R.id.btn_add_500).setOnClickListener { addWater(500) }
        findViewById<MaterialButton>(R.id.btn_add_750).setOnClickListener { addWater(750) }

        updateWaterLevel(0f)
    }

    private fun setupClickListeners() {
        btnAddGlass.setOnClickListener { addWater(glassSize) }
        btnAdd.setOnClickListener { addWater(glassSize) }
        btnSubtract.setOnClickListener { removeWater(glassSize) }
        findViewById<ImageButton>(R.id.btn_settings).setOnClickListener { showSettingsDialog() }
    }

    private fun addWater(amount: Int) {
        if (isGoalAchieved) return // Prevent adding more water after goal is achieved

        val previousIntake = currentIntake
        currentIntake += amount
        if (currentIntake > dailyGoal) currentIntake = dailyGoal

        currentGlasses = (currentIntake / glassSize).coerceAtMost(goalGlasses)

        animateWaterChange(previousIntake, currentIntake)
        updateUI()

        // Check if goal is reached
        if (currentIntake >= dailyGoal && !isGoalAchieved) {
            isGoalAchieved = true
            showTrophyCelebration()
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
        updateUI()
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
        tvStreak.text = "8 days"

        val progress = currentIntake.toFloat() / dailyGoal
        updateWaterLevel(progress)
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
            .create()
        dialog.show()
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
        updateUI()
    }
}