package be.codewriter.melodymatrix.view.view.spectrum

import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.event.AudioSpectrumEvent
import be.codewriter.melodymatrix.view.event.MidiDataEvent
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.MmxEventType
import be.codewriter.melodymatrix.view.view.MmxView
import be.codewriter.melodymatrix.view.view.MmxViewMetadata
import javafx.animation.AnimationTimer
import javafx.geometry.Insets
import javafx.geometry.VPos
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Combined visualizer that overlays a live audio spectrum with a scrolling MIDI piano-roll.
 *
 * The top canvas draws a labelled piano-roll: incoming MIDI notes appear as rectangles that
 * drift to the left over [ROLL_WINDOW_MILLIS] milliseconds. Octave "C" rows are highlighted
 * and labelled on the left side so the pitch range is easy to read.
 *
 * The bottom canvas draws the most recent [AudioSpectrumEvent] on a log-frequency X axis
 * (~40 Hz .. 16 kHz) and a dB-scaled Y axis (-70..0 dB) so both low-mid and high frequencies
 * are readable simultaneously. Bars are aggregated into perceptually spaced bands and
 * peak-hold smoothed for stability.
 *
 * The view refreshes both canvases at ~60 fps via a single [AnimationTimer]. All mutation
 * of shared state from [onEvent] happens via thread-safe primitives.
 *
 * @see MmxView
 * @see AudioSpectrumEvent
 * @see MidiDataEvent
 */
class AudioSpectrumMidiView : MmxView() {

    private val naturalW = 900.0
    private val naturalH = 520.0
    private val pianoRollHeight = 270.0
    private val spectrumHeight = 230.0

    private val pianoRollCanvas = Canvas(naturalW, pianoRollHeight)
    private val spectrumCanvas = Canvas(naturalW, spectrumHeight)
    private val canvasStack = StackPane(
        VBox(pianoRollCanvas, spectrumCanvas).apply {
            spacing = 6.0
            padding = Insets(6.0)
        }
    ).apply {
        style = "-fx-background-color: #101820;"
    }

    private val latestSpectrum = AtomicReference<AudioSpectrumEvent?>()

    // Smoothed magnitude buffer (per-bin exponential decay to avoid flicker).
    private var smoothedMagnitudes: FloatArray = FloatArray(0)

    // Piano-roll state.
    private val activeNotes = ConcurrentHashMap<Note, Long>()
    private val notesLock = Any()
    private val recentNotes = ArrayDeque<PianoRollNote>()

    private val timer = object : AnimationTimer() {
        override fun handle(now: Long) {
            drawPianoRoll()
            drawSpectrum()
        }
    }

    init {
        setupSurface(
            rootNode = canvasStack,
            naturalWidth = naturalW,
            naturalHeight = naturalH,
            captureNode = canvasStack,
            captureWidth = naturalW.toInt(),
            captureHeight = naturalH.toInt()
        ) {
            timer.stop()
        }
        timer.start()
    }

    override fun onEvent(event: MmxEvent) {
        when (event.type) {
            MmxEventType.MIDI -> handleMidiEvent(event as? MidiDataEvent ?: return)
            MmxEventType.AUDIO_SPECTRUM -> handleAudioSpectrum(event as? AudioSpectrumEvent ?: return)
            MmxEventType.PLAY -> {
                // Not needed here
            }

            MmxEventType.CHORD -> {
                // Not needed here
            }
        }
    }

    private fun handleMidiEvent(midiDataEvent: MidiDataEvent) {
        if (midiDataEvent.note == Note.UNDEFINED) return
        val now = System.nanoTime()
        when (midiDataEvent.event) {
            MidiEvent.NOTE_ON -> {
                if (midiDataEvent.velocity == 0) {
                    closeNote(midiDataEvent.note, now)
                } else {
                    activeNotes[midiDataEvent.note] = now
                    synchronized(notesLock) {
                        recentNotes.addLast(
                            PianoRollNote(
                                pitch = midiDataEvent.note.byteValue,
                                startNs = now,
                                endNs = null,
                                velocity = midiDataEvent.velocity
                            )
                        )
                    }
                }
            }

            MidiEvent.NOTE_OFF -> closeNote(midiDataEvent.note, now)
            else -> {
                // ignore other MIDI events
            }
        }
    }

    private fun closeNote(note: Note, nowNs: Long) {
        activeNotes.remove(note)
        synchronized(notesLock) {
            for (i in recentNotes.indices.reversed()) {
                val n = recentNotes[i]
                if (n.pitch == note.byteValue && n.endNs == null) {
                    recentNotes[i] = n.copy(endNs = nowNs)
                    break
                }
            }
        }
    }

    private fun handleAudioSpectrum(event: AudioSpectrumEvent) {
        latestSpectrum.set(event)
    }

    private fun drawPianoRoll() {
        val gc = pianoRollCanvas.graphicsContext2D
        val width = pianoRollCanvas.width
        val height = pianoRollCanvas.height
        gc.fill = BG_TOP
        gc.fillRect(0.0, 0.0, width, height)

        drawPianoRollHeader(gc, width)

        val plotX = PIANO_ROLL_LEFT_MARGIN
        val plotY = PIANO_ROLL_TOP_MARGIN
        val plotW = width - plotX - PIANO_ROLL_RIGHT_MARGIN
        val plotH = height - plotY - PIANO_ROLL_BOTTOM_MARGIN
        if (plotW <= 0 || plotH <= 0) return

        drawPianoRollGrid(gc, plotX, plotY, plotW, plotH)

        val nowNs = System.nanoTime()
        val windowNs = ROLL_WINDOW_MILLIS * 1_000_000L

        val snapshot: List<PianoRollNote>
        synchronized(notesLock) {
            val cutoff = nowNs - windowNs
            while (recentNotes.isNotEmpty()) {
                val head = recentNotes.first()
                val effectiveEnd = head.endNs ?: nowNs
                if (effectiveEnd < cutoff) {
                    recentNotes.removeFirst()
                } else {
                    break
                }
            }
            snapshot = recentNotes.toList()
        }

        val pitchRange = (MAX_PITCH - MIN_PITCH).toDouble()
        val rowHeight = plotH / pitchRange

        for (note in snapshot) {
            val startNs = note.startNs
            val endNs = note.endNs ?: nowNs
            val startX = timeToX(startNs, nowNs, windowNs, plotX, plotW)
            val endX = timeToX(endNs, nowNs, windowNs, plotX, plotW)
            val x = max(plotX, startX)
            val rectW = max(2.0, endX - x)
            val y = plotY + plotH - ((note.pitch - MIN_PITCH) + 1) * rowHeight
            val velocityFactor = (note.velocity.coerceIn(0, 127)) / 127.0
            val hue = 200.0 + (note.pitch % 12) * 12.0
            gc.fill = Color.hsb(hue, 0.7, 0.5 + 0.5 * velocityFactor)
            gc.fillRect(x, y, rectW, rowHeight.coerceAtLeast(2.0))
        }

        drawPianoRollAxisLabels(gc, plotX, plotY, plotW, plotH)
    }

    private fun drawPianoRollHeader(gc: GraphicsContext, width: Double) {
        gc.fill = TEXT_PRIMARY
        gc.font = Font.font("System", 14.0)
        gc.textAlign = TextAlignment.LEFT
        gc.textBaseline = VPos.TOP
        gc.fillText("MIDI notes (last ${ROLL_WINDOW_MILLIS / 1000}\u202fs)", 8.0, 6.0)

        gc.fill = TEXT_SECONDARY
        gc.font = Font.font("System", 11.0)
        gc.textAlign = TextAlignment.RIGHT
        gc.fillText("newer \u2192", width - 10.0, 8.0)
    }

    private fun drawPianoRollGrid(
        gc: GraphicsContext,
        plotX: Double,
        plotY: Double,
        plotW: Double,
        plotH: Double
    ) {
        val pitchRange = (MAX_PITCH - MIN_PITCH).toDouble()
        val rowHeight = plotH / pitchRange

        // Horizontal note-row lines, brighter for each C.
        var pitch = MIN_PITCH
        while (pitch <= MAX_PITCH) {
            val y = plotY + plotH - ((pitch - MIN_PITCH) + 1) * rowHeight
            gc.stroke = if (pitch % 12 == 0) GRID_C_LINE else GRID_SUBTLE
            gc.lineWidth = if (pitch % 12 == 0) 1.0 else 0.5
            gc.strokeLine(plotX, y, plotX + plotW, y)
            pitch++
        }

        // Vertical time gridlines every second.
        gc.stroke = GRID_SUBTLE
        gc.lineWidth = 0.5
        val secondsInWindow = ROLL_WINDOW_MILLIS / 1000
        for (s in 0..secondsInWindow.toInt()) {
            val fraction = s.toDouble() / secondsInWindow.toDouble()
            val x = plotX + plotW - fraction * plotW
            gc.strokeLine(x, plotY, x, plotY + plotH)
        }

        // Plot outline.
        gc.stroke = AXIS_LINE
        gc.lineWidth = 1.0
        gc.strokeRect(plotX, plotY, plotW, plotH)
    }

    private fun drawPianoRollAxisLabels(
        gc: GraphicsContext,
        plotX: Double,
        plotY: Double,
        plotW: Double,
        plotH: Double
    ) {
        val pitchRange = (MAX_PITCH - MIN_PITCH).toDouble()
        val rowHeight = plotH / pitchRange

        // Left: label each C octave.
        gc.fill = TEXT_SECONDARY
        gc.font = Font.font("System", 10.0)
        gc.textAlign = TextAlignment.RIGHT
        gc.textBaseline = VPos.CENTER
        var pitch = MIN_PITCH
        while (pitch <= MAX_PITCH) {
            if (pitch % 12 == 0) {
                val octave = (pitch / 12) - 1
                val y = plotY + plotH - ((pitch - MIN_PITCH) + 0.5) * rowHeight
                gc.fillText("C$octave", plotX - 4.0, y)
            }
            pitch++
        }

        // Bottom: time markers ("-5 s", "-4 s", ..., "0 s").
        gc.textAlign = TextAlignment.CENTER
        gc.textBaseline = VPos.TOP
        val secondsInWindow = ROLL_WINDOW_MILLIS / 1000
        for (s in 0..secondsInWindow.toInt()) {
            val fraction = s.toDouble() / secondsInWindow.toDouble()
            val x = plotX + plotW - fraction * plotW
            val label = if (s == 0) "now" else "-${s}s"
            gc.fillText(label, x, plotY + plotH + 4.0)
        }

        // Y-axis title (rotated).
        gc.save()
        gc.translate(12.0, plotY + plotH / 2.0)
        gc.rotate(-90.0)
        gc.fill = TEXT_SECONDARY
        gc.font = Font.font("System", 11.0)
        gc.textAlign = TextAlignment.CENTER
        gc.textBaseline = VPos.CENTER
        gc.fillText("Pitch (C octaves)", 0.0, 0.0)
        gc.restore()
    }

    private fun timeToX(ns: Long, nowNs: Long, windowNs: Long, plotX: Double, plotW: Double): Double {
        val ageNs = nowNs - ns
        val fraction = 1.0 - (ageNs.toDouble() / windowNs.toDouble())
        return plotX + fraction.coerceIn(0.0, 1.0) * plotW
    }

    private fun drawSpectrum() {
        val gc = spectrumCanvas.graphicsContext2D
        val width = spectrumCanvas.width
        val height = spectrumCanvas.height
        gc.fill = BG_BOTTOM
        gc.fillRect(0.0, 0.0, width, height)

        val plotX = SPECTRUM_LEFT_MARGIN
        val plotY = SPECTRUM_TOP_MARGIN
        val plotW = width - plotX - SPECTRUM_RIGHT_MARGIN
        val plotH = height - plotY - SPECTRUM_BOTTOM_MARGIN
        if (plotW <= 0 || plotH <= 0) return

        val event = latestSpectrum.get()
        drawSpectrumHeader(gc, width, event)

        val magnitudes = event?.magnitudes
        val binHz = event?.binHz ?: 0.0f

        // Smooth magnitudes with an exponential decay so peaks are visible for a few frames.
        if (magnitudes != null) {
            if (smoothedMagnitudes.size != magnitudes.size) {
                smoothedMagnitudes = FloatArray(magnitudes.size)
            }
            for (i in magnitudes.indices) {
                val incoming = magnitudes[i]
                val previous = smoothedMagnitudes[i] * SMOOTHING_DECAY
                smoothedMagnitudes[i] = if (incoming > previous) incoming else previous
            }
        }

        if (magnitudes != null && binHz > 0.0f && smoothedMagnitudes.isNotEmpty()) {
            drawSpectrumBars(gc, plotX, plotY, plotW, plotH, binHz)
        }

        drawSpectrumGridAndAxes(gc, plotX, plotY, plotW, plotH, binHz)
    }

    private fun drawSpectrumBars(
        gc: GraphicsContext,
        plotX: Double,
        plotY: Double,
        plotW: Double,
        plotH: Double,
        binHz: Float
    ) {
        val mags = smoothedMagnitudes
        val minFreq = MIN_FREQ_DISPLAYED_HZ.toDouble()
        val maxFreq = MAX_FREQ_DISPLAYED_HZ.toDouble()
        val logMinFreq = log10(minFreq)
        val logMaxFreq = log10(maxFreq)
        val logSpan = logMaxFreq - logMinFreq

        val bandWidthPx = plotW / SPECTRUM_BAND_COUNT

        for (band in 0 until SPECTRUM_BAND_COUNT) {
            val bandStartFrac = band / SPECTRUM_BAND_COUNT.toDouble()
            val bandEndFrac = (band + 1) / SPECTRUM_BAND_COUNT.toDouble()
            val freqStart = 10.0.pow(logMinFreq + bandStartFrac * logSpan)
            val freqEnd = 10.0.pow(logMinFreq + bandEndFrac * logSpan)

            val binStart = (freqStart / binHz).toInt().coerceIn(0, mags.size - 1)
            val binEnd = (freqEnd / binHz).toInt().coerceIn(binStart, mags.size - 1)

            var peak = 0.0f
            for (b in binStart..binEnd) {
                if (mags[b] > peak) peak = mags[b]
            }

            val amp = amplitudeToUnit(peak)
            if (amp <= 0.0) continue
            val barH = amp * plotH
            val x = plotX + band * bandWidthPx
            val hue = 200.0 - bandStartFrac * 200.0
            gc.fill = Color.hsb(hue, 0.85, 0.4 + 0.6 * amp)
            gc.fillRect(x, plotY + plotH - barH, bandWidthPx + 0.5, barH)
        }
    }

    private fun amplitudeToUnit(magnitude: Float): Double {
        if (magnitude <= 0.0f) return 0.0
        val db = 20.0 * log10(magnitude.toDouble())
        val clamped = db.coerceIn(MIN_DB, MAX_DB)
        return (clamped - MIN_DB) / (MAX_DB - MIN_DB)
    }

    private fun drawSpectrumHeader(gc: GraphicsContext, width: Double, event: AudioSpectrumEvent?) {
        gc.fill = TEXT_PRIMARY
        gc.font = Font.font("System", 14.0)
        gc.textAlign = TextAlignment.LEFT
        gc.textBaseline = VPos.TOP
        gc.fillText("Audio spectrum (live FFT)", 8.0, 6.0)

        gc.fill = TEXT_SECONDARY
        gc.font = Font.font("System", 11.0)
        gc.textAlign = TextAlignment.RIGHT
        val statusText = if (event != null) {
            val sampleRateKHz = event.sampleRate / 1000.0
            "${"%.1f".format(sampleRateKHz)} kHz \u00b7 ${"%.1f".format(event.binHz)} Hz/bin"
        } else {
            "waiting for audio\u2026"
        }
        gc.fillText(statusText, width - 10.0, 8.0)
    }

    private fun drawSpectrumGridAndAxes(
        gc: GraphicsContext,
        plotX: Double,
        plotY: Double,
        plotW: Double,
        plotH: Double,
        @Suppress("UNUSED_PARAMETER") binHz: Float
    ) {
        // Plot outline.
        gc.stroke = AXIS_LINE
        gc.lineWidth = 1.0
        gc.strokeRect(plotX, plotY, plotW, plotH)

        val minFreq = MIN_FREQ_DISPLAYED_HZ.toDouble()
        val maxFreq = MAX_FREQ_DISPLAYED_HZ.toDouble()
        val logMinFreq = log10(minFreq)
        val logMaxFreq = log10(maxFreq)
        val logSpan = logMaxFreq - logMinFreq

        // Log-frequency gridlines with labels.
        gc.font = Font.font("System", 10.0)
        gc.textAlign = TextAlignment.CENTER
        gc.textBaseline = VPos.TOP
        for (freq in FREQ_GRID_HZ) {
            if (freq < MIN_FREQ_DISPLAYED_HZ || freq > MAX_FREQ_DISPLAYED_HZ) continue
            val fraction = (log10(freq.toDouble()) - logMinFreq) / logSpan
            val x = plotX + fraction * plotW
            gc.stroke = GRID_SUBTLE
            gc.lineWidth = 0.5
            gc.strokeLine(x, plotY, x, plotY + plotH)
            gc.fill = TEXT_SECONDARY
            val label = if (freq >= 1000.0f) "${(freq / 1000.0f).roundToInt()}k" else "${freq.roundToInt()}"
            gc.fillText(label, x, plotY + plotH + 4.0)
        }

        // dB reference lines.
        gc.textAlign = TextAlignment.RIGHT
        gc.textBaseline = VPos.CENTER
        for (db in DB_GRID) {
            val fraction = (db - MIN_DB) / (MAX_DB - MIN_DB)
            if (fraction < 0.0 || fraction > 1.0) continue
            val y = plotY + plotH - fraction * plotH
            gc.stroke = GRID_SUBTLE
            gc.lineWidth = 0.5
            gc.strokeLine(plotX, y, plotX + plotW, y)
            gc.fill = TEXT_SECONDARY
            gc.fillText("${db.roundToInt()} dB", plotX - 4.0, y)
        }

        // Axis titles.
        gc.textAlign = TextAlignment.CENTER
        gc.textBaseline = VPos.BOTTOM
        gc.fill = TEXT_SECONDARY
        gc.font = Font.font("System", 11.0)
        gc.fillText("Frequency (Hz, log)", plotX + plotW / 2.0, plotY + plotH + 26.0)

        gc.save()
        gc.translate(12.0, plotY + plotH / 2.0)
        gc.rotate(-90.0)
        gc.textAlign = TextAlignment.CENTER
        gc.textBaseline = VPos.CENTER
        gc.fillText("Magnitude (dB)", 0.0, 0.0)
        gc.restore()
    }

    private data class PianoRollNote(
        val pitch: Int,
        val startNs: Long,
        val endNs: Long?,
        val velocity: Int
    )

    companion object : MmxViewMetadata {
        override fun getViewTitle(): String = "Audio Spectrum + MIDI"
        override fun getViewDescription(): String =
            "Overlays a live audio-input spectrum with a scrolling MIDI piano-roll."

        override fun getViewImagePath(): String = "/view/midi.png"

        private const val ROLL_WINDOW_MILLIS: Long = 5_000
        private const val MIN_PITCH: Int = 21   // A0
        private const val MAX_PITCH: Int = 108  // C8

        private const val PIANO_ROLL_LEFT_MARGIN: Double = 46.0
        private const val PIANO_ROLL_RIGHT_MARGIN: Double = 10.0
        private const val PIANO_ROLL_TOP_MARGIN: Double = 28.0
        private const val PIANO_ROLL_BOTTOM_MARGIN: Double = 22.0

        private const val SPECTRUM_LEFT_MARGIN: Double = 46.0
        private const val SPECTRUM_RIGHT_MARGIN: Double = 10.0
        private const val SPECTRUM_TOP_MARGIN: Double = 28.0
        private const val SPECTRUM_BOTTOM_MARGIN: Double = 40.0

        private const val MIN_FREQ_DISPLAYED_HZ: Float = 40.0f
        private const val MAX_FREQ_DISPLAYED_HZ: Float = 16_000.0f
        private val FREQ_GRID_HZ = floatArrayOf(50f, 100f, 250f, 500f, 1000f, 2000f, 5000f, 10_000f)

        /** Number of visual bars in the log-frequency display. */
        private const val SPECTRUM_BAND_COUNT: Int = 96

        /** dB range mapped onto the vertical axis. */
        private const val MIN_DB: Double = -70.0
        private const val MAX_DB: Double = 0.0
        private val DB_GRID = doubleArrayOf(-60.0, -40.0, -20.0, 0.0)

        /** Per-frame decay factor for peak-hold smoothing (0..1, higher = slower decay). */
        private const val SMOOTHING_DECAY: Float = 0.85f

        private val BG_TOP: Color = Color.rgb(15, 24, 34)
        private val BG_BOTTOM: Color = Color.rgb(20, 28, 40)
        private val GRID_SUBTLE: Color = Color.rgb(40, 55, 70)
        private val GRID_C_LINE: Color = Color.rgb(80, 110, 140)
        private val AXIS_LINE: Color = Color.rgb(90, 120, 150)
        private val TEXT_PRIMARY: Color = Color.rgb(220, 230, 240)
        private val TEXT_SECONDARY: Color = Color.rgb(150, 170, 185)
    }
}
