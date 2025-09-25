package com.example.healthapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Splash_Screen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)

        // Delay for 2 seconds then go to main activity
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, Onboarding1::class.java)
            startActivity(intent)
            finish()
        }, 2000)
    }
}