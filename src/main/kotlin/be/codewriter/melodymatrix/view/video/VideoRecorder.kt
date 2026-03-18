package be.codewriter.melodymatrix.view.video

import javafx.beans.property.BooleanProperty
import javafx.scene.Node

/**
 * Interface for recording a JavaFX node as a video.
 *
 * Implementations capture a specific JavaFX [Node] (e.g. the piano scene) to a video file.
 * The recording state is exposed as an observable [isRecording] property for UI binding.
 *
 * @see DummyVideoRecorder
 */
interface VideoRecorder {

    /** Observable property indicating whether recording is currently active. */
    val isRecording: BooleanProperty

    /**
     * Starts capturing the given node to a video file.
     *
     * @param node   The JavaFX node to record
     * @param width  The desired output video width in pixels
     * @param height The desired output video height in pixels
     */
    fun startRecording(node: Node, width: Int, height: Int)

    /**
     * Stops the current recording and finalises the output file.
     */
    fun stopRecording()
}