package be.codewriter.melodymatrix.view.view.chord

import be.codewriter.melodymatrix.view.component.ToggleButton
import be.codewriter.melodymatrix.view.component.ZoomableNode
import be.codewriter.melodymatrix.view.event.ChordEvent
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.MmxEventType
import be.codewriter.melodymatrix.view.helper.SettingHelper
import be.codewriter.melodymatrix.view.view.MmxView
import be.codewriter.melodymatrix.view.view.MmxViewMetadata
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox

/**
 * Visualizer stage that shows the last detected chord at the center of a radial graph and
 * surrounds it with harmonically related chords.
 */
class ChordRelationView(
    private val settings: SettingHelper? = null
) : MmxView() {

    override val fitToViewport: Boolean = true

    companion object : MmxViewMetadata {
        private const val REGISTRY_PREFIX = "view.chordRelation"

        override fun getViewTitle(): String = "Chord Relationship"
        override fun getViewDescription(): String =
            "Shows harmonic relationships between the last detected chord and related chords."

        override fun getViewImagePath(): String = "/view/chord-relation.png"

        private const val TOOLBAR_CONTROL_HEIGHT = 40.0
    }

    private val visualizer = ChordRelationVisualizer()

    init {
        restoreSettings()

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

    private fun restoreSettings() {
        settings?.bindBoolean(visualizer.majorEnabledProperty, "$REGISTRY_PREFIX.major")
        settings?.bindBoolean(visualizer.minorEnabledProperty, "$REGISTRY_PREFIX.minor")
        settings?.bindBoolean(visualizer.dominantEnabledProperty, "$REGISTRY_PREFIX.dominant")
        settings?.bindBoolean(visualizer.diminishedEnabledProperty, "$REGISTRY_PREFIX.diminished")
        settings?.bindBoolean(visualizer.halfDiminishedEnabledProperty, "$REGISTRY_PREFIX.halfDiminished")
        settings?.bindBoolean(visualizer.tritoneEnabledProperty, "$REGISTRY_PREFIX.tritone")
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
                ToggleButton("Major", visualizer.majorEnabledProperty, TOOLBAR_CONTROL_HEIGHT),
                ToggleButton("Minor", visualizer.minorEnabledProperty, TOOLBAR_CONTROL_HEIGHT),
                ToggleButton("Dominant", visualizer.dominantEnabledProperty, TOOLBAR_CONTROL_HEIGHT),
                ToggleButton("Dim", visualizer.diminishedEnabledProperty, TOOLBAR_CONTROL_HEIGHT),
                ToggleButton("Half-Dim", visualizer.halfDiminishedEnabledProperty, TOOLBAR_CONTROL_HEIGHT),
                ToggleButton("Tritone", visualizer.tritoneEnabledProperty, TOOLBAR_CONTROL_HEIGHT)
            )
        }
    }
}