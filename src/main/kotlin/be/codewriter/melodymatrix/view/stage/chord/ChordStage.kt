package be.codewriter.melodymatrix.view.stage.chord

import be.codewriter.melodymatrix.view.definition.Chord
import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.event.ChordEvent
import be.codewriter.melodymatrix.view.event.MidiDataEvent
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.MmxEventType
import be.codewriter.melodymatrix.view.helper.FileLoader
import be.codewriter.melodymatrix.view.stage.ViewStage
import be.codewriter.melodymatrix.view.stage.ViewStageMetadata
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import java.io.InputStream

/**
 * Visualizer stage that displays notes on a two-octave music staff and shows the detected chord.
 *
 * Renders a simplified treble and bass staff using a music font (Musiqwik). Each note position
 * on the staff is backed by a [Label]; when a MIDI note is pressed the corresponding label is
 * highlighted. The current chord name and active notes are also shown as text labels.
 *
 * @see ViewStage
 * @see ChordEvent
 * @see MidiDataEvent
 */
class ChordStage : ViewStage() {

    val notes: MutableMap<Note, Label> = mutableMapOf()
    private val activeNotes: MutableSet<Note> = mutableSetOf()
    private val chordLabel = Label("Chord: -")
    private val chordNotesLabel = Label("Notes: -")

    init {
        val inputStream: InputStream? = FileLoader.getResource("/fonts/musiqwik/Musiqwik-rvL8.ttf")
        val font = Font.loadFont(inputStream, 40.0)

        chordLabel.style = "-fx-font-size: 22; -fx-font-weight: bold;"
        chordNotesLabel.style = "-fx-font-size: 16;"

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

        title = getViewTitle()
        scene = Scene(VBox(chordLabel, chordNotesLabel, row1, row2).apply {
            spacing = 8.0
            padding = Insets(12.0, 0.0, 0.0, 20.0)
        }, 610.0, 220.0)

        setOnCloseRequest {
            // Must be defined or the stage close flow is unstable on some systems.
        }
    }

    /**
     * Creates a styled music-font label and optionally registers it for a specific note.
     *
     * @param musicFont The music font to apply, or null to use the default font
     * @param content The text content (music font character) to display
     * @param note The note this label represents, or null for decorative staff elements
     * @return A [Label] configured with the given font and text
     */
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

    /**
     * Handles incoming MelodyMatrix events.
     *
     * Routes MIDI events to [handleMidiEvent] and CHORD events to update the chord label.
     * PLAY events are ignored.
     *
     * @param event The MelodyMatrix event to process
     */
    override fun onEvent(event: MmxEvent) {
        when (event.type) {
            MmxEventType.MIDI -> {
                val midiDataEvent = event as? MidiDataEvent ?: return
                handleMidiEvent(midiDataEvent)
            }

            MmxEventType.PLAY -> {
                // Not needed here
            }

            MmxEventType.CHORD -> {
                val chordEvent = event as? ChordEvent ?: return
                Platform.runLater {
                    val isChordOn = (chordEvent.chord != Chord.UNDEFINED) && chordEvent.on
                    chordLabel.text = if (isChordOn) "Chord: ${chordEvent.chord.label}" else "Chord: -"
                    chordNotesLabel.text = if (isChordOn) notesText() else "Notes: -"
                }
            }
        }
    }

    /**
     * Processes a MIDI event by updating the set of active notes and refreshing the staff display.
     *
     * Drum notes and undefined notes are ignored.
     *
     * @param midiDataEvent The MIDI data event to handle
     */
    private fun handleMidiEvent(midiDataEvent: MidiDataEvent) {
        if (midiDataEvent.isDrum || midiDataEvent.note == Note.UNDEFINED) {
            return
        }

        Platform.runLater {
            when (midiDataEvent.event) {
                MidiEvent.NOTE_ON -> activeNotes.add(midiDataEvent.note)
                MidiEvent.NOTE_OFF -> activeNotes.remove(midiDataEvent.note)
                else -> return@runLater
            }
            updateHighlightedNotes()
            chordNotesLabel.text = notesText()
        }
    }

    /**
     * Builds a formatted string listing all currently active notes sorted by pitch.
     *
     * @return A string in the form "Notes: C4, E4, G4", or "Notes: -" when no notes are active
     */
    private fun notesText(): String {
        val labels = activeNotes
            .asSequence()
            .filter { it != Note.UNDEFINED }
            .sortedBy { it.byteValue }
            .map { "${it.mainNote.label}${it.octave.octave}" }
            .toList()

        return if (labels.isEmpty()) "Notes: -" else "Notes: ${labels.joinToString(", ")}"
    }

    /**
     * Refreshes the visual highlighting of note labels based on the current active notes.
     *
     * Natural notes are highlighted yellow/red; sharp notes are highlighted green/blue.
     */
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

    /**
     * Resolves the visible staff label for the given note.
     *
     * Sharp notes share a visual position with their parent (natural) note,
     * so this method returns the parent's label for sharps.
     *
     * @param note The note whose label should be returned
     * @return The label for the note, or null if the note has no label in the staff
     */
    private fun noteToVisibleLabel(note: Note): Label? {
        return if (note.mainNote.isSharp) {
            notes[note.parentNote]
        } else {
            notes[note]
        }
    }

    companion object : ViewStageMetadata {
        override fun getViewTitle(): String = "See your chords..."
        override fun getViewDescription(): String = "Displays detected chords and active notes on a two-octave staff."
        override fun getViewImagePath(): String = "/stage/chord.png"
    }
}