package be.codewriter.melodymatrix.view.view.learnstrip

import be.codewriter.melodymatrix.view.component.ZoomableNode
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.PlayEvent
import be.codewriter.melodymatrix.view.event.PlaybackPositionEvent
import be.codewriter.melodymatrix.view.event.ScoreLoadedEvent
import be.codewriter.melodymatrix.view.helper.SettingHelper
import be.codewriter.melodymatrix.view.i18n.I18n
import be.codewriter.melodymatrix.view.view.MmxView
import be.codewriter.melodymatrix.view.view.MmxViewMetadata
import be.codewriter.melodymatrix.view.view.sheet.ScoreFollower
import be.codewriter.melodymatrix.view.view.sheet.SheetSettingsPane
import com.sheetmusic4j.core.model.MusicElement
import com.sheetmusic4j.core.model.Score
import com.sheetmusic4j.engraving.layout.NoteAnchor
import com.sheetmusic4j.fxviewer.StripSheetView
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.layout.Background
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color

/**
 * Scrolling one-line "learn" view: renders the currently-playing (or currently-input)
 * notes as a horizontal strip of notation with a fixed cursor and live per-note
 * highlighting.
 *
 * What is shown and where the cursor is comes from a [ScoreFollower]: a loaded score
 * ([ScoreLoadedEvent]) is shown as written and followed by the song position of the
 * [PlaybackPositionEvent]s; without one, the [PlayEvent]s of a playback or the notes played
 * live are engraved. With the "note by note" display mode the strip starts empty and every
 * note appears when it is played.
 *
 * The colours, the cursor position and the note spacing can be changed in the settings panel
 * and are stored in [settings].
 */
class LearnStripView(settings: SettingHelper? = null) : MmxView() {

    override val fitToViewport: Boolean = true

    private val bundle = I18n.registerBundle("i18n/view/learnstrip")

    private val highlightColor: ObjectProperty<Color> = SimpleObjectProperty(Color.LIMEGREEN)

    private val strip = StripSheetView().apply {
        cursorScreenPosition = 0.3
        cursorColor = Color.CRIMSON
    }

    private val follower = ScoreFollower(object : ScoreFollower.Target {
        override fun showScore(score: Score) = strip.setScore(score)

        override fun noteAnchors(): List<NoteAnchor> = strip.layout?.noteAnchors().orEmpty()

        override fun moveCursor(quarters: Double, revealQuarters: Double) {
            strip.cursorTimeProperty().set(quarters)
            strip.revealTimeProperty().set(revealQuarters)
        }

        override fun highlight(element: MusicElement) {
            strip.noteHighlights()[element] = highlightColor.get()
        }

        override fun unhighlight(element: MusicElement) {
            strip.noteHighlights().remove(element)
        }

        override fun clearHighlights() = strip.noteHighlights().clear()
    })

    private val hintLabel = Label().apply {
        textProperty().bind(I18n.binding(bundle, "learnstrip.label.waiting"))
        style = "-fx-font-size: 13; -fx-text-fill: -color-fg-muted;"
        padding = Insets(4.0, 0.0, 4.0, 8.0)
    }

    init {
        val settingsPane = SheetSettingsPane(settings, REGISTRY_KEY) { getViewTitle() }
            .displayMode(follower.displayModeProperty)
            .section("sheet.section.colors")
            .color("sheet.label.pageColor", strip.pageColorProperty(), "pageColor")
            .color("sheet.label.inkColor", strip.inkColorProperty(), "inkColor")
            .color("sheet.label.cursorColor", strip.cursorColorProperty(), "cursorColor")
            .color("sheet.label.highlightColor", highlightColor, "highlightColor")
            .section("sheet.section.layout")
            .slider("sheet.label.cursorPosition", strip.cursorScreenPositionProperty(), 0.1, 0.9, "cursorPosition")
            .slider(
                "sheet.label.noteSpacing", strip.stripSpacingFactorProperty(),
                0.5, 3.0, "noteSpacing"
            )
        highlightColor.addListener { _, _, color ->
            strip.noteHighlights().replaceAll { _, _ -> color }
        }

        val zoomable = ZoomableNode(
            content = strip,
            naturalWidth = NATURAL_WIDTH,
            naturalHeight = NATURAL_HEIGHT,
            minWidthValue = 240.0,
            minHeightValue = 80.0,
            fitMode = ZoomableNode.FitMode.CONTAIN
        )
        val root = BorderPane().apply {
            top = VBox(settingsPane, hintLabel)
            center = zoomable
            padding = Insets(0.0)
        }
        // The strip paints its own page colour, also past the end of a short score.
        zoomable.backgroundProperty().bind(strip.pageColorProperty().map { Background.fill(it) })
        setupSurface(
            root, NATURAL_WIDTH, NATURAL_HEIGHT + HINT_HEIGHT + SheetSettingsPane.TOOLBAR_HEIGHT, strip,
            onDispose = { follower.dispose() }
        )
    }

    override fun onEvent(event: MmxEvent) = follower.onEvent(event)

    companion object : MmxViewMetadata {
        override val bundleBaseName = "i18n/view/learnstrip"
        override fun getViewImagePath(): String = "/view/scale.png"

        private const val REGISTRY_KEY: String = "learnStrip"
        private const val NATURAL_WIDTH: Double = 1000.0
        private const val NATURAL_HEIGHT: Double = 200.0
        private const val HINT_HEIGHT: Double = 24.0
    }
}
