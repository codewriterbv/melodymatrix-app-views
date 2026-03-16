package be.codewriter.melodymatrix.view.stage.piano.animation

import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.stage.piano.PianoStage.Companion.PIANO_BACKGROUND_HEIGHT
import be.codewriter.melodymatrix.view.stage.piano.data.FireworksExplosionType
import javafx.geometry.Point2D
import javafx.scene.paint.Color
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

class AnimationCalculator(
    private val updateCallback: (AnimationState) -> Unit
) {
    private sealed interface AnimationCommand {
        data class KeyPress(val note: Note, val isPressed: Boolean) : AnimationCommand
        data class Explosion(
            val x: Double,
            val y: Double,
            val velocity: Int,
            val radius: Double,
            val color: Color,
            val particleCount: Int,
            val particleSize: Double,
            val randomColor: Boolean
        ) : AnimationCommand

        data class Fireworks(
            val x: Double,
            val y: Double,
            val velocity: Int,
            val radius: Double,
            val color: Color,
            val particleCount: Int,
            val particleSize: Double,
            val randomColor: Boolean,
            val tailParticleCount: Int,
            val launchHeightMultiplier: Double,
            val explosionType: FireworksExplosionType
        ) : AnimationCommand
    }

    private val executor: ScheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor { task ->
            Thread.ofVirtual().name("animation-calculator").unstarted(task)
        }

    private val isRunning = AtomicBoolean(false)
    private val stateLock = Any()
    private var lastUpdateTime = System.nanoTime()
    private val commandQueue = ConcurrentLinkedQueue<AnimationCommand>()

    // State tracking
    private val activeParticles = mutableListOf<ParticleInfo>()
    private val aboveKeyParticles = mutableListOf<AboveKeyParticleInfo>()
    private val activeKeys = mutableMapOf<Note, KeyAnimationInfo>()
    private val aboveKeySmokeGenerator = AboveKeySmokeGenerator()
    private val fireworksGenerator = FireworksGenerator()
    private val explosionGenerator = ExplosionGenerator()

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
            drainQueuedCommands()
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
        commandQueue.add(AnimationCommand.KeyPress(note, isPressed))
    }

    fun updateAboveKeyEffect(enabled: Boolean, startColor: Color, endColor: Color) {
        synchronized(stateLock) {
            aboveKeySmokeGenerator.updateConfig(enabled, startColor, endColor, aboveKeyParticles)
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
        commandQueue.add(
            AnimationCommand.Explosion(
                x = x,
                y = y,
                velocity = velocity,
                radius = radius,
                color = color,
                particleCount = particleCount,
                particleSize = particleSize,
                randomColor = randomColor
            )
        )
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
        tailParticleCount: Int,
        launchHeightMultiplier: Double,
        explosionType: FireworksExplosionType
    ) {
        commandQueue.add(
            AnimationCommand.Fireworks(
                x = x,
                y = y,
                velocity = velocity,
                radius = radius,
                color = color,
                particleCount = particleCount,
                particleSize = particleSize,
                randomColor = randomColor,
                tailParticleCount = tailParticleCount,
                launchHeightMultiplier = launchHeightMultiplier,
                explosionType = explosionType
            )
        )
    }

    private fun drainQueuedCommands() {
        while (true) {
            val command = commandQueue.poll() ?: break
            when (command) {
                is AnimationCommand.KeyPress -> {
                    if (command.isPressed) {
                        activeKeys[command.note] = KeyAnimationInfo(command.note, isPressed = true)
                    } else {
                        activeKeys[command.note]?.isPressed = false
                    }
                }

                is AnimationCommand.Explosion -> {
                    activeParticles.addAll(
                        explosionGenerator.generate(
                            x = command.x,
                            y = command.y,
                            velocity = command.velocity,
                            radius = command.radius,
                            color = command.color,
                            particleCount = command.particleCount,
                            particleSize = command.particleSize,
                            randomColor = command.randomColor
                        )
                    )
                }

                is AnimationCommand.Fireworks -> {
                    activeParticles.addAll(
                        fireworksGenerator.generate(
                            x = command.x,
                            y = command.y,
                            velocity = command.velocity,
                            radius = command.radius,
                            color = command.color,
                            particleCount = command.particleCount,
                            particleSize = command.particleSize,
                            randomColor = command.randomColor,
                            tailParticleCount = command.tailParticleCount,
                            launchHeightMultiplier = command.launchHeightMultiplier,
                            explosionType = command.explosionType
                        )
                    )
                }
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
        val activeNotes = activeKeys.filter { it.value.isPressed }.keys
        aboveKeySmokeGenerator.update(deltaTime, activeNotes, aboveKeyParticles)
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