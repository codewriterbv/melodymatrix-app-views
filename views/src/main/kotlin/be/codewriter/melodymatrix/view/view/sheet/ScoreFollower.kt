package be.codewriter.melodymatrix.view.view.sheet

import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.event.MidiDataEvent
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.MmxEventType
import be.codewriter.melodymatrix.view.event.PlayEvent
import be.codewriter.melodymatrix.view.event.PlaybackPositionEvent
import be.codewriter.melodymatrix.view.event.ScoreLoadedEvent
import com.sheetmusic4j.core.model.Chord
import com.sheetmusic4j.core.model.MusicElement
import com.sheetmusic4j.core.model.Score
import com.sheetmusic4j.engraving.layout.NoteAnchor
import javafx.animation.AnimationTimer
import javafx.application.Platform
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import java.time.Duration
import kotlin.math.abs
import com.sheetmusic4j.core.model.Note as ScoreNote

/**
 * The play-along logic shared by the scrolling notation views (the play-along strip and the
 * fly-over): which score to show, where the cursor is and which notes sound.
 *
 * Three sources of music are followed:
 *  - **A loaded score** ([ScoreLoadedEvent]): shown as written; the cursor follows the song
 *    position of the [PlaybackPositionEvent]s, or the timing of the PLAY events when the host
 *    sends no positions.
 *  - **Recording playback without a score**: the [PlayEvent]s, which arrive as a burst before
 *    the first note sounds, are engraved into a score so the whole timeline is known up front.
 *  - **Live MIDI input**: completed NOTE_ON/NOTE_OFF pairs are appended to the score as they
 *    are played.
 *
 * [MidiDataEvent]s also drive the "currently sounding" highlights. With
 * [SheetDisplayMode.NOTE_BY_NOTE] the music of a loaded score or a playback timeline only
 * appears once the cursor has reached it (a live capture only holds played notes anyway).
 *
 * `PLAYBACK_STOP` resets to the waiting state (a loaded score stays visible).
 * Call [onEvent] from any thread; everything else runs on the FX thread.
 */
class ScoreFollower(private val target: Target) {

    /** What a view does with the follower's decisions; all calls happen on the FX thread. */
    interface Target {
        /** Show [score] instead of the current one (engrave it). */
        fun showScore(score: Score)

        /** Anchors of the engraved score, empty when nothing has been engraved. */
        fun noteAnchors(): List<NoteAnchor>

        /**
         * Move the cursor to [quarters] and show only the music starting at or before
         * [revealQuarters] (positive infinity shows everything).
         */
        fun moveCursor(quarters: Double, revealQuarters: Double)

        /** Mark [element] as sounding. */
        fun highlight(element: MusicElement) {}

        /** [element] stopped sounding. */
        fun unhighlight(element: MusicElement) {}

        /** Remove every sounding mark. */
        fun clearHighlights() {}
    }

    /** Full score with a moving marker, or note by note. */
    val displayModeProperty: ObjectProperty<SheetDisplayMode> =
        SimpleObjectProperty(SheetDisplayMode.FULL_SCORE)

    private val clock = ScorePlaybackClock()

    /**
     * The timeline the score is (re-)built from when no score was loaded: PLAY events of a
     * playback, or completed NOTE_ON/NOTE_OFF pairs of live input. Once a PLAY event has been
     * seen, live notes are no longer captured, so the same notes are not added twice.
     */
    private val capturedEvents = mutableListOf<PlayEvent>()

    /** NOTE_ON events awaiting their matching NOTE_OFF to become a captured [PlayEvent]. */
    private val pendingByMidi = HashMap<Int, PendingNote>()

    /**
     * Notes currently sounding, mapped to the [MusicElement] that was highlighted, so a NOTE_OFF
     * clears exactly the highlight its NOTE_ON created, also when the score was rebuilt in between.
     */
    private val activeHighlights = HashMap<Int, MusicElement>()

    /** Score → PlayEvent map produced by the last rebuild. */
    private var elementByPlayEvent: Map<PlayEvent, MusicElement> = emptyMap()

    /** True while a rebuild has already been queued for the next FX pulse. */
    private var rebuildPending: Boolean = false

    /** True once the cursor runs: a score was engraved from events, or a loaded score is followed. */
    private var cursorRunning: Boolean = false

    /** Authored score from the last [ScoreLoadedEvent]; when set, PLAY events no longer rebuild. */
    private var loadedScore: Score? = null

    /** Set on dispose; events queued with runLater may still arrive and must not restart the timer. */
    private var disposed: Boolean = false

    private val cursorAnimation = object : AnimationTimer() {
        override fun handle(now: Long) {
            pushCursor()
        }
    }

    init {
        displayModeProperty.addListener { _, _, _ -> pushCursor() }
    }

    /** Handles an event from the event bus; safe to call from any thread. */
    fun onEvent(event: MmxEvent) {
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

            MmxEventType.PLAYBACK_POSITION -> {
                val position = event as? PlaybackPositionEvent ?: return
                Platform.runLater { handlePosition(position) }
            }

            MmxEventType.SCORE_LOADED -> {
                val loaded = event as? ScoreLoadedEvent ?: return
                Platform.runLater { showLoadedScore(loaded) }
            }

            MmxEventType.CHORD,
            MmxEventType.AUDIO_SPECTRUM -> {
                // Not used here
            }
        }
    }

    /** Stops the cursor animation; later events are ignored. */
    fun dispose() {
        disposed = true
        cursorAnimation.stop()
    }

    /**
     * Handle a PLAY event: switch into playback mode (so MIDI events stop adding the same notes
     * again) and append the event to the timeline. PLAY events carry `startTime` in wall-clock
     * nanoseconds.
     */
    private fun handlePlayEvent(play: PlayEvent) {
        if (disposed) return
        val startMs = play.startTime / 1_000_000L
        if (clock.onPlay(startMs, play.duration.toMillis())) {
            // Live notes captured before the first PLAY event were from a previous session (or a
            // stale keyboard); the recording is now authoritative.
            capturedEvents.clear()
            pendingByMidi.clear()
        }
        capturedEvents.add(play.copy(startTime = startMs))
        if (loadedScore != null) {
            startCursor()
        } else {
            scheduleRebuild()
        }
    }

    /** Playback (re)started at a song position: the cursor of a loaded score follows it. */
    private fun handlePosition(position: PlaybackPositionEvent) {
        if (disposed) return
        clock.onPosition(position)
        if (loadedScore != null) {
            startCursor()
        }
    }

    /** Shows an authored score as written; the cursor follows the playback that comes with it. */
    private fun showLoadedScore(event: ScoreLoadedEvent) {
        if (disposed) return
        loadedScore = event.score
        target.showScore(event.score)
        target.clearHighlights()
        activeHighlights.clear()
        val anchors = soundingAnchors()
        clock.loadScore(
            firstOnset = anchors.firstOrNull()?.onsetQuarters() ?: 0.0,
            lastEnd = anchors.maxOfOrNull { it.endQuarters() } ?: 0.0,
            bpm = event.bpm,
            songOffsetMs = event.songOffsetMs
        )
        if (clock.following) {
            startCursor()
        } else {
            pushCursor()
        }
    }

    private fun startCursor() {
        if (!cursorRunning) {
            cursorRunning = true
            if (!disposed) cursorAnimation.start()
        }
    }

    /** Hands the current cursor and reveal position to the target. */
    private fun pushCursor() {
        if (disposed) return
        val quarters = if (cursorRunning) clock.quartersAt(System.currentTimeMillis()) else 0.0
        target.moveCursor(quarters, revealQuarters(quarters))
    }

    /**
     * Up to where the music is shown: everything in [SheetDisplayMode.FULL_SCORE] and for a live
     * capture (which only holds played notes), otherwise what the cursor has reached (nothing
     * before the playback starts).
     */
    private fun revealQuarters(cursor: Double): Double {
        val followsTimeline = loadedScore != null || clock.playbackSeen
        return when {
            displayModeProperty.get() == SheetDisplayMode.FULL_SCORE || !followsTimeline -> Double.POSITIVE_INFINITY
            !cursorRunning -> NOTHING_REVEALED
            else -> cursor + REVEAL_LEAD_QUARTERS
        }
    }

    /**
     * Handle a live MIDI event: a NOTE_ON starts a note and highlights it; a NOTE_OFF (or
     * NOTE_ON with velocity 0) completes a live-captured note and drops the highlight.
     */
    private fun handleMidiEvent(midi: MidiDataEvent) {
        if (disposed) return
        val nowMs = System.currentTimeMillis()
        val isNoteOn = midi.event == MidiEvent.NOTE_ON && midi.velocity > 0
        val isNoteOff = midi.event == MidiEvent.NOTE_OFF ||
                (midi.event == MidiEvent.NOTE_ON && midi.velocity == 0)

        val midiNumber = midi.note.byteValue and 0x7F
        when {
            isNoteOn -> {
                pendingByMidi[midiNumber] = PendingNote(midi.note, nowMs, midi.velocity)
                highlightNoteOn(midi.note)
            }

            isNoteOff -> {
                val pending = pendingByMidi.remove(midiNumber)
                if (pending != null && !clock.playbackSeen && loadedScore == null) {
                    val durationMs = (nowMs - pending.startTimeMs).coerceAtLeast(1L)
                    val play = PlayEvent(
                        note = pending.note,
                        startTime = pending.startTimeMs,
                        duration = Duration.ofMillis(durationMs),
                        velocity = pending.velocity
                    )
                    if (capturedEvents.isEmpty()) {
                        clock.startLive(pending.startTimeMs)
                    }
                    capturedEvents.add(play)
                    scheduleRebuild()
                }
                highlightNoteOff(midi.note)
            }
        }
    }

    private fun highlightNoteOn(note: Note) {
        val midiNumber = note.byteValue and 0x7F
        // Before the first engrave there is nothing to highlight; the next rebuild picks it up.
        val element = findElementForNote(note) ?: return
        activeHighlights.remove(midiNumber)?.let { target.unhighlight(it) }
        target.highlight(element)
        activeHighlights[midiNumber] = element
    }

    private fun highlightNoteOff(note: Note) {
        val midiNumber = note.byteValue and 0x7F
        val element = activeHighlights.remove(midiNumber) ?: return
        target.unhighlight(element)
    }

    /**
     * The [MusicElement] that represents [note] now: in a loaded score the one at the cursor; in
     * a playback timeline the event of this pitch closest to the wall clock; live, the latest.
     */
    private fun findElementForNote(note: Note): MusicElement? {
        if (loadedScore != null) return findLoadedElementForNote(note)
        if (elementByPlayEvent.isEmpty()) return null
        val midiNumber = note.byteValue
        val candidates = capturedEvents.filter { it.note.byteValue == midiNumber }
        if (candidates.isEmpty()) return null
        val target = if (clock.playbackSeen) {
            val nowMs = System.currentTimeMillis()
            candidates.minByOrNull { abs(it.startTime - nowMs) } ?: return null
        } else {
            candidates.last()
        }
        return elementByPlayEvent[target]
    }

    /** In a loaded score: the note or chord with this pitch whose onset is closest to the cursor. */
    private fun findLoadedElementForNote(note: Note): MusicElement? {
        if (!clock.following) return null
        val midiNumber = note.byteValue and 0x7F
        val now = clock.quartersAt(System.currentTimeMillis())
        return soundingAnchors()
            .filter { anchor -> pitchesOf(anchor.elementRef()).contains(midiNumber) }
            .minByOrNull { anchor -> abs(anchor.onsetQuarters() - now) }
            ?.elementRef()
    }

    /** Notes and chords of the engraved score, sorted by onset. */
    private fun soundingAnchors(): List<NoteAnchor> =
        target.noteAnchors()
            .filter { it.durationQuarters() > 0 && (it.elementRef() is ScoreNote || it.elementRef() is Chord) }
            .sortedBy { it.onsetQuarters() }

    private fun pitchesOf(element: MusicElement): List<Int> = when (element) {
        is ScoreNote -> listOf(element.pitch().toMidiNumber())
        is Chord -> element.notes().map { it.pitch().toMidiNumber() }
        else -> emptyList()
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
        if (disposed || capturedEvents.isEmpty() || loadedScore != null) return

        // Zero-based ms timestamps keep the quantization stable across sessions.
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

        target.showScore(result.score)
        clock.msPerQuarter = 60_000.0 / result.bpm.coerceAtLeast(1)
        startCursor()

        // Re-apply the sounding highlights to the new elements: the rebuild replaced them.
        if (activeHighlights.isNotEmpty()) {
            val stillActive = pendingByMidi.keys.toList()
            activeHighlights.clear()
            target.clearHighlights()
            stillActive.forEach { midiNumber ->
                val pending = pendingByMidi[midiNumber] ?: return@forEach
                highlightNoteOn(pending.note)
            }
        }
        pushCursor()
    }

    private fun reset() {
        if (disposed) return
        cursorAnimation.stop()
        capturedEvents.clear()
        pendingByMidi.clear()
        activeHighlights.clear()
        elementByPlayEvent = emptyMap()
        cursorRunning = false
        rebuildPending = false
        clock.reset()
        target.clearHighlights()
        pushCursor()
    }

    private data class PendingNote(
        val note: Note,
        val startTimeMs: Long,
        val velocity: Int
    )

    companion object {
        /** Reveal time before the playback starts: before the first note. */
        const val NOTHING_REVEALED: Double = -1.0

        /**
         * How far ahead of the cursor notes are revealed, in quarters: a note appears as it is
         * played, also when the cursor clock runs a few milliseconds behind the sound.
         */
        const val REVEAL_LEAD_QUARTERS: Double = 0.05
    }
}
