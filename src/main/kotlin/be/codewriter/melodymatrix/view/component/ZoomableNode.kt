package be.codewriter.melodymatrix.view.component

import javafx.scene.Node
import javafx.scene.canvas.Canvas
import javafx.scene.layout.Pane
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.scene.transform.Scale

/**
 * Reusable container that applies width-based zoom to a fixed-size content node.
 *
 * The wrapped content keeps its natural layout size while a [Scale] transform makes it
 * fit the available width. An optional [onLayout] hook lets callers perform additional
 * per-frame resize logic (e.g., canvas backing size updates).
 */
class ZoomableNode(
    private val content: Node,
    private val naturalWidth: Double,
    private val naturalHeight: Double,
    private val minWidthValue: Double = 100.0,
    private val minHeightValue: Double = 50.0,
    private val fitMode: FitMode = FitMode.WIDTH,
    private val minScale: Double = 0.1,
    private val onLayout: ((content: Node, availableWidth: Double, availableHeight: Double, scale: Double) -> Unit)? = null
) : Region() {

    enum class FitMode {
        WIDTH,
        CONTAIN
    }

    companion object {
        /**
         * Utility for scaled canvases/panes inside a StackPane so draw buffers remain crisp.
         */
        fun resizeStackPaneChildrenForScale(
            content: Node,
            availableWidth: Double,
            availableHeight: Double,
            scale: Double
        ) {
            if (content !is StackPane) return
            for (child in content.children) {
                if (child is Canvas) {
                    child.width = availableWidth / scale
                    child.height = availableHeight / scale
                } else if (child is Pane) {
                    child.prefWidth = availableWidth / scale
                    child.prefHeight = availableHeight / scale
                    child.resize(availableWidth / scale, availableHeight / scale)
                }
            }
        }
    }

    private val scaleTransform = Scale(1.0, 1.0, 0.0, 0.0)

    init {
        content.transforms.add(scaleTransform)
        children.add(content)
    }

    override fun layoutChildren() {
        if (width <= 0.0 || height <= 0.0) {
            return
        }

        val widthScale = width / naturalWidth
        val heightScale = height / naturalHeight
        val scale = when (fitMode) {
            FitMode.WIDTH -> widthScale
            FitMode.CONTAIN -> minOf(widthScale, heightScale)
        }.coerceAtLeast(minScale)
        scaleTransform.x = scale
        scaleTransform.y = scale

        if (content is Region) {
            content.resize(naturalWidth, naturalHeight)
        }
        content.relocate(0.0, 0.0)

        onLayout?.invoke(content, width, height, scale)
    }

    override fun computePrefWidth(height: Double): Double = naturalWidth

    override fun computePrefHeight(width: Double): Double = naturalHeight

    override fun computeMinWidth(height: Double): Double = minWidthValue

    override fun computeMinHeight(width: Double): Double = minHeightValue

    override fun computeMaxWidth(height: Double): Double = Double.MAX_VALUE

    override fun computeMaxHeight(width: Double): Double = Double.MAX_VALUE
}

