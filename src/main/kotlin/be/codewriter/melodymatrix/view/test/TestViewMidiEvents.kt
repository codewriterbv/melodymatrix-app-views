package be.codewriter.melodymatrix.view.test

import be.codewriter.melodymatrix.view.component.TickerSlider
import be.codewriter.melodymatrix.view.definition.*
import be.codewriter.melodymatrix.view.event.ChordEvent
import be.codewriter.melodymatrix.view.event.MidiDataEvent
import javafx.beans.value.ObservableValue
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * Test control panel for emitting simulated MIDI and chord events.
 *
 * Provides buttons and sliders to drive [MidiSimulator] with note sequences,
 * random chords, program changes, controller messages, pitch-bend events,
 * and a simple high-frequency FPS stress test.
 *
 * @property midiSimulator The simulator used to broadcast events to active visualizers
 * @see MidiSimulator
 * @see TestView
 */
class TestViewMidiEvents(val midiSimulator: MidiSimulator) : VBox() {

    private val logger: Logger = LogManager.getLogger(TestViewMidiEvents::class.java.name)

    private enum class ChordPlaybackMode { NONE, RANDOM, RELATED }

    private val random = Random(System.currentTimeMillis())
    private val randomChordScheduler = Executors.newSingleThreadScheduledExecutor()
    private var randomChordTask: ScheduledFuture<*>? = null
    private var activeChord: Chord = Chord.UNDEFINED
    private var chordDelayMillis: Long = 500
    private var relatedChordIndex: Int = 0
    private var chordPlaybackMode: ChordPlaybackMode = ChordPlaybackMode.NONE

    private val relatedChordProgression = listOf(
        Chord.C_MAJOR,
        Chord.G_DOMINANT_SEVENTH,
        Chord.D_MINOR,
        Chord.F_MAJOR,
        Chord.C_DOMINANT_SEVENTH,
        Chord.G_MINOR,
        Chord.D_DOMINANT_SEVENTH,
        Chord.A_DIMINISHED_SEVENTH,
        Chord.F_DOMINANT_SEVENTH,
        Chord.C_MINOR,
        Chord.G_DOMINANT_SEVENTH,
        Chord.D_DIMINISHED_SEVENTH,
        Chord.A_SHARP_DOMINANT_SEVENTH, // Bb7 enharmonic
        Chord.F_MINOR,
        Chord.C_DOMINANT_SEVENTH,
        Chord.G_DIMINISHED_SEVENTH
    )

    init {
        spacing = 10.0
        padding = Insets(20.0)

        val slider = TickerSlider().apply {
            min = 250.0
            max = 5000.0
            value = 500.0
            valueProperty().addListener { _: ObservableValue<out Number>?, _: Number, newValue: Number ->
                chordDelayMillis = newValue.toLong()
                midiSimulator.setDelay(chordDelayMillis)
                restartRandomChordPlaybackIfRunning()
            }
        }

        children.setAll(
            Label("Midi events selection"),
            slider,
            createButton(
                "Play all notes (repeat)", Note.entries
                    .toList()
                    .sortedWith(compareBy({ it.octave }, { it.mainNote.sortingKey })), true
            ),
            createButton(
                "Play all notes random (repeat)", Note.entries
                    .toList()
                    .shuffled(), true
            ),
            Button("Play random chords (repeat)").apply {
                minWidth = 200.0
                setOnMouseClicked { _ ->
                    midiSimulator.stop()
                    startRandomChordPlayback()
                }
            },
            Button("Play related chords (repeat)").apply {
                minWidth = 200.0
                setOnMouseClicked { _ ->
                    midiSimulator.stop()
                    startRelatedChordPlayback()
                }
            },
            createButton(
                "Play C5 only (once)",
                Collections.singletonList(Note.C5),
                false
            ),
            createButton(
                "Play all from octave 5 (repeat)",
                Note.entries.stream()
                    .filter { n -> n.octave == Octave.OCTAVE_5 }
                    .toList()
                    .sortedBy { it.mainNote.sortingKey },
                true
            ),
            Button("Stop notes").apply {
                minWidth = 200.0
                setOnMouseClicked { _ ->
                    midiSimulator.stop()
                    stopRandomChordPlayback()
                }
            },
            createButton(
                "Set instrument 5",
                MidiDataEvent(byteArrayOf("11000100".toInt(2).toByte(), 0x05, 0x00))
            ),
            createButton(
                "Set instrument 9",
                MidiDataEvent(byteArrayOf("11000100".toInt(2).toByte(), 0x09, 0x00))
            ),
            createButton(
                "Send controller message",
                MidiDataEvent(byteArrayOf("10110000".toInt(2).toByte(), 0x21, 0x34))
            ),
            createButton(
                "Send pitch bend message",
                MidiDataEvent(byteArrayOf("11100000".toInt(2).toByte(), 0x00, 0x60))
            ),
            Button("Start FPS test").apply {
                minWidth = 200.0
                setOnMouseClicked { _ ->
                    stopRandomChordPlayback()
                    midiSimulator.setNotes(
                        Note.entries
                            .toList()
                            .shuffled(), true
                    )
                    slider.value = 300.0
                    midiSimulator.setDelay(slider.value.toLong())
                }
            }
        )
    }

    /**
     * Creates a button that, when clicked, starts playback of the provided note list.
     *
     * @param label  Button caption
     * @param notes  Sequence of notes to play
     * @param repeat Whether the sequence should loop
     * @return A configured [Button] node
     */
    private fun createButton(
        label: String,
        notes: List<Note>,
        repeat: Boolean
    ): Node {
        val view = Button(label).apply {
            minWidth = 200.0
            setOnMouseClicked { _ ->
                stopRandomChordPlayback()
                midiSimulator.setNotes(notes, repeat)
            }
        }

        return view
    }

    /**
     * Creates a button that sends a single raw [MidiDataEvent] when clicked.
     *
     * @param label         Button caption
     * @param midiDataEvent Event to send
     * @return A configured [Button] node
     */
    private fun createButton(label: String, midiDataEvent: MidiDataEvent): Node {
        val view = Button(label).apply {
            minWidth = 200.0
            setOnMouseClicked { _ ->
                stopRandomChordPlayback()
                midiSimulator.notifyListeners(midiDataEvent)
            }
        }

        return view
    }

    /**
     * Starts periodic random chord playback at the current [chordDelayMillis] interval.
     */
    private fun startRandomChordPlayback() {
        stopRandomChordPlayback()
        chordPlaybackMode = ChordPlaybackMode.RANDOM
        playRandomChord()
        randomChordTask = randomChordScheduler.scheduleWithFixedDelay(
            { playRandomChord() },
            chordDelayMillis,
            chordDelayMillis,
            TimeUnit.MILLISECONDS
        )
    }

    /**
     * Starts periodic playback of a fixed related-chord progression.
     */
    private fun startRelatedChordPlayback() {
        stopRandomChordPlayback()
        chordPlaybackMode = ChordPlaybackMode.RELATED
        relatedChordIndex = 0
        playNextRelatedChord()
        randomChordTask = randomChordScheduler.scheduleWithFixedDelay(
            { playNextRelatedChord() },
            chordDelayMillis,
            chordDelayMillis,
            TimeUnit.MILLISECONDS
        )
    }

    private fun playNextRelatedChord() {
        sendChord(activeChord, false)
        val nextChord = relatedChordProgression[relatedChordIndex]
        logger.info("Playing related chord: $nextChord")
        sendChord(nextChord, true)
        activeChord = nextChord
        relatedChordIndex = (relatedChordIndex + 1) % relatedChordProgression.size
    }

    /**
     * Restarts random chord playback if it is currently active.
     *
     * Used when the delay slider changes.
     */
    private fun restartRandomChordPlaybackIfRunning() {
        if (randomChordTask == null) return
        when (chordPlaybackMode) {
            ChordPlaybackMode.RANDOM -> startRandomChordPlayback()
            ChordPlaybackMode.RELATED -> startRelatedChordPlayback()
            ChordPlaybackMode.NONE -> Unit
        }
    }

    /**
     * Stops random chord playback and sends a chord-off event for the active chord.
     */
    private fun stopRandomChordPlayback() {
        randomChordTask?.cancel(false)
        randomChordTask = null
        sendChord(activeChord, false)
        activeChord = Chord.UNDEFINED
        chordPlaybackMode = ChordPlaybackMode.NONE
    }

    /**
     * Chooses a new random chord and emits chord-off/chord-on transitions.
     */
    private fun playRandomChord() {
        sendChord(activeChord, false)
        val nextChord = Chord.entries.filter { it != Chord.UNDEFINED }.random(random)
        sendChord(nextChord, true)
        activeChord = nextChord
    }

    /**
     * Resolves concrete notes for the given chord in the middle register.
     *
     * Currently supports major and minor triads for the random-chord tester.
     *
     * @param chord The chord to expand
     * @return Notes that compose the chord
     */
    private fun chordNotes(chord: Chord): List<Note> {
        if (chord == Chord.UNDEFINED) return emptyList()

        val intervals = when (chord.quality) {
            ChordQuality.MAJOR -> listOf(0, 4, 7)
            ChordQuality.MINOR -> listOf(0, 3, 7)
            ChordQuality.DOMINANT -> listOf(0, 4, 7, 10)
            ChordQuality.DIMINISHED,
            ChordQuality.HALF_DIMINISHED -> when (chord.extension) {
                ChordExtension.DIMINISHED_SEVENTH -> listOf(0, 3, 6, 9)
                ChordExtension.MINOR_SEVENTH -> listOf(0, 3, 6, 10)
                else -> listOf(0, 3, 6)
            }

            ChordQuality.TRITONE -> listOf(0, 6)
        }.toMutableList()

        when (chord.extension) {
            ChordExtension.MAJOR_NINTH -> {
                if (!intervals.contains(11)) intervals.add(11)
                if (!intervals.contains(2)) intervals.add(2)
            }

            ChordExtension.DOMINANT_NINTH,
            ChordExtension.MINOR_NINTH -> {
                if (!intervals.contains(10)) intervals.add(10)
                if (!intervals.contains(2)) intervals.add(2)
            }

            else -> Unit
        }

        val root = 48 + chord.pitchClass
        return intervals.mapNotNull { interval ->
            val candidate = Note.from((root + interval).toByte())
            if (candidate == Note.UNDEFINED) null else candidate
        }
    }

    /**
     * Emits a [ChordEvent] and corresponding NOTE_ON/NOTE_OFF events for its notes.
     *
     * @param chord Chord to emit
     * @param on    True for chord-on, false for chord-off
     */
    private fun sendChord(chord: Chord, on: Boolean) {
        midiSimulator.notifyListeners(ChordEvent(chord, on))
        chordNotes(chord).forEach { note ->
            midiSimulator.notifyListeners(
                MidiDataEvent(
                    byteArrayOf(
                        if (on) "10010000".toInt(2).toByte() else "10000000".toInt(2).toByte(),
                        note.byteValue.toByte(),
                        if (on) random.nextInt(60, 127).toByte() else 0
                    )
                )
            )
        }
    }
}
