package be.codewriter.melodymatrix.view.view

import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Needs a display (`-Dmelodymatrix.perf=true`, same switch as the performance test): the tracker follows a real
 * window, so it is checked with a real stage.
 */
@EnabledIfSystemProperty(named = "melodymatrix.perf", matches = "true")
class OnScreenTrackerTest {

    @Test
    fun `follows scene, window, tab visibility and minimising`() {
        try {
            Platform.startup { }
        } catch (_: IllegalStateException) {
        }
        val result = CompletableFuture<Unit>()
        Platform.runLater {
            try {
                val view = Pane()
                val tab = StackPane(view)
                val tracker = OnScreenTracker(view)
                assertFalse(tracker.showing.get(), "not in a scene yet")

                val stage = Stage()
                stage.scene = Scene(tab, 200.0, 200.0)
                assertFalse(tracker.showing.get(), "scene, but window not shown")
                stage.show()
                assertTrue(tracker.showing.get(), "window shown")

                tab.isVisible = false
                assertFalse(tracker.showing.get(), "tab hidden (not selected)")
                tab.isVisible = true
                assertTrue(tracker.showing.get(), "tab selected again")

                tab.children.remove(view)
                assertFalse(tracker.showing.get(), "removed from the scene graph")
                tab.children.add(view)
                assertTrue(tracker.showing.get(), "added again")

                view.isVisible = false
                assertFalse(tracker.showing.get(), "own visibility")
                view.isVisible = true

                stage.isIconified = true
                assertFalse(tracker.showing.get(), "window minimised")
                stage.isIconified = false
                stage.hide()
                assertFalse(tracker.showing.get(), "window closed")
                tracker.dispose()
                result.complete(Unit)
            } catch (t: Throwable) {
                result.completeExceptionally(t)
            }
        }
        result.get(1, TimeUnit.MINUTES)
    }
}
