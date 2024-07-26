package be.codewriter.melodymatrix.view.stage.scale

import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.InputStream

class ScaleStage : be.codewriter.melodymatrix.view.VisualizerStage() {

    val notes: MutableMap<be.codewriter.melodymatrix.view.definition.Note, Label> = mutableMapOf()

    init {
        val inputStream: InputStream = javaClass.getResourceAsStream("/fonts/musiqwik/Musiqwik-rvL8.ttf")
        val font = Font.loadFont(inputStream, 40.0)

        // "=&==========================rstuvwxyz{|}~=="
        val row1 = HBox().apply {
            spacing = 0.0
            padding = Insets(0.0)
            children.addAll(
                getLabel(font, "=&==========================", null),
                getLabel(font, "r", be.codewriter.melodymatrix.view.definition.Note.C4),
                getLabel(font, "s", be.codewriter.melodymatrix.view.definition.Note.D4),
                getLabel(font, "t", be.codewriter.melodymatrix.view.definition.Note.E4),
                getLabel(font, "u", be.codewriter.melodymatrix.view.definition.Note.F4),
                getLabel(font, "v", be.codewriter.melodymatrix.view.definition.Note.G4),
                getLabel(font, "w", be.codewriter.melodymatrix.view.definition.Note.A4),
                getLabel(font, "x", be.codewriter.melodymatrix.view.definition.Note.B4),
                getLabel(font, "y", be.codewriter.melodymatrix.view.definition.Note.C5),
                getLabel(font, "z", be.codewriter.melodymatrix.view.definition.Note.D5),
                getLabel(font, "{", be.codewriter.melodymatrix.view.definition.Note.E5),
                getLabel(font, "|", be.codewriter.melodymatrix.view.definition.Note.F5),
                getLabel(font, "}", be.codewriter.melodymatrix.view.definition.Note.G5),
                getLabel(font, "~", be.codewriter.melodymatrix.view.definition.Note.A5),
                getLabel(font, "==", null)
            )
        }

        // "=¯==rstuvwxyz{|}============================"
        val row2 = HBox().apply {
            spacing = 0.0
            padding = Insets(0.0)
            children.addAll(
                getLabel(font, "=¯==", null),
                getLabel(font, "r", be.codewriter.melodymatrix.view.definition.Note.E2),
                getLabel(font, "s", be.codewriter.melodymatrix.view.definition.Note.F2),
                getLabel(font, "t", be.codewriter.melodymatrix.view.definition.Note.G2),
                getLabel(font, "u", be.codewriter.melodymatrix.view.definition.Note.A2),
                getLabel(font, "v", be.codewriter.melodymatrix.view.definition.Note.B2),
                getLabel(font, "w", be.codewriter.melodymatrix.view.definition.Note.C3),
                getLabel(font, "x", be.codewriter.melodymatrix.view.definition.Note.D3),
                getLabel(font, "y", be.codewriter.melodymatrix.view.definition.Note.E3),
                getLabel(font, "z", be.codewriter.melodymatrix.view.definition.Note.F3),
                getLabel(font, "{", be.codewriter.melodymatrix.view.definition.Note.G3),
                getLabel(font, "|", be.codewriter.melodymatrix.view.definition.Note.A3),
                getLabel(font, "}", be.codewriter.melodymatrix.view.definition.Note.B3),
                getLabel(font, "============================", null)
            )
        }

        title = "See your music on a scale..."
        scene = Scene(VBox(row1, row2).apply {
            spacing = 0.0
            padding = Insets(0.0, 0.0, 0.0, 20.0)
        }, 610.0, 200.0)

        setOnCloseRequest {
            // Nothing needed here, but must be defined or will cause a problem when closing the window
        }
    }

    private fun getLabel(
        musicFont: Font?,
        content: String,
        note: be.codewriter.melodymatrix.view.definition.Note?
    ): Label {
        var label = Label().apply {
            text = content
            font = musicFont
        }
        if (note != null) {
            notes[note] = label
        }
        return label
    }

    override fun onMidiData(midiData: be.codewriter.melodymatrix.view.data.MidiData) {
        Platform.runLater {
            val note = midiData.note
            val label = if (note.mainNote.isSharp) notes[note.parentNote!!] else notes[note]
            if (label != null) {
                if (midiData.event == be.codewriter.melodymatrix.view.definition.MidiEvent.NOTE_ON) {
                    if (note.mainNote.isSharp) {
                        label.style = "-fx-text-fill: blue; -fx-background-color: green;"
                    } else {
                        label.style = "-fx-text-fill: red; -fx-background-color: yellow;"
                    }
                } else {
                    label.style = ""
                }
            }
        }
    }

    override fun onPlayEvent(playEvent: be.codewriter.melodymatrix.view.data.PlayEvent) {
        // Not needed here
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(ScaleStage::class.java.name)
    }
}