package be.codewriter.melodymatrix.view.view.piano.configurator

import atlantafx.base.controls.ToggleSwitch
import be.codewriter.melodymatrix.view.i18n.BundleRef
import be.codewriter.melodymatrix.view.i18n.I18n
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.binding.StringBinding
import javafx.beans.property.BooleanProperty
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
 * Provides consistent content padding/spacing plus a standard modal
 * wrapper with a scrolling body and a close button.
 *
 * All label helpers ([sectionTitle], [labeledControl], [twoLabeledControls])
 * take **i18n keys** rather than raw strings. Keys are resolved against
 * the shared piano bundle so labels update live on language change.
 */
open class BaseConfigurator : BorderPane() {

    companion object {
        private const val HEADER_TOP_INSET = 30.0

        /** Bundle for all piano configurator strings. */
        internal val pianoBundle: BundleRef = I18n.registerBundle("i18n/view/piano")

        /** Bundle for reusable strings (dialog Close, state Visible/Hidden/…). */
        internal val commonBundle: BundleRef = I18n.registerBundle("i18n/common")

        /**
         * Returns a live [StringBinding] that resolves to
         * `state.visible` / `state.hidden` depending on [property].
         */
        fun visibleHiddenBinding(property: BooleanProperty): StringBinding =
            Bindings.`when`(property)
                .then(I18n.binding(commonBundle, "state.visible"))
                .otherwise(I18n.binding(commonBundle, "state.hidden"))

        /**
         * Returns a live [StringBinding] that resolves to
         * `state.enabled` / `state.disabled` depending on [property].
         */
        fun enabledDisabledBinding(property: BooleanProperty): StringBinding =
            Bindings.`when`(property)
                .then(I18n.binding(commonBundle, "state.enabled"))
                .otherwise(I18n.binding(commonBundle, "state.disabled"))
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

    /** Section-title label bound to the piano bundle key [titleKey]. */
    protected fun sectionTitle(titleKey: String): Label {
        return Label().apply {
            textProperty().bind(I18n.binding(pianoBundle, titleKey))
            style = "-fx-font-size: 16px; -fx-font-weight: bold;"
        }
    }

    /** VBox with a bound label (piano-bundle key [titleKey]) above the given control. */
    protected fun labeledControl(titleKey: String, control: Node): VBox {
        return VBox(4.0).apply {
            children.addAll(
                Label().apply { textProperty().bind(I18n.binding(pianoBundle, titleKey)) },
                control
            )
        }
    }

    /**
     * Creates a [ToggleSwitch] whose text follows [textBinding] and whose
     * label width is locked to a stable size derived from the maximum of
     * the localized `state.visible` / `state.hidden` strings, so the
     * control's width does not jitter as the state toggles.
     */
    protected fun stableToggle(textBinding: StringBinding): ToggleSwitch {
        return ToggleSwitch().apply {
            textProperty().bind(textBinding)
            sceneProperty().addListener { _, _, scene ->
                if (scene != null && minWidth == USE_COMPUTED_SIZE) {
                    Platform.runLater {
                        // Measure with the longer of the two current translations.
                        textProperty().unbind()
                        val visible = I18n.get(commonBundle, "state.visible")
                        val hidden = I18n.get(commonBundle, "state.hidden")
                        text = if (visible.length >= hidden.length) visible else hidden
                        applyCss()
                        layout()
                        minWidth = prefWidth(-1.0) + 8.0
                        textProperty().bind(textBinding)
                    }
                }
            }
        }
    }

    /** Two labeled controls side-by-side; both labels are i18n keys. */
    protected fun twoLabeledControls(
        titleKey1: String,
        control1: Node,
        titleKey2: String,
        control2: Node
    ): HBox {
        return HBox(12.0).apply {
            children.addAll(
                labeledControl(titleKey1, control1),
                labeledControl(titleKey2, control2)
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
            children.add(Label().apply {
                textProperty().bind(I18n.binding(commonBundle, "configurator.dialog_title", dialogTitle))
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
            children.add(Button().apply {
                textProperty().bind(I18n.binding(commonBundle, "dialog.close"))
                isDefaultButton = true
                setOnAction { onClose() }
            })
        }

        return dragStrip
    }

    /**
     * Applies `StageStyle.EXTENDED` when the JavaFX preview feature is
     * enabled, otherwise falls back to `StageStyle.DECORATED`.
     *
     * On JavaFX 26 `EXTENDED` exists as an enum value but calling
     * `initStyle(EXTENDED)` throws at runtime unless the JVM was launched
     * with `-Djavafx.enablePreview=true`. Because the TestApp launcher
     * does not set that flag, we must catch the failure and fall back
     * gracefully.
     */
    private fun applyExtendedOrDecoratedStyle(stage: Stage) {
        val extended = runCatching { StageStyle.valueOf("EXTENDED") }.getOrNull()
        if (extended != null) {
            try {
                stage.initStyle(extended)
                return
            } catch (_: RuntimeException) {
                // Preview feature not enabled on this JVM; fall through.
            }
        }
        stage.initStyle(StageStyle.DECORATED)
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
        val stage = Stage().apply {
            applyExtendedOrDecoratedStyle(this)
            titleProperty().bind(I18n.binding(commonBundle, "configurator.dialog_title", title))
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
