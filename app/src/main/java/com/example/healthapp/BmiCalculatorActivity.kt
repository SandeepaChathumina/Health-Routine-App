package com.example.healthapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import java.text.DecimalFormat

class BmiCalculatorActivity : AppCompatActivity() {

    private lateinit var etHeight: TextInputEditText
    private lateinit var etWeight: TextInputEditText
    private lateinit var btnCalculate: MaterialButton
    private lateinit var tvBmiResult: TextView
    private lateinit var tvBmiCategory: TextView
    private lateinit var backButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bmi_calculator)

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        etHeight = findViewById(R.id.et_height)
        etWeight = findViewById(R.id.et_weight)
        btnCalculate = findViewById(R.id.btn_calculate)
        tvBmiResult = findViewById(R.id.tv_bmi_result)
        tvBmiCategory = findViewById(R.id.tv_bmi_category)
        backButton = findViewById(R.id.btn_back)
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish()
        }

        btnCalculate.setOnClickListener {
            calculateBMI()
        }

        // Add click listeners for BMI categories for educational purposes
        setupCategoryClickListeners()
    }

    private fun calculateBMI() {
        val heightStr = etHeight.text.toString().trim()
        val weightStr = etWeight.text.toString().trim()

        // Validate inputs
        if (TextUtils.isEmpty(heightStr)) {
            showError("Please enter your height")
            return
        }

        if (TextUtils.isEmpty(weightStr)) {
            showError("Please enter your weight")
            return
        }

        val height = heightStr.toDoubleOrNull()
        val weight = weightStr.toDoubleOrNull()

        if (height == null || height <= 0) {
            showError("Please enter a valid height")
            return
        }

        if (weight == null || weight <= 0) {
            showError("Please enter a valid weight")
            return
        }

        if (height > 300) {
            showError("Please enter height in centimeters (max 300cm)")
            return
        }

        if (weight > 500) {
            showError("Please enter a reasonable weight (max 500kg)")
            return
        }

        // Calculate BMI: weight (kg) / (height (m) * height (m))
        val heightInMeters = height / 100
        val bmi = weight / (heightInMeters * heightInMeters)

        // Format BMI to 1 decimal place
        val decimalFormat = DecimalFormat("#.#")
        val formattedBmi = decimalFormat.format(bmi)

        // Display result
        displayBMIResult(bmi, formattedBmi)
    }

    private fun displayBMIResult(bmi: Double, formattedBmi: String) {
        tvBmiResult.text = formattedBmi
        tvBmiResult.setTextColor(getBmiColor(bmi))

        val category = getBmiCategory(bmi)
        tvBmiCategory.text = category
        tvBmiCategory.setTextColor(getBmiColor(bmi))

        // Show success message
        showSuccess("BMI calculated successfully!")
    }

    private fun getBmiCategory(bmi: Double): String {
        return when {
            bmi < 18.5 -> {
                "Underweight - You may need to gain some weight"
            }
            bmi < 25 -> {
                "Normal weight - Keep up the good work!"
            }
            bmi < 30 -> {
                "Overweight - Consider some lifestyle changes"
            }
            else -> {
                "Obese - It's recommended to consult a doctor"
            }
        }
    }

    private fun getBmiColor(bmi: Double): Int {
        return when {
            bmi < 18.5 -> Color.parseColor("#60A5FA") // Blue for underweight
            bmi < 25 -> Color.parseColor("#10B981")   // Green for normal
            bmi < 30 -> Color.parseColor("#F59E0B")   // Orange for overweight
            else -> Color.parseColor("#EF4444")       // Red for obese
        }
    }

    private fun setupCategoryClickListeners() {
        // Set up click listeners for BMI categories (educational)
        val underweightCategory = findViewById<View>(R.id.underweight_category)
        val normalCategory = findViewById<View>(R.id.normal_category)
        val overweightCategory = findViewById<View>(R.id.overweight_category)
        val obeseCategory = findViewById<View>(R.id.obese_category)

        underweightCategory?.setOnClickListener {
            showCategoryInfo("Underweight (BMI < 18.5)",
                "• May indicate nutritional deficiencies\n" +
                        "• Can lead to weakened immune system\n" +
                        "• May cause fatigue and weakness\n" +
                        "• Consult a healthcare provider for guidance")
        }

        normalCategory?.setOnClickListener {
            showCategoryInfo("Normal Weight (BMI 18.5 - 24.9)",
                "• Associated with lowest health risks\n" +
                        "• Maintain through balanced diet\n" +
                        "• Regular physical activity recommended\n" +
                        "• Continue healthy lifestyle habits")
        }

        overweightCategory?.setOnClickListener {
            showCategoryInfo("Overweight (BMI 25 - 29.9)",
                "• Increased risk of health problems\n" +
                        "• May lead to high blood pressure\n" +
                        "• Consider dietary changes\n" +
                        "• Regular exercise recommended")
        }

        obeseCategory?.setOnClickListener {
            showCategoryInfo("Obese (BMI ≥ 30)",
                "• Significantly increased health risks\n" +
                        "• Higher risk of heart disease, diabetes\n" +
                        "• Professional medical advice recommended\n" +
                        "• Lifestyle changes and support may help")
        }
    }

    private fun showCategoryInfo(title: String, message: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showError(message: String) {
        Snackbar.make(btnCalculate, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(Color.parseColor("#EF4444"))
            .setTextColor(Color.WHITE)
            .show()
    }

    private fun showSuccess(message: String) {
        Snackbar.make(btnCalculate, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(Color.parseColor("#10B981"))
            .setTextColor(Color.WHITE)
            .show()
    }

    // Optional: Clear inputs when returning to the activity
    override fun onResume() {
        super.onResume()

    }
}