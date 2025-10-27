package be.codewriter.melodymatrix.view.stage.piano.animation

import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.stage.piano.PianoStage.Companion.PIANO_BACKGROUND_HEIGHT
import be.codewriter.melodymatrix.view.stage.piano.particle.ParticleEmitter
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
    private var lastUpdateTime = System.nanoTime()

    // State tracking
    private val particleEmitters = mutableListOf<ParticleEmitter>()
    private val activeKeys = mutableMapOf<Note, KeyAnimationInfo>()

    fun addEmitter(emitter: ParticleEmitter) {
        particleEmitters.add(emitter)
    }

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

        // Perform heavy calculations here
        updateParticles(deltaTime)
        updateKeyAnimations(deltaTime)
        updateFireEffect(deltaTime)

        // Create immutable state snapshot
        val state = AnimationState(
            timestamp = currentTime,
            particleEmitters = particleEmitters,
            fireEmitterState = calculateFireState(),
            keyStates = activeKeys.mapValues { it.value.toState() }
        )

        // Send to JavaFX thread
        updateCallback(state)
    }


    private fun updateParticles(deltaTime: Double) {

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

    data class KeyAnimationInfo(
        val note: Note,
        var isPressed: Boolean,
        var animationProgress: Double = 0.0
    ) {
        fun toState() = KeyState(isPressed, animationProgress)
    }
}