package be.codewriter.melodymatrix.view.video

import javafx.scene.Node

interface VideoRecorder {

    fun startRecording(node: Node, width: Int, height: Int)

    fun stopRecording()
}