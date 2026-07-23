package be.codewriter.melodymatrix.view.view.learnstrip

import be.codewriter.melodymatrix.view.component.ZoomableNode
import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.event.MidiDataEvent
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.MmxEventType
import be.codewriter.melodymatrix.view.event.PlayEvent
import be.codewriter.melodymatrix.view.i18n.I18n
import be.codewriter.melodymatrix.view.view.MmxView
import be.codewriter.melodymatrix.view.view.MmxViewMetadata
import be.codewriter.melodymatrix.view.view.sheet.SheetMusicAdapter
import com.sheetmusic4j.core.model.MusicElement
import com.sheetmusic4j.fxviewer.StripSheetView
import javafx.animation.AnimationTimer
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.layout.BorderPane
import javafx.scene.paint.Color
import javafx.util.Duration as FxDuration

/**
 * Scrolling one-line "learn" view: renders the currently-playing (or currently-input)
 * notes as a horizontal strip of notation with a fixed cursor and live per-note
 * highlighting.
 *
 * Two input paths are supported:
 *  - **Recording playback**: [PlayEvent]s dispatched all-at-once by
 *    [be.codewriter.melodymatrix.engine.recording.PlaybackEventScheduler] populate the
 *    strip with the full timeline before the first note actually sounds, so the user
 *    can see what is coming.
 *  - **Live MIDI input** (connected piano, MIDI simulator, …): NOTE_ON/NOTE_OFF pairs
 *    from [MidiDataEvent] are captured as they happen and appended to the score, so the
 *    strip grows in step with what is being played.
 *
 * In both paths, [MidiDataEvent]s are also used to highlight the notehead of the note
 * that is currently sounding (green while held, cleared on NOTE_OFF).
 *
 * `PLAYBACK_STOP` resets the strip to its initial waiting state.
 */
class LearnStripView : MmxView() {

    override val fitToViewport: Boolean = true

    private val bundle = I18n.registerBundle("i18n/view/learnstrip")

    private val strip = StripSheetView().apply {
        setCursorScreenPosition(0.3)
        setCursorColor(Color.CRIMSON)
    }

    private val hintLabel = Label().apply {
        textProperty().bind(I18n.binding(bundle, "learnstrip.label.waiting"))
        style = "-fx-font-size: 13; -fx-text-fill: -color-fg-muted;"
        padding = Insets(4.0, 0.0, 4.0, 8.0)
    }

    /**
     * Live capture state.
     *
     * `capturedEvents` is the authoritative timeline the score is (re-)built from. It
     * is fed by two producers:
     *  - PLAY events (recording playback timeline, arrives as a burst up front); and
     *  - completed NOTE_ON/NOTE_OFF pairs from live MIDI input.
     *
     * The two producers are mutually exclusive per playback session: once a PLAY event
     * has been seen we switch to `playbackMode` and stop capturing MIDI-derived events
     * to avoid duplicating the same notes twice.
     */
    private val capturedEvents = mutableListOf<PlayEvent>()

    /** NOTE_ON events awaiting their matching NOTE_OFF to become a captured [PlayEvent]. */
    private val pendingByMidi = HashMap<Int, PendingNote>()

    /**
     * Notes currently sounding, mapped to the [MusicElement] whose highlight has been
     * set. Kept so a NOTE_OFF can clear exactly the highlight the corresponding NOTE_ON
     * created — including the case where the score is re-engraved between ON and OFF.
     */
    private val activeHighlights = HashMap<Int, MusicElement>()

    /** True when we have received PLAY events → the timeline is authored by playback. */
    private var playbackMode: Boolean = false

    /** Wall-clock ms of the very first note in the current session — anchors the cursor. */
    private var timelineStartMs: Long = 0L

    /** Milliseconds per quarter note used by the cursor animation. */
    private var msPerQuarter: Double = 60_000.0 / SheetMusicAdapter.DEFAULT_BPM

    /** Score → PlayEvent map produced by the last score rebuild. */
    private var elementByPlayEvent: Map<PlayEvent, MusicElement> = emptyMap()

    /** True while a score rebuild has already been queued for the next FX pulse. */
    private var rebuildPending: Boolean = false

    /** True once at least one event has been captured and a score engraved. */
    private var scoreEngraved: Boolean = false

    private val cursorAnimation = object : AnimationTimer() {
        override fun handle(now: Long) {
            if (!scoreEngraved) return
            val elapsedMs = System.currentTimeMillis() - timelineStartMs
            if (elapsedMs <= 0) {
                strip.cursorTimeProperty().set(0.0)
                return
            }
            strip.cursorTimeProperty().set(elapsedMs / msPerQuarter)
        }
    }

    init {
        val zoomable = ZoomableNode(
            content = strip,
            naturalWidth = NATURAL_WIDTH,
            naturalHeight = NATURAL_HEIGHT,
            minWidthValue = 240.0,
            minHeightValue = 80.0,
            fitMode = ZoomableNode.FitMode.CONTAIN
        )
        val root = BorderPane().apply {
            top = hintLabel
            center = zoomable
            padding = Insets(0.0)
            // Opaque white background so areas the strip does not draw over (e.g. the
            // empty space past the end of a short score) are painted cleanly instead of
            // leaving the previous canvas contents smeared as the strip scrolls left.
            style = "-fx-background-color: white;"
        }
        setupSurface(root, NATURAL_WIDTH, NATURAL_HEIGHT + HINT_HEIGHT, strip, onDispose = {
            cursorAnimation.stop()
        })
    }

    override fun onEvent(event: MmxEvent) {
        when (event.type) {
            MmxEventType.PLAY -> {
                val play = event as? PlayEvent ?: return
                if (play.note == Note.UNDEFINED) return
                Platform.runLater { handlePlayEvent(play) }
            }

            MmxEventType.MIDI -> {
                val midi = event as? MidiDataEvent ?: return
                if (midi.isDrum || midi.note == Note.UNDEFINED) return
                Platform.runLater { handleMidiEvent(midi) }
            }

            MmxEventType.PLAYBACK_STOP -> Platform.runLater { reset() }

            MmxEventType.CHORD,
            MmxEventType.AUDIO_SPECTRUM,
            MmxEventType.SCORE_LOADED -> {
                // Not used here
            }
            }
            }

    /**
     * Handle a PLAY event: switch into playback mode (so MIDI events stop double-adding
     * the same notes) and append the event to the captured timeline.
     *
     * PLAY events carry `startTime` in wall-clock nanoseconds; we normalise to
     * milliseconds and anchor the cursor at the earliest event we have seen.
     */
    private fun handlePlayEvent(play: PlayEvent) {
        val startMs = play.startTime / 1_000_000L
        if (!playbackMode) {
            playbackMode = true
            // Any live-captured notes accumulated before the first PLAY event were from a
            // previous session (or a stale keyboard); the recording is now authoritative.
            capturedEvents.clear()
            pendingByMidi.clear()
            timelineStartMs = startMs
        } else if (startMs < timelineStartMs) {
            timelineStartMs = startMs
        }
        capturedEvents.add(play.copy(startTime = startMs))
        scheduleRebuild()
    }

    /**
     * Handle a live MIDI event.
     *
     *  - NOTE_ON (velocity > 0): remember start time so we can finalise the length on
     *    NOTE_OFF; also drive the "currently sounding" highlight.
     *  - NOTE_OFF (or NOTE_ON with velocity 0): finalise a captured [PlayEvent] in live
     *    mode; drop the highlight in both live and playback mode.
     */
    private fun handleMidiEvent(midi: MidiDataEvent) {
        val nowMs = System.currentTimeMillis()
        val isNoteOn = midi.event == MidiEvent.NOTE_ON && midi.velocity > 0
        val isNoteOff = midi.event == MidiEvent.NOTE_OFF ||
                (midi.event == MidiEvent.NOTE_ON && midi.velocity == 0)

        val midiNumber = midi.note.byteValue.toInt() and 0x7F
        when {
            isNoteOn -> {
                pendingByMidi[midiNumber] = PendingNote(midi.note, nowMs, midi.velocity)
                highlightNoteOn(midi.note)
            }

            isNoteOff -> {
                val pending = pendingByMidi.remove(midiNumber)
                if (pending != null && !playbackMode) {
                    // Live-capture: turn the completed pair into a captured PlayEvent.
                    val durationMs = (nowMs - pending.startTimeMs).coerceAtLeast(1L)
                    val play = PlayEvent(
                        note = pending.note,
                        startTime = pending.startTimeMs,
                        duration = FxDuration.millis(durationMs.toDouble()),
                        velocity = pending.velocity
                    )
                    if (capturedEvents.isEmpty()) {
                        timelineStartMs = pending.startTimeMs
                    }
                    capturedEvents.add(play)
                    scheduleRebuild()
                }
                highlightNoteOff(midi.note)
            }
        }
    }

    private fun highlightNoteOn(note: Note) {
        val midiNumber = note.byteValue.toInt() and 0x7F
        // If the score has not been engraved yet (very first live NOTE_ON) there is
        // nothing to highlight — the note will be picked up on the next rebuild.
        val element = findElementForNote(note) ?: return
        strip.noteHighlights()[element] = ACTIVE_COLOR
        activeHighlights[midiNumber] = element
    }

    private fun highlightNoteOff(note: Note) {
        val midiNumber = note.byteValue.toInt() and 0x7F
        val element = activeHighlights.remove(midiNumber) ?: return
        strip.noteHighlights().remove(element)
    }

    /**
     * Best-effort lookup of the [MusicElement] representing [note] in the current score.
     *
     * For recording playback we look up by "the earliest captured event for this MIDI
     * number that is close to the wall clock right now". For live mode we take the
     * most recently captured event with this MIDI number.
     */
    private fun findElementForNote(note: Note): MusicElement? {
        if (elementByPlayEvent.isEmpty()) return null
        val midiNumber = note.byteValue.toInt()
        val candidates = capturedEvents.filter { it.note.byteValue.toInt() == midiNumber }
        if (candidates.isEmpty()) return null
        val target = if (playbackMode) {
            val nowMs = System.currentTimeMillis()
            candidates.minByOrNull { kotlin.math.abs(it.startTime - nowMs) } ?: return null
        } else {
            candidates.last()
        }
        return elementByPlayEvent[target]
    }

    private fun scheduleRebuild() {
        if (rebuildPending) return
        rebuildPending = true
        Platform.runLater {
            rebuildPending = false
            rebuildScore()
        }
    }

    private fun rebuildScore() {
        if (capturedEvents.isEmpty()) return

        // Feed the adapter zero-based ms timestamps so quantization is stable across
        // sessions (adapter uses `startTime - minStartTime` internally as well, but
        // being explicit keeps the mapping straight for tests).
        val originMs = capturedEvents.minOf { it.startTime }
        val normalized = capturedEvents.map { evt -> evt.copy(startTime = evt.startTime - originMs) }
        val originalToNormalized = capturedEvents.zip(normalized).toMap()

        val result = SheetMusicAdapter.playEventsToScore(normalized, bpm = SheetMusicAdapter.DEFAULT_BPM)

        val newMapping = HashMap<PlayEvent, MusicElement>(capturedEvents.size)
        for (original in capturedEvents) {
            val norm = originalToNormalized[original] ?: continue
            val element = result.elementByPlayEvent[norm] ?: continue
            newMapping[original] = element
        }
        elementByPlayEvent = newMapping

        strip.setScore(result.score)
        msPerQuarter = 60_000.0 / result.bpm.coerceAtLeast(1)

        if (!scoreEngraved) {
            scoreEngraved = true
            cursorAnimation.start()
        }

        // Re-apply currently sounding highlights to the freshly engraved elements — the
        // previous `MusicElement` instances have been replaced by the rebuild.
        if (activeHighlights.isNotEmpty()) {
            val stillActive = pendingByMidi.keys.toList()
            activeHighlights.clear()
            strip.noteHighlights().clear()
            stillActive.forEach { midiNumber ->
                val pending = pendingByMidi[midiNumber] ?: return@forEach
                highlightNoteOn(pending.note)
            }
        }
    }

    private fun reset() {
        cursorAnimation.stop()
        capturedEvents.clear()
        pendingByMidi.clear()
        activeHighlights.clear()
        elementByPlayEvent = emptyMap()
        playbackMode = false
        scoreEngraved = false
        rebuildPending = false
        timelineStartMs = 0L
        strip.cursorTimeProperty().set(0.0)
        strip.noteHighlights().clear()
    }

    private data class PendingNote(
        val note: Note,
        val startTimeMs: Long,
        val velocity: Int
    )

    companion object : MmxViewMetadata {
        override val bundleBaseName = "i18n/view/learnstrip"
        override fun getViewImagePath(): String = "/view/scale.png"

        private const val NATURAL_WIDTH: Double = 1000.0
        private const val NATURAL_HEIGHT: Double = 200.0
        private const val HINT_HEIGHT: Double = 24.0

        private val ACTIVE_COLOR: Color = Color.LIMEGREEN
    }
}
