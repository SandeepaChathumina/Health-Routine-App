package com.example.healthapp

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText

class CreateProfile : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private var selectedGender: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_profile)

        sharedPreferences = getSharedPreferences("user_profile", MODE_PRIVATE)

        val etName = findViewById<TextInputEditText>(R.id.et_name)
        val etAge = findViewById<TextInputEditText>(R.id.et_age)
        val cardMale = findViewById<MaterialCardView>(R.id.card_male)
        val cardFemale = findViewById<MaterialCardView>(R.id.card_female)
        val btnCreateProfile = findViewById<MaterialButton>(R.id.btn_create_profile)

        // Setup gender selection cards
        cardMale.setOnClickListener {
            selectGender("Male", cardMale, cardFemale)
        }

        cardFemale.setOnClickListener {
            selectGender("Female", cardFemale, cardMale)
        }

        btnCreateProfile.setOnClickListener {
            val name = etName.text.toString().trim()
            val ageText = etAge.text.toString().trim()

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

            if (selectedGender.isEmpty()) {
                Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save profile data
            saveProfileData(name, age, selectedGender)
            
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

    private fun selectGender(gender: String, selectedCard: MaterialCardView, unselectedCard: MaterialCardView) {
        selectedGender = gender
        
        // Update visual state
        selectedCard.setCardBackgroundColor(getColor(R.color.card_background_light))
        selectedCard.strokeWidth = 3
        selectedCard.strokeColor = getColor(R.color.primary_blue)
        
        unselectedCard.setCardBackgroundColor(getColor(R.color.card_background_light))
        unselectedCard.strokeWidth = 0
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