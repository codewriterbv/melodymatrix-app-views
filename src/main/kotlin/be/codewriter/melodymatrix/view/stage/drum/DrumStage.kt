package be.codewriter.melodymatrix.view.stage.drum

import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class DrumStage : be.codewriter.melodymatrix.view.VisualizerStage() {

    val notes: MutableMap<be.codewriter.melodymatrix.view.definition.Note, Label> = mutableMapOf()

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

    override fun onMidiData(midiData: be.codewriter.melodymatrix.view.data.MidiData) {
        Platform.runLater {
            // TODO
        }
    }

    override fun onPlayEvent(playEvent: be.codewriter.melodymatrix.view.data.PlayEvent) {
        // Not needed here
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(DrumStage::class.java.name)
    }
}