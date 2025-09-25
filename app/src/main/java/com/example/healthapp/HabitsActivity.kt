package com.example.healthapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HabitsActivity : AppCompatActivity() {
    private lateinit var habitsRecyclerView: RecyclerView
    private lateinit var habitsAdapter: HabitsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_habits)

        setupToolbar()
        setupRecyclerView()
        loadHabits()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        findViewById<FloatingActionButton>(R.id.fab_add).setOnClickListener {
            // Open add habit activity
        }
    }

    private fun setupRecyclerView() {
        habitsRecyclerView = findViewById(R.id.rv_habits)
        habitsAdapter = HabitsAdapter()
        habitsRecyclerView.layoutManager = LinearLayoutManager(this)
        habitsRecyclerView.adapter = habitsAdapter
    }

    private fun loadHabits() {
        val habits = listOf(
            Habit("Drink 8 glasses of water", "Health", 8, 8, true, 12),
            Habit("10-minute meditation", "Mindfulness", 1, 1, true, 8),
            Habit("Read for 30 minutes", "Learning", 0, 1, false, 5),
            Habit("5000 steps", "Fitness", 3200, 5000, false, 15),
            Habit("Take vitamins", "Health", 1, 1, true, 20)
        )
        habitsAdapter.submitList(habits)
    }

    data class Habit(
        val title: String,
        val category: String,
        val progress: Int,
        val target: Int,
        val completed: Boolean,
        val streak: Int
    )

    class HabitsAdapter : ListAdapter<Habit, HabitsAdapter.ViewHolder>(DiffCallback()) {
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(R.id.tv_title)
            val category: TextView = view.findViewById(R.id.tv_category)
            val progress: ProgressBar = view.findViewById(R.id.progress_bar)
            val progressText: TextView = view.findViewById(R.id.tv_progress)
            val streak: TextView = view.findViewById(R.id.tv_streak)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_habit, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val habit = getItem(position)
            holder.title.text = habit.title
            holder.category.text = habit.category
            holder.progress.max = habit.target
            holder.progress.progress = habit.progress
            holder.progressText.text = "${habit.progress}/${habit.target}"
            holder.streak.text = "${habit.streak} days"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Habit>() {
        override fun areItemsTheSame(oldItem: Habit, newItem: Habit): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: Habit, newItem: Habit): Boolean {
            return oldItem == newItem
        }
    }
}