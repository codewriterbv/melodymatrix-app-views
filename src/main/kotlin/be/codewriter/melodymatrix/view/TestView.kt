package be.codewriter.melodymatrix.view

import atlantafx.base.theme.PrimerLight

import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.system.exitProcess


class TestView : Application() {

    fun run() {
        launch()
    }

    override fun start(stage: Stage) {
        setUserAgentStylesheet(PrimerLight().userAgentStylesheet)

        with(stage) {
            scene = Scene(BorderPane().apply {
                top = LogoBar()
                left = MidiCard(midiHandler)
                center = RecordingsCard(midiHandler)
                right = ViewersCard(stage, midiHandler)
            }, 1000.0, 800.0)
            title = "MIDI Viewer"

            setOnCloseRequest {
                logger.warn("Closing application...")
                Platform.exit()
                exitProcess(0)
            }
            show()
        }
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(TestView::class.java.name)
    }
}