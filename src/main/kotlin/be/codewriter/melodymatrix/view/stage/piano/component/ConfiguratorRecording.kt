package be.codewriter.melodymatrix.view.stage.piano.component

import be.codewriter.melodymatrix.view.data.LicenseStatus
import be.codewriter.melodymatrix.view.stage.piano.component.PianoGenerator.Companion.PIANO_HEIGHT
import be.codewriter.melodymatrix.view.stage.piano.component.PianoGenerator.Companion.PIANO_WIDTH
import be.codewriter.melodymatrix.view.video.VideoRecorder
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox


class ConfiguratorRecording(licenseStatus: LicenseStatus, videoRecorder: VideoRecorder, node: Node) : VBox() {

    init {
        spacing = 5.0
        children.addAll(
            HBox().apply {
                spacing = 5.0
                children.addAll(
                    Button("Record to video").apply {
                        setOnMouseClicked { _ ->
                            videoRecorder.startRecording(node, PIANO_WIDTH, PIANO_HEIGHT)
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
    }
}