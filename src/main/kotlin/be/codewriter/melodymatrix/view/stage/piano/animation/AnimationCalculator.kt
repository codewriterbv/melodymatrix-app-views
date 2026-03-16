package be.codewriter.melodymatrix.view.stage.piano.animation

import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.stage.piano.PianoStage.Companion.PIANO_BACKGROUND_HEIGHT
import javafx.geometry.Point2D
import javafx.scene.paint.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class AnimationCalculator(
    private val updateCallback: (AnimationState) -> Unit
) {
    private val executor: ScheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor { task ->
            Thread.ofVirtual().name("animation-calculator").unstarted(task)
        }

    private val isRunning = AtomicBoolean(false)
    private val stateLock = Any()
    private var lastUpdateTime = System.nanoTime()

    // State tracking
    private val activeParticles = mutableListOf<ParticleInfo>()
    private val activeKeys = mutableMapOf<Note, KeyAnimationInfo>()

    fun start() {
        if (isRunning.compareAndSet(false, true)) {
            // Schedule at 60 FPS (16.67ms per frame)
            executor.scheduleAtFixedRate(
                ::calculateFrame,
                0,
                16_667_000, // nanoseconds
                TimeUnit.NANOSECONDS
            )
        }
    }

    fun stop() {
        if (isRunning.compareAndSet(true, false)) {
            executor.shutdown()
        }
    }

    private fun calculateFrame() {
        val currentTime = System.nanoTime()
        val deltaTime = (currentTime - lastUpdateTime) / 1_000_000_000.0 // Convert to seconds
        lastUpdateTime = currentTime

        val state = synchronized(stateLock) {
            // Perform heavy calculations here
            updateParticles(deltaTime)
            updateKeyAnimations(deltaTime)
            updateFireEffect(deltaTime)

            // Create immutable state snapshot
            AnimationState(
                timestamp = currentTime,
                particlePositions = activeParticles.map { it.toData() },
                fireEmitterState = calculateFireState(),
                keyStates = activeKeys.mapValues { it.value.toState() }
            )
        }

        // Send to JavaFX thread
        updateCallback(state)
    }

    fun addParticle(x: Double, y: Double, velocity: Point2D, color: Color, lifespan: Double) {
        synchronized(stateLock) {
            activeParticles.add(ParticleInfo(x, y, velocity, color, lifespan, 5.0))
        }
    }

    fun updateKeyPress(note: Note, isPressed: Boolean) {
        synchronized(stateLock) {
            if (isPressed) {
                activeKeys[note] = KeyAnimationInfo(note, isPressed = true)
            } else {
                activeKeys[note]?.isPressed = false
            }
        }
    }

    fun addExplosion(
        x: Double,
        y: Double,
        velocity: Int,
        radius: Double,
        color: Color,
        particleCount: Int,
        particleSize: Double,
        randomColor: Boolean
    ) {
        synchronized(stateLock) {
            repeat(particleCount.coerceAtLeast(1)) {
                val angle = Random.nextDouble(0.0, Math.PI * 2)
                val speed = Random.nextDouble(radius * 5.0, radius * 11.0) * (0.35 + velocity.coerceAtLeast(1) / 127.0)
                activeParticles.add(
                    ParticleInfo(
                        x = x,
                        y = y,
                        velocity = Point2D(cos(angle) * speed, sin(angle) * speed - Random.nextDouble(30.0, 80.0)),
                        color = resolveColor(color, randomColor),
                        lifespan = Random.nextDouble(0.45, 1.2),
                        size = particleSize
                    )
                )
            }
        }
    }

    fun addFireworks(
        x: Double,
        y: Double,
        velocity: Int,
        radius: Double,
        color: Color,
        particleCount: Int,
        particleSize: Double,
        randomColor: Boolean,
        tailParticleCount: Int
    ) {
        synchronized(stateLock) {
            val clampedVelocity = velocity.coerceAtLeast(1)
            val burstY = (y - (100.0 + clampedVelocity * 1.6)).coerceAtLeast(40.0)

            repeat(tailParticleCount.coerceAtLeast(1)) {
                val progress = it.toDouble() / tailParticleCount.coerceAtLeast(1)
                activeParticles.add(
                    ParticleInfo(
                        x = x + Random.nextDouble(-4.0, 4.0),
                        y = y - progress * (y - burstY),
                        velocity = Point2D(Random.nextDouble(-10.0, 10.0), -Random.nextDouble(50.0, 130.0)),
                        color = resolveColor(color, randomColor),
                        lifespan = Random.nextDouble(0.2, 0.6),
                        size = (particleSize * 0.7).coerceAtLeast(1.0)
                    )
                )
            }

            repeat(particleCount.coerceAtLeast(1)) {
                val angle = Random.nextDouble(0.0, Math.PI * 2)
                val speed = Random.nextDouble(radius * 7.0, radius * 13.0) * (0.30 + clampedVelocity / 127.0)
                activeParticles.add(
                    ParticleInfo(
                        x = x,
                        y = burstY,
                        velocity = Point2D(cos(angle) * speed, sin(angle) * speed),
                        color = resolveColor(color, randomColor),
                        lifespan = Random.nextDouble(0.6, 1.6),
                        size = particleSize
                    )
                )
            }
        }
    }

    private fun updateParticles(deltaTime: Double) {
        activeParticles.removeIf { particle ->
            particle.age += deltaTime
            particle.age >= particle.lifespan
        }

        activeParticles.forEach { particle ->
            // Physics calculations
            particle.x += particle.velocity.x * deltaTime
            particle.y += particle.velocity.y * deltaTime
            particle.velocity = particle.velocity.add(0.0, 9.8 * deltaTime) // Gravity
        }
    }

    private fun updateKeyAnimations(deltaTime: Double) {
        activeKeys.values.forEach { key ->
            key.animationProgress += deltaTime * 5.0 // Animation speed
            if (!key.isPressed && key.animationProgress >= 1.0) {
                key.animationProgress = 0.0
            }
        }
    }

    private fun updateFireEffect(deltaTime: Double): FireState {
        // Calculate fire animation state
        return FireState(0.0, 0.0, 1.0)
    }

    private fun calculateFireState(): FireState {
        // Simplified fire state calculation
        return FireState(0.0, PIANO_BACKGROUND_HEIGHT - 5.0, 1.0)
    }

    private fun resolveColor(baseColor: Color, randomColor: Boolean): Color {
        if (!randomColor) {
            return baseColor
        }

        return Color.color(
            Random.nextDouble(0.4, 1.0),
            Random.nextDouble(0.4, 1.0),
            Random.nextDouble(0.4, 1.0)
        )
    }

    // Helper classes
    data class ParticleInfo(
        var x: Double,
        var y: Double,
        var velocity: Point2D,
        val color: Color,
        val lifespan: Double,
        val size: Double,
        var age: Double = 0.0
    ) {
        fun toData() = ParticleData(x, y, color, size, (1.0 - (age / lifespan)).coerceIn(0.0, 1.0))
    }

    data class KeyAnimationInfo(
        val note: Note,
        var isPressed: Boolean,
        var animationProgress: Double = 0.0
    ) {
        fun toState() = KeyState(isPressed, animationProgress)
    }
}