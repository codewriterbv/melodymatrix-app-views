package be.codewriter.melodymatrix.view.view.chord

import atlantafx.base.controls.ToggleSwitch
import be.codewriter.melodymatrix.view.component.ZoomableNode
import be.codewriter.melodymatrix.view.event.ChordEvent
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.MmxEventType
import be.codewriter.melodymatrix.view.view.MmxView
import be.codewriter.melodymatrix.view.view.MmxViewMetadata
import javafx.application.Platform
import javafx.beans.property.BooleanProperty
import javafx.event.ActionEvent
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ContentDisplay
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox

/**
 * Visualizer stage that shows the last detected chord at the center of a radial graph and
 * surrounds it with harmonically related chords.
 */
class ChordRelationView : MmxView() {

    override val fitToViewport: Boolean = true

    companion object : MmxViewMetadata {
        override fun getViewTitle(): String = "Chord Relationship"
        override fun getViewDescription(): String =
            "Shows harmonic relationships between the last detected chord and related chords."

        override fun getViewImagePath(): String = "/view/chord-relation.png"

        private const val TOOLBAR_CONTROL_HEIGHT = 40.0
    }

    private val visualizer = ChordRelationVisualizer()

    init {
        val root = BorderPane().apply {
            top = buildToolbar()
            center = ZoomableNode(
                content = visualizer.contentGroup,
                naturalWidth = ChordRelationVisualizer.W,
                naturalHeight = ChordRelationVisualizer.GRAPH_H,
                minWidthValue = 100.0,
                minHeightValue = 100.0,
                fitMode = ZoomableNode.FitMode.CONTAIN
            )
        }

        setupSurface(
            rootNode = root,
            naturalWidth = ChordRelationVisualizer.W,
            naturalHeight = ChordRelationVisualizer.H,
            captureNode = visualizer.contentGroup,
            captureWidth = ChordRelationVisualizer.W.toInt(),
            captureHeight = ChordRelationVisualizer.GRAPH_H.toInt()
        ) {
            visualizer.stop()
        }
    }

    override fun onEvent(event: MmxEvent) {
        if (event.type != MmxEventType.CHORD) return
        val chordEvent = event as? ChordEvent ?: return
        Platform.runLater {
            if (chordEvent.on) {
                visualizer.showChord(chordEvent.chord)
            }
        }
    }

    private fun buildToolbar(): HBox {
        return HBox(8.0).apply {
            alignment = Pos.CENTER_LEFT
            padding = Insets(10.0, 12.0, 8.0, 12.0)
            children.addAll(
                chordTypeButton("Major", visualizer.majorEnabledProperty),
                chordTypeButton("Minor", visualizer.minorEnabledProperty),
                chordTypeButton("Dominant", visualizer.dominantEnabledProperty),
                chordTypeButton("Dim", visualizer.diminishedEnabledProperty),
                chordTypeButton("Half-Dim", visualizer.halfDiminishedEnabledProperty),
                chordTypeButton("Tritone", visualizer.tritoneEnabledProperty)
            )
        }
    }

    private fun chordTypeButton(title: String, property: BooleanProperty): Button {
        val toggle = ToggleSwitch().apply {
            selectedProperty().bindBidirectional(property)
            addEventHandler(MouseEvent.MOUSE_PRESSED) { it.consume() }
            addEventHandler(MouseEvent.MOUSE_RELEASED) { it.consume() }
            addEventHandler(MouseEvent.MOUSE_CLICKED) { it.consume() }
            addEventHandler(ActionEvent.ACTION) { it.consume() }
        }

        return Button(title).apply {
            graphic = toggle
            contentDisplay = ContentDisplay.RIGHT
            graphicTextGap = 10.0
            minHeight = TOOLBAR_CONTROL_HEIGHT
            prefHeight = TOOLBAR_CONTROL_HEIGHT
            maxHeight = TOOLBAR_CONTROL_HEIGHT
            setOnAction {
                property.set(!property.get())
            }
        }
    }
}