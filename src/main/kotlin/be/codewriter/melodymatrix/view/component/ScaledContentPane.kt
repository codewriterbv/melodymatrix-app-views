package be.codewriter.melodymatrix.view.component

import be.codewriter.melodymatrix.view.stage.MmxViewSurface
import javafx.scene.Node
import javafx.scene.layout.Region
import javafx.scene.transform.Scale

/**
 * A container that scales its single child uniformly (only ever down, never up) so it fits
 * inside the available space while preserving the child's natural dimensions and layout.
 *
 * The child itself is never internally resized — only a [Scale] transform is applied, which
 * means complex components like the piano keep their exact pixel layout unchanged.
 *
 * The scale is recomputed automatically whenever this pane is resized (e.g. on window resize).
 *
 * @param content      The node to embed.  Must not already be a child of another parent.
 * @param naturalWidth The "designed" width of the content (typically the stage scene width).
 * @param naturalHeight The "designed" height of the content (typically the stage scene height).
 */
class ScaledContentPane(
    private val content: Node,
    private val naturalWidth: Double,
    private val naturalHeight: Double
) : Region() {

    private val scaleTransform = Scale(1.0, 1.0, 0.0, 0.0)

    init {
        content.transforms.add(scaleTransform)
        children.add(content)
    }

    /**
     * Lays out the child by computing a uniform scale factor and applying it via [scaleTransform].
     *
     * The scale is capped at 1.0 so the content is never magnified beyond its natural size.
     */
    override fun layoutChildren() {
        if (naturalWidth <= 0.0 || naturalHeight <= 0.0) return

        // Only scale down; never magnify beyond 1:1.
        val s = minOf(width / naturalWidth, height / naturalHeight, 1.0)
        scaleTransform.x = s
        scaleTransform.y = s

        // Top-left aligned: Scale pivot is at (0,0) and we place the content at (0,0),
        // so the visual top-left corner is always anchored to the pane's top-left.
        if (content is Region) {
            content.resize(naturalWidth, naturalHeight)
        }
        content.relocate(0.0, 0.0)
    }

    // Return small values so we don't inflate the parent SplitPane/StackPane preferred size.
    // The actual size is driven by the dock container; layoutChildren() scales to fit.
    /** Returns the minimum width to avoid inflating the parent container's preferred size. */
    override fun computePrefWidth(height: Double) = computeMinWidth(height)

    /** Returns the minimum height to avoid inflating the parent container's preferred size. */
    override fun computePrefHeight(width: Double) = computeMinHeight(width)

    /** Returns a small minimum width (100 px) so the pane can shrink freely. */
    override fun computeMinWidth(height: Double) = 100.0

    /** Returns a small minimum height (50 px) so the pane can shrink freely. */
    override fun computeMinHeight(width: Double) = 50.0

    companion object {
        /**
         * Wraps a [MmxViewSurface] in a [ScaledContentPane] so it can be embedded safely in a Bento tab.
         */
        fun fromSurface(surface: MmxViewSurface): Node {
            return ScaledContentPane(
                content = surface.rootNode,
                naturalWidth = surface.naturalWidth,
                naturalHeight = surface.naturalHeight
            )
        }
    }
}
