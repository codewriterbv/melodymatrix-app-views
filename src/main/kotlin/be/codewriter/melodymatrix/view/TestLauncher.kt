package be.codewriter.melodymatrix.view

/**
 * Main entry point for launching the MelodyMatrix test application.
 *
 * This launcher creates and starts a [TestApp] instance, which opens the standalone
 * test viewer window. Use this class to run the viewer during development and testing.
 *
 * @see TestApp
 */
object TestLauncher {
    /**
     * Application main method. Creates a [TestApp] and starts it.
     *
     * @param args Command-line arguments (not used)
     */
    @JvmStatic
    fun main(args: Array<String>) {
        val app = TestApp()
        app.run()
    }
}
