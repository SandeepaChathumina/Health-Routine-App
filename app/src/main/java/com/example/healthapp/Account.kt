package com.example.healthapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Account : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_account)

        setupToolbar()
        loadUserData()
        setupSaveButton()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun loadUserData() {
        val prefs = getSharedPreferences("user_data", MODE_PRIVATE)

        findViewById<EditText>(R.id.et_full_name).setText(prefs.getString("user_name", "Sarah Johnson"))
        findViewById<EditText>(R.id.et_email).setText(prefs.getString("user_email", "sarah.johnson@email.com"))
        findViewById<EditText>(R.id.et_phone).setText(prefs.getString("user_phone", "+1 (555) 123-4567"))
        findViewById<EditText>(R.id.et_water_goal).setText(prefs.getInt("water_goal", 2500).toString())
        findViewById<Switch>(R.id.switch_notifications).isChecked = prefs.getBoolean("notifications", true)
    }

    private fun setupSaveButton() {
        findViewById<Button>(R.id.btn_save_changes).setOnClickListener {
            saveUserData()
            Toast.makeText(this, "Changes saved successfully", Toast.LENGTH_SHORT).show()
            onBackPressed()
        }

        findViewById<ImageButton>(R.id.btn_change_photo).setOnClickListener {
            // Open image picker for profile picture
        }
    }

    private fun saveUserData() {
        val prefs = getSharedPreferences("user_data", MODE_PRIVATE)
        val editor = prefs.edit()

        editor.putString("user_name", findViewById<EditText>(R.id.et_full_name).text.toString())
        editor.putString("user_email", findViewById<EditText>(R.id.et_email).text.toString())
        editor.putString("user_phone", findViewById<EditText>(R.id.et_phone).text.toString())
        editor.putInt("water_goal", findViewById<EditText>(R.id.et_water_goal).text.toString().toIntOrNull() ?: 2500)
        editor.putBoolean("notifications", findViewById<Switch>(R.id.switch_notifications).isChecked)

        editor.apply()
    }
}