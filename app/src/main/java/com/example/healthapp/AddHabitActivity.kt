package com.example.healthapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class AddHabitActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_habit)

        val etTitle = findViewById<TextInputEditText>(R.id.et_habit_title)
        val etTargetCount = findViewById<TextInputEditText>(R.id.et_target_count)
        val btnSave = findViewById<MaterialButton>(R.id.btn_save_habit)

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val targetCount = etTargetCount.text.toString().toIntOrNull() ?: 1
            val category = getSelectedCategory()

            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter habit title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val habit = Habit(
                id = System.currentTimeMillis().toInt(),
                title = title,
                category = category,
                currentStreak = 0,
                targetCount = targetCount,
                completedCount = 0,
                isCompleted = false
            )

            val resultIntent = Intent()
            resultIntent.putExtra("new_habit", habit)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun getSelectedCategory(): String {
        val radioGroup = findViewById<RadioGroup>(R.id.rg_category)
        return when (radioGroup.checkedRadioButtonId) {
            R.id.rb_health -> "Health"
            R.id.rb_fitness -> "Fitness"
            R.id.rb_mindfulness -> "Mindfulness"
            R.id.rb_work -> "Work"
            else -> "Health"
        }
    }
}