package stage.ledstrip

import atlantafx.base.controls.ToggleSwitch
import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.event.MidiDataEvent
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.MmxEventType
import be.codewriter.melodymatrix.view.stage.MmxMmxView
import be.codewriter.melodymatrix.view.stage.MmxViewMetadata
import be.codewriter.melodymatrix.view.stage.ledstrip.pixelblaze.PixelblazeOutputExpanderHelper
import com.fazecast.jSerialComm.SerialPort
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.geometry.Pos
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
import stage.ledstrip.LedStripView.Companion.effectSpeed
import stage.ledstrip.LedStripView.Companion.effectWidth
import stage.ledstrip.LedStripView.Companion.updateLedStrip
import java.util.concurrent.Executors
import kotlin.math.abs


/**
 * Visualizer stage that drives a physical LED strip and an on-screen preview of it.
 *
 * Each piano key maps to a rectangle in the on-screen preview and to a corresponding
 * LED on the strip. When a NOTE_ON event is received the matching LED (and its neighbours
 * within [effectWidth]) fade from the highlight colour back to the normal colour.
 *
 * The LED strip is driven via a Pixelblaze Output Expander connected over a serial port.
 * A background [BoxUpdater] thread refreshes the fade animation at ~50 fps, and a
 * [LedStripSender] thread pushes colour data to the hardware at the same rate.
 *
 * @see MmxMmxView
 * @see ColorBox
 * @see PixelblazeOutputExpanderHelper
 */
class LedStripView : MmxMmxView() {

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

        val root = VBox().apply {
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
        }

        val factory = Thread.ofVirtual().name("led-strip-", 0).factory()
        val executor = Executors.newThreadPerTaskExecutor(factory)
        val threadUiUpdate = Thread.startVirtualThread(BoxUpdater())
        executor.submit(threadUiUpdate)
        val threadLedUpdate = Thread.startVirtualThread(LedStripSender())
        executor.submit(threadLedUpdate)

        setupSurface(root, windowWidth, BOX_HEIGHT + 320) {
            updateLedStrip = false
            for (i in 0 until 8) {
                try {
                    pixelblazeOutputExpanderHelper!!.sendAllOff(i, boxes.size)
                } catch (e: Exception) {
                    // Serial can be tricky, so let's catch any exception here...
                }
            }
        }
    }

    /**
     * Builds the colour configuration panel.
     *
     * Creates controls for white key normal/highlight colours, black key normal/highlight colours,
     * a toggle to mirror white-key colours on black keys, and sliders for effect width and duration.
     *
     * @return A [VBox] containing all colour configuration controls
     */
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

    /**
     * Builds the serial port and channel selection panel.
     *
     * Creates a [ComboBox] populated with available serial ports and a row of
     * channel toggle switches (0–7) for the Pixelblaze Output Expander.
     *
     * @return An [HBox] containing the serial port selector and channel toggles
     */
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

    /**
     * Retrieves all available serial ports on the current machine.
     *
     * @return An observable list of [SerialPort] instances; empty if none are found or an error occurs
     */
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

    /**
     * Background runnable that continuously refreshes the fade animation for all colour boxes.
     *
     * Runs in a dedicated thread and calls [ColorBox.update] every 20 ms until interrupted.
     */
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

    /**
     * Background runnable that continuously sends the current colour state to the LED strip hardware.
     *
     * Runs in a dedicated thread, serialises all [ColorBox] colours into a byte array, and sends
     * them via [PixelblazeOutputExpanderHelper] every 20 ms while [updateLedStrip] is true.
     */
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

        /**
         * Sends the current colour state of all boxes to the Pixelblaze Output Expander.
         *
         * Re-opens the serial connection if the port has changed, then writes RGB byte triplets
         * for every enabled channel.
         */
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

    /**
     * Starts the fade animation for the box that corresponds to the given note and its neighbours.
     *
     * The number of neighbouring boxes affected is controlled by [effectWidth].
     * Each neighbour receives a delay proportional to its distance from the played note.
     *
     * @param note The note whose corresponding LED box should be highlighted
     */
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

    /**
     * Represents a single LED / piano-key colour rectangle with a fade animation.
     *
     * The box interpolates from the highlight colour back to the normal colour over time.
     * The animation speed and initial delay depend on [effectSpeed] and the note's distance
     * from the played note, respectively.
     *
     * @property note The piano key this box represents
     * @property boxWidth The width of the on-screen rectangle in pixels
     * @property box The JavaFX [Rectangle] used for the on-screen preview
     */
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

        /**
         * Schedules this box to begin its fade animation after a delay proportional to [distance].
         *
         * @param distance The number of keys away from the played note (used for ripple delay)
         */
        fun startFade(distance: Int) {
            this.distance = distance
            startTimestamp = System.currentTimeMillis() + (distance * 20)
        }

        /**
         * Applies [getCurrentColor] to the rectangle on the JavaFX application thread.
         */
        fun update() {
            Platform.runLater {
                box.fill = getCurrentColor()
            }
        }

        /**
         * Calculates the current interpolated colour based on the fade animation state.
         *
         * @return The colour the box should display at this moment
         */
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

        /**
         * Returns the current fill colour of the rectangle as a [Color].
         *
         * @return The current fill colour
         */
        fun getColor(): Color {
            return (box.fill as Color)
        }
    }

    /**
     * Handles incoming MelodyMatrix events.
     *
     * Triggers [highlightBox] for NOTE_ON MIDI events. PLAY and CHORD events are ignored.
     *
     * @param event The MelodyMatrix event to process
     */
    override fun onEvent(event: MmxEvent) {
        when (event.type) {
            MmxEventType.MIDI -> {
                val midiDataEvent = event as? MidiDataEvent ?: return
                if (midiDataEvent.event == MidiEvent.NOTE_ON) {
                    highlightBox(midiDataEvent.note)
                }
            }

            MmxEventType.PLAY -> {
                // Not need in this viewer
            }

            MmxEventType.CHORD -> {
                // Not needed in this viewer
            }
        }
    }

    companion object : MmxViewMetadata {
        override fun getViewTitle(): String = "Let's flash some lights..."
        override fun getViewDescription(): String =
            "Drives an LED strip and on-screen LED preview from incoming MIDI notes."

        override fun getViewImagePath(): String = "/stage/led.png"
        private val logger: Logger = LogManager.getLogger(LedStripView::class.java.name)
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