package be.codewriter.melodymatrix.view.test

import be.codewriter.melodymatrix.view.data.MmxEventHandler
import be.codewriter.melodymatrix.view.event.AudioSpectrumEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.math.exp
import kotlin.math.max

/**
 * Emits synthetic [AudioSpectrumEvent]s at a fixed rate to registered [MmxEventHandler]s.
 *
 * Used by the standalone [TestView] so views can be exercised without a real audio input.
 * The magnitudes describe a Gaussian peak that slides across the frequency range.
 *
 * @see MmxEventHandler
 * @see AudioSpectrumEvent
 */
class AudioSpectrumSimulator {

    private val listeners = CopyOnWriteArrayList<MmxEventHandler>()
    private val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor { runnable ->
        Thread(runnable, "AudioSpectrumSimulator").apply { isDaemon = true }
    }
    private var started = false
    private var frame = 0

    fun registerListener(listener: MmxEventHandler) {
        logger.info("Adding audio simulator listener {}", listener)
        listeners.add(listener)
        ensureStarted()
    }

    fun removeListener(listener: MmxEventHandler) {
        logger.info("Removing audio simulator listener {}", listener)
        listeners.remove(listener)
    }

    fun stop() {
        scheduler.shutdownNow()
    }

    @Synchronized
    private fun ensureStarted() {
        if (started) return
        started = true
        scheduler.scheduleAtFixedRate(
            { tick() },
            0L,
            (1000L / EVENTS_PER_SECOND),
            TimeUnit.MILLISECONDS
        )
    }

    private fun tick() {
        try {
            val magnitudes = generateMagnitudes(frame++)
            val event = AudioSpectrumEvent(
                magnitudes = magnitudes,
                sampleRate = SAMPLE_RATE,
                binHz = SAMPLE_RATE / FFT_SIZE.toFloat()
            )
            for (listener in listeners) {
                try {
                    listener.onEvent(event)
                } catch (e: Exception) {
                    logger.error("Audio simulator dispatch failed for listener {}", listener, e)
                }
            }
        } catch (t: Throwable) {
            logger.error("AudioSpectrumSimulator tick failed", t)
        }
    }

    private fun generateMagnitudes(frameIndex: Int): FloatArray {
        val bins = FFT_SIZE / 2
        val magnitudes = FloatArray(bins)
        // Slide the peak across roughly the lower half of the spectrum.
        val period = 240.0
        val phase = (frameIndex % period.toInt()) / period
        val peakBin = (phase * bins * 0.5).toInt().coerceAtLeast(4)
        val width = max(6.0, bins * 0.02)
        val invWidthSq = 1.0 / (width * width)

        for (i in 0 until bins) {
            val distance = (i - peakBin).toDouble()
            val gaussian = exp(-(distance * distance) * invWidthSq)
            magnitudes[i] = gaussian.toFloat().coerceIn(0.0f, 1.0f)
        }
        return magnitudes
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(AudioSpectrumSimulator::class.java.name)
        private const val EVENTS_PER_SECOND = 30L
        private const val SAMPLE_RATE = 48_000f
        private const val FFT_SIZE = 2048
        }
        }
