package be.codewriter.melodymatrix.view

import atlantafx.base.theme.PrimerLight
import be.codewriter.melodymatrix.view.test.TestView
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.stage.Stage
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class TestApp : Application() {
    fun run() {
        launch()
    }

    override fun start(stage: Stage) {
        setUserAgentStylesheet(PrimerLight().userAgentStylesheet)

        val testView = TestView()

        with(stage) {
            scene = Scene(testView, 1600.0, 1000.0)
            title = "Test application for the MelodyMatrix Viewers"

            setOnCloseRequest {
                logger.warn("Closing application...")
                testView.shutdown()
                Platform.exit()
            }
            show()
        }
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(TestApp::class.java.name)
    }
}