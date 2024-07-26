package be.codewriter.melodymatrix.view.test

import be.codewriter.melodymatrix.view.video.DummyVideoRecorder
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.stage.Stage

class TestViewStages(
    val parentStage: Stage,
    val midiSimulator: be.codewriter.melodymatrix.view.test.MidiSimulator,
    licenseStatus: be.codewriter.melodymatrix.view.data.LicenseStatus
) : VBox() {

    init {
        spacing = 10.0

        children.setAll(
            Label("Open one or more views"),
            createButton("Midi") { be.codewriter.melodymatrix.view.stage.midi.MidiStage() },
            createButton("Piano") {
                be.codewriter.melodymatrix.view.stage.piano.PianoStage(
                    licenseStatus,
                    DummyVideoRecorder()
                )
            },
            createButton("Charts") { be.codewriter.melodymatrix.view.stage.chart.ChartsStage() },
            createButton("Scale") { be.codewriter.melodymatrix.view.stage.scale.ScaleStage() },
            createButton("Drum") { be.codewriter.melodymatrix.view.stage.drum.DrumStage() },
            createButton("LED Strip") { be.codewriter.melodymatrix.view.stage.ledstrip.LedStripStage() }
        )
    }

    private fun createButton(
        label: String,
        stageSupplier: () -> be.codewriter.melodymatrix.view.VisualizerStage
    ): Node {
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
