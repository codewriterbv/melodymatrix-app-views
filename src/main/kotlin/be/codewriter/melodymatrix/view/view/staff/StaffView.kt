package stage.scale

import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.event.MidiDataEvent
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.MmxEventType
import be.codewriter.melodymatrix.view.helper.FileLoader
import be.codewriter.melodymatrix.view.view.MmxView
import be.codewriter.melodymatrix.view.view.MmxViewMetadata
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import java.io.InputStream

/**
 * Visualizer stage that displays notes on a two-octave music staff and highlights them as they are played.
 *
 * Similar to [ChordStage] but focused on individual scale/melodic note highlighting rather than
 * chord detection. Renders treble and bass staves using the Musiqwik music font. Each note label
 * is highlighted when the corresponding key is pressed and cleared on release.
 *
 * @see MmxView
 * @see MidiDataEvent
 */
class StaffView : MmxView() {

    val notes: MutableMap<Note, Label> = mutableMapOf()

    init {
        val inputStream: InputStream? = FileLoader.getResource("/fonts/musiqwik/Musiqwik-rvL8.ttf")

        val font = Font.loadFont(inputStream, 40.0)

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

        // "=¯==rstuvwxyz{|}============================"
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

        val root = VBox(row1, row2).apply {
            spacing = 0.0
            padding = Insets(0.0, 0.0, 0.0, 20.0)
        }

        setupSurface(root, 610.0, 200.0)
    }

    /**
     * Creates a styled music-font label and optionally registers it for a specific note.
     *
     * @param musicFont The music font to apply, or null for the default font
     * @param content   The text content (music font character) to display
     * @param note      The note this label represents, or null for decorative staff elements
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
     * Highlights the corresponding staff label on NOTE_ON and removes highlighting on NOTE_OFF.
     * Sharp notes share a visual position with their natural parent note.
     * PLAY and CHORD events are ignored.
     *
     * @param event The MelodyMatrix event to process
     */
    override fun onEvent(event: MmxEvent) {
        when (event.type) {
            MmxEventType.MIDI -> {
                val midiDataEvent = event as? MidiDataEvent ?: return
                Platform.runLater {
                    val note = midiDataEvent.note
                    val label = if (note.mainNote.isSharp) notes[note.parentNote!!] else notes[note]
                    if (label != null) {
                        if (midiDataEvent.event == MidiEvent.NOTE_ON) {
                            if (note.mainNote.isSharp) {
                                label.style = "-fx-text-fill: blue; -fx-background-color: green;"
                            } else {
                                label.style = "-fx-text-fill: red; -fx-background-color: yellow;"
                            }
                        } else {
                            label.style = ""
                        }
                    } else {
                        // No UI note to update
                    }
                }
            }

            MmxEventType.PLAY -> {
                // Not needed here
            }

            MmxEventType.CHORD -> {
                // Not needed here
            }
        }
    }

    companion object : MmxViewMetadata {
        override fun getViewTitle(): String = "Treble and Bass Clef Staff"
        override fun getViewDescription(): String = "Highlights played notes on a two-octave staff view."
        override fun getViewImagePath(): String = "/stage/scale.png"
    }
}