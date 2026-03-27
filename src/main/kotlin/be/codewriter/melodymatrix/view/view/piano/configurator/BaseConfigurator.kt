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
import javafx.stage.Window

/**
 * Shared base for piano configurator panels shown inside modal dialogs.
 *
 * Provides consistent content padding/spacing plus a standard modal wrapper
 * with a scrolling body and a close button.
 */
open class BaseConfigurator : BorderPane() {

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

    protected fun labeledRow(title: String, component: Node, labelWidth: Double = 100.0): HBox {
        return HBox(8.0).apply {
            alignment = Pos.CENTER_LEFT
            children.addAll(
                Label(title).apply { prefWidth = labelWidth },
                component
            )
        }
    }

    /**
     * Wraps this configurator in a modal-friendly container with scrolling and a close button.
     */
    fun createModalContent(onClose: () -> Unit) {
        center = (center as? ScrollPane) ?: ScrollPane(scrollContent).apply {
            isFitToWidth = true
            hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
            vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
            prefViewportWidth = 420.0
            prefViewportHeight = 500.0
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
    }

    /** Creates a modal dialog window for this configurator with a reliable native titlebar close. */
    fun toDialog(title: String, owner: Window?): Stage {
        val stage = Stage().apply {
            this.title = "$title Settings"
            owner?.let { initOwner(it) }
            initModality(Modality.WINDOW_MODAL)
            isResizable = true
        }

        createModalContent { stage.close() }
        stage.scene = Scene(this)
        stage.sizeToScene()
        stage.minWidth = 360.0
        stage.minHeight = 260.0

        return stage
    }
}

