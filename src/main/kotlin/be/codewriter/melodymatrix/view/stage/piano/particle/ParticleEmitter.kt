package be.codewriter.melodymatrix.view.stage.piano.particle

import javafx.scene.paint.Color
import kotlin.random.Random

class ParticleEmitter(
    var x: Double,
    var y: Double,
    var emissionRate: Double = 50.0, // particles per second
    var startColor: Color = Color.ORANGE,
    var endColor: Color = Color.RED,
    var minLifespan: Double = 0.5,
    var maxLifespan: Double = 1.5,
    var minVelocityX: Double = -20.0,
    var maxVelocityX: Double = 20.0,
    var minVelocityY: Double = -80.0,
    var maxVelocityY: Double = -120.0,
    var startSize: Double = 8.0,
    var endSize: Double = 2.0,
    var enabled: Boolean = true,
    var opacity: Double = 1.0 // For fade in/out effects
) {
    private val particles = mutableListOf<Particle>()
    private var accumulator = 0.0

    fun update(deltaTime: Double) {
        if (!enabled) return

        // Emit new particles
        accumulator += deltaTime
        val particlesToEmit = (accumulator * emissionRate).toInt()
        accumulator -= particlesToEmit / emissionRate

        repeat(particlesToEmit) {
            emitParticle()
        }

        // Update existing particles
        particles.forEach { it.update(deltaTime) }

        // Remove dead particles
        particles.removeIf { !it.isAlive }
    }

    private fun emitParticle() {
        val lifespan = Random.nextDouble(minLifespan, maxLifespan)
        val velocityX = Random.nextDouble(minVelocityX, maxVelocityX)
        val velocityY = Random.nextDouble(minVelocityY, maxVelocityY)

        // Add slight random offset to spawn position
        val spawnX = x + Random.nextDouble(-5.0, 5.0)
        val spawnY = y + Random.nextDouble(-5.0, 5.0)

        particles.add(
            Particle(
                x = spawnX,
                y = spawnY,
                velocityX = velocityX,
                velocityY = velocityY,
                lifespan = lifespan,
                startSize = startSize,
                endSize = endSize,
                startColor = startColor,
                endColor = endColor
            )
        )
    }

    fun getParticles(): List<Particle> = particles

    fun clear() {
        particles.clear()
    }
}