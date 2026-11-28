package be.codewriter.melodymatrix.view.view

import javafx.beans.InvalidationListener
import javafx.beans.Observable
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyBooleanWrapper
import javafx.scene.Node
import javafx.stage.Stage

/**
 * Tells whether a [node] can actually be seen: it is in a scene, the window is showing and not minimised, and the
 * node and all its ancestors are visible. A tab that is not selected usually stays in the scene graph, only
 * invisible, so "has a scene" is not enough to decide that a view may stop drawing.
 *
 * Listens to every property the answer depends on and updates [showing] when one changes. Call [dispose] when the
 * node is no longer used.
 */
class OnScreenTracker(private val node: Node) {

    private val wrapper = ReadOnlyBooleanWrapper(false)

    /** `true` while the node is visible on screen. */
    val showing: ReadOnlyBooleanProperty = wrapper.readOnlyProperty

    private val watched = ArrayList<Observable>()
    private val listener = InvalidationListener { refresh() }

    init {
        refresh()
    }

    private fun watch(observable: Observable) {
        observable.addListener(listener)
        watched.add(observable)
    }

    private fun refresh() {
        watched.forEach { it.removeListener(listener) }
        watched.clear()

        var visible = true
        var current: Node? = node
        while (current != null) {
            watch(current.visibleProperty())
            watch(current.parentProperty())
            visible = visible && current.isVisible
            current = current.parent
        }

        watch(node.sceneProperty())
        val scene = node.scene
        var windowShowing = false
        var minimised = false
        if (scene != null) {
            watch(scene.windowProperty())
            val window = scene.window
            if (window != null) {
                watch(window.showingProperty())
                windowShowing = window.isShowing
                if (window is Stage) {
                    watch(window.iconifiedProperty())
                    minimised = window.isIconified
                }
            }
        }
        wrapper.set(visible && windowShowing && !minimised)
    }

    fun dispose() {
        watched.forEach { it.removeListener(listener) }
        watched.clear()
    }
}
