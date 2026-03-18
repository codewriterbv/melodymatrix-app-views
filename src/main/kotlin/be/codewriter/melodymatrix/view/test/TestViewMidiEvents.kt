package be.codewriter.melodymatrix.view.test

import be.codewriter.melodymatrix.view.definition.Chord
import be.codewriter.melodymatrix.view.definition.ChordExtension
import be.codewriter.melodymatrix.view.definition.ChordQuality
import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.definition.Octave
import be.codewriter.melodymatrix.view.event.MidiDataEvent
import javafx.beans.value.ObservableValue
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.Slider
import javafx.scene.layout.VBox
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class TestViewMidiEvents(val midiSimulator: MidiSimulator) : VBox() {

    private val random = Random(System.currentTimeMillis())
    private val randomChordScheduler = Executors.newSingleThreadScheduledExecutor()
    private var randomChordTask: ScheduledFuture<*>? = null
    private val randomTriadChords = Chord.entries.filter {
        it != Chord.UNDEFINED &&
            it.extension == ChordExtension.NONE &&
            (it.quality == ChordQuality.MAJOR || it.quality == ChordQuality.MINOR)
    }
    private var activeChord: Chord = Chord.UNDEFINED
    private var chordDelayMillis: Long = 500

    init {
        spacing = 10.0

        val slider = Slider().apply {
            min = 250.0
            max = 1000.0
            value = 500.0
            blockIncrement = 5.0
            isShowTickMarks = true
            isShowTickLabels = true
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

    private fun startRandomChordPlayback() {
        stopRandomChordPlayback()
        playRandomChord()
        randomChordTask = randomChordScheduler.scheduleAtFixedRate(
            { playRandomChord() },
            chordDelayMillis,
            chordDelayMillis,
            TimeUnit.MILLISECONDS
        )
    }

    private fun restartRandomChordPlaybackIfRunning() {
        if (randomChordTask != null) {
            startRandomChordPlayback()
        }
    }

    private fun stopRandomChordPlayback() {
        randomChordTask?.cancel(false)
        randomChordTask = null
        sendChordOff(activeChord)
        activeChord = Chord.UNDEFINED
    }

    private fun playRandomChord() {
        sendChordOff(activeChord)
        val nextChord = randomChord()
        sendChordOn(nextChord)
        activeChord = nextChord
    }

    private fun randomChord(): Chord {
        return randomTriadChords.random(random)
    }

    private fun chordNotes(chord: Chord): List<Note> {
        if (chord == Chord.UNDEFINED) return emptyList()

        val intervals = when (chord.quality) {
            ChordQuality.MAJOR -> listOf(0, 4, 7)
            ChordQuality.MINOR -> listOf(0, 3, 7)
            else -> return emptyList()
        }

        val root = 48 + chord.pitchClass
        return intervals.mapNotNull { interval ->
            val candidate = Note.from((root + interval).toByte())
            if (candidate == Note.UNDEFINED) null else candidate
        }
    }

    private fun sendChordOn(chord: Chord) {
        chordNotes(chord).forEach { note ->
            midiSimulator.notifyListeners(
                MidiDataEvent(
                    byteArrayOf(
                        "10010000".toInt(2).toByte(),
                        note.byteValue.toByte(),
                        random.nextInt(60, 127).toByte()
                    )
                )
            )
        }
    }

    private fun sendChordOff(chord: Chord) {
        chordNotes(chord).forEach { note ->
            midiSimulator.notifyListeners(
                MidiDataEvent(
                    byteArrayOf(
                        "10000000".toInt(2).toByte(),
                        note.byteValue.toByte(),
                        0
                    )
                )
            )
        }
    }
}
