package com.example.healthapp

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class CreateProfile : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_profile)

        sharedPreferences = getSharedPreferences("user_profile", MODE_PRIVATE)

        val etName = findViewById<TextInputEditText>(R.id.et_name)
        val etAge = findViewById<TextInputEditText>(R.id.et_age)
        val etGender = findViewById<AutoCompleteTextView>(R.id.et_gender)
        val btnCreateProfile = findViewById<MaterialButton>(R.id.btn_create_profile)

        // Setup gender dropdown
        val genderOptions = arrayOf("Male", "Female", "Other", "Prefer not to say")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genderOptions)
        etGender.setAdapter(genderAdapter)
        
        // Make the dropdown show when clicked
        etGender.setOnClickListener {
            etGender.showDropDown()
        }
        
        // Prevent keyboard from showing
        etGender.keyListener = null

        btnCreateProfile.setOnClickListener {
            val name = etName.text.toString().trim()
            val ageText = etAge.text.toString().trim()
            val gender = etGender.text.toString().trim()

            // Validation
            if (name.isEmpty()) {
                etName.error = "Please enter your name"
                return@setOnClickListener
            }

            if (ageText.isEmpty()) {
                etAge.error = "Please enter your age"
                return@setOnClickListener
            }

            val age = ageText.toIntOrNull()
            if (age == null || age < 1 || age > 120) {
                etAge.error = "Please enter a valid age (1-120)"
                return@setOnClickListener
            }

            if (gender.isEmpty()) {
                etGender.error = "Please select your gender"
                return@setOnClickListener
            }

            // Save profile data
            saveProfileData(name, age, gender)
            
            // Mark profile as complete using NavigationManager
            val navigationManager = NavigationManager(this)
            navigationManager.markProfileComplete()

            // Navigate to home with clear task stack
            val intent = Intent(this, Home::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun saveProfileData(name: String, age: Int, gender: String) {
        val editor = sharedPreferences.edit()
        editor.putString("user_name", name)
        editor.putInt("user_age", age)
        editor.putString("user_gender", gender)
        editor.putBoolean("profile_created", true)
        editor.apply()
    }
}