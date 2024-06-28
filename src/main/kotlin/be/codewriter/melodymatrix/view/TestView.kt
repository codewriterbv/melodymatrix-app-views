package be.codewriter.melodymatrix.view

import atlantafx.base.theme.PrimerLight
import be.codewriter.melodymatrix.view.data.LicenseStatus
import be.codewriter.melodymatrix.view.test.MidiSimulator
import be.codewriter.melodymatrix.view.test.TestViewLicense
import be.codewriter.melodymatrix.view.test.TestViewMidiEvents
import be.codewriter.melodymatrix.view.test.TestViewStages
import javafx.application.Application
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.layout.HBox
import javafx.stage.Stage
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.system.exitProcess


class TestView : Application() {

    private val midiSimulator = MidiSimulator()

    fun run() {
        launch()
    }

    override fun start(stage: Stage) {
        setUserAgentStylesheet(PrimerLight().userAgentStylesheet)
        val licenseStatus = LicenseStatus()
        val mainView = HBox(
            TestViewMidiEvents(midiSimulator),
            TestViewStages(stage, midiSimulator, licenseStatus),
            TestViewLicense(licenseStatus)
        ).apply {
            spacing = 25.0
            padding = Insets(25.0)
        }

        with(stage) {
            scene = Scene(mainView, 800.0, 600.0)
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
        private val logger: Logger = LogManager.getLogger(TestView::class.java.name)
    }
}