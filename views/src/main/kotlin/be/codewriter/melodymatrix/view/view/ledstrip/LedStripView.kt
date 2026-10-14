package be.codewriter.melodymatrix.view.view.ledstrip

import atlantafx.base.controls.ToggleSwitch
import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.event.MidiDataEvent
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.MmxEventType
import be.codewriter.melodymatrix.view.view.MmxView
import be.codewriter.melodymatrix.view.view.MmxViewMetadata
import be.codewriter.melodymatrix.view.view.ledstrip.pixelblaze.PixelblazeOutputExpanderHelper
import be.codewriter.melodymatrix.view.view.ledstrip.serial.JSerialCommPorts
import be.codewriter.melodymatrix.view.view.ledstrip.serial.SerialPortInfo
import be.codewriter.melodymatrix.view.view.ledstrip.serial.SerialPorts
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
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs


/**
 * Visualizer stage that drives a physical LED strip and an on-screen preview of it.
 *
 * Each piano key maps to a rectangle in the on-screen preview and to a corresponding
 * LED on the strip. When a NOTE_ON event is received the matching LED (and its neighbours
 * within [effectWidth]) fade from the highlight colour back to the normal colour.
 *
 * The LED strip is driven via a Pixelblaze Output Expander connected over a serial port.
 * A background [BoxUpdater] thread triggers the fade animation at ~50 fps on the JavaFX thread,
 * which also produces an immutable [LedSnapshot]. A [LedStripSender] thread pushes that snapshot
 * to the hardware at the same rate, so it never reads JavaFX controls itself.
 *
 * @see MmxView
 * @see ColorBox
 * @param serialPorts access to the serial ports; jSerialComm by default
 * @see PixelblazeOutputExpanderHelper
 */
class LedStripView(private val serialPorts: SerialPorts = JSerialCommPorts) : MmxView() {

    private val boxes: MutableMap<Note, ColorBox> = linkedMapOf()
    private val colorWhiteKeyNormal = ColorPicker()
    private val colorWhiteKeyHighlighted = ColorPicker()
    private val colorBlackKeyNormal = ColorPicker()
    private val colorBlackKeyHighlighted = ColorPicker()
    private val sameColor = ToggleSwitch("Same as white keys")
    private val effectWidth = Slider()
    private val effectSpeed = Slider()
    private val serialPort = ComboBox<SerialPortInfo>()
    private val channelSelections = List(8) { ToggleSwitch() }

    /** Latest state for the [LedStripSender], replaced on the JavaFX thread. */
    @Volatile
    private var ledSnapshot = LedSnapshot(null, ByteArray(0), emptyList())

    init {
        val boxHolder = HBox().apply {
            spacing = 2.0
            alignment = Pos.TOP_CENTER
        }

        val windowWidth = 960.0
        val boxWidth = ((windowWidth - 80) / Note.pianoKeys().size) - boxHolder.spacing
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
        // The boxes use the colours of the pickers, so create them after the colour controls
        Note.pianoKeys().forEach { note ->
            val colorBox = ColorBox(note, boxWidth)
            boxes[note] = colorBox
            boxHolder.children.add(colorBox.box)
        }

        // Both loops run until interrupted, so they must be stopped when the view is closed.
        val threadUiUpdate = Thread.ofVirtual().name("led-strip-ui").start(BoxUpdater())
        val threadLedUpdate = Thread.ofVirtual().name("led-strip-sender").start(LedStripSender())

        setupSurface(root, windowWidth, BOX_HEIGHT + 320) {
            threadUiUpdate.interrupt()
            // The sender switches the LEDs off and closes the port when it stops
            threadLedUpdate.interrupt()
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
        channelSelections[0].isSelected = true
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
                    channelSelections[i],
                )
            }
        }
    }

    /**
     * Retrieves all available serial ports on the current machine.
     *
     * @return An observable list of the available ports; empty if none are found
     */
    private fun getSerialPorts(): ObservableList<SerialPortInfo> =
        FXCollections.observableArrayList(serialPorts.available())

    /**
     * Background runnable that refreshes the fade animation for all colour boxes every 20 ms.
     *
     * The colours are calculated and applied on the JavaFX thread, in one `runLater` per frame
     * (skipped while the previous frame is still pending), which also publishes a new [LedSnapshot].
     */
    private inner class BoxUpdater : Runnable {
        private val framePending = AtomicBoolean(false)

        override fun run() {
            try {
                while (!Thread.interrupted()) {
                    if (framePending.compareAndSet(false, true)) {
                        Platform.runLater {
                            try {
                                updateFrame()
                            } finally {
                                framePending.set(false)
                            }
                        }
                    }
                    Thread.sleep(20)
                }
            } catch (_: InterruptedException) {
                // View closed
            }
        }

        private fun updateFrame() {
            val data = ByteArray(boxes.size * 3)
            boxes.values.forEachIndexed { i, colorBox ->
                val color = colorBox.update()
                data[(i * 3) + 0] = (color.red * 255).toInt().toByte()
                data[(i * 3) + 1] = (color.green * 255).toInt().toByte()
                data[(i * 3) + 2] = (color.blue * 255).toInt().toByte()
            }
            ledSnapshot = LedSnapshot(
                serialPort.value,
                data,
                channelSelections.indices.filter { channelSelections[it].isSelected }
            )
        }
    }

    /**
     * What the [LedStripSender] sends: the selected port, RGB byte triplets for every box, and the
     * enabled Pixelblaze channels. Created on the JavaFX thread and never changed afterwards.
     */
    private class LedSnapshot(val port: SerialPortInfo?, val data: ByteArray, val channels: List<Int>)

    /**
     * Background runnable that sends the latest [LedSnapshot] to the LED strip hardware every 20 ms.
     *
     * Only this thread uses the serial connection: when the view is closed (interrupt) it switches
     * all LEDs off and closes the port.
     */
    private inner class LedStripSender : Runnable {
        private var pixelblazeOutputExpanderHelper: PixelblazeOutputExpanderHelper? = null

        override fun run() {
            try {
                while (!Thread.currentThread().isInterrupted) {
                    try {
                        updateLeds(ledSnapshot)
                        Thread.sleep(20)
                    } catch (_: InterruptedException) {
                        return // View closed
                    } catch (e: Exception) {
                        logger.warn("LED strip sender thread exception: {}", e.message)
                    }
                }
            } finally {
                switchOff()
            }
        }

        /**
         * Sends the colours of the snapshot to the Pixelblaze Output Expander.
         *
         * Re-opens the serial connection if the port has changed, then writes the RGB data
         * for every enabled channel.
         */
        private fun updateLeds(snapshot: LedSnapshot) {
            val port = snapshot.port ?: return
            var helper = pixelblazeOutputExpanderHelper
            if (helper == null || helper.address != port.systemName) {
                helper?.closePort()
                helper = PixelblazeOutputExpanderHelper(port.systemName, serialPorts.open(port.systemName))
                pixelblazeOutputExpanderHelper = helper
                helper.sendAllOff(0, boxes.size)
                Thread.sleep(500)
            }
            for (channel in snapshot.channels) {
                helper.sendColors(channel, snapshot.data, false)
            }
        }

        private fun switchOff() {
            val helper = pixelblazeOutputExpanderHelper ?: return
            for (i in 0 until 8) {
                try {
                    helper.sendAllOff(i, boxes.size)
                } catch (_: Exception) {
                    // Serial can be tricky, so let's catch any exception here...
                }
            }
            try {
                helper.closePort()
            } catch (_: Exception) {
                // Port already gone
            }
            pixelblazeOutputExpanderHelper = null
        }
    }

    /**
     * Starts the fade animation for the box that corresponds to the given note and its neighbours.
     *
     * The number of neighbouring boxes affected is controlled by [effectWidth].
     * Each neighbour receives a delay proportional to its distance from the played note.
     *
     * Must be called on the JavaFX thread, as it reads the [effectWidth] slider and changes the
     * fade state that [ColorBox.update] uses.
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
    inner class ColorBox(
        val note: Note,
        val boxWidth: Double
    ) {
        val box: Rectangle = Rectangle(
            boxWidth,
            BOX_HEIGHT,
            if (note.mainNote.isSharp) colorBlackKeyNormal.value else colorWhiteKeyNormal.value
        )
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
         * Applies [getCurrentColor] to the rectangle. Must be called on the JavaFX thread.
         *
         * @return The colour that was applied
         */
        fun update(): Color = getCurrentColor().also { box.fill = it }

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
                    // Events arrive on the MIDI/playback thread
                    Platform.runLater { highlightBox(midiDataEvent.note) }
                }
            }

            MmxEventType.PLAY -> {
                // Not need in this viewer
            }

            MmxEventType.CHORD -> {
                // Not needed in this viewer
            }

            MmxEventType.AUDIO_SPECTRUM -> {
                // Not needed in this viewer
            }

            MmxEventType.PLAYBACK_STOP -> {
                // Not needed here
            }

            MmxEventType.SCORE_LOADED -> {
                // Not needed here
            }
            }
            }

    companion object : MmxViewMetadata {
        override val bundleBaseName = "i18n/view/led"
        override fun getViewImagePath(): String = "/view/led.png"
        private val logger: Logger = LogManager.getLogger(LedStripView::class.java.name)
        const val BOX_HEIGHT = 50.0
    }
}
