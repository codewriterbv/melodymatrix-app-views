package be.codewriter.melodymatrix.view.stage

import be.codewriter.melodymatrix.view.data.MmxEventHandler
import javafx.scene.Node

/**
 * Embedded visualizer contract used by host containers (Bento tabs, test app panes).
 *
 * A [MmxViewSurface] exposes a JavaFX root [Node], natural design dimensions used for
 * scaling, and an explicit [dispose] lifecycle hook for releasing resources.
 */
interface MmxViewSurface : MmxEventHandler {
    val rootNode: Node
    val naturalWidth: Double
    val naturalHeight: Double

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
abstract class MmxMmxView : MmxViewSurface {
    final override lateinit var rootNode: Node
        protected set

    final override var naturalWidth: Double = 1.0
        protected set

    final override var naturalHeight: Double = 1.0
        protected set

    private var disposeAction: () -> Unit = {}

    protected fun setupSurface(
        rootNode: Node,
        naturalWidth: Double,
        naturalHeight: Double,
        onDispose: () -> Unit = {}
    ) {
        this.rootNode = rootNode
        this.naturalWidth = naturalWidth
        this.naturalHeight = naturalHeight
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

