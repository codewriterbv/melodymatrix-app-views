package be.codewriter.melodymatrix.view.view.guitar

import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.event.MidiDataEvent
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.MmxEventType
import be.codewriter.melodymatrix.view.view.MmxView
import be.codewriter.melodymatrix.view.view.MmxViewMetadata
import javafx.application.Platform

/**
 * Visualizer stage that displays playable guitar positions for incoming MIDI notes.
 */
class GuitarNoteView : MmxView() {

    private val visualizer = GuitarVisualizer(GuitarVisualizer.Mode.NOTE)
    private val activeNotes = linkedSetOf<Note>()

    init {
        setupSurface(visualizer.rootNode, 860.0, 350.0, visualizer.rootNode)
    }

    override fun onEvent(event: MmxEvent) {
        if (event.type != MmxEventType.MIDI) {
            return
        }

        val midiDataEvent = event as? MidiDataEvent ?: return
        if (midiDataEvent.isDrum || midiDataEvent.note == Note.UNDEFINED) {
            return
        }

        Platform.runLater {
            when (midiDataEvent.event) {
                MidiEvent.NOTE_ON -> {
                    activeNotes.remove(midiDataEvent.note)
                    activeNotes.add(midiDataEvent.note)
                    visualizer.showNote(midiDataEvent.note)
                }

                MidiEvent.NOTE_OFF -> {
                    activeNotes.remove(midiDataEvent.note)
                    val fallbackNote = activeNotes.lastOrNull()
                    if (fallbackNote == null) {
                        visualizer.clear()
                    } else {
                        visualizer.showNote(fallbackNote)
                    }
                }

                else -> {
                    // Ignore non-note MIDI messages.
                }
            }
        }
    }

    companion object : MmxViewMetadata {
        override fun getViewTitle(): String = "Guitar Note"
        override fun getViewDescription(): String = "Displays playable guitar positions for notes."
        override fun getViewImagePath(): String = "/stage/guitar-chord.png"
    }
}

