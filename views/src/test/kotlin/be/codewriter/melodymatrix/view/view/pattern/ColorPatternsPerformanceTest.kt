package be.codewriter.melodymatrix.view.view.pattern

import javafx.application.Platform
import javafx.beans.property.IntegerProperty
import javafx.beans.property.ObjectProperty
import javafx.scene.Scene
import javafx.stage.Stage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * Frame cost of every pattern of [ColorPatternsView], with a chord-heavy simulated performance.
 *
 * Not part of the normal test run: `-Dmelodymatrix.perf=true`, plus `-DargLine="-Dglass.platform=Headless"` on a
 * machine without display. Every frame is followed by a snapshot, so the time includes the rasterising and not only
 * the recording of the canvas commands. Headless uses the software pipeline, so absolute numbers are pessimistic;
 * compare the patterns with each other. A budget of 16.7 ms per frame is 60 fps.
 */
@EnabledIfSystemProperty(named = "melodymatrix.perf", matches = "true")
class ColorPatternsPerformanceTest {

    @Suppress("UNCHECKED_CAST")
    private fun <T> field(view: Any, name: String): T =
        view.javaClass.getDeclaredField(name).apply { isAccessible = true }.get(view) as T

    @Test
    fun `frame cost per pattern`() {
        try {
            Platform.startup { }
        } catch (_: IllegalStateException) {
            // toolkit already running
        }
        val done = CompletableFuture<String>()
        Platform.runLater {
            try {
                done.complete(measure())
            } catch (t: Throwable) {
                done.completeExceptionally(t)
            }
        }
        println(done.get(10, TimeUnit.MINUTES))
    }

    private fun measure(): String {
        val view = ColorPatternsView()
        val stage = Stage()
        val scene = Scene(view.rootNode as javafx.scene.layout.Region, WIDTH, HEIGHT)
        stage.scene = scene
        stage.show()
        view.dispose() // stop the animation timer: the frames are driven from here

        val pattern: ObjectProperty<ColorPatternsView.Pattern> = field(view, "pattern")
        val bars: IntegerProperty = field(view, "bars")
        val energy: PitchEnergy = field(view, "energy")
        val frame = view.javaClass.getDeclaredMethod("frame", Double::class.javaPrimitiveType).apply { isAccessible = true }
        val random = Random(1)

        fun play() {
            // ~6 new notes per frame over the whole keyboard keeps most columns busy
            repeat(6) { energy.noteOn(21 + random.nextInt(88), 60 + random.nextInt(67)) }
            if (random.nextInt(4) == 0) repeat(10) { energy.noteOff(21 + random.nextInt(88)) }
        }

        val rows = StringBuilder("Frame cost at ${WIDTH.toInt()}x${HEIGHT.toInt()} (ms, avg / worst of $FRAMES frames)\n")
        for (columns in listOf(44, 88)) {
            bars.set(columns)
            rows.append("-- $columns columns --\n")
            for (p in ColorPatternsView.Pattern.entries) {
                pattern.set(p)
                energy.reset()
                repeat(WARMUP) { play(); frame.invoke(view, 0.016); scene.snapshot(null) }
                var total = 0L
                var worst = 0L
                repeat(FRAMES) {
                    play()
                    val start = System.nanoTime()
                    frame.invoke(view, 0.016)
                    scene.snapshot(null)
                    val took = System.nanoTime() - start
                    total += took
                    worst = maxOf(worst, took)
                }
                rows.append("%-10s %6.2f / %6.2f%n".format(p.key, total / FRAMES / 1e6, worst / 1e6))
            }
        }
        stage.close()
        return rows.toString()
    }

    private companion object {
        const val WIDTH = 1600.0
        const val HEIGHT = 900.0
        const val WARMUP = 30
        const val FRAMES = 150
    }
}
