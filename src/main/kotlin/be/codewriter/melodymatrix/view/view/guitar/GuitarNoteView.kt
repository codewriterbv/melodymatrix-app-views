package be.codewriter.melodymatrix.view.view.guitar

import be.codewriter.melodymatrix.view.component.ToggleButton
import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.event.MidiDataEvent
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.MmxEventType
import be.codewriter.melodymatrix.view.helper.RegistryHelper
import be.codewriter.melodymatrix.view.view.MmxView
import be.codewriter.melodymatrix.view.view.MmxViewMetadata
import javafx.application.Platform
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox

/**
 * Visualizer stage that displays playable guitar positions for incoming MIDI notes.
 */
class GuitarNoteView : MmxView() {

    companion object : MmxViewMetadata {
        private const val TOOLBAR_CONTROL_HEIGHT = 40.0
        private const val REGISTRY_SHOW_ALL_POSITIONS = "view.guitarNote.showAllPositions"

        override fun getViewTitle(): String = "Guitar Note"
        override fun getViewDescription(): String = "Displays playable guitar positions for notes."
        override fun getViewImagePath(): String = "/view/guitar-chord.png"
    }

    private val visualizer = GuitarVisualizer(GuitarVisualizer.Mode.NOTE)
    private val activeNotes = linkedSetOf<Note>()
    private val showAllPositionsProperty: BooleanProperty = SimpleBooleanProperty(false)

    init {
        RegistryHelper.bindBoolean(showAllPositionsProperty, REGISTRY_SHOW_ALL_POSITIONS)

        val root = BorderPane().apply {
            top = buildToolbar()
            center = visualizer.rootNode
        }
        setupSurface(root, 860.0, 390.0, visualizer.rootNode)

        // Wire property changes after UI is initialized
        showAllPositionsProperty.addListener { _, _, showAll ->
            visualizer.showAllPositions = showAll
            // Refresh display with new mode
            val currentNote = activeNotes.lastOrNull()
            if (currentNote != null) visualizer.showNote(currentNote) else visualizer.clear()
        }
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

    private fun buildToolbar(): HBox {
        return HBox(8.0).apply {
            alignment = Pos.CENTER_LEFT
            padding = Insets(10.0, 12.0, 8.0, 12.0)
            children.add(ToggleButton("Show all positions", showAllPositionsProperty, TOOLBAR_CONTROL_HEIGHT))
        }
    }
}

