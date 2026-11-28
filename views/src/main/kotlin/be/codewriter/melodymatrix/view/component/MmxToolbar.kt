package be.codewriter.melodymatrix.view.component

import be.codewriter.melodymatrix.view.i18n.BundleRef
import be.codewriter.melodymatrix.view.i18n.I18n
import be.codewriter.melodymatrix.view.view.piano.configurator.BaseConfigurator
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import javafx.stage.Window

/**
 * The standard top bar of a view, as set by "Piano with effects": controls of [CONTROL_HEIGHT] pixels,
 * 8 pixels apart, left aligned, with 8 pixels below the bar, inside a frame with 10 pixels of padding.
 *
 * Views build their bar with [create] and wrap it with [frame], so every view looks and spaces the same.
 */
object MmxToolbar {
    const val CONTROL_HEIGHT = 40.0

    /** Style class of every top bar, so a host (fullscreen mode) can find the bars of a view. */
    const val STYLE_CLASS = "mmx-toolbar"

    private const val SPACING = 8.0
    private const val BAR_BOTTOM_PADDING = 8.0

    /** The space around a bar: 8 pixels below it. */
    val BAR_PADDING = Insets(0.0, 0.0, BAR_BOTTOM_PADDING, 0.0)
    const val FRAME_PADDING = 10.0

    /** A bar holding [nodes]. */
    fun create(vararg nodes: Node): HBox = HBox(SPACING).apply {
        alignment = Pos.CENTER_LEFT
        padding = BAR_PADDING
        styleClass.add(STYLE_CLASS)
        children.addAll(nodes)
    }

    /**
     * A button that opens a modal settings dialog, like the buttons of "Piano with effects". The button and the
     * dialog title are the i18n key [titleKey] of [bundle]; [form] builds the dialog content each time it opens
     * (an [MmxSettingsForm] or any other configurator); [owner] gives the window the dialog belongs to.
     */
    fun settingsButton(
        titleKey: String,
        bundle: BundleRef,
        owner: () -> Window?,
        form: () -> BaseConfigurator
    ): Button = Button().apply {
        textProperty().bind(I18n.binding(bundle, titleKey))
        setOnAction { form().toDialog(I18n.get(bundle, titleKey), owner()).showAndWait() }
        toolbarHeight()
    }

    /** A plain action button of the standard height. */
    fun actionButton(titleKey: String, bundle: BundleRef, action: () -> Unit): Button = Button().apply {
        textProperty().bind(I18n.binding(bundle, titleKey))
        setOnAction { action() }
        toolbarHeight()
    }

    /** The view frame: [toolbar] above [content]. */
    fun frame(toolbar: Node, content: Node): BorderPane = BorderPane().apply {
        padding = Insets(FRAME_PADDING)
        top = toolbar
        center = content
    }
}

/** Gives a control the standard toolbar height. */
fun <T : Region> T.toolbarHeight(): T = apply {
    minHeight = MmxToolbar.CONTROL_HEIGHT
    prefHeight = MmxToolbar.CONTROL_HEIGHT
    maxHeight = MmxToolbar.CONTROL_HEIGHT
}
