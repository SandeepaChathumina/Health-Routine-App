package com.example.healthapp

import android.content.Context
import android.content.SharedPreferences

class NavigationManager(private val context: Context) {
    
    private val profilePrefs: SharedPreferences = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)
    
    /**
     * identify where the user should be navigated after splash screen
     * @return Class of the activity to navigate to
     */
    fun getNextDestination(): Class<*> {
        return if (isUserProfileComplete()) {
            // User has completed profile setup, go directly to home
            Home::class.java
        } else {
            // New user or data cleared, start onboarding flow
            Onboarding1::class.java
        }
    }
    
    /**
     * Checks if user has completed the profile creation process
     * @return true if profile is complete, false otherwise
     */
    private fun isUserProfileComplete(): Boolean {
        val profileCreated = profilePrefs.getBoolean("profile_created", false)
        val userName = profilePrefs.getString("user_name", null)
        val userAge = profilePrefs.getInt("user_age", -1)
        val userGender = profilePrefs.getString("user_gender", null)
        
        // Check if all required profile data exists
        return profileCreated && 
               !userName.isNullOrEmpty() && 
               userAge > 0 && 
               !userGender.isNullOrEmpty()
    }
    

    // Marks the profile as complete

    fun markProfileComplete() {
        profilePrefs.edit().putBoolean("profile_created", true).apply()
    }
    

    //  Clears all user data (useful for testing or logout)

    // Clear profile data
    fun clearAllUserData() {
        profilePrefs.edit().clear().apply()
        
        // Clear habits data
        val habitsPrefs = context.getSharedPreferences("HabitsPrefs", Context.MODE_PRIVATE)
        habitsPrefs.edit().clear().apply()
        
        // Clear hydration data
        val hydrationPrefs = context.getSharedPreferences("hydration_data", Context.MODE_PRIVATE)
        hydrationPrefs.edit().clear().apply()
        
        // Clear mood data
        val moodPrefs = context.getSharedPreferences("MoodPrefs", Context.MODE_PRIVATE)
        moodPrefs.edit().clear().apply()
    }
    
    /**
     * Checks if this is the first app launch
     * @return true if first launch, false otherwise
     */
    fun isFirstLaunch(): Boolean {
        val firstLaunch = profilePrefs.getBoolean("first_launch", true)
        if (firstLaunch) {
            // Mark as not first launch anymore
            profilePrefs.edit().putBoolean("first_launch", false).apply()
        }
        return firstLaunch
    }
    
    /**
     * Gets user profile completion status for debugging
     * @return String describing current profile status
     */
    fun getProfileStatus(): String {
        val profileCreated = profilePrefs.getBoolean("profile_created", false)
        val userName = profilePrefs.getString("user_name", "Not set")
        val userAge = profilePrefs.getInt("user_age", -1)
        val userGender = profilePrefs.getString("user_gender", "Not set")
        
        return """
            Profile Created: $profileCreated
            Name: $userName
            Age: ${if (userAge > 0) userAge else "Not set"}
            Gender: $userGender
            Complete: ${isUserProfileComplete()}
        """.trimIndent()
    }
}
