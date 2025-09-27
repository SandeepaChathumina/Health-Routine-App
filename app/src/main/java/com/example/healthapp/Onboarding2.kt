package com.example.healthapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Onboarding2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_onboarding2)

        //previous button

        val previousButton: Button = findViewById(R.id.btn_previous)
        previousButton.setOnClickListener {
            // ✅ Navigate to OnBoarding3 instead of OnBoarding2
            val intent = Intent(this, Onboarding1::class.java)
            startActivity(intent)
        }

        //next button

        val nextButton: Button = findViewById(R.id.btn_next)
        nextButton.setOnClickListener {
            // ✅ Navigate to OnBoarding3 instead of OnBoarding2
            val intent = Intent(this, Onboarding3::class.java)
            startActivity(intent)
        }

    }
}