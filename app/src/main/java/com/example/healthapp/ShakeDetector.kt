package com.example.healthapp

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class ShakeDetector(
    private val onShakeDetected: (Int) -> Unit
) : SensorEventListener {

    private var lastUpdate = 0L
    private var lastShakeTime = 0L
    private var shakeCount = 0
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private var isShakeInProgress = false

    companion object {
        private const val SHAKE_THRESHOLD = 12f // Lower threshold for smooth, gentle shakes
        private const val SHAKE_TIMEOUT = 3000L // 3 seconds - longer timeout
        private const val UPDATE_INTERVAL = 100L // 100ms - faster updates for smooth detection
        private const val MIN_SHAKE_INTERVAL = 300L // Minimum 300ms between shakes
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val currentTime = System.currentTimeMillis()
            
            if (currentTime - lastUpdate > UPDATE_INTERVAL) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val deltaX = x - lastX
                val deltaY = y - lastY
                val deltaZ = z - lastZ

                val delta = sqrt((deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ).toDouble()).toFloat()

                // Smooth shake detection - no additional filtering
                if (delta > SHAKE_THRESHOLD) {
                    handleShake(currentTime)
                }

                lastX = x
                lastY = y
                lastZ = z
                lastUpdate = currentTime
            }
        }
    }

    private fun handleShake(currentTime: Long) {
        // Check minimum interval between shakes
        if (currentTime - lastShakeTime < MIN_SHAKE_INTERVAL) return
        
        // Prevent multiple rapid detections during the same shake
        if (isShakeInProgress) return
        
        isShakeInProgress = true
        lastShakeTime = currentTime
        
        // Always add 250ml regardless of shake count
        val waterAmount = 250
        
        // Reset shake in progress after a short delay
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            isShakeInProgress = false
        }, 200) // Short delay for smooth detection
        
        // Trigger callback with water amount
        onShakeDetected(waterAmount)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }
}
