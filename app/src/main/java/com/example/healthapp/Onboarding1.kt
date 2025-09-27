package com.example.healthapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Onboarding1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_onboarding1)

        val startedButton: Button = findViewById(R.id.btn_next)

        startedButton.setOnClickListener {
            // ✅ Navigate to OnBoarding3 instead of OnBoarding2
            val intent = Intent(this, Onboarding2::class.java)
            startActivity(intent)
        }
    }
}