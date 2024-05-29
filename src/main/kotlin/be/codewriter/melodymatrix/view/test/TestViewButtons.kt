package be.codewriter.melodymatrix.view.test

import be.codewriter.melodymatrix.view.VisualizerStage
import be.codewriter.melodymatrix.view.stage.chart.ChartsStage
import be.codewriter.melodymatrix.view.stage.piano.PianoStage
import be.codewriter.melodymatrix.view.stage.scale.ScaleStage
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import javafx.stage.Stage

class TestViewButtons(val parentStage: Stage, val midiSimulator: MidiSimulator) : VBox() {

    init {
        spacing = 10.0

        children.setAll(
            createVisualizerButton("Piano") { PianoStage() },
            createVisualizerButton("Charts") { ChartsStage() },
            createVisualizerButton("Scale") { ScaleStage() }
        )
    }

    private fun createVisualizerButton(label: String, stageSupplier: () -> VisualizerStage): Node {
        val view = Button(label).apply {
            minWidth = 200.0
            setOnMouseClicked { _ ->
                val stage = stageSupplier()

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
