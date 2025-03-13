package stage.drum

import be.codewriter.melodymatrix.view.VisualizerStage
import be.codewriter.melodymatrix.view.data.MidiData
import be.codewriter.melodymatrix.view.data.PlayEvent
import be.codewriter.melodymatrix.view.definition.Note
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox


class DrumStage : VisualizerStage() {

    val notes: MutableMap<Note, Label> = mutableMapOf()

    init {
        var imageView = ImageView().apply {
            image = Image("/drum/drum-notes.png")
        }

        title = "Hi Drummer!"
        scene = Scene(
            VBox(
                imageView,
                Label("Image from https://www.drumeo.com/beat/how-to-read-drum-music/")
            ).apply {
                spacing = 0.0
                padding = Insets(0.0, 0.0, 0.0, 20.0)
                alignment = Pos.CENTER
            }, 800.0, 550.0
        )

        setOnCloseRequest {
            // Nothing needed here, but must be defined or will cause a problem when closing the window
        }
    }

    override fun onMidiData(midiData: MidiData) {
        Platform.runLater {
            // TODO
        }
    }

    override fun onPlayEvent(playEvent: PlayEvent) {
        // Not needed here
    }
}