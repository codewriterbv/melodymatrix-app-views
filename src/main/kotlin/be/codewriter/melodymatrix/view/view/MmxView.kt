package be.codewriter.melodymatrix.view.view

import be.codewriter.melodymatrix.view.data.MmxEventHandler
import javafx.scene.Node

/**
 * Embedded visualizer contract used by host containers (Bento tabs, test app panes).
 *
 * A [MmxViewSurface] exposes a JavaFX root [Node], natural design dimensions used for
 * scaling, and an explicit [dispose] lifecycle hook for releasing resources.
 *
 * Implementations may also expose a [captureNode] — a sub-node representing only the
 * visual output (without toolbars or configuration panels) — that the host can use as
 * the source for screenshot or video recording. When [captureNode] is `null` the host
 * should fall back to [rootNode].
 */
interface MmxViewSurface : MmxEventHandler {
    val rootNode: Node
    val naturalWidth: Double
    val naturalHeight: Double

    /**
     * Optional node that represents the pure visual output of this view, suitable for
     * image capture or video recording. Returns `null` when the view does not designate
     * a specific capture target (host should fall back to [rootNode]).
     */
    val captureNode: Node?
        get() = null

    /**
     * Natural pixel width of [captureNode] at 1:1 scale (i.e. without any zoom transform).
     * Zero when [captureNode] is `null`.
     */
    val captureWidth: Int
        get() = 0

    /**
     * Natural pixel height of [captureNode] at 1:1 scale (i.e. without any zoom transform).
     * Zero when [captureNode] is `null`.
     */
    val captureHeight: Int
        get() = 0

    fun dispose() {
        // Default no-op; override where cleanup is required.
    }
}

/**
 * Base class for visualizer implementations.
 *
 * Keeps the existing class hierarchy name while no longer inheriting from a JavaFX window type.
 * Implementations call [setupSurface] once during init to register root node, natural size,
 * and optional disposal logic.
 *
 * Metadata methods let each stage provide a display title, short description,
 * and an optional image path for selectors/previews.
 *
 * @see be.codewriter.melodymatrix.view.data.MmxEventHandler
 */
abstract class MmxView : MmxViewSurface {
    final override lateinit var rootNode: Node
        protected set

    final override var naturalWidth: Double = 1.0
        protected set

    final override var naturalHeight: Double = 1.0
        protected set

    /**
     * Optional capture target exposed to the host for screenshot / video recording.
     * Set via [setupSurface]; remains `null` when the view does not designate a specific node.
     */
    final override var captureNode: Node? = null
        protected set

    /** Natural pixel width of [captureNode] at 1:1 scale. Zero when no capture node is set. */
    final override var captureWidth: Int = 0
        protected set

    /** Natural pixel height of [captureNode] at 1:1 scale. Zero when no capture node is set. */
    final override var captureHeight: Int = 0
        protected set

    private var disposeAction: () -> Unit = {}

    protected fun setupSurface(
        rootNode: Node,
        naturalWidth: Double,
        naturalHeight: Double,
        captureNode: Node? = null,
        captureWidth: Int = 0,
        captureHeight: Int = 0,
        onDispose: () -> Unit = {}
    ) {
        this.rootNode = rootNode
        this.naturalWidth = naturalWidth
        this.naturalHeight = naturalHeight
        this.captureNode = captureNode
        this.captureWidth = captureWidth
        this.captureHeight = captureHeight
        this.disposeAction = onDispose
    }

    override fun dispose() {
        disposeAction()
    }
}

interface MmxViewMetadata {
    fun getViewTitle(): String

    fun getViewDescription(): String

    fun getViewImagePath(): String
}

