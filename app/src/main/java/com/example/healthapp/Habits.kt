package com.example.healthapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

class Habits : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var habitAdapter: HabitAdapter
    private val habits = mutableListOf<Habit>()

    private val addHabitLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val habit = result.data?.getSerializableExtra("new_habit") as? Habit
            habit?.let {
                habits.add(it)
                updateHabitsList()
                updateStats()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_habits)

        setupBottomNavigation()
        setupRecyclerView()
        setupClickListeners()
        initializeSampleHabits()
        updateStats()
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.rv_habits)
        recyclerView.layoutManager = LinearLayoutManager(this)

        habitAdapter = HabitAdapter(
            habits = habits,
            onHabitChecked = { habit, isChecked ->
                // Prevent unchecking completed habits and only allow checking once per day
                if (isChecked && !habit.isCompleted) {
                    val updatedHabit = habit.copy(
                        isCompleted = true,
                        completedCount = habit.completedCount + 1,
                        currentStreak = habit.currentStreak + 1 // Increase streak when completed
                    )
                    val index = habits.indexOfFirst { it.id == habit.id }
                    if (index != -1) {
                        habits[index] = updatedHabit
                        updateHabitsList()
                        updateStats()
                    }
                }
                // If user tries to uncheck, we ignore it and keep it checked
            },
            onMoreClicked = { habit ->
                // Handle more options (edit, delete, etc.)
                showHabitOptions(habit)
            }
        )

        recyclerView.adapter = habitAdapter
    }

    private fun setupClickListeners() {
        val btnAddHabit = findViewById<ImageButton>(R.id.btn_add_habit)
        btnAddHabit.setOnClickListener {
            val intent = Intent(this, AddHabitActivity::class.java)
            addHabitLauncher.launch(intent)
        }

        // Setup category filter buttons
        setupCategoryFilters()

        // Setup sort button
        val tvSort = findViewById<TextView>(R.id.tv_sort)
        tvSort.setOnClickListener {
            // Toggle sort order
            toggleSortOrder()
        }
    }

    private fun setupCategoryFilters() {
        val btnAll = findViewById<MaterialButton>(R.id.btn_all)
        val btnHealth = findViewById<MaterialButton>(R.id.btn_health)
        val btnFitness = findViewById<MaterialButton>(R.id.btn_fitness)
        val btnMindfulness = findViewById<MaterialButton>(R.id.btn_mindfulness)
        val btnWork = findViewById<MaterialButton>(R.id.btn_work)

        val filterButtons = listOf(btnAll, btnHealth, btnFitness, btnMindfulness, btnWork)

        filterButtons.forEach { button ->
            button.setOnClickListener {
                // Reset all buttons to inactive state
                filterButtons.forEach { btn ->
                    btn.setBackgroundColor(getColor(android.R.color.transparent))
                    btn.setTextColor(getColor(R.color.slate_500))
                    btn.strokeColor = getColorStateList(R.color.slate_200)
                }

                // Set active state for clicked button
                button.setBackgroundColor(getColor(R.color.blue_600))
                button.setTextColor(getColor(android.R.color.white))
                button.strokeColor = getColorStateList(R.color.blue_600)

                // Filter habits
                when (button) {
                    btnAll -> updateHabitsList()
                    btnHealth -> filterHabitsByCategory("Health")
                    btnFitness -> filterHabitsByCategory("Fitness")
                    btnMindfulness -> filterHabitsByCategory("Mindfulness")
                    btnWork -> filterHabitsByCategory("Work")
                }
            }
        }
    }

    private fun filterHabitsByCategory(category: String) {
        val filteredHabits = if (category == "All") {
            habits
        } else {
            habits.filter { it.category == category }
        }
        habitAdapter.updateHabits(filteredHabits)
    }

    private fun toggleSortOrder() {
        // Sort by recent (newest first) or by name
        habits.sortByDescending { it.id }
        updateHabitsList()

        val tvSort = findViewById<TextView>(R.id.tv_sort)
        tvSort.text = "Sort by Name"
    }

    private fun initializeSampleHabits() {
        habits.addAll(
            listOf(
                Habit(
                    id = 1,
                    title = "Drink 8 glasses of water",
                    category = "Health",
                    currentStreak = 12,
                    targetCount = 8,
                    completedCount = 8,
                    isCompleted = true
                ),
                Habit(
                    id = 2,
                    title = "10 minute meditation",
                    category = "Mindfulness",
                    currentStreak = 8,
                    targetCount = 1,
                    completedCount = 0,
                    isCompleted = false
                )
            )
        )
        updateHabitsList()
    }

    private fun updateHabitsList() {
        habitAdapter.updateHabits(habits)
    }

    private fun updateStats() {
        val completedToday = habits.count { it.isCompleted }
        val bestStreak = habits.maxOfOrNull { it.currentStreak } ?: 0
        val successRate = if (habits.isNotEmpty()) {
            val totalCompletions = habits.sumOf { it.completedCount }
            val totalTargets = habits.sumOf { it.targetCount }
            if (totalTargets > 0) (totalCompletions.toFloat() / totalTargets.toFloat() * 100).toInt() else 0
        } else {
            0
        }

        findViewById<TextView>(R.id.tv_completed_today).text = "$completedToday"
        findViewById<TextView>(R.id.tv_best_streak).text = "$bestStreak"
        findViewById<TextView>(R.id.tv_success_rate).text = "$successRate%"
    }

    private fun showHabitOptions(habit: Habit) {
        // Implement dialog for habit options (edit, delete, etc.)
        // You can use AlertDialog or BottomSheetDialog
        // For now, just show a simple delete option
        android.app.AlertDialog.Builder(this)
            .setTitle("Habit Options")
            .setMessage("What would you like to do with '${habit.title}'?")
            .setPositiveButton("Delete") { dialog, which ->
                habits.remove(habit)
                updateHabitsList()
                updateStats()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_habits

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_habits -> {
                    // Already on habits, do nothing
                    true
                }
                R.id.nav_home -> {
                    val intent = Intent(this, Home::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_mood -> {
                    val intent = Intent(this, Mood::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_hydration -> {
                    val intent = Intent(this, Hydration::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, Profile::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}