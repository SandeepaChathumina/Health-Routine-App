package com.example.healthapp

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class WorkoutCompleteActivity : AppCompatActivity() {

    private lateinit var celebrationIcon: View
    private lateinit var pulsingCircle: View
    private lateinit var tvCongratulations: TextView
    private lateinit var tvMessage: TextView
    private lateinit var statsCard: View
    private lateinit var achievementBadge: View
    private lateinit var btnDone: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_workout_complete)

        initViews()
        setupClickListeners()

        // Start celebration sequence after a short delay
        Handler(Looper.getMainLooper()).postDelayed({
            startCelebrationSequence()
        }, 300)
    }

    private fun initViews() {
        celebrationIcon = findViewById(R.id.celebration_icon)
        pulsingCircle = findViewById(R.id.pulsing_circle)
        tvCongratulations = findViewById(R.id.tv_congratulations)
        tvMessage = findViewById(R.id.tv_message)
        statsCard = findViewById(R.id.stats_card)
        achievementBadge = findViewById(R.id.achievement_badge)
        btnDone = findViewById(R.id.btn_done)

        val backButton: ImageButton = findViewById(R.id.btn_back)
        backButton.setOnClickListener {
            navigateToDailyWorkout()
        }
    }

    private fun setupClickListeners() {
        val backButton: ImageButton = findViewById(R.id.btn_back)
        val doneButton: MaterialButton = findViewById(R.id.btn_done)

        backButton.setOnClickListener {
            navigateToDailyWorkout()
        }

        doneButton.setOnClickListener {
            navigateToDailyWorkout()
        }
    }

    private fun startCelebrationSequence() {
        // 1. Start pulsing circle animation
        startPulsingAnimation()

        // 2. Icon scale and fade in animation
        Handler(Looper.getMainLooper()).postDelayed({
            startIconAnimation()
        }, 500)

        // 3. Text animations
        Handler(Looper.getMainLooper()).postDelayed({
            startTextAnimations()
        }, 1000)

        // 4. Stats card animation
        Handler(Looper.getMainLooper()).postDelayed({
            startStatsCardAnimation()
        }, 1300)

        // 5. Achievement badge animation
        Handler(Looper.getMainLooper()).postDelayed({
            startAchievementBadgeAnimation()
        }, 1800)

        // 6. Button animation
        Handler(Looper.getMainLooper()).postDelayed({
            startButtonAnimation()
        }, 2200)
    }

    private fun startPulsingAnimation() {
        val scaleX = ObjectAnimator.ofFloat(pulsingCircle, "scaleX", 1f, 1.5f)
        val scaleY = ObjectAnimator.ofFloat(pulsingCircle, "scaleY", 1f, 1.5f)
        val alpha = ObjectAnimator.ofFloat(pulsingCircle, "alpha", 1f, 0f)

        val pulseAnimator = AnimatorSet()
        pulseAnimator.playTogether(scaleX, scaleY, alpha)
        pulseAnimator.duration = 2000
        pulseAnimator.interpolator = AccelerateDecelerateInterpolator()
        pulseAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                pulsingCircle.scaleX = 1f
                pulsingCircle.scaleY = 1f
                pulsingCircle.alpha = 1f
                pulseAnimator.start()
            }
        })
        pulseAnimator.start()
    }

    private fun startIconAnimation() {
        val scaleX = ObjectAnimator.ofFloat(celebrationIcon, "scaleX", 0.5f, 1.2f, 1f)
        val scaleY = ObjectAnimator.ofFloat(celebrationIcon, "scaleY", 0.5f, 1.2f, 1f)
        val alpha = ObjectAnimator.ofFloat(celebrationIcon, "alpha", 0f, 1f)
        val rotation = ObjectAnimator.ofFloat(celebrationIcon, "rotation", -30f, 10f, 0f)

        val iconAnimator = AnimatorSet()
        iconAnimator.playTogether(scaleX, scaleY, alpha, rotation)
        iconAnimator.duration = 800
        iconAnimator.interpolator = OvershootInterpolator()
        iconAnimator.start()
    }

    private fun startTextAnimations() {
        val congratsAlpha = ObjectAnimator.ofFloat(tvCongratulations, "alpha", 0f, 1f)
        val congratsTranslation = ObjectAnimator.ofFloat(tvCongratulations, "translationY", 50f, 0f)

        val messageAlpha = ObjectAnimator.ofFloat(tvMessage, "alpha", 0f, 1f)
        val messageTranslation = ObjectAnimator.ofFloat(tvMessage, "translationY", 50f, 0f)

        val textAnimator = AnimatorSet()
        textAnimator.playTogether(congratsAlpha, congratsTranslation, messageAlpha, messageTranslation)
        textAnimator.duration = 600
        textAnimator.interpolator = DecelerateInterpolator()
        textAnimator.start()
    }

    private fun startStatsCardAnimation() {
        val alpha = ObjectAnimator.ofFloat(statsCard, "alpha", 0f, 1f)
        val translation = ObjectAnimator.ofFloat(statsCard, "translationY", 30f, 0f)
        val scaleX = ObjectAnimator.ofFloat(statsCard, "scaleX", 0.9f, 1f)
        val scaleY = ObjectAnimator.ofFloat(statsCard, "scaleY", 0.9f, 1f)

        val cardAnimator = AnimatorSet()
        cardAnimator.playTogether(alpha, translation, scaleX, scaleY)
        cardAnimator.duration = 500
        cardAnimator.interpolator = OvershootInterpolator()
        cardAnimator.start()
    }

    private fun startAchievementBadgeAnimation() {
        achievementBadge.visibility = View.VISIBLE

        val alpha = ObjectAnimator.ofFloat(achievementBadge, "alpha", 0f, 1f)
        val scaleX = ObjectAnimator.ofFloat(achievementBadge, "scaleX", 0.8f, 1.1f, 1f)
        val scaleY = ObjectAnimator.ofFloat(achievementBadge, "scaleY", 0.8f, 1.1f, 1f)

        val badgeAnimator = AnimatorSet()
        badgeAnimator.playTogether(alpha, scaleX, scaleY)
        badgeAnimator.duration = 600
        badgeAnimator.interpolator = BounceInterpolator()
        badgeAnimator.start()
    }

    private fun startButtonAnimation() {
        val alpha = ObjectAnimator.ofFloat(btnDone, "alpha", 0f, 1f)
        val translation = ObjectAnimator.ofFloat(btnDone, "translationY", 30f, 0f)

        val buttonAnimator = AnimatorSet()
        buttonAnimator.playTogether(alpha, translation)
        buttonAnimator.duration = 500
        buttonAnimator.interpolator = DecelerateInterpolator()
        buttonAnimator.start()
    }

    private fun navigateToDailyWorkout() {
        val intent = Intent(this, DailyWorkoutActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}