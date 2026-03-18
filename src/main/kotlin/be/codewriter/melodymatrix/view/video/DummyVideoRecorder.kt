package be.codewriter.melodymatrix.view.video

import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.Node
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * A no-op implementation of [VideoRecorder] used for testing and development.
 *
 * Does not capture any actual video; instead it logs when recording starts and stops,
 * and updates the [isRecording] property so UI controls bound to it behave correctly.
 *
 * @see VideoRecorder
 */
class DummyVideoRecorder : VideoRecorder {
    override val isRecording: BooleanProperty = SimpleBooleanProperty(false)

    /**
     * Simulates starting a recording by logging a message and setting [isRecording] to true.
     *
     * @param node   Ignored in this implementation
     * @param width  Ignored in this implementation
     * @param height Ignored in this implementation
     */
    override fun startRecording(node: Node, width: Int, height: Int) {
        logger.info("Dummy recording started...")
        isRecording.set(true)
    }

    /**
     * Simulates stopping a recording by logging a message and setting [isRecording] to false.
     */
    override fun stopRecording() {
        logger.info("Dummy recording stopped...")
        isRecording.set(false)
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(DummyVideoRecorder::class.java.name)
    }
}