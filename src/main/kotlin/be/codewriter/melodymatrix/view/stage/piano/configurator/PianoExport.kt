package be.codewriter.melodymatrix.view.stage.piano.configurator

import be.codewriter.melodymatrix.view.data.LicenseStatus
import be.codewriter.melodymatrix.view.stage.piano.PianoView.Companion.PIANO_HEIGHT
import be.codewriter.melodymatrix.view.stage.piano.PianoView.Companion.PIANO_WIDTH
import be.codewriter.melodymatrix.view.video.VideoRecorder
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox


/**
 * Settings panel for starting and stopping video recording of the piano scene.
 *
 * Provides "Record to video" and "Stop recording" buttons wired to [VideoRecorder].
 * Both buttons are disabled when the license is invalid; the record button is also
 * disabled while recording is already in progress, and the stop button while idle.
 *
 * @param licenseStatus The current license status; recording is only enabled for valid licenses
 * @param videoRecorder The video recorder implementation to invoke
 * @param node          The JavaFX node to capture (typically the piano scene canvas)
 * @see PianoStage
 * @see VideoRecorder
 */
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