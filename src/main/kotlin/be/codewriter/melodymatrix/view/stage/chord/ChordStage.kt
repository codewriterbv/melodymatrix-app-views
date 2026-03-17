package be.codewriter.melodymatrix.view.stage.chord

import be.codewriter.melodymatrix.view.VisualizerStage
import be.codewriter.melodymatrix.view.data.MidiData
import be.codewriter.melodymatrix.view.data.PlayEvent
import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.helper.FileLoader
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.InputStream
import java.util.*

class ChordStage : VisualizerStage() {

    private data class TimedNoteOn(val note: Note, val timestampMillis: Long)

    val notes: MutableMap<Note, Label> = mutableMapOf()
    private val activeNotes: MutableSet<Note> = mutableSetOf()
    private val recentNoteOnEvents: ArrayDeque<TimedNoteOn> = ArrayDeque()
    private val chordLabel = Label("Chord: -")

    init {
        val inputStream: InputStream? = FileLoader.getResource("/fonts/musiqwik/Musiqwik-rvL8.ttf")
        val font = Font.loadFont(inputStream, 40.0)

        chordLabel.style = "-fx-font-size: 22; -fx-font-weight: bold;"

        val row1 = HBox().apply {
            spacing = 0.0
            padding = Insets(0.0)
            children.addAll(
                getLabel(font, "=&==========================", null),
                getLabel(font, "r", Note.C4),
                getLabel(font, "s", Note.D4),
                getLabel(font, "t", Note.E4),
                getLabel(font, "u", Note.F4),
                getLabel(font, "v", Note.G4),
                getLabel(font, "w", Note.A4),
                getLabel(font, "x", Note.B4),
                getLabel(font, "y", Note.C5),
                getLabel(font, "z", Note.D5),
                getLabel(font, "{", Note.E5),
                getLabel(font, "|", Note.F5),
                getLabel(font, "}", Note.G5),
                getLabel(font, "~", Note.A5),
                getLabel(font, "==", null)
            )
        }

        val row2 = HBox().apply {
            spacing = 0.0
            padding = Insets(0.0)
            children.addAll(
                getLabel(font, "=¯==", null),
                getLabel(font, "r", Note.E2),
                getLabel(font, "s", Note.F2),
                getLabel(font, "t", Note.G2),
                getLabel(font, "u", Note.A2),
                getLabel(font, "v", Note.B2),
                getLabel(font, "w", Note.C3),
                getLabel(font, "x", Note.D3),
                getLabel(font, "y", Note.E3),
                getLabel(font, "z", Note.F3),
                getLabel(font, "{", Note.G3),
                getLabel(font, "|", Note.A3),
                getLabel(font, "}", Note.B3),
                getLabel(font, "============================", null)
            )
        }

        title = "See your chords..."
        scene = Scene(VBox(chordLabel, row1, row2).apply {
            spacing = 8.0
            padding = Insets(12.0, 0.0, 0.0, 20.0)
        }, 610.0, 220.0)

        setOnCloseRequest {
            // Must be defined or the stage close flow is unstable on some systems.
        }
    }

    private fun getLabel(
        musicFont: Font?,
        content: String,
        note: Note?
    ): Label {
        val label = Label().apply {
            text = content
            font = musicFont
        }
        if (note != null) {
            notes[note] = label
        }
        return label
    }

    override fun onMidiData(midiData: MidiData) {
        if (midiData.isDrum || midiData.note == Note.UNDEFINED) {
            return
        }

        Platform.runLater {
            val now = System.currentTimeMillis()

            when (midiData.event) {
                MidiEvent.NOTE_ON -> {
                    activeNotes.add(midiData.note)
                    recentNoteOnEvents.addLast(TimedNoteOn(midiData.note, now))
                }

                MidiEvent.NOTE_OFF -> activeNotes.remove(midiData.note)
                else -> return@runLater
            }

            pruneOldNoteOnEvents(now)
            updateHighlightedNotes()
            updateDetectedChord(now)
        }
    }

    private fun updateHighlightedNotes() {
        notes.values.forEach { it.style = "" }

        activeNotes.forEach { note ->
            val label = noteToVisibleLabel(note) ?: return@forEach
            label.style = if (note.mainNote.isSharp) {
                "-fx-text-fill: blue; -fx-background-color: green;"
            } else {
                "-fx-text-fill: red; -fx-background-color: yellow;"
            }
        }
    }

    private fun updateDetectedChord(now: Long) {
        val groupedRecentEvents = groupedRecentNoteOnEvents(now)
        val groupedRootPriority = groupedRecentEvents
            .asReversed()
            .map { noteToPitchClass(it.note) }
            .distinct()

        val detectedChord = ChordDetector.detect(activeNotes, groupedRootPriority)
        chordLabel.text = detectedChord?.let { "Chord: ${it.label}" } ?: "Chord: -"
    }

    private fun groupedRecentNoteOnEvents(now: Long): List<TimedNoteOn> {
        pruneOldNoteOnEvents(now)
        if (recentNoteOnEvents.isEmpty()) {
            return emptyList()
        }

        val newestTimestamp = recentNoteOnEvents.last.timestampMillis
        return recentNoteOnEvents
            .filter { newestTimestamp - it.timestampMillis <= CHORD_GROUP_WINDOW_MILLIS }
            .filter { activeNotes.contains(it.note) }
    }

    private fun pruneOldNoteOnEvents(now: Long) {
        while (recentNoteOnEvents.isNotEmpty() &&
            now - recentNoteOnEvents.first.timestampMillis > NOTE_EVENT_RETENTION_MILLIS
        ) {
            recentNoteOnEvents.removeFirst()
        }
    }

    private fun noteToVisibleLabel(note: Note): Label? {
        return if (note.mainNote.isSharp) {
            notes[note.parentNote]
        } else {
            notes[note]
        }
    }

    private fun noteToPitchClass(note: Note): Int {
        return note.byteValue % 12
    }

    override fun onPlayEvent(playEvent: PlayEvent) {
        // Not needed here
    }

    companion object {
        private const val CHORD_GROUP_WINDOW_MILLIS: Long = 220
        private const val NOTE_EVENT_RETENTION_MILLIS: Long = 1000
        private val logger: Logger = LogManager.getLogger(ChordStage::class.java.name)
    }
}