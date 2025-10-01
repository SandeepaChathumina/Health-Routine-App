package com.example.healthapp

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class CreateProfile : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_profile)

        sharedPreferences = getSharedPreferences("user_profile", MODE_PRIVATE)

        val etFullName = findViewById<TextInputEditText>(R.id.et_full_name)
        val etEmail = findViewById<TextInputEditText>(R.id.et_email)
        val btnCreateProfile = findViewById<MaterialButton>(R.id.btn_create_profile)

        btnCreateProfile.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()

            if (fullName.isEmpty()) {
                etFullName.error = "Please enter your full name"
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                etEmail.error = "Please enter your email"
                return@setOnClickListener
            }

            // Save profile data
            saveProfileData(fullName, email)

            // Navigate to main activity (or home)
            val intent = Intent(this, Home::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun saveProfileData(fullName: String, email: String) {
        val editor = sharedPreferences.edit()
        editor.putString("user_name", fullName)
        editor.putString("user_email", email)
        editor.putBoolean("profile_created", true)
        editor.apply()
    }
}