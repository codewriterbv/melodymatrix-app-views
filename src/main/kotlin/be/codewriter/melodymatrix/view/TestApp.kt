package be.codewriter.melodymatrix.view

import atlantafx.base.theme.PrimerLight
import be.codewriter.melodymatrix.view.test.TestView
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.stage.Stage
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.system.exitProcess

class TestApp : Application() {
    fun run() {
        launch()
    }

    override fun start(stage: Stage) {
        setUserAgentStylesheet(PrimerLight().userAgentStylesheet)
        with(stage) {
            scene = Scene(TestView(stage), 800.0, 600.0)
            title = "Test application for the MelodyMatrix Viewers"

            setOnCloseRequest {
                logger.warn("Closing application...")
                Platform.exit()
                exitProcess(0)
            }
            show()
        }
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(TestApp::class.java.name)
    }
}