package be.codewriter.melodymatrix.view.view.chord

import be.codewriter.melodymatrix.view.component.ZoomableNode
import be.codewriter.melodymatrix.view.definition.ChordQualityPreset
import be.codewriter.melodymatrix.view.event.ChordEvent
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.MmxEventType
import be.codewriter.melodymatrix.view.helper.SettingHelper
import be.codewriter.melodymatrix.view.view.MmxView
import be.codewriter.melodymatrix.view.view.MmxViewMetadata
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.ToggleButton
import javafx.scene.control.ToggleGroup
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

        private const val TOOLBAR_CONTROL_HEIGHT = 34.0
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
        settings?.bindEnum(visualizer.presetProperty, "$REGISTRY_PREFIX.preset", ChordQualityPreset::class.java)
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

    /**
     * Builds a compact segmented-button bar letting the user pick one of the
     * [ChordQualityPreset] presets.  The selected button is highlighted; clicking
     * it again has no effect (a preset is always active).
     */
    private fun buildToolbar(): HBox {
        val group = ToggleGroup()

        val buttons = ChordQualityPreset.entries.map { preset ->
            ToggleButton(preset.label).apply {
                toggleGroup = group
                minHeight = TOOLBAR_CONTROL_HEIGHT
                prefHeight = TOOLBAR_CONTROL_HEIGHT
                isSelected = visualizer.presetProperty.value == preset
                userData = preset
                styleClass.addAll("preset-button")
                // Round first / last for a pill look
                when (preset) {
                    ChordQualityPreset.entries.first() -> styleClass.add("preset-button-first")
                    ChordQualityPreset.entries.last()  -> styleClass.add("preset-button-last")
                    else -> {}
                }
            }
        }

        // Keep at least one selected (always-selected toggle group behaviour)
        group.selectedToggleProperty().addListener { _, oldToggle, newToggle ->
            if (newToggle == null) {
                // Re-select the previously-selected button if the user clicks the active one
                group.selectToggle(oldToggle)
            } else {
                val preset = (newToggle as ToggleButton).userData as ChordQualityPreset
                visualizer.presetProperty.set(preset)
            }
        }

        // Keep buttons in sync when the property changes externally (e.g. settings restore)
        visualizer.presetProperty.addListener { _, _, newPreset: ChordQualityPreset ->
            buttons.firstOrNull { (it.userData as ChordQualityPreset) == newPreset }?.isSelected = true
        }

        val label = Label("Show:").apply {
            style = "-fx-font-size: 12; -fx-text-fill: -color-fg-muted;"
        }

        return HBox(8.0).apply {
            alignment = Pos.CENTER_LEFT
            padding = Insets(8.0, 12.0, 6.0, 12.0)
            children.add(label)
            children.addAll(buttons)
        }
    }
}