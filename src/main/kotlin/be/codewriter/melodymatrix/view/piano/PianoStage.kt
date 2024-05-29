package be.codewriter.melodymatrix.app.fxview.viewer

import be.codewriter.melodymatrix.app.data.DataLine
import be.codewriter.melodymatrix.app.fxview.viewer.piano.PianoConfiguratorBackground
import be.codewriter.melodymatrix.app.fxview.viewer.piano.PianoConfiguratorEffect
import be.codewriter.melodymatrix.app.fxview.viewer.piano.PianoConfiguratorKey
import be.codewriter.melodymatrix.app.fxview.viewer.piano.PianoGenerator
import be.codewriter.melodymatrix.app.fxview.viewer.piano.PianoGenerator.Companion.PIANO_HEIGHT
import be.codewriter.melodymatrix.app.fxview.viewer.piano.PianoGenerator.Companion.PIANO_WIDTH
import be.codewriter.melodymatrix.view.VisualizerStage
import com.almasb.fxgl.app.GameApplication
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Accordion
import javafx.scene.control.TitledPane
import javafx.scene.layout.BorderPane
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class PianoStage : VisualizerStage() {
    private val pianoConfiguratorBackground = PianoConfiguratorBackground()
    private val pianoConfiguratorKey = PianoConfiguratorKey()
    private val pianoConfiguratorEffect = PianoConfiguratorEffect()
    private val pianoGenerator: PianoGenerator = PianoGenerator(pianoConfiguratorBackground, pianoConfiguratorKey)

    init {
        val holder = BorderPane().apply {
            padding = Insets(10.0)
            center = GameApplication.embeddedLaunch(pianoGenerator)
            left = getPianoSettingsAccordion()
        }

        title = "See your music being played on a piano..."
        scene = Scene(holder, PIANO_WIDTH.toDouble() + 350 + 20 + 10, PIANO_HEIGHT.toDouble() + 20)
        isResizable = false

        setOnCloseRequest {
            GameApplication.embeddedShutdown()
        }
    }

    private fun getPianoSettingsAccordion(): Accordion {
        return Accordion().apply {
            padding = Insets(0.0)
            prefWidth = 350.0
            minWidth = 350.0
            panes.addAll(
                TitledPane().apply {
                    text = "Background settings"
                    content = pianoConfiguratorBackground
                },
                TitledPane().apply {
                    text = "Piano key settings"
                    content = pianoConfiguratorKey
                },
                TitledPane().apply {
                    text = "Particle effect"
                    content = pianoConfiguratorEffect
                }
            )
            expandedPane = panes[0]
        }
    }

    override fun onMidiDataReceived(dataLine: DataLine) {
        Platform.runLater {
            logger.debug(
                "Received note {} {}",
                dataLine.getData().note(),
                (if (dataLine.getData().isNoteOn()) "ON" else "OFF")
            )

            pianoGenerator.playNote(
                dataLine.getData().note(),
                dataLine.getData().isNoteOn(),
                pianoConfiguratorEffect.getPianoEffectSettings()
            )
        }
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(PianoStage::class.java.name)
    }
}
