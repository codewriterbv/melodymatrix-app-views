package be.codewriter.melodymatrix.view.stage.piano.animation

import be.codewriter.melodymatrix.view.data.MidiData
import be.codewriter.melodymatrix.view.stage.piano.data.PianoConfiguration
import be.codewriter.melodymatrix.view.stage.piano.particle.ParticleEmitters
import be.codewriter.melodymatrix.view.stage.piano.particle.ParticleEngine
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
    private val particleEngine = ParticleEngine()

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
        particleEngine.update(deltaTime)
    }

    fun playNote(midiData: MidiData, config: PianoConfiguration) {
        if (config.explosionEnabled.get()) {
            var emittor =
                ParticleEmitters.newFireEmitter(config.explosionColor.get(), config.explosionColor.get()).apply {
                    startColor = config.aboveKeyColorStart.value
                    endColor = config.aboveKeyColorEnd.value
                    emissionRate = 80.0
                    minVelocityY = -100.0
                    maxVelocityY = -60.0
                    opacity = 0.0
                }
            particleEngine.add(emittor)
        }
    }
}