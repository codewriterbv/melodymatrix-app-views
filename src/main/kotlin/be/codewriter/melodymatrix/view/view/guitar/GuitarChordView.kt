package be.codewriter.melodymatrix.view.view.guitar

import be.codewriter.melodymatrix.view.component.ToggleButton
import be.codewriter.melodymatrix.view.definition.Chord
import be.codewriter.melodymatrix.view.event.ChordEvent
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
 * Visualizer stage that displays guitar chord fingerings for detected chords.
 *
 * Shows a fretboard grid (6 strings × 13 frets) and marks the finger positions
 * for the current chord. When a chord event arrives the board is updated with the
 * best available voicing from [GuitarChordVoicing]. A legend explains the fretboard markers.
 *
 * @see MmxView
 * @see GuitarChordVoicing
 * @see ChordEvent
 */
class GuitarChordView : MmxView() {

    companion object : MmxViewMetadata {
        private const val TOOLBAR_CONTROL_HEIGHT = 40.0
        private const val REGISTRY_KEEP_LAST_CHORD = "view.guitarChord.keepLastChord"

        override fun getViewTitle(): String = "Guitar Chord"
        override fun getViewDescription(): String = "Displays guitar fretboard finger settings for chords."
        override fun getViewImagePath(): String = "/view/guitar-chord.png"
    }

    private val visualizer = GuitarVisualizer(GuitarVisualizer.Mode.CHORD)
    private val stickyChordDisplayProperty: BooleanProperty = SimpleBooleanProperty(true)

    init {
        RegistryHelper.bindBoolean(stickyChordDisplayProperty, REGISTRY_KEEP_LAST_CHORD)

        val root = BorderPane().apply {
            top = buildToolbar()
            center = visualizer.rootNode
        }

        setupSurface(root, 860.0, 390.0, visualizer.rootNode)
    }

    /**
     * Handles incoming events.
     *
     * Only CHORD events are processed; MIDI and PLAY events are ignored.
     * On a chord-on event the fretboard is updated with the matching voicing;
     * on chord-off the board is cleared.
     *
     * @param event The MelodyMatrix event to process
     */
    override fun onEvent(event: MmxEvent) {
        if (event.type != MmxEventType.CHORD) {
            return
        }

        val chordEvent = event as? ChordEvent ?: return
        Platform.runLater {
            val showChord = chordEvent.on && chordEvent.chord != Chord.UNDEFINED
            if (!showChord) {
                if (!stickyChordDisplayProperty.get()) {
                    visualizer.clear()
                }
                return@runLater
            }

            visualizer.showChord(chordEvent.chord)
        }
    }

    private fun buildToolbar(): HBox {
        return HBox(8.0).apply {
            alignment = Pos.CENTER_LEFT
            padding = Insets(10.0, 12.0, 8.0, 12.0)
            children.add(ToggleButton("Keep last chord", stickyChordDisplayProperty, TOOLBAR_CONTROL_HEIGHT))
        }
    }
}
