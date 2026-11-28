package be.codewriter.melodymatrix.view.view

import be.codewriter.melodymatrix.view.data.MmxEventHandler
import be.codewriter.melodymatrix.view.i18n.BundleRef
import be.codewriter.melodymatrix.view.i18n.I18n
import org.kordamp.ikonli.Ikon
import javafx.beans.binding.StringBinding
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyBooleanWrapper
import javafx.scene.Node

/**
 * Embedded visualizer contract used by host containers (dock tabs, test app panes).
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
     * Indicates whether host containers should fit this view to the available viewport size.
     *
     * Views that use [be.codewriter.melodymatrix.view.component.ZoomableNode] typically set
     * this to `true` so resize events are propagated by the host [ScrollPane].
     */
    val fitToViewport: Boolean
        get() = false

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

    /**
     * Settings panel [name] (e.g. `fireworks`) as a node that edits the live view settings, for the
     * video recorder, which shows it beside the view. `null` when the view has no such panel.
     */
    fun demoSettingsPanel(name: String): Node? = null

    /**
     * Sets a control of the view that is not a stored setting (video recorder script key `ui.<name>`), on the
     * JavaFX thread. Returns false when the view does not know [name].
     */
    fun demoSetting(name: String, value: String): Boolean = false

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
    private var onScreenTracker: OnScreenTracker? = null
    private val onScreenWrapper = ReadOnlyBooleanWrapper(false)

    /** `true` while the view is really visible: its tab is selected, the window is showing and not minimised. */
    protected val onScreen: ReadOnlyBooleanProperty
        get() = onScreenWrapper.readOnlyProperty

    /**
     * Runs [show] when the view becomes visible and [hide] when it no longer is, and once right away for the current
     * state. Use it to start and stop animation timers: a view that cannot be seen must not draw. Call it after
     * [setupSurface].
     */
    protected fun whileOnScreen(show: () -> Unit, hide: () -> Unit) {
        onScreenWrapper.addListener { _, _, visible -> if (visible) show() else hide() }
        if (onScreenWrapper.get()) show() else hide()
    }

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
        onScreenTracker?.dispose()
        onScreenTracker = OnScreenTracker(rootNode).also { onScreenWrapper.bind(it.showing) }
    }

    override fun dispose() {
        disposeAction()
        onScreenWrapper.unbind()
        onScreenTracker?.dispose()
        onScreenTracker = null
    }
}

/**
 * Contract exposing the display metadata (title, description, image) of a
 * concrete view. Titles and descriptions are pulled from a `.properties`
 * bundle whose directory is declared by [bundleBaseName], so translations
 * can be added without touching the view code.
 *
 * Multiple related views may share a single bundle directory
 * (e.g. `PianoWithEffectsView` and `PianoSimpleView` both use
 * `i18n/view/piano/`). In that case each view overrides [bundleKeyPrefix]
 * so its keys stay unique inside the shared file:
 *
 * ```kotlin
 * companion object : MmxViewMetadata {
 *     override val bundleBaseName = "i18n/view/piano"
 *     override val bundleKeyPrefix = "effects."
 *     override fun getViewImagePath(): String = "/view/piano.png"
 *     override fun getViewIcon(): Ikon = FontAwesomeSolid.KEYBOARD
 * }
 * ```
 *
 * With the prefix above, `titleBinding()` resolves to the
 * `effects.title` key inside `piano/en.properties`.
 */
interface MmxViewMetadata {
    /**
     * Base resource **directory** (no locale suffix, no `.properties`
     * extension) that contains one `.properties` file per language — for
     * example `"i18n/view/piano"` resolves to `i18n/view/piano/en.properties`,
     * `"i18n/view/piano/nl.properties"`, etc.
     */
    val bundleBaseName: String

    /**
     * Optional prefix prepended to every key looked up through this
     * metadata (`title`, `description`, or any custom key). Use this when
     * several views share a bundle. Typically ends with a dot
     * (e.g. `"effects."`).
     */
    val bundleKeyPrefix: String
        get() = ""

    /**
     * Class loader that owns the bundle files. Defaults to the loader of
     * the concrete metadata implementation; override when the resources
     * live in a different module than the [MmxViewMetadata] descriptor.
     */
    val bundleClassLoader: ClassLoader
        get() = javaClass.classLoader

    private val bundle: BundleRef
        get() = I18n.registerBundle(bundleBaseName, bundleClassLoader)

    private fun key(leaf: String): String = if (bundleKeyPrefix.isEmpty()) leaf else "$bundleKeyPrefix$leaf"

    /** Current locale value of the `title` key. */
    fun getViewTitle(): String = I18n.get(bundle, key("title"))

    /** Current locale value of the `description` key. */
    fun getViewDescription(): String = I18n.get(bundle, key("description"))

    /**
     * Path to the preview image (kept as a raw resource path — not
     * translated).
     */
    fun getViewImagePath(): String

    /**
     * Icon identifying the view next to its title: in the tab of the view and
     * in the list of available views.
     */
    fun getViewIcon(): Ikon

    /** Live-updating binding for [getViewTitle]. */
    fun titleBinding(): StringBinding = I18n.binding(bundle, key("title"))

    /** Live-updating binding for [getViewDescription]. */
    fun descriptionBinding(): StringBinding = I18n.binding(bundle, key("description"))
}

