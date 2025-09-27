package com.example.healthapp

import android.annotation.SuppressLint
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView

class Home : AppCompatActivity() {
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        setContentView(R.menu.bottom_navigation_menu)

        //habit button

        val habit_btn: Button = findViewById(R.id.nav_habits)
        habit_btn.setOnClickListener {
            // ✅ Navigate to OnBoarding3 instead of OnBoarding2
            val intent = Intent(this, Habits::class.java)
            startActivity(intent)
        }


    }
}
