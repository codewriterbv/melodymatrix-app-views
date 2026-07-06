package be.codewriter.melodymatrix.view

import atlantafx.base.theme.NordDark
import be.codewriter.melodymatrix.view.i18n.I18n
import be.codewriter.melodymatrix.view.test.TestView
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.stage.Stage
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * JavaFX application entry point for the MelodyMatrix test viewer.
 *
 * Launches a standalone test window containing the [TestView] layout.
 * This application is intended for development and testing purposes,
 * allowing all viewer components to be exercised without the full application stack.
 *
 * The language defaults to the system locale on each launch (no
 * persistence — `TestApp` intentionally does not depend on the Engine's
 * `SettingService`). Use the language combo in [TestView] to switch
 * language at runtime and confirm every view relabels live.
 *
 * @see TestView
 */
class TestApp : Application() {
    /**
     * Convenience method to launch the JavaFX application.
     */
    fun run() {
        launch()
    }

    /**
     * Initialises and shows the primary stage of the test application.
     *
     * Applies the Nord Dark theme, creates a [TestView] scene, sets the window title,
     * and registers a close handler that shuts down the view and the JavaFX platform.
     *
     * @param stage The primary stage provided by the JavaFX runtime
     */
    override fun start(stage: Stage) {
        setUserAgentStylesheet(NordDark().userAgentStylesheet)

        // Bootstrap i18n from the OS language on every launch.
        I18n.currentLocale.set(I18n.detectSystemLocale())

        val testView = TestView()
        val commonBundle = I18n.registerBundle("i18n/common")

        with(stage) {
            scene = Scene(testView, 1600.0, 1000.0)
            titleProperty().bind(I18n.binding(commonBundle, "common.testapp.title"))

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
