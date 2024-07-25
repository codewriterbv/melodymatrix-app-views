package be.codewriter.melodymatrix.view.video

import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.Node
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class DummyVideoRecorder : VideoRecorder {
    override val isRecording: BooleanProperty = SimpleBooleanProperty(false)

    override fun startRecording(node: Node, width: Int, height: Int) {
        logger.info("Dummy recording started...")
        isRecording.set(true)
    }

    override fun stopRecording() {
        logger.info("Dummy recording stopped...")
        isRecording.set(false)
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(DummyVideoRecorder::class.java.name)
    }
}