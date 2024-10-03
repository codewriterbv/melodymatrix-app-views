package stage.ledstrip

import atlantafx.base.controls.ToggleSwitch
import be.codewriter.melodymatrix.view.VisualizerStage
import be.codewriter.melodymatrix.view.data.MidiData
import be.codewriter.melodymatrix.view.data.PlayEvent
import be.codewriter.melodymatrix.view.definition.MidiEvent
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
            hgap = 2.0
            vgap = 0.0
        }

        var counter = 0
        Note.entries.forEach { note ->
            val colorBox = ColorBox(note)
            boxes[note] = colorBox
            grid.add(colorBox.box, counter, 0)
            counter++
        }

        title = "Let's flash some lights..."
        scene = Scene(VBox().apply {
            spacing = 25.0
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
        }, (boxes.size * (BOX_WIDTH + 2)) + 20, BOX_HEIGHT + 320)

        val factory = Thread.ofVirtual().name("led-strip-", 0).factory()
        val executor = Executors.newThreadPerTaskExecutor(factory)
        val threadUiUpdate = Thread.startVirtualThread(BoxUpdater())
        executor.submit(threadUiUpdate)
        val threadLedUpdate = Thread.startVirtualThread(LedStripSender())
        executor.submit(threadLedUpdate)

        setOnCloseRequest {
            // Nothing needed here, but must be defined or will cause a problem when closing the window
        }
    }

    fun getColorControls(): VBox {
        colorWhiteKeyNormal.apply {
            value = Color.NAVY
        }
        colorWhiteKeyHighlighted.apply {
            value = Color.RED
        }

        colorBlackKeyNormal.apply {
            value = Color.NAVY
            disableProperty().bind(sameColor.selectedProperty())
        }
        colorBlackKeyHighlighted.apply {
            value = Color.RED
            disableProperty().bind(sameColor.selectedProperty())
        }

        sameColor.apply {
            isSelected = true
        }

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

        return VBox().apply {
            spacing = 10.0
            alignment = Pos.CENTER_LEFT
            children.addAll(
                HBox().apply {
                    spacing = 10.0
                    alignment = Pos.CENTER_LEFT
                    children.addAll(
                        Label("White keys").apply {
                            minWidth = 150.0
                        },
                        Label("Normal"),
                        colorWhiteKeyNormal,
                        Label("Highlight"),
                        colorWhiteKeyHighlighted
                    )
                },

                HBox().apply {
                    spacing = 10.0
                    alignment = Pos.CENTER_LEFT
                    children.addAll(
                        Label("Black keys").apply {
                            minWidth = 150.0
                        },
                        Label("Normal"),
                        colorBlackKeyNormal,
                        Label("Highlight"),
                        colorBlackKeyHighlighted,
                        sameColor
                    )
                },
                HBox().apply {
                    spacing = 10.0
                    alignment = Pos.CENTER_LEFT
                    children.addAll(
                        Label("Effect settings").apply {
                            minWidth = 150.0
                        },
                        Label("Width"),
                        effectWidth,
                        Label("Duration"),
                        effectSpeed
                    )
                }
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
                serialPort.apply {
                    maxWidth = 200.0
                },
                Label("Channels: 0"),
                channel0.apply {
                    isSelected = true
                },
                Label("1"),
                channel1,
                Label("2"),
                channel2,
                Label("3"),
                channel3,
                Label("4"),
                channel4,
                Label("5"),
                channel5,
                Label("6"),
                channel6,
                Label("7"),
                channel7
            )
        }
    }

    private fun getSerialPorts(): ObservableList<SerialPort> {
        val serialPorts = FXCollections.observableArrayList<SerialPort>()
        try {
            val ports = SerialPort.getCommPorts()
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
                Thread.sleep(20)
            }
        }

        fun updateLeds() {
            val serialPort = serialPort.value
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
                val color = boxes.values.elementAt(i).getColor()
                data[(i * 3) + 0] = (color.red * 255).toInt().toByte()
                data[(i * 3) + 1] = (color.green * 255).toInt().toByte()
                data[(i * 3) + 2] = (color.blue * 255).toInt().toByte()
            }
            if (channel0.isSelected) {
                pixelblazeOutputExpanderHelper!!.sendColors(0, data, false)
            }
            if (channel1.isSelected) {
                pixelblazeOutputExpanderHelper!!.sendColors(1, data, false)
            }
            if (channel2.isSelected) {
                pixelblazeOutputExpanderHelper!!.sendColors(2, data, false)
            }
            if (channel3.isSelected) {
                pixelblazeOutputExpanderHelper!!.sendColors(3, data, false)
            }
            if (channel4.isSelected) {
                pixelblazeOutputExpanderHelper!!.sendColors(4, data, false)
            }
            if (channel5.isSelected) {
                pixelblazeOutputExpanderHelper!!.sendColors(5, data, false)
            }
            if (channel6.isSelected) {
                pixelblazeOutputExpanderHelper!!.sendColors(6, data, false)
            }
            if (channel7.isSelected) {
                pixelblazeOutputExpanderHelper!!.sendColors(7, data, false)
            }
        }
    }

    private fun highlightBox(note: Note) {
        val idx = boxes.keys.indexOf(note)
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
        val note: Note,
        val box: Rectangle = Rectangle(
            BOX_WIDTH,
            BOX_HEIGHT,
            if (note.mainNote.isSharp) colorBlackKeyNormal.value else colorWhiteKeyNormal.value
        )
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
            var normalColor = colorWhiteKeyNormal.value
            var highlightColor = colorWhiteKeyHighlighted.value
            if (note.mainNote.isSharp && !sameColor.isSelected) {
                normalColor = colorBlackKeyNormal.value
                highlightColor = colorBlackKeyHighlighted.value
            }
            val startColor = highlightColor.interpolate(normalColor, distance.toDouble() / effectWidth.value.toInt())
            if (startTimestamp != 0L && System.currentTimeMillis() >= startTimestamp) {
                startTimestamp = 0
                step = 0
                return startColor
            }

            if (step == -1) {
                return normalColor
            }

            step++
            if (step > effectSpeed.value) {
                step = -1
                return normalColor
            }

            return startColor.interpolate(normalColor, step.toDouble() / effectSpeed.value)
        }

        fun getColor(): Color {
            return (box.fill as Color)
        }
    }

    override fun onMidiData(midiData: MidiData) {
        if (midiData.event == MidiEvent.NOTE_ON) {
            highlightBox(midiData.note)
        }
    }

    override fun onPlayEvent(playEvent: PlayEvent) {
        // Not need in this viewer
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(LedStripStage::class.java.name)
        val boxes: MutableMap<Note, ColorBox> = mutableMapOf()
        const val BOX_WIDTH = 8.0
        const val BOX_HEIGHT = 50.0

        val colorWhiteKeyNormal = ColorPicker()
        val colorWhiteKeyHighlighted = ColorPicker()
        val colorBlackKeyNormal = ColorPicker()
        val colorBlackKeyHighlighted = ColorPicker()
        val sameColor = ToggleSwitch("Same as white keys")
        val effectWidth = Slider()
        val effectSpeed = Slider()
        val serialPort = ComboBox<SerialPort>()
        val channel0 = ToggleSwitch()
        val channel1 = ToggleSwitch()
        val channel2 = ToggleSwitch()
        val channel3 = ToggleSwitch()
        val channel4 = ToggleSwitch()
        val channel5 = ToggleSwitch()
        val channel6 = ToggleSwitch()
        val channel7 = ToggleSwitch()
        var pixelblazeOutputExpanderHelper: PixelblazeOutputExpanderHelper? = null
    }
}