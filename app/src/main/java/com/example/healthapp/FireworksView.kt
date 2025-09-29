package com.example.healthapp

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import kotlin.random.Random

class FireworksView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val particles = mutableListOf<Particle>()
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private var animator: ValueAnimator? = null

    data class Particle(
        var x: Float,
        var y: Float,
        var radius: Float,
        var color: Int,
        var velocityX: Float,
        var velocityY: Float,
        var alpha: Int = 255,
        var life: Float = 1f
    )

    fun startFireworks(centerX: Float, centerY: Float) {
        particles.clear()

        // Create multiple particles for fireworks effect
        val colors = intArrayOf(
            Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN,
            Color.MAGENTA, Color.BLUE, Color.WHITE
        )

        for (i in 0..50) {
            val angle = Random.nextDouble(0.0, 2 * Math.PI).toFloat()
            val speed = Random.nextFloat() * 8f + 4f
            val radius = Random.nextFloat() * 8f + 4f

            particles.add(
                Particle(
                    x = centerX,
                    y = centerY,
                    radius = radius,
                    color = colors[Random.nextInt(colors.size)],
                    velocityX = (Math.cos(angle.toDouble()) * speed).toFloat(),
                    velocityY = (Math.sin(angle.toDouble()) * speed).toFloat(),
                    alpha = 255,
                    life = 1f
                )
            )
        }

        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1500L
            interpolator = AccelerateInterpolator()
            addUpdateListener { animation ->
                updateParticles(animation.animatedFraction)
                invalidate()

                if (animation.animatedFraction >= 1f) {
                    particles.clear()
                    visibility = View.GONE
                }
            }
            start()
        }

        visibility = View.VISIBLE
    }

    private fun updateParticles(progress: Float) {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val particle = iterator.next()

            // Update position
            particle.x += particle.velocityX
            particle.y += particle.velocityY

            // Apply gravity
            particle.velocityY += 0.2f

            // Fade out
            particle.life = 1f - progress
            particle.alpha = (255 * particle.life).toInt()

            // Remove dead particles
            if (particle.life <= 0f || particle.alpha <= 0) {
                iterator.remove()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (particle in particles) {
            if (particle.alpha > 0) {
                paint.color = particle.color
                paint.alpha = particle.alpha
                canvas.drawCircle(particle.x, particle.y, particle.radius, paint)
            }
        }
    }

    fun cleanup() {
        animator?.cancel()
        particles.clear()
        visibility = View.GONE
    }
}