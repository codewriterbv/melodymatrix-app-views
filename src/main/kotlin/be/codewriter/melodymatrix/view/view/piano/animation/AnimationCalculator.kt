package be.codewriter.melodymatrix.view.view.piano.animation

import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.view.piano.PianoView.Companion.PIANO_BACKGROUND_HEIGHT
import be.codewriter.melodymatrix.view.view.piano.data.FireworksExplosionType
import javafx.geometry.Point2D
import javafx.scene.paint.Color
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Off-thread physics engine that calculates the piano animation state at ~60 FPS.
 *
 * Runs a [ScheduledExecutorService] loop on a virtual thread. Each frame it drains a
 * lock-free command queue, advances particle physics, updates key-press animations, and
 * produces an immutable [AnimationState] snapshot that is forwarded to [updateCallback]
 * for rendering on the JavaFX thread.
 *
 * Commands (key presses, explosions, fireworks) are submitted via thread-safe queue methods;
 * they are applied at the start of the next frame inside the state lock.
 *
 * @param updateCallback Invoked on every calculated frame with the new [AnimationState]
 * @see AnimationState
 * @see ExplosionGenerator
 * @see FireworksGenerator
 * @see CloudGenerator
 */
class AnimationCalculator(
    private val updateCallback: (AnimationState) -> Unit
) {
    /**
     * Sealed hierarchy of commands submitted to the calculator's command queue.
     */
    private sealed interface AnimationCommand {
        /** Notifies the calculator that a key was pressed or released. */
        data class KeyPress(val note: Note, val isPressed: Boolean) : AnimationCommand

        /** Requests a radial explosion burst at the given position. */
        data class Explosion(
            val x: Double,
            val y: Double,
            val velocity: Int,
            val radius: Double,
            val color: Color,
            val particleCount: Int,
            val particleSize: Double,
            val tailParticleCount: Int,
            val liftMultiplier: Double,
            val randomColor: Boolean
        ) : AnimationCommand

        /** Requests a fireworks burst launched from the given position. */
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
            val liftMultiplier: Double,
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
    private val cloudGenerator = CloudGenerator()
    private val fireworksGenerator = FireworksGenerator()
    private val explosionGenerator = ExplosionGenerator()

    /**
     * Starts the 60 FPS animation loop. No-ops if already running.
     */
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

    /**
     * Stops the animation loop and shuts down the executor. No-ops if already stopped.
     */
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

    /**
     * Enqueues a key-press or key-release command to be applied on the next frame.
     *
     * @param note      The note whose key state changed
     * @param isPressed True for key-down, false for key-up
     */
    fun updateKeyPress(note: Note, isPressed: Boolean) {
        commandQueue.add(AnimationCommand.KeyPress(note, isPressed))
    }

    /**
     * Updates the above-key smoke effect configuration immediately (thread-safe).
     *
     * @param enabled         Whether the smoke effect should be active
     * @param startColor      Base/idle smoke colour
     * @param endColor        Colour smoke shifts toward near active keys
     * @param particleCount   Number of particles seeded across the keyboard
     * @param particleSize    Nominal blob size (actual size varies ±40 % around this value)
     * @param driftSpeed      Maximum horizontal drift speed in pixels/second
     * @param wobbleAmplitude Vertical wobble amplitude in pixels
     * @param opacity         Base opacity (actual opacity varies ±50 % around this value)
     * @param spawnRadius     X-radius around a pressed key used for density checks and spawning
     */
    fun updateAboveKeyEffect(
        enabled: Boolean,
        startColor: Color,
        endColor: Color,
        particleCount: Int,
        particleSize: Double,
        driftSpeed: Double,
        wobbleAmplitude: Double,
        opacity: Double,
        spawnRadius: Double
    ) {
        synchronized(stateLock) {
            cloudGenerator.updateConfig(
                enabled, startColor, endColor,
                particleCount, particleSize, driftSpeed, wobbleAmplitude, opacity, spawnRadius,
                aboveKeyParticles
            )
        }
    }

    /**
     * Enqueues a radial explosion command to be processed on the next animation frame.
     *
     * @param x            Horizontal origin in scene coordinates
     * @param y            Vertical origin in scene coordinates
     * @param velocity     MIDI velocity (1–127)
     * @param radius       Base spread radius
     * @param color        Base particle colour
     * @param particleCount Number of particles to generate
     * @param particleSize  Base particle diameter in pixels
     * @param tailParticleCount Number of trailing particles emitted upward from the key
     * @param liftMultiplier Multiplier for vertical lift strength (higher = taller bursts)
     * @param randomColor  When true uses a cinematic colour palette
     */
    fun addExplosion(
        x: Double,
        y: Double,
        velocity: Int,
        radius: Double,
        color: Color,
        particleCount: Int,
        particleSize: Double,
        tailParticleCount: Int,
        liftMultiplier: Double,
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
                tailParticleCount = tailParticleCount,
                liftMultiplier = liftMultiplier,
                randomColor = randomColor
            )
        )
    }

    /**
     * Enqueues a fireworks launch command to be processed on the next animation frame.
     *
     * @param x                     Horizontal launch origin in scene coordinates
     * @param y                     Vertical launch origin in scene coordinates
     * @param velocity               MIDI velocity (1–127)
     * @param radius                 Burst spread radius
     * @param color                  Base colour for the burst
     * @param particleCount          Number of burst particles
     * @param particleSize           Base particle diameter in pixels
     * @param randomColor            When true uses a cinematic colour palette
     * @param tailParticleCount      Trailing particles emitted during ascent
     * @param launchHeightMultiplier Multiplier applied to the launch height (0.6–2.8)
     * @param liftMultiplier          Multiplier applied to upward fireworks lift (0.6–1.8)
     * @param explosionType          The burst pattern style
     */
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
        liftMultiplier: Double,
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
                liftMultiplier = liftMultiplier,
                explosionType = explosionType
            )
        )
    }

    /** Drains all queued commands and applies them to the current animation state. */
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
                            tailParticleCount = command.tailParticleCount,
                            liftMultiplier = command.liftMultiplier,
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
                            liftMultiplier = command.liftMultiplier,
                            explosionType = command.explosionType
                        )
                    )
                }
            }
        }
    }

    /** Advances all active explosion/fireworks particles by [deltaTime] seconds, removing expired ones. */
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

    /** Advances above-key smoke particles, delegating to [CloudGenerator]. */
    private fun updateAboveKeyParticles(deltaTime: Double) {
        val activeNotes = activeKeys.filter { it.value.isPressed }.keys
        cloudGenerator.update(deltaTime, activeNotes, aboveKeyParticles)
    }

    /** Advances key-press animation progress values for all tracked keys. */
    private fun updateKeyAnimations(deltaTime: Double) {
        activeKeys.values.forEach { key ->
            key.animationProgress += deltaTime * 5.0 // Animation speed
            if (!key.isPressed && key.animationProgress >= 1.0) {
                key.animationProgress = 0.0
            }
        }
    }

    /** Updates above-key particles and returns the current [FireState]. */
    private fun updateFireEffect(deltaTime: Double): FireState {
        updateAboveKeyParticles(deltaTime)
        // Calculate fire animation state
        return FireState(0.0, 0.0, 1.0)
    }

    /** Returns the current fire emitter state anchored at the bottom of the piano scene. */
    private fun calculateFireState(): FireState {
        // Simplified fire state calculation
        return FireState(0.0, PIANO_BACKGROUND_HEIGHT - 5.0, 1.0)
    }


    // Helper classes
    /**
     * Mutable runtime state for a single explosion/fireworks particle.
     *
     * @property x        Current X position
     * @property y        Current Y position
     * @property velocity Current velocity vector (pixels/second)
     * @property color    Particle colour
     * @property lifespan Total lifespan in seconds
     * @property size     Particle diameter in pixels
     * @property age      Elapsed age in seconds (0 → lifespan)
     */
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

    /**
     * Mutable runtime state for a single above-key smoke particle.
     *
     * @property x               Current X position
     * @property y               Computed Y position (baseY + wobble)
     * @property baseY           Anchor Y position near the top of the keyboard
     * @property size            Particle diameter in pixels
     * @property color           Current interpolated colour
     * @property opacity         Current opacity (0.0–1.0)
     * @property baseOpacity     Mid-point opacity around which [opacityPhase] oscillates
     * @property driftSpeed      Horizontal drift speed in pixels/second
     * @property phase           Current wobble phase angle (radians)
     * @property phaseSpeed      Wobble phase advance rate (radians/second)
     * @property wobbleAmplitude Maximum vertical wobble displacement in pixels
     * @property opacityPhase    Current opacity oscillation phase (radians)
     * @property opacitySpeed    Opacity phase advance rate (radians/second)
     */
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

    /**
     * Mutable runtime tracking for a single piano key's press animation.
     *
     * @property note              The musical note this key represents
     * @property isPressed         Whether the key is currently held down
     * @property animationProgress Progress of the press animation (0.0 = start, 1.0 = complete)
     */
    data class KeyAnimationInfo(
        val note: Note,
        var isPressed: Boolean,
        var animationProgress: Double = 0.0
    ) {
        fun toState() = KeyState(isPressed, animationProgress)
    }
}