package com.example.healthapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.view.animation.AnimationUtils
import android.widget.ImageView

class Splash_Screen : AppCompatActivity() {

    private lateinit var heartImageView: ImageView //heart image
    private lateinit var navigationManager: NavigationManager
    private val SPLASH_DELAY: Long = 3000 // 3 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)

        // Initialize variables
        heartImageView = findViewById(R.id.heartImageView)
        navigationManager = NavigationManager(this)

        startHeartAnimation()
        navigateToNextScreen()
    }

    //Heart Animation
    private fun startHeartAnimation(){
        val heartBeatAnim = AnimationUtils.loadAnimation(this, R.anim.heart_beat)

        Handler(Looper.getMainLooper()).postDelayed({
            heartImageView.startAnimation(heartBeatAnim)
        }, 500)
    }

    //Next screen navigation
    private fun navigateToNextScreen(){
        Handler(Looper.getMainLooper()).postDelayed({
            // Determine next destination based on user profile status
            val nextDestination = navigationManager.getNextDestination()
            
            // Log for debugging
            Log.d("SplashScreen", "Profile Status: ${navigationManager.getProfileStatus()}")
            Log.d("SplashScreen", "Navigating to: ${nextDestination.simpleName}")
            
            val intent = Intent(this, nextDestination)
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