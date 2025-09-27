package com.example.healthapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.view.animation.AnimationUtils
import android.widget.ImageView // Add this import

class Splash_Screen : AppCompatActivity() {

    private lateinit var heartImageView: ImageView // Declare here
    private val SPLASH_DELAY: Long = 3000 // 3 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)

        // Initialize the ImageView
        heartImageView = findViewById(R.id.heartImageView)

        startHeartAnimation()
        navigateToMainActivity()
    }

    private fun startHeartAnimation(){
        val heartBeatAnim = AnimationUtils.loadAnimation(this, R.anim.heart_beat)

        Handler(Looper.getMainLooper()).postDelayed({
            heartImageView.startAnimation(heartBeatAnim)
        }, 500)
    }

    private fun navigateToMainActivity(){
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, Onboarding1::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, SPLASH_DELAY)
    }

    override fun onPause() {
        super.onPause()
        heartImageView.clearAnimation()
    }
}