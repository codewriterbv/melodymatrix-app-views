package be.codewriter.melodymatrix.view.video

import javafx.scene.Node
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class DummyVideoRecorder : VideoRecorder {
    override fun startRecording(node: Node, width: Int, height: Int) {
        logger.info("Recording started...")
    }

    override fun stopRecording() {
        logger.info("Recording stopped...")
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(DummyVideoRecorder::class.java.name)
    }
}