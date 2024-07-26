package be.codewriter.melodymatrix.view

/**
 * The AppLauncher class provides the main entry point for launching the application.
 */
object TestLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        val app = be.codewriter.melodymatrix.view.TestApp()
        app.run()
    }
}
