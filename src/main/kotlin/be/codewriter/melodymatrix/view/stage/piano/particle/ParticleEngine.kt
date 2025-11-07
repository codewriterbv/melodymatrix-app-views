package be.codewriter.melodymatrix.view.stage.piano.particle

import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.beans.value.WritableValue
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.util.Duration
import kotlin.random.Random

class ParticleEngine {

    private var emitters: List<ParticleEmitter> = mutableListOf()

    public fun add(emitter: ParticleEmitter) {


        // Position the emitter
        //val randomYOffset = Random.nextDouble(-10.0, 10.0)
        //emitter?.x = width / 2.0 // Center horizontally
        //emitter?.y = height - 50.0 + randomYOffset

        // Start fire effect with a smooth fade-in
        Timeline(
            KeyFrame(
                Duration.seconds(1.5),
                KeyValue(object : WritableValue<Double> {
                    override fun getValue() = emitter.opacity
                    override fun setValue(value: Double) {
                        emitter.opacity = value
                    }
                }, 1.0)
            )
        ).play()

        // Ensure updateFireState is called every 1 second to adjust fire properties
        startFireStateUpdates()

        // Add to list
        emitters.
    }

     fun update(deltaTime: Double) {
        if (emitters.isEmpty()) {
            return
        }
        emitters.forEach { emitter ->
            emitter.apply {
                // Add some variation to fire intensity
                val intensity = Random.nextDouble(0.8, 1.2)
                emissionRate = 80.0 * intensity
                minVelocityY = -100.0 * intensity
                maxVelocityY = -60.0 * intensity

                // Update colors from config
                startColor = config.aboveKeyColorStart.value
                endColor = config.aboveKeyColorEnd.value
            }
        }
    }

    fun render(ctx : GraphicsContext) {
        if (emitters.isEmpty()) {
            return
        }
        emitters.forEach { emitter ->
            val particles = emitter.getParticles()

            particles.forEach { particle ->
                val color = particle.getColor()
                val size = particle.getSize()
                val opacity = particle.getOpacity() * emitter.opacity

                // Apply opacity to the color
                ctx.fill = Color(
                    color.red,
                    color.green,
                    color.blue,
                    color.opacity * opacity
                )

                // Draw particle as a circle
                ctx.fillOval(
                    particle.x - size / 2,
                    particle.y - size / 2,
                    size,
                    size
                )
            }
        }
    }
}