package be.codewriter.melodymatrix.view.stage.piano

import be.codewriter.melodymatrix.view.VisualizerStage
import be.codewriter.melodymatrix.view.data.LicenseStatus
import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.event.MidiDataEvent
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.MmxEventType
import be.codewriter.melodymatrix.view.stage.piano.configurator.*
import be.codewriter.melodymatrix.view.stage.piano.data.PianoConfiguration
import be.codewriter.melodymatrix.view.stage.piano.keyboard.KeyboardView
import be.codewriter.melodymatrix.view.stage.piano.scene.PianoScene
import be.codewriter.melodymatrix.view.video.VideoRecorder
import javafx.animation.AnimationTimer
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Accordion
import javafx.scene.control.Label
import javafx.scene.control.TitledPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
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
 * @property videoRecorder The video recorder used for optional screen capture
 * @property testMode When true, an FPS counter overlay is displayed
 *
 * @see VisualizerStage
 * @see PianoScene
 * @see KeyboardView
 */
class PianoStage(
    private val licenseStatus: LicenseStatus,
    private val videoRecorder: VideoRecorder,
    private val testMode: Boolean = false
) :
    VisualizerStage() {

    private val holder = BorderPane()
    private val config = PianoConfiguration()

    private val pianoScene: PianoScene
    private val keyboardView: KeyboardView

    // Frame rate counter
    private var fpsCounter: Label? = null
    private var fpsTimer: AnimationTimer? = null
    private var frameCount = 0
    private var lastTime = 0L

    init {
        pianoScene = PianoScene(config)
        keyboardView = KeyboardView(config)

        var pianoView = VBox().apply {
            prefWidth = PIANO_WIDTH
            prefHeight = PIANO_HEIGHT
            children.addAll(pianoScene, keyboardView)
        }

        // Wrap the game node with FPS counter if in test mode
        val centerNode = if (testMode) {
            StackPane().apply {
                children.add(pianoView)

                // Create FPS counter label
                fpsCounter = Label("FPS: 0").apply {
                    font = Font.font("Monospaced", FontWeight.BOLD, 24.0)
                    textFill = Color.LIME
                    style = "-fx-background-color: rgba(0, 0, 0, 0.7); -fx-padding: 10px; -fx-background-radius: 5px;"
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
            center = centerNode
            left = getPianoSettingsAccordion()
        }

        title = "See your music being played on a piano..."
        scene = Scene(holder, PIANO_WIDTH + 350 + 20 + 10, PIANO_HEIGHT + 20)
        isResizable = false

        setOnCloseRequest {
            fpsTimer?.stop()
            pianoScene.stop()
        }
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
     * Creates the settings accordion panel displayed on the left side of the stage.
     *
     * Each pane in the accordion covers a different configuration category:
     * background, key colours, explosion effect, fireworks effect, key effect, and export.
     *
     * @return An [Accordion] containing all piano setting panels
     */
    private fun getPianoSettingsAccordion(): Accordion {
        return Accordion().apply {
            padding = Insets(0.0)
            prefWidth = 350.0
            minWidth = 350.0
            panes.addAll(
                TitledPane().apply {
                    text = "Background settings"
                    content = BackgroundScene(config, licenseStatus)
                },
                TitledPane().apply {
                    text = "Piano key settings"
                    content = KeyColors(config)
                },
                TitledPane().apply {
                    text = "Explosion effect"
                    content = EffectExplosion(config)
                },
                TitledPane().apply {
                    text = "Fireworks effect"
                    content = EffectFireworks(config)
                },
                TitledPane().apply {
                    text = "Effect above the keys"
                    content = KeyEffect(config)
                },
                TitledPane().apply {
                    text = "Record"
                    content = PianoExport(
                        licenseStatus,
                        videoRecorder,
                        holder.center
                    )
                }
            )
            expandedPane = panes[0]
        }
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
                        pianoScene.playNote(midiDataEvent, keyOrigin)
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

    companion object {
        private val logger: Logger = LogManager.getLogger(PianoStage::class.java.name)

        const val PIANO_BACKGROUND_HEIGHT = 600.0
        const val PIANO_KEYBOARD_HEIGHT = 120
        const val PIANO_WIDTH = 1280.0
        const val PIANO_HEIGHT = PIANO_BACKGROUND_HEIGHT + PIANO_KEYBOARD_HEIGHT
    }
}
