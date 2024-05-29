package be.codewriter.melodymatrix.view

import atlantafx.base.theme.PrimerLight
import be.codewriter.melodymatrix.view.test.MidiSimulator
import be.codewriter.melodymatrix.view.test.TestViewButtons
import be.codewriter.melodymatrix.view.test.TestViewMusicSelection
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.layout.HBox
import javafx.stage.Stage
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.system.exitProcess


class TestView : Application() {

    val midiSimulator = MidiSimulator()

    fun run() {
        launch()
    }

    override fun start(stage: Stage) {
        setUserAgentStylesheet(PrimerLight().userAgentStylesheet)

        var mainView = HBox(
            TestViewMusicSelection(midiSimulator),
            TestViewButtons(stage, midiSimulator)
        ).apply {

        }

        with(stage) {
            scene = Scene(mainView, 600.0, 400.0)
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