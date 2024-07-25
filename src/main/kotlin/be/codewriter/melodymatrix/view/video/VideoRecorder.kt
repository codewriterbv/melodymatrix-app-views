package be.codewriter.melodymatrix.view.video

import javafx.beans.property.BooleanProperty
import javafx.scene.Node

interface VideoRecorder {

    val isRecording: BooleanProperty

    fun startRecording(node: Node, width: Int, height: Int)

    fun stopRecording()
}