package be.codewriter.melodymatrix.view.view.piano

import atlantafx.base.controls.ToggleSwitch
import be.codewriter.melodymatrix.view.data.LicenseStatus
import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.event.MidiDataEvent
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.MmxEventType
import be.codewriter.melodymatrix.view.view.MmxView
import be.codewriter.melodymatrix.view.view.MmxViewMetadata
import be.codewriter.melodymatrix.view.view.piano.configurator.*
import be.codewriter.melodymatrix.view.view.piano.data.PianoConfiguration
import be.codewriter.melodymatrix.view.view.piano.keyboard.KeyboardView
import be.codewriter.melodymatrix.view.view.piano.scene.PianoCanvas
import javafx.animation.AnimationTimer
import javafx.application.Platform
import javafx.beans.property.BooleanProperty
import javafx.event.ActionEvent
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.transform.Scale
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * Visualizer stage that displays an interactive piano keyboard.
 *
 * Shows a piano keyboard view and an animated scene above it that reacts to MIDI events.
 * Supports configurable background, key colours, explosion and fireworks effects,
 * and optional video recording. When [testMode] is enabled, an FPS counter is overlaid.
 *
 * @property licenseStatus The current application license status (used for gated features)
 * @property testMode When true, an FPS counter overlay is displayed
 *
 * @see MmxView
 * @see PianoCanvas
 * @see KeyboardView
 */
class PianoView(private val licenseStatus: LicenseStatus, val showDebugInfo: Boolean) : MmxView() {

    private val holder = BorderPane()
    private val config = PianoConfiguration()

    private val pianoCanvas: PianoCanvas = PianoCanvas(config)
    private val keyboardView: KeyboardView = KeyboardView(config)

    // Frame rate counter
    private var fpsCounter: Label? = null
    private var fpsTimer: AnimationTimer? = null
    private var frameCount = 0
    private var lastTime = 0L

    init {
        config.showDebugInfo.value = showDebugInfo

        val pianoView = VBox().apply {
            prefWidth = PIANO_WIDTH
            prefHeight = PIANO_HEIGHT
            minWidth = PIANO_WIDTH
            maxWidth = PIANO_WIDTH
            minHeight = PIANO_HEIGHT
            maxHeight = PIANO_HEIGHT
            children.addAll(pianoCanvas, keyboardView)

            // Keep fixed internal dimensions; host scaling is handled by zoom transform.
            keyboardView.minHeight = PIANO_KEYBOARD_HEIGHT.toDouble()
            keyboardView.prefHeight = PIANO_KEYBOARD_HEIGHT.toDouble()
            keyboardView.maxHeight = PIANO_KEYBOARD_HEIGHT.toDouble()
        }

        // Wrap the game node with FPS counter if in test mode
        val centerNode = if (config.showDebugInfo.value) {
            StackPane().apply {
                children.add(pianoView)

                // Create FPS counter label
                fpsCounter = Label("FPS: 0").apply {
                    font = Font.font("Monospaced", FontWeight.BOLD, 24.0)
                    textFill = Color.LIME
                    style = "-fx-background-color: rgba(0, 0, 0, 0.7); -fx-padding: 10px; -fx-background-radius: 5px;"
                    visibleProperty().bind(config.showDebugInfo) // Show/hide based on debug info setting
                }

                children.add(fpsCounter)
                StackPane.setAlignment(fpsCounter, Pos.TOP_RIGHT)
                StackPane.setMargin(fpsCounter, Insets(10.0))

                // Start FPS timer
                startFpsCounter()
            }
        } else {
            pianoView
        }

        holder.apply {
            padding = Insets(10.0)
            top = createSettingsToolbar()
            center = createZoomedCenterNode(centerNode)
        }

        setupSurface(
            rootNode = holder,
            naturalWidth = PIANO_WIDTH + 20,
            naturalHeight = PIANO_HEIGHT + 72,
            captureNode = pianoView,
            captureWidth = PIANO_WIDTH.toInt(),
            captureHeight = PIANO_HEIGHT.toInt()
        ) {
            fpsTimer?.stop()
            pianoCanvas.stop()
        }
    }

    private fun createZoomedCenterNode(content: Node): Region {
        val scaleTransform = Scale(1.0, 1.0, 0.0, 0.0)
        content.transforms.add(scaleTransform)

        return object : Region() {
            init {
                children.add(content)
            }

            override fun layoutChildren() {
                if (width <= 0.0 || height <= 0.0) {
                    return
                }

                // Zoom to the available width while preserving aspect ratio.
                val scale = (width / PIANO_WIDTH).coerceAtLeast(0.1)
                scaleTransform.x = scale
                scaleTransform.y = scale

                if (content is Region) {
                    content.resize(PIANO_WIDTH, PIANO_HEIGHT)
                }
                content.relocate(0.0, 0.0)
            }

            override fun computePrefWidth(height: Double): Double = PIANO_WIDTH

            override fun computePrefHeight(width: Double): Double = PIANO_HEIGHT

            override fun computeMinWidth(height: Double): Double = 100.0

            override fun computeMinHeight(width: Double): Double = 50.0
        }
    }

    private fun createSettingsToolbar(): HBox {
        return HBox(8.0).apply {
            alignment = Pos.CENTER_LEFT
            padding = Insets(0.0, 0.0, 8.0, 0.0)
            children.addAll(
                ColorPicker().apply {
                    valueProperty().bindBidirectional(config.backgroundColor)
                    tooltip = Tooltip("Background color")
                    minHeight = TOOLBAR_CONTROL_HEIGHT
                    prefHeight = TOOLBAR_CONTROL_HEIGHT
                    maxHeight = TOOLBAR_CONTROL_HEIGHT
                },
                createSettingsButton("Piano Keys") {
                    KeyColorsConfigurator(config)
                },
                createSettingsButton("Image", config.backgroundImageEnabled) {
                    ImageConfigurator(config, licenseStatus)
                },
                createSettingsButton("Explosion", config.explosionEnabled) {
                    ExplosionConfigurator(config)
                },
                createSettingsButton("Fireworks", config.fireworksEnabled) {
                    FireworksConfigurator(config)
                },
                createSettingsButton("Clouds", config.cloudEnabled) {
                    CloudConfigurator(config)
                }

            )
            if (showDebugInfo) {
                children.addAll(
                    Label("FPS"),
                    ToggleSwitch().apply {
                        selectedProperty().bindBidirectional(config.showDebugInfo)
                    }
                )
            }
        }
    }

    private fun createSettingsButton(title: String, contentSupplier: () -> BaseConfigurator): Button {
        return styledToolbarButton(Button(title).apply {
            setOnAction {
                openSettingsModal(title, contentSupplier())
            }
        })
    }

    private fun createSettingsButton(
        title: String,
        enabledProperty: BooleanProperty,
        contentSupplier: () -> BaseConfigurator
    ): Button {
        val toggle = ToggleSwitch().apply {
            selectedProperty().bindBidirectional(enabledProperty)
            addEventHandler(MouseEvent.MOUSE_PRESSED) { it.consume() }
            addEventHandler(MouseEvent.MOUSE_RELEASED) { it.consume() }
            addEventHandler(MouseEvent.MOUSE_CLICKED) { it.consume() }
            addEventHandler(ActionEvent.ACTION) { it.consume() }
        }

        return styledToolbarButton(Button(title).apply {
            graphic = toggle
            contentDisplay = ContentDisplay.RIGHT
            graphicTextGap = 10.0
            setOnAction {
                openSettingsModal(title, contentSupplier())
            }
        })
    }

    private fun styledToolbarButton(button: Button): Button {
        return button.apply {
            minHeight = TOOLBAR_CONTROL_HEIGHT
            prefHeight = TOOLBAR_CONTROL_HEIGHT
            maxHeight = TOOLBAR_CONTROL_HEIGHT
        }
    }

    private fun openSettingsModal(title: String, content: BaseConfigurator) {
        content.toDialog(title, holder.scene?.window).showAndWait()
    }

    /**
     * Starts the FPS counter animation timer.
     *
     * Creates and starts an [AnimationTimer] that updates the FPS label every second.
     * Only called when [testMode] is enabled.
     */
    private fun startFpsCounter() {
        fpsTimer = object : AnimationTimer() {
            override fun handle(now: Long) {
                if (lastTime == 0L) {
                    lastTime = now
                    return
                }

                frameCount++

                // Update FPS every second
                val elapsed = now - lastTime
                if (elapsed >= 1_000_000_000L) { // 1 second in nanoseconds
                    val fps = (frameCount * 1_000_000_000.0 / elapsed).toInt()
                    fpsCounter?.text = "FPS: $fps"
                    frameCount = 0
                    lastTime = now
                }
            }
        }
        fpsTimer?.start()
    }


    /**
     * Handles incoming MelodyMatrix events.
     *
     * Reacts to MIDI events by updating the keyboard view and piano scene.
     * PLAY and CHORD events are ignored in this stage.
     *
     * @param event The MelodyMatrix event to process
     */
    override fun onEvent(event: MmxEvent) {
        when (event.type) {
            MmxEventType.MIDI -> {
                val midiDataEvent = event as? MidiDataEvent ?: return
                Platform.runLater {
                    logger.debug(
                        "Received note {} {}",
                        midiDataEvent.note,
                        (if (midiDataEvent.event == MidiEvent.NOTE_ON) "ON" else "OFF")
                    )

                    keyboardView.playNote(midiDataEvent)
                    keyboardView.getEffectOrigin(midiDataEvent.note)?.let { keyOrigin ->
                        pianoCanvas.playNote(midiDataEvent, keyOrigin)
                    }
                }
            }

            MmxEventType.PLAY -> {
                // Not needed here
            }

            MmxEventType.CHORD -> {
                // Not needed here
            }
        }
    }

    companion object : MmxViewMetadata {
        override fun getViewTitle(): String = "Piano with visual effects"
        override fun getViewDescription(): String =
            "Renders an animated piano keyboard view with configurable visual effects."

        override fun getViewImagePath(): String = "/stage/piano.png"
        private val logger: Logger = LogManager.getLogger(PianoView::class.java.name)

        private const val TOOLBAR_CONTROL_HEIGHT = 40.0

        const val PIANO_BACKGROUND_HEIGHT = 600.0
        const val PIANO_KEYBOARD_HEIGHT = 120
        const val PIANO_WIDTH = 1280.0
        const val PIANO_HEIGHT = PIANO_BACKGROUND_HEIGHT + PIANO_KEYBOARD_HEIGHT
    }
}
