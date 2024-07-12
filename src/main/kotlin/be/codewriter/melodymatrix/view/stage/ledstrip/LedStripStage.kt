package be.codewriter.melodymatrix.view.stage.ledstrip

import be.codewriter.melodymatrix.view.VisualizerStage
import be.codewriter.melodymatrix.view.data.MidiData
import be.codewriter.melodymatrix.view.data.PlayEvent
import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.stage.ledstrip.pixelblaze.PixelblazeOutputExpanderHelper
import com.fazecast.jSerialComm.SerialPort
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.ColorPicker
import javafx.scene.control.ComboBox
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
            spacing = 10.0
            padding = Insets(10.0)
            children.addAll(
                grid,
                Label("Color settings").apply {
                    style = "-fx-font-size: 18px; -fx-font-weight: bold;"
                },
                getColorControls(),
                Label("LED strip controller settings").apply {
                    style = "-fx-font-size: 18px; -fx-font-weight: bold;"
                },
                getSerialControls()
            )
        }, (boxes.size * BOX_WIDTH) + 20, BOX_HEIGHT + 200)

        val factory = Thread.ofVirtual().name("led-strip-", 0).factory()
        val executor = Executors.newThreadPerTaskExecutor(factory)
        var threadUiUpdate = Thread.startVirtualThread(BoxUpdater())
        executor.submit(threadUiUpdate)
        var threadLedUpdate = Thread.startVirtualThread(LedStripSender())
        executor.submit(threadLedUpdate)

        setOnCloseRequest {
            // Nothing needed here, but must be defined or will cause a problem when closing the window
        }
    }

    fun getColorControls(): HBox {
        colorNormal.value = Color.LIGHTBLUE
        colorHighlighted.value = Color.RED
        effectWidth.apply {
            min = 1.0
            max = 50.0
            value = 10.0
        }
        effectSpeed.apply {
            min = 10.0
            max = 200.0
            value = 20.0
        }
        return HBox().apply {
            spacing = 10.0
            alignment = Pos.CENTER_LEFT
            children.addAll(
                Label("Normal"),
                colorNormal,
                Label("Highlight"),
                colorHighlighted,
                Label("Effect width"),
                effectWidth,
                Label("Effect duration"),
                effectSpeed
            )
        }
    }

    fun getSerialControls(): HBox {
        serialPort.apply {
            items = getSerialPorts()
        }
        return HBox().apply {
            spacing = 10.0
            alignment = Pos.CENTER_LEFT
            children.addAll(
                Label("Serial port"),
                serialPort
            )
        }
    }

    private fun getSerialPorts(): ObservableList<SerialPort> {
        var serialPorts = FXCollections.observableArrayList<SerialPort>()
        try {
            var ports = SerialPort.getCommPorts()
            if (ports != null) {
                serialPorts.addAll(ports)
            }
        } catch (e: Exception) {
            logger.error("Could not get serial ports: {}", e.message)
        }
        return serialPorts
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

    class LedStripSender : Runnable {
        override fun run() {
            while (!Thread.interrupted()) {
                if (pixelblazeOutputExpanderHelper != null || serialPort.value != null) {
                    updateLeds()
                }
                Thread.sleep(10)
            }
        }

        fun updateLeds() {
            var serialPort = serialPort.value
            if (pixelblazeOutputExpanderHelper == null || pixelblazeOutputExpanderHelper!!.address != serialPort.systemPortName) {
                if (pixelblazeOutputExpanderHelper != null) {
                    pixelblazeOutputExpanderHelper!!.closePort()
                }
                pixelblazeOutputExpanderHelper = PixelblazeOutputExpanderHelper(serialPort.systemPortName)
                pixelblazeOutputExpanderHelper!!.sendAllOff(0, boxes.size)
                Thread.sleep(500)
            }
            val data = ByteArray(boxes.size * 3)
            for (i in 0 until boxes.size) {
                data[(i * 3) + 0] = boxes.values.elementAt(i).getGreen()
                data[(i * 3) + 1] = boxes.values.elementAt(i).getRed()
                data[(i * 3) + 2] = boxes.values.elementAt(i).getBlue()
            }
            pixelblazeOutputExpanderHelper!!.sendColors(0, data, false)
        }
    }

    private fun highlightBox(note: Note) {
        var idx = boxes.keys.indexOf(note)
        var start = idx - effectWidth.value.toInt()
        if (start < 0) {
            start = 0
        }
        var end = idx + effectWidth.value.toInt()
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
        val box: Rectangle = Rectangle(BOX_WIDTH, BOX_HEIGHT, colorNormal.value)
    ) : Rectangle() {
        var distance: Int = 0
        var startTimestamp: Long = 0L
        var step: Int = -1

        fun startFade(distance: Int) {
            this.distance = distance
            startTimestamp = System.currentTimeMillis() + (distance * 20)
        }

        fun update() {
            Platform.runLater {
                box.fill = getCurrentColor()
            }
        }

        fun getCurrentColor(): Color {
            var startColor =
                colorHighlighted.value.interpolate(colorNormal.value, distance.toDouble() / effectWidth.value.toInt())
            if (startTimestamp != 0L && System.currentTimeMillis() >= startTimestamp) {
                startTimestamp = 0
                step = 0
                return startColor
            }

            if (step == -1) {
                return colorNormal.value
            }

            step++

            if (step > effectSpeed.value) {
                step = -1
                return colorNormal.value
            }

            var fadeColor = startColor.interpolate(colorNormal.value, step.toDouble() / effectSpeed.value)
            return fadeColor
        }

        fun getRed(): Byte {
            return (box.fill as Color).red.toInt().toByte()
        }

        fun getGreen(): Byte {
            return (box.fill as Color).green.toInt().toByte()
        }

        fun getBlue(): Byte {
            return (box.fill as Color).blue.toInt().toByte()
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

        val colorNormal = ColorPicker()
        val colorHighlighted = ColorPicker()
        val effectWidth = Slider()
        val effectSpeed = Slider()
        val serialPort = ComboBox<SerialPort>()

        var pixelblazeOutputExpanderHelper: PixelblazeOutputExpanderHelper? = null
    }
}