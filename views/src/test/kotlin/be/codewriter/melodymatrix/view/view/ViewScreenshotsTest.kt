package be.codewriter.melodymatrix.view.view

import be.codewriter.melodymatrix.view.view.chart.ChartsView
import be.codewriter.melodymatrix.view.view.chord.ChordRelationView
import be.codewriter.melodymatrix.view.view.flyover.FlyOverView
import be.codewriter.melodymatrix.view.view.guitar.GuitarView
import be.codewriter.melodymatrix.view.view.ledstrip.LedStripView
import be.codewriter.melodymatrix.view.view.learnstrip.LearnStripView
import be.codewriter.melodymatrix.view.view.midi.MidiActivityView
import be.codewriter.melodymatrix.view.view.midi.MidiView
import be.codewriter.melodymatrix.view.view.mood.MoodColorView
import be.codewriter.melodymatrix.view.view.pattern.ColorPatternsView
import be.codewriter.melodymatrix.view.view.sheetmusic.SheetMusicView
import javafx.application.Platform
import javafx.embed.swing.SwingFXUtils
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.layout.Region
import javafx.stage.Stage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

/**
 * Saves a picture of every view with a top bar (and of its settings panels) to the folder in `-Dmelodymatrix.shots`,
 * to compare the bars by eye. Needs a display; same switch as the other UI checks (`-Dmelodymatrix.perf=true`).
 */
@EnabledIfSystemProperty(named = "melodymatrix.perf", matches = "true")
class ViewScreenshotsTest {

    @Test
    fun `screenshots of the views`() {
        try {
            Platform.startup { }
        } catch (_: IllegalStateException) {
        }
        val folder = File(System.getProperty("melodymatrix.shots", "target/shots")).apply { mkdirs() }
        val done = CompletableFuture<String>()
        Platform.runLater {
            try {
                done.complete(shoot(folder))
            } catch (t: Throwable) {
                done.completeExceptionally(t)
            }
        }
        println(done.get(5, TimeUnit.MINUTES))
        Thread.sleep(3000) // the chart pictures are taken by tasks queued after the others
    }

    private fun shoot(folder: File): String {
        val report = StringBuilder()
        val views = linkedMapOf<String, () -> MmxView>(
            "mood" to { MoodColorView() },
            "patterns" to { ColorPatternsView() },
            "guitar" to { GuitarView() },
            "chord_relations" to { ChordRelationView() },
            "midi" to { MidiView() },
            "midi_activity" to { MidiActivityView() },
            "sheetmusic" to { SheetMusicView() },
            "learnstrip" to { LearnStripView() },
            "flyover" to { FlyOverView() },
            "led" to { LedStripView() },
            "charts_bar" to { chartsWithNotes(0) },
            "charts_ridge" to { chartsWithNotes(3) },
            "charts_radar" to { chartsWithNotes(4) }
        )
        val pending = mutableListOf<CompletableFuture<Unit>>()
        views.forEach { (name, create) ->
            try {
                val view = create()
                val stage = Stage()
                stage.scene = Scene(view.rootNode as Region, 1100.0, 640.0)
                stage.show()
                if (name.startsWith("charts_")) {
                    // The charts apply their notes with Platform.runLater: take the picture after that has run
                    val later = CompletableFuture<Unit>()
                    Platform.runLater {
                        Platform.runLater {
                            repeat(3) { stage.scene.snapshot(null) }
                            save(stage.scene.root, File(folder, "$name.png"))
                            later.complete(Unit)
                        }
                    }
                    pending.add(later)
                    return@forEach
                }
                repeat(3) { stage.scene.snapshot(null) }
                save(stage.scene.root, File(folder, "$name.png"))
                listOf("mood", "patterns").firstOrNull { it == name }?.let {
                    view.demoSettingsPanel(if (name == "mood") "mood" else "patterns")?.let { panel ->
                        val settings = Stage()
                        settings.scene = Scene(panel as Region, 460.0, 700.0)
                        settings.show()
                        save(settings.scene.root, File(folder, "${name}_settings.png"))
                        settings.close()
                    }
                }
                view.dispose()
                stage.close()
                report.append("ok   $name\n")
            } catch (t: Throwable) {
                report.append("FAIL $name: $t\n")
            }
        }
        // Wait for the chart pictures without blocking the FX thread: they are taken by tasks queued after this one
        report.append("charts pictures queued: ${pending.size}\n")
        return report.toString()
    }

    private fun chartsWithNotes(chart: Int): ChartsView {
        val view = ChartsView()
        @Suppress("UNCHECKED_CAST")
        val holders = view.javaClass.getDeclaredField("chartHolders").apply { isAccessible = true }
            .get(view) as List<ChartsView.ChartHolder>
        val random = java.util.Random(3)
        val keys = be.codewriter.melodymatrix.view.definition.Note.pianoKeys()
        repeat(300) { val note = keys[random.nextInt(keys.size)]; holders.forEach { it.chart.onNote(note) } }
        view.demoSetting("chart", chart.toString())
        return view
    }

    private fun save(node: Node, file: File) {
        val image = node.snapshot(null, null)
        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file)
    }
}
