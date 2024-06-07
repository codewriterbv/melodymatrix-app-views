package be.codewriter.melodymatrix.view.test

import be.codewriter.melodymatrix.view.VisualizerStage
import be.codewriter.melodymatrix.view.stage.chart.ChartsStage
import be.codewriter.melodymatrix.view.stage.midi.MidiStage
import be.codewriter.melodymatrix.view.stage.piano.PianoStage
import be.codewriter.melodymatrix.view.stage.scale.ScaleStage
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.stage.Stage

class TestViewButtons(val parentStage: Stage, val midiSimulator: MidiSimulator) : VBox() {

    init {
        spacing = 10.0

        children.setAll(
            Label("Open one or more views"),
            createButton("Midi") { MidiStage() },
            createButton("Piano") { PianoStage() },
            createButton("Charts") { ChartsStage() },
            createButton("Scale") { ScaleStage() }
        )
    }

    private fun createButton(label: String, stageSupplier: () -> VisualizerStage): Node {
        val view = Button(label).apply {
            minWidth = 200.0
            setOnMouseClicked { _ ->
                val stage = stageSupplier()
                midiSimulator.registerListener(stage)

                val onClose = stage.onCloseRequest
                stage.setOnCloseRequest {
                    midiSimulator.removeListener(stage)
                    onClose.handle(it)
                }

                stage.initOwner(parentStage)
                stage.show()
            }
        }

        return view
    }
}
