package be.codewriter.melodymatrix.view.stage.ledstrip

import be.codewriter.melodymatrix.view.VisualizerStage
import be.codewriter.melodymatrix.view.data.MidiData
import be.codewriter.melodymatrix.view.data.PlayEvent
import be.codewriter.melodymatrix.view.definition.Note
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.ColorPicker
import javafx.scene.control.Label
import javafx.scene.control.Slider
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.Executors
import kotlin.math.abs

class LedStripStage : VisualizerStage() {
    init {
        val grid = GridPane().apply {
            hgap = 0.0
            vgap = 0.0
        }

        var counter = 0
        Note.entries.forEach { note ->
            var colorBox = ColorBox(counter, note)
            boxes[note] = colorBox
            grid.add(colorBox.box, counter, 0)
            counter++
        }

        title = "Let's flash some lights..."
        scene = Scene(VBox().apply {
            spacing = 25.0
            children.addAll(
                grid,
                getControls()
            )
        }, boxes.size * BOX_WIDTH, BOX_HEIGHT + 100)

        val factory = Thread.ofVirtual().name("led-highlighter-", 0).factory()
        val executor = Executors.newThreadPerTaskExecutor(factory)
        var thread = Thread.startVirtualThread(BoxUpdater())
        executor.submit(thread)

        setOnCloseRequest {
            // Nothing needed here, but must be defined or will cause a problem when closing the window
        }
    }

    fun getControls(): HBox {
        return HBox().apply {
            spacing = 10.0
            alignment = Pos.BASELINE_CENTER
            children.addAll(
                Label("Normal"),
                colorNormal,
                Label("Highlight"),
                colorHighlighted,
                Label("Effect width"),
                effectWidth,
                Label("Effect speed"),
                effectSpeed
            )
        }
    }

    class BoxUpdater : Runnable {
        override fun run() {
            while (!Thread.interrupted()) {
                for (box in boxes) {
                    box.value.update()
                }
                Thread.sleep(20)
            }
        }
    }

    private fun highlightBox(note: Note) {
        var idx = boxes.keys.indexOf(note)
        var start = idx - 10
        if (start < 0) {
            start = 0
        }
        var end = idx + 10
        if (end > boxes.size - 1) {
            end = boxes.size - 1
        }

        for (i in start until end) {
            val delay = abs(idx - i)
            boxes.values.elementAtOrNull(i)?.startFade(delay)
        }
    }

    class ColorBox(
        val counter: Int,
        val note: Note,
        val box: Rectangle = Rectangle(BOX_WIDTH, BOX_HEIGHT, BASE_COLOR)
    ) : Rectangle() {
        var distance: Int = 0
        var startTimestamp: Long = 0L
        var step: Int = -1

        fun startFade(distance: Int) {
            this.distance = distance
            startTimestamp = System.currentTimeMillis() + (distance * 20)
        }

        fun update() {
            var startColor = HIGHLIGHT_COLOR.interpolate(BASE_COLOR, distance.toDouble() / 10)
            if (startTimestamp != 0L && System.currentTimeMillis() >= startTimestamp) {
                Platform.runLater {
                    box.fill = startColor
                }
                startTimestamp = 0
                step = 0
                return
            }

            if (step == -1) {
                return
            }

            step++

            if (step > 20) {
                step = -1
                return
            }

            var fadeColor = startColor.interpolate(BASE_COLOR, step.toDouble() / 20)
            Platform.runLater {
                box.fill = fadeColor
            }

            if (note == Note.C5) {
                logger.info("Fading {} to {}", step, fadeColor)
            }
        }
    }

    override fun onMidiData(midiData: MidiData) {
        highlightBox(midiData.note)
    }

    override fun onPlayEvent(playEvent: PlayEvent) {
        TODO("Not yet implemented")
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(LedStripStage::class.java.name)
        val boxes: MutableMap<Note, ColorBox> = mutableMapOf()
        const val BOX_WIDTH = 10.0
        const val BOX_HEIGHT = 50.0
        val BASE_COLOR = Color.LIGHTBLUE
        val HIGHLIGHT_COLOR = Color.RED

        val colorNormal: ColorPicker = ColorPicker()
        val colorHighlighted: ColorPicker = ColorPicker()
        val effectWidth: Slider = Slider()
        val effectSpeed: Slider = Slider()
    }
}