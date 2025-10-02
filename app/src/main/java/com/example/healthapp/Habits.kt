package com.example.healthapp

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Habits : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var habitAdapter: HabitAdapter
    private lateinit var fireworksView: FireworksView
    private lateinit var emptyStateLayout: LinearLayout
    private val habits = mutableListOf<Habit>()

    // SharedPreferences for data persistence
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    private val HABITS_KEY = "saved_habits"

    private val addHabitLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val habit = result.data?.getSerializableExtra("new_habit") as? Habit
            habit?.let {
                habits.add(it)
                saveHabitsToStorage()
                updateHabitsList()
                updateStats()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_habits)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("HabitsPrefs", MODE_PRIVATE)

        fireworksView = findViewById(R.id.fireworksView)
        emptyStateLayout = findViewById(R.id.layout_empty_state)

        setupBottomNavigation()
        setupRecyclerView()
        setupClickListeners()
        loadHabitsFromStorage()
        updateStats()
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.rv_habits)
        recyclerView.layoutManager = LinearLayoutManager(this)

        habitAdapter = HabitAdapter(
            habits = habits,
            onHabitChecked = { habit, isChecked ->
                // Only process if user is checking the habit and it's not fully completed
                if (isChecked && !habit.isFullyCompleted()) {
                    val updatedHabit = habit.markOneCompletion()
                    val index = habits.indexOfFirst { it.id == habit.id }
                    if (index != -1) {
                        val wasFullyCompletedBefore = habit.isFullyCompleted()
                        habits[index] = updatedHabit

                        // Check if habit just became fully completed
                        val isNowFullyCompleted = updatedHabit.isFullyCompleted()

                        // Save to storage whenever habits change
                        saveHabitsToStorage()
                        updateHabitsList()
                        updateStats()

                        // Show fireworks if habit just became fully completed
                        if (isNowFullyCompleted && !wasFullyCompletedBefore) {
                            showFireworksForHabit(index)
                        }
                    }
                }
                // If user tries to uncheck or habit is already fully completed, ignore
            },
            onMoreClicked = { habit ->
                showHabitOptions(habit)
            }
        )

        recyclerView.adapter = habitAdapter
    }

    private fun saveHabitsToStorage() {
        try {
            val habitsJson = gson.toJson(habits)
            sharedPreferences.edit().putString(HABITS_KEY, habitsJson).apply()
            Log.d("Habits", "Habits saved: ${habits.size} habits")
        } catch (e: Exception) {
            Log.e("Habits", "Error saving habits: ${e.message}")
        }
    }

    private fun loadHabitsFromStorage() {
        try {
            val habitsJson = sharedPreferences.getString(HABITS_KEY, null)
            if (habitsJson != null) {
                val type = object : TypeToken<MutableList<Habit>>() {}.type
                val savedHabits = gson.fromJson<MutableList<Habit>>(habitsJson, type)
                habits.clear()
                habits.addAll(savedHabits)
                Log.d("Habits", "Habits loaded: ${habits.size} habits")
            } else {
                // No saved habits - start with empty list
                habits.clear()
            }
            updateHabitsList()
        } catch (e: Exception) {
            Log.e("Habits", "Error loading habits: ${e.message}")
            // If error loading, start with empty list
            habits.clear()
        }
    }

    private fun showFireworksForHabit(habitIndex: Int) {
        // Get the position of the habit item in the RecyclerView
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
        val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()

        if (habitIndex in firstVisiblePosition..lastVisiblePosition) {
            // Habit is visible on screen
            val habitView = layoutManager.findViewByPosition(habitIndex)
            habitView?.let { view ->
                // Calculate center position of the habit item
                val location = IntArray(2)
                view.getLocationOnScreen(location)
                val screenX = location[0] + view.width / 2f
                val screenY = location[1] + view.height / 2f

                // Show fireworks at the habit position
                fireworksView.startFireworks(screenX, screenY)

                // Also show a celebration message
                showCelebrationMessage(habits[habitIndex].title)
            }
        } else {
            // Habit is not visible, show fireworks from center of screen
            val centerX = recyclerView.width / 2f
            val centerY = recyclerView.height / 2f
            fireworksView.startFireworks(centerX, centerY)
            showCelebrationMessage(habits[habitIndex].title)
        }
    }

    private fun showCelebrationMessage(habitTitle: String) {
        val celebrationText = "🎉 Great job! '$habitTitle' completed! 🎉"
        Toast.makeText(this, celebrationText, Toast.LENGTH_SHORT).show()
    }

    private fun setupClickListeners() {
        val btnAddHabit = findViewById<ImageButton>(R.id.btn_add_habit)
        btnAddHabit.setOnClickListener {
            val intent = Intent(this, AddHabitActivity::class.java)
            addHabitLauncher.launch(intent)
        }
        
        // Empty state button
        val btnAddFirstHabit = findViewById<MaterialButton>(R.id.btn_add_first_habit)
        btnAddFirstHabit.setOnClickListener {
            val intent = Intent(this, AddHabitActivity::class.java)
            addHabitLauncher.launch(intent)
        }

        // Setup category filter buttons
        setupCategoryFilters()

        // Setup sort button
        val tvSort = findViewById<TextView>(R.id.tv_sort)
        tvSort.setOnClickListener {
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
                    btn.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
                    btn.setTextColor(ContextCompat.getColor(this, R.color.slate_500))
                }

                // Set active state for clicked button
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_600))
                button.setTextColor(ContextCompat.getColor(this, android.R.color.white))

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
        val filteredHabits = when (category) {
            "All" -> habits
            "Health" -> habits.filter { it.category == "Health" }
            "Fitness" -> habits.filter { it.category == "Fitness" }
            "Mindfulness" -> habits.filter { it.category == "Mindfulness" }
            "Work" -> habits.filter { it.category == "Work" }
            else -> habits
        }
        habitAdapter.updateHabits(filteredHabits)
    }

    private fun toggleSortOrder() {
        // Simple sort toggle - sort by recent (newest first)
        habits.sortByDescending { it.id }
        saveHabitsToStorage() // Save the sorted order
        updateHabitsList()

        val tvSort = findViewById<TextView>(R.id.tv_sort)
        tvSort.text = "Sort by Name"
    }


    private fun updateHabitsList() {
        habitAdapter.updateHabits(habits)
        updateEmptyState()
    }
    
    private fun updateEmptyState() {
        if (habits.isEmpty()) {
            // Show empty state, hide RecyclerView
            emptyStateLayout.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            // Show RecyclerView, hide empty state
            emptyStateLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun updateStats() {
        val completedToday = habits.count { it.isFullyCompleted() }
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
        val options = arrayOf("Delete", "Reset Today", "Cancel")

        AlertDialog.Builder(this)
            .setTitle("Habit Options")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> deleteHabit(habit) // Delete
                    1 -> resetHabit(habit)  // Reset Today
                    // 2 is Cancel - do nothing
                }
            }
            .show()
    }

    private fun deleteHabit(habit: Habit) {
        AlertDialog.Builder(this)
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete '${habit.title}'?")
            .setPositiveButton("Delete") { dialog, which ->
                habits.remove(habit)
                saveHabitsToStorage() // Save after deletion
                updateHabitsList()
                updateStats()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun resetHabit(habit: Habit) {
        val index = habits.indexOfFirst { it.id == habit.id }
        if (index != -1) {
            val resetHabit = habit.copy(
                completedCount = 0,
                isCompleted = false
            )
            habits[index] = resetHabit
            saveHabitsToStorage() // Save after reset
            updateHabitsList()
            updateStats()
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_habits

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_habits -> true
                R.id.nav_home -> {
                    startActivity(Intent(this, Home::class.java))
                    finish()
                    true
                }
                R.id.nav_mood -> {
                    startActivity(Intent(this, Mood::class.java))
                    finish()
                    true
                }
                R.id.nav_hydration -> {
                    startActivity(Intent(this, Hydration::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, Profile::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Save habits when the activity goes to background
        saveHabitsToStorage()
    }

    override fun onDestroy() {
        super.onDestroy()
        fireworksView.cleanup()
    }
}