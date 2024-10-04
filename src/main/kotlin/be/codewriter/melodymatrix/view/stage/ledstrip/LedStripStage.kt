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
        val boxHolder = HBox().apply {
            spacing = 2.0
            alignment = Pos.TOP_CENTER
        }

        var windowWidth = 960.0
        var counter = 0
        var boxWidth = ((windowWidth - 80) / Note.pianoKeys().size) - boxHolder.spacing
        Note.pianoKeys().forEach { note ->
            val colorBox = ColorBox(note, boxWidth)
            boxes[note] = colorBox
            boxHolder.children.add(colorBox.box)
            counter++
        }

        title = "Let's flash some lights..."
        scene = Scene(VBox().apply {
            spacing = 25.0
            padding = Insets(10.0)
            children.addAll(
                boxHolder,
                Label("Color settings").apply {
                    style = "-fx-font-size: 18px; -fx-font-weight: bold;"
                },
                getColorControls(),
                Label("LED strip controller settings").apply {
                    style = "-fx-font-size: 18px; -fx-font-weight: bold;"
                },
                getSerialControls()
            )
        }, windowWidth, BOX_HEIGHT + 320)

        val factory = Thread.ofVirtual().name("led-strip-", 0).factory()
        val executor = Executors.newThreadPerTaskExecutor(factory)
        val threadUiUpdate = Thread.startVirtualThread(BoxUpdater())
        executor.submit(threadUiUpdate)
        val threadLedUpdate = Thread.startVirtualThread(LedStripSender())
        executor.submit(threadLedUpdate)

        setOnCloseRequest {
            updateLedStrip = false
            for (i in 0 until 8) {
                pixelblazeOutputExpanderHelper!!.sendAllOff(i, boxes.size)
            }
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
        channel0.apply {
            isSelected = true
        }
        return HBox().apply {
            spacing = 10.0
            alignment = Pos.CENTER_LEFT
            children.addAll(
                Label("Serial port"),
                serialPort.apply {
                    maxWidth = 200.0
                },
                Label("Channels:")
            )
            for (i in 0 until 8) {
                children.addAll(
                    Label((i + 1).toString()),
                    channelSelections.get(i),
                )
            }
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
            while (!Thread.interrupted() && updateLedStrip) {
                try {
                    if (pixelblazeOutputExpanderHelper != null || serialPort.value != null) {
                        updateLeds()
                    }
                    Thread.sleep(20)
                } catch (e: Exception) {
                    logger.warn("LED strip sender thread exception: {}", e.message)
                }
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
            for (i in 0 until 8) {
                if (channelSelections.get(i).isSelected) {
                    pixelblazeOutputExpanderHelper!!.sendColors(i, data, false)
                }
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
        for (i in start until end + 1) {
            val delay = abs(idx - i)
            boxes.values.elementAtOrNull(i)?.startFade(delay)
        }
    }

    class ColorBox(
        val note: Note,
        val boxWidth: Double,
        val box: Rectangle = Rectangle(
            boxWidth,
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
        const val BOX_HEIGHT = 50.0

        var updateLedStrip = true
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
        val channelSelections = listOf(channel0, channel1, channel2, channel3, channel4, channel5, channel6, channel7)
        var pixelblazeOutputExpanderHelper: PixelblazeOutputExpanderHelper? = null
    }
}