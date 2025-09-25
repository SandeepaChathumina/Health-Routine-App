package com.example.healthapp

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: OnboardingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_container)

        setupViewPager()
    }

    private fun setupViewPager() {
        adapter = OnboardingAdapter(this)
        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = adapter

        // Add page change listener if you want indicators
    }

    fun goToNextPage() {
        if (viewPager.currentItem < adapter.itemCount - 1) {
            viewPager.currentItem = viewPager.currentItem + 1
        } else {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    fun goToPreviousPage() {
        if (viewPager.currentItem > 0) {
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }
}