package be.codewriter.melodymatrix.view.stage.piano

import be.codewriter.melodymatrix.view.VisualizerStage
import be.codewriter.melodymatrix.view.data.LicenseStatus
import be.codewriter.melodymatrix.view.data.MidiData
import be.codewriter.melodymatrix.view.data.PlayEvent
import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.stage.piano.component.*
import be.codewriter.melodymatrix.view.stage.piano.component.PianoGenerator.Companion.PIANO_HEIGHT
import be.codewriter.melodymatrix.view.stage.piano.component.PianoGenerator.Companion.PIANO_WIDTH
import be.codewriter.melodymatrix.view.video.VideoRecorder
import com.almasb.fxgl.app.GameApplication
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Accordion
import javafx.scene.control.TitledPane
import javafx.scene.layout.BorderPane
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class PianoStage(private val licenseStatus: LicenseStatus, private val videoRecorder: VideoRecorder) :
    VisualizerStage() {
    private val holder = BorderPane()
    private val configuratorBackground = ConfiguratorBackground(licenseStatus)
    private val configuratorEffect = ConfiguratorEffect()
    private val configuratorKey = ConfiguratorKey()
    private val pianoGenerator: PianoGenerator =
        PianoGenerator(configuratorBackground, configuratorEffect, configuratorKey)

    init {
        holder.apply {
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
                    content = configuratorBackground
                },
                TitledPane().apply {
                    text = "Piano key settings"
                    content = configuratorKey
                },
                TitledPane().apply {
                    text = "Particle effect"
                    content = configuratorEffect
                },
                TitledPane().apply {
                    text = "Record"
                    content = ConfiguratorRecording(licenseStatus, videoRecorder, holder.center)
                }
            )
            expandedPane = panes[0]
        }
    }

    override fun onMidiData(midiData: MidiData) {
        Platform.runLater {
            logger.debug(
                "Received note {} {}",
                midiData.note,
                (if (midiData.event == MidiEvent.NOTE_ON) "ON" else "OFF")
            )

            pianoGenerator.playNote(midiData)
        }
    }

    override fun onPlayEvent(playEvent: PlayEvent) {
        // Not needed here
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(PianoStage::class.java.name)
    }
}
