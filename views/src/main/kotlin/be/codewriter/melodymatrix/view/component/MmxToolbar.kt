package be.codewriter.melodymatrix.view.component

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Region

/**
 * The standard top bar of a view, as set by "Piano with effects": controls of [CONTROL_HEIGHT] pixels,
 * 8 pixels apart, left aligned, with 8 pixels below the bar, inside a frame with 10 pixels of padding.
 *
 * Views build their bar with [create] and wrap it with [frame], so every view looks and spaces the same.
 */
object MmxToolbar {
    const val CONTROL_HEIGHT = 40.0

    private const val SPACING = 8.0
    private const val BAR_BOTTOM_PADDING = 8.0
    private const val FRAME_PADDING = 10.0

    /** A bar holding [nodes]. */
    fun create(vararg nodes: Node): HBox = HBox(SPACING).apply {
        alignment = Pos.CENTER_LEFT
        padding = Insets(0.0, 0.0, BAR_BOTTOM_PADDING, 0.0)
        children.addAll(nodes)
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
