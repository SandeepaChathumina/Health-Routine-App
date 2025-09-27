package com.example.healthapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Onboarding3 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_onboarding3)

        //previous button

        val previousButton: Button = findViewById(R.id.btn_previous)
        previousButton.setOnClickListener {
            // ✅ Navigate to OnBoarding3 instead of OnBoarding2
            val intent = Intent(this, Onboarding2::class.java)
            startActivity(intent)
        }

        //get started button

        val nextButton: Button = findViewById(R.id.btn_get_started)
        nextButton.setOnClickListener {
            // ✅ Navigate to OnBoarding3 instead of OnBoarding2
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }

    }
}