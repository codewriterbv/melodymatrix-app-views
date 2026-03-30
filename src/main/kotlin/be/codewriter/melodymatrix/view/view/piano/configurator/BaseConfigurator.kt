package be.codewriter.melodymatrix.view.view.piano.configurator

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.stage.Window

/**
 * Shared base for piano configurator panels shown inside modal dialogs.
 *
 * Provides consistent content padding/spacing plus a standard modal wrapper
 * with a scrolling body and a close button.
 */
open class BaseConfigurator : BorderPane() {

    companion object {
        private const val HEADER_TOP_INSET = 30.0
    }

    protected val contentBox = VBox(8.0).apply {
        isFillWidth = true
        maxWidth = Double.MAX_VALUE
    }

    private val scrollContent = VBox(contentBox).apply {
        isFillWidth = true
        // Reserve space for overlay scrollbars so controls are never obscured.
        padding = Insets(0.0, 14.0, 0.0, 0.0)
    }

    init {
        padding = Insets(12.0, 12.0, 0.0, 12.0)
        center = scrollContent
    }

    protected fun sectionTitle(text: String): Label {
        return Label(text).apply {
            style = "-fx-font-size: 16px; -fx-font-weight: bold;"
        }
    }

    protected fun labeledControl(title: String, control: Node): VBox {
        return VBox(4.0).apply {
            children.addAll(Label(title), control)
        }
    }

    protected fun twoLabeledControls(title1: String, control1: Node, title2: String, control2: Node): HBox {
        return HBox(12.0).apply {
            children.addAll(
                labeledControl(title1, control1),
                labeledControl(title2, control2)
            )
        }
    }

    /**
     * Wraps this configurator in a modal-friendly container with a drag strip, scrolling body,
     * and a close button.
     */
    fun createModalContent(dialogTitle: String, onClose: () -> Unit): HBox {
        val dragStrip = HBox().apply {
            alignment = Pos.CENTER_LEFT
            // Keep title clear of native window controls when using EXTENDED stage style.
            padding = Insets(HEADER_TOP_INSET, 0.0, 12.0, 0.0)
            children.add(Label("$dialogTitle Settings").apply {
                style = "-fx-font-size: 22px; -fx-font-weight: bold;"
            })
        }
        top = dragStrip

        center = (center as? ScrollPane) ?: ScrollPane(scrollContent).apply {
            isFitToWidth = true
            hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
            vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
            prefViewportWidth = 420.0
            prefViewportHeight = 650.0
            style = "-fx-background-color: transparent;"
        }
        bottom = HBox().apply {
            alignment = Pos.CENTER_RIGHT
            padding = Insets(10.0, 0.0, 15.0, 0.0)
            children.add(Button("Close").apply {
                isDefaultButton = true
                setOnAction { onClose() }
            })
        }

        return dragStrip
    }

    private fun installManualWindowDrag(stage: Stage, dragNode: Node) {
        var dragDeltaX = 0.0
        var dragDeltaY = 0.0

        dragNode.setOnMousePressed { event ->
            dragDeltaX = stage.x - event.screenX
            dragDeltaY = stage.y - event.screenY
        }
        dragNode.setOnMouseDragged { event ->
            stage.x = event.screenX + dragDeltaX
            stage.y = event.screenY + dragDeltaY
        }
    }

    /** Creates a modal dialog window for this configurator with a reliable native titlebar close. */
    fun toDialog(title: String, owner: Window?): Stage {
        val preferredStyle = runCatching { StageStyle.valueOf("EXTENDED") }
            .getOrElse { StageStyle.DECORATED }
        val stage = Stage().apply {
            initStyle(preferredStyle)
            this.title = "$title Settings"
            owner?.let { initOwner(it) }
            initModality(Modality.WINDOW_MODAL)
            isResizable = true
        }

        val dragStrip = createModalContent(title) { stage.close() }
        installManualWindowDrag(stage, dragStrip)
        stage.scene = Scene(this)
        stage.sizeToScene()
        stage.minWidth = 360.0
        stage.minHeight = 260.0

        return stage
    }
}

