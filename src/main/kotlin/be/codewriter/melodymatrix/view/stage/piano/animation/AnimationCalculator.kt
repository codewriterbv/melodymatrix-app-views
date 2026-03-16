package be.codewriter.melodymatrix.view.stage.piano.animation

import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.stage.piano.PianoStage.Companion.PIANO_BACKGROUND_HEIGHT
import be.codewriter.melodymatrix.view.stage.piano.PianoStage.Companion.PIANO_WIDTH
import javafx.geometry.Point2D
import javafx.scene.paint.Color
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class AnimationCalculator(
    private val updateCallback: (AnimationState) -> Unit
) {
    private data class AboveKeyEffectConfig(
        val enabled: Boolean = true,
        val startColor: Color = Color.rgb(255, 0, 0),
        val endColor: Color = Color.rgb(0, 0, 2)
    )

    private val executor: ScheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor { task ->
            Thread.ofVirtual().name("animation-calculator").unstarted(task)
        }

    private val isRunning = AtomicBoolean(false)
    private val stateLock = Any()
    private var lastUpdateTime = System.nanoTime()

    // State tracking
    private val activeParticles = mutableListOf<ParticleInfo>()
    private val aboveKeyParticles = mutableListOf<AboveKeyParticleInfo>()
    private val activeKeys = mutableMapOf<Note, KeyAnimationInfo>()
    private var aboveKeyConfig = AboveKeyEffectConfig()

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
                aboveKeyParticles = aboveKeyParticles.map { it.toData() },
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

    fun updateAboveKeyEffect(enabled: Boolean, startColor: Color, endColor: Color) {
        synchronized(stateLock) {
            val previousEnabled = aboveKeyConfig.enabled
            val previousStart = aboveKeyConfig.startColor
            val previousEnd = aboveKeyConfig.endColor

            aboveKeyConfig = AboveKeyEffectConfig(enabled, startColor, endColor)

            if (!enabled) {
                aboveKeyParticles.clear()
            } else if (!previousEnabled || previousStart != startColor || previousEnd != endColor) {
                seedAboveKeyParticles()
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

    private fun updateAboveKeyParticles(deltaTime: Double) {
        if (!aboveKeyConfig.enabled) {
            aboveKeyParticles.clear()
            return
        }

        if (aboveKeyParticles.isEmpty()) {
            seedAboveKeyParticles()
        }

        // Animate color transitions for smoke particles near played keys
        val activeKeyXs = activeKeys.filter { it.value.isPressed }.map { noteInfo ->
            // Map note to x position (approximate)
            val note = noteInfo.key
            val idx = note.ordinal // Assuming Note is enum
            val totalKeys = 88 // Standard piano
            val x = (idx.toDouble() / totalKeys) * PIANO_WIDTH
            x
        }

        aboveKeyParticles.forEach { particle ->
            particle.x += particle.driftSpeed * deltaTime
            particle.phase += deltaTime * particle.phaseSpeed
            particle.opacityPhase += deltaTime * particle.opacitySpeed

            if (particle.x < -particle.size) {
                particle.x = PIANO_WIDTH + particle.size * 0.5
            } else if (particle.x > PIANO_WIDTH + particle.size) {
                particle.x = -particle.size * 0.5
            }

            particle.y = particle.baseY + sin(particle.phase) * particle.wobbleAmplitude
            particle.opacity = (particle.baseOpacity + sin(particle.opacityPhase) * 0.035).coerceIn(0.04, 0.22)

            // If near a pressed key, animate color toward endColor
            val proximity = activeKeyXs.any { keyX -> Math.abs(particle.x - keyX) < 80.0 }
            if (proximity) {
                // Blend color toward endColor
                val blendFactor = 0.12
                particle.color = blend(particle.color, aboveKeyConfig.endColor, blendFactor)
            } else {
                // Slowly revert to startColor
                val blendFactor = 0.04
                particle.color = blend(particle.color, aboveKeyConfig.startColor, blendFactor)
            }
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
        updateAboveKeyParticles(deltaTime)
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

    private fun seedAboveKeyParticles() {
        aboveKeyParticles.clear()
        // Increase particle count for denser smoke
        repeat(32) {
            aboveKeyParticles.add(createAboveKeyParticle(Random.nextDouble(0.0, PIANO_WIDTH)))
        }
    }

    private fun createAboveKeyParticle(initialX: Double): AboveKeyParticleInfo {
        val size = Random.nextDouble(100.0, 220.0) // Larger, more irregular
        return AboveKeyParticleInfo(
            x = initialX,
            y = 0.0,
            baseY = Random.nextDouble(PIANO_BACKGROUND_HEIGHT - 50.0, PIANO_BACKGROUND_HEIGHT - 15.0),
            size = size,
            color = blend(aboveKeyConfig.startColor, aboveKeyConfig.endColor, Random.nextDouble(0.0, 1.0)),
            opacity = Random.nextDouble(0.06, 0.18),
            baseOpacity = Random.nextDouble(0.06, 0.18),
            driftSpeed = Random.nextDouble(-22.0, 22.0),
            phase = Random.nextDouble(0.0, Math.PI * 2),
            phaseSpeed = Random.nextDouble(0.22, 0.55),
            wobbleAmplitude = Random.nextDouble(8.0, 22.0),
            opacityPhase = Random.nextDouble(0.0, Math.PI * 2),
            opacitySpeed = Random.nextDouble(0.18, 0.45)
        ).apply {
            y = baseY + sin(phase) * wobbleAmplitude
        }
    }

    private fun blend(startColor: Color, endColor: Color, factor: Double): Color {
        val clampedFactor = factor.coerceIn(0.0, 1.0)
        return Color.color(
            startColor.red + (endColor.red - startColor.red) * clampedFactor,
            startColor.green + (endColor.green - startColor.green) * clampedFactor,
            startColor.blue + (endColor.blue - startColor.blue) * clampedFactor
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

    data class AboveKeyParticleInfo(
        var x: Double,
        var y: Double,
        val baseY: Double,
        val size: Double,
        var color: Color,
        var opacity: Double,
        val baseOpacity: Double,
        val driftSpeed: Double,
        var phase: Double,
        val phaseSpeed: Double,
        val wobbleAmplitude: Double,
        var opacityPhase: Double,
        val opacitySpeed: Double
    ) {
        fun toData() = AboveKeyParticleData(x, y, color, size, opacity)
    }

    data class KeyAnimationInfo(
        val note: Note,
        var isPressed: Boolean,
        var animationProgress: Double = 0.0
    ) {
        fun toState() = KeyState(isPressed, animationProgress)
    }
}