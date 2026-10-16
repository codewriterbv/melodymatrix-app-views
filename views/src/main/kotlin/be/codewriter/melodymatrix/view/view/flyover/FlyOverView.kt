package be.codewriter.melodymatrix.view.view.flyover

import be.codewriter.melodymatrix.view.component.ZoomableNode
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.helper.SettingHelper
import be.codewriter.melodymatrix.view.i18n.I18n
import be.codewriter.melodymatrix.view.view.MmxView
import be.codewriter.melodymatrix.view.view.MmxViewMetadata
import be.codewriter.melodymatrix.view.view.sheet.ScoreFollower
import be.codewriter.melodymatrix.view.view.sheet.SheetSettingsPane
import com.sheetmusic4j.core.model.Score
import com.sheetmusic4j.engraving.layout.NoteAnchor
import com.sheetmusic4j.fxviewer.FlyOverSheetView
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color

/**
 * Play-along "fly-over" view: the score lies on a lit sheet of paper in 3D, a camera glides
 * along it and a glowing ball per voice hops from note to note, landing on each one as it is
 * played; the played noteheads light up in the colour of their staff.
 *
 * The music and the playback position come from a [ScoreFollower], so it follows the same
 * sources as the play-along strip: a loaded score, the PLAY events of a recording, or what is
 * played live. With the "note by note" display mode the paper starts empty and every note is
 * printed when the ball reaches it.
 *
 * Colours, camera angle and the size of the balls can be changed in the settings panel and are
 * stored in [settings]. Needs 3D support (see [FlyOverSheetView.is3DSupported]); without it the
 * view only shows a message.
 */
class FlyOverView(settings: SettingHelper? = null) : MmxView() {

    override val fitToViewport: Boolean = true

    private val bundle = I18n.registerBundle(BUNDLE_BASE_NAME)

    private val upperStaffColor: ObjectProperty<Color> = SimpleObjectProperty(DEFAULT_UPPER_STAFF_COLOR)
    private val lowerStaffColor: ObjectProperty<Color> = SimpleObjectProperty(DEFAULT_LOWER_STAFF_COLOR)

    private val flyOver = FlyOverSheetView()

    private val follower = ScoreFollower(object : ScoreFollower.Target {
        override fun showScore(score: Score) = flyOver.setScore(score)

        override fun noteAnchors(): List<NoteAnchor> = flyOver.layout?.noteAnchors().orEmpty()

        override fun moveCursor(quarters: Double, revealQuarters: Double) {
            flyOver.cursorTimeProperty().set(quarters)
            flyOver.revealTimeProperty().set(revealQuarters)
        }
    })

    init {
        val settingsPane = SheetSettingsPane(settings, REGISTRY_KEY) { getViewTitle() }
            .displayMode(follower.displayModeProperty)
            .section("sheet.section.colors")
            .color("sheet.label.backgroundColor", flyOver.backgroundColorProperty(), "backgroundColor")
            .color("sheet.label.paperColor", flyOver.paperColorProperty(), "paperColor")
            .color("sheet.label.inkColor", flyOver.inkColorProperty(), "inkColor")
            .color("sheet.label.lightColor", flyOver.lightColorProperty(), "lightColor")
            .color("sheet.label.upperStaffColor", upperStaffColor, "upperStaffColor")
            .color("sheet.label.lowerStaffColor", lowerStaffColor, "lowerStaffColor")
            .color("sheet.label.cursorColor", flyOver.cursorColorProperty(), "cursorColor")
            .section("sheet.section.camera")
            .toggle("sheet.label.showCursor", flyOver.cursorVisibleProperty(), "showCursor")
            .slider("sheet.label.cameraPitch", flyOver.cameraPitchProperty(), 0.0, 85.0, "cameraPitch")
            .slider("sheet.label.cameraYaw", flyOver.cameraYawProperty(), -90.0, 90.0, "cameraYaw")
            .slider("sheet.label.cameraDistance", flyOver.cameraDistanceProperty(), 0.8, 6.0, "cameraDistance")
            .section("sheet.section.notes")
            .slider("sheet.label.ballSize", flyOver.ballSizeProperty(), 0.0, 6.0, "ballSize")
            .slider("sheet.label.jumpHeight", flyOver.jumpHeightProperty(), 0.0, 8.0, "jumpHeight")
            .slider("sheet.label.noteGlow", flyOver.noteGlowProperty(), 0.0, 1.5, "noteGlow")
            .slider("sheet.label.lightRadius", flyOver.lightRadiusProperty(), 0.0, 2.0, "lightRadius")
            .slider("sheet.label.noteSpacing", flyOver.stripSpacingFactorProperty(), 0.5, 3.0, "noteSpacing")

        updateNoteColors()
        upperStaffColor.addListener { _, _, _ -> updateNoteColors() }
        lowerStaffColor.addListener { _, _, _ -> updateNoteColors() }

        val content = if (FlyOverSheetView.is3DSupported()) {
            flyOver
        } else {
            StackPane(Label().apply {
                textProperty().bind(I18n.binding(bundle, "flyover.label.no3d"))
                isWrapText = true
            }).apply { alignment = Pos.CENTER }
        }
        val hintLabel = Label().apply {
            textProperty().bind(I18n.binding(bundle, "flyover.label.waiting"))
            style = "-fx-font-size: 13; -fx-text-fill: -color-fg-muted;"
            padding = Insets(4.0, 0.0, 4.0, 8.0)
        }
        val zoomable = ZoomableNode(
            content = content,
            naturalWidth = NATURAL_WIDTH,
            naturalHeight = NATURAL_HEIGHT,
            minWidthValue = 320.0,
            minHeightValue = 180.0,
            fitMode = ZoomableNode.FitMode.CONTAIN
        )
        val root = BorderPane().apply {
            top = VBox(settingsPane, hintLabel)
            center = zoomable
        }
        setupSurface(
            root, NATURAL_WIDTH, NATURAL_HEIGHT + HINT_HEIGHT + SheetSettingsPane.TOOLBAR_HEIGHT,
            captureNode = flyOver,
            captureWidth = NATURAL_WIDTH.toInt(),
            captureHeight = NATURAL_HEIGHT.toInt(),
            onDispose = { follower.dispose() }
        )
    }

    override fun onEvent(event: MmxEvent) = follower.onEvent(event)

    /** Staff `i` of a system uses note colour `i % 2`: upper (treble) and lower (bass) staff. */
    private fun updateNoteColors() {
        flyOver.noteColors().setAll(upperStaffColor.get(), lowerStaffColor.get())
    }

    companion object : MmxViewMetadata {
        override val bundleBaseName = "i18n/view/flyover"
        override fun getViewImagePath(): String = "/view/scale.png"

        private const val BUNDLE_BASE_NAME: String = "i18n/view/flyover"
        private const val REGISTRY_KEY: String = "flyOver"
        private const val NATURAL_WIDTH: Double = 1000.0
        private const val NATURAL_HEIGHT: Double = 560.0
        private const val HINT_HEIGHT: Double = 24.0

        private val DEFAULT_UPPER_STAFF_COLOR: Color = Color.web("#ffb02e")
        private val DEFAULT_LOWER_STAFF_COLOR: Color = Color.web("#33e0ff")
    }
}
