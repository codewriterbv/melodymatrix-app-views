package be.codewriter.melodymatrix.view.view.chord

import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import be.codewriter.melodymatrix.view.component.MmxToolbar
import be.codewriter.melodymatrix.view.component.PillSwitch
import be.codewriter.melodymatrix.view.component.ZoomableNode
import be.codewriter.melodymatrix.view.definition.ChordQualityPreset
import be.codewriter.melodymatrix.view.event.ChordEvent
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.MmxEventType
import be.codewriter.melodymatrix.view.helper.SettingHelper
import be.codewriter.melodymatrix.view.i18n.I18n
import be.codewriter.melodymatrix.view.view.MmxView
import be.codewriter.melodymatrix.view.view.MmxViewMetadata
import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Label
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

        override val bundleBaseName = "i18n/view/chord"
        override val bundleKeyPrefix = "relation."
        override fun getViewImagePath(): String = "/view/chord-relation.png"
        override fun getViewIcon(): Ikon = FontAwesomeSolid.PROJECT_DIAGRAM

    }

    private val visualizer = ChordRelationVisualizer()

    init {
        restoreSettings()

        val root = MmxToolbar.frame(
            buildToolbar(),
            ZoomableNode(
                content = visualizer.contentGroup,
                naturalWidth = ChordRelationVisualizer.W,
                naturalHeight = ChordRelationVisualizer.GRAPH_H,
                minWidthValue = 100.0,
                minHeightValue = 100.0,
                fitMode = ZoomableNode.FitMode.CONTAIN
            )
        )

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
        val bundle = I18n.registerBundle("i18n/view/chord")
        val label = Label().apply {
            textProperty().bind(I18n.binding(bundle, "relation.label.show"))
            style = "-fx-font-size: 12; -fx-text-fill: -color-fg-muted;"
        }
        return MmxToolbar.create(
            label,
            PillSwitch(
                options = ChordQualityPreset.entries,
                property = visualizer.presetProperty,
                labelOf = { SimpleStringProperty(it.label) }
            )
        )
    }
}