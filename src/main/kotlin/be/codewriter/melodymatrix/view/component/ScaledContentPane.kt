package be.codewriter.melodymatrix.view.component

import be.codewriter.melodymatrix.view.VisualizerStage
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.layout.Pane
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
    override fun computePrefWidth(height: Double) = computeMinWidth(height)
    override fun computePrefHeight(width: Double) = computeMinHeight(width)
    override fun computeMinWidth(height: Double) = 100.0
    override fun computeMinHeight(width: Double) = 50.0

    companion object {
        /**
         * Detaches the root node from a [VisualizerStage]'s scene and wraps it in a
         * [ScaledContentPane] so it can be embedded safely in a Bento tab.
         */
        fun fromStage(stage: VisualizerStage): Node {
            val scene = stage.scene
                ?: throw IllegalStateException("Visualizer stage ${stage::class.simpleName} did not initialize a scene")

            val content = scene.root
            val naturalWidth = scene.resolveNaturalWidth()
            val naturalHeight = scene.resolveNaturalHeight()

            // Detach the original content so it can be re-hosted elsewhere.
            scene.root = Pane()

            return ScaledContentPane(content, naturalWidth, naturalHeight)
        }

        private fun Scene.resolveNaturalWidth(): Double {
            return width.takeIf { it > 0.0 }
                ?: root.layoutBounds.width.takeIf { it > 0.0 }
                ?: 1.0
        }

        private fun Scene.resolveNaturalHeight(): Double {
            return height.takeIf { it > 0.0 }
                ?: root.layoutBounds.height.takeIf { it > 0.0 }
                ?: 1.0
        }
    }
}
