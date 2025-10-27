package be.codewriter.melodymatrix.view.stage.piano.configurator

import be.codewriter.melodymatrix.view.data.LicenseStatus
import be.codewriter.melodymatrix.view.stage.piano.PianoStage.Companion.PIANO_HEIGHT
import be.codewriter.melodymatrix.view.stage.piano.PianoStage.Companion.PIANO_WIDTH
import be.codewriter.melodymatrix.view.video.VideoRecorder
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox


class PianoExport(licenseStatus: LicenseStatus, videoRecorder: VideoRecorder, node: Node) : VBox() {

    init {
        children.addAll(
            HBox().apply {
                spacing = 5.0
                children.addAll(
                    Button("Record to video").apply {
                        setOnMouseClicked { _ ->
                            videoRecorder.startRecording(node, PIANO_WIDTH.toInt(), PIANO_HEIGHT.toInt())
                        }
                        disableProperty().bind(licenseStatus.isValid.not().or(videoRecorder.isRecording))
                    },
                    Button("Stop recording").apply {
                        setOnMouseClicked { _ ->
                            videoRecorder.stopRecording()
                        }
                        disableProperty().bind(licenseStatus.isValid.not().or(videoRecorder.isRecording.not()))
                    })
            }
        )

        spacing = 5.0
    }
}