package be.codewriter.melodymatrix.view.test

import be.codewriter.melodymatrix.view.VisualizerStage
import be.codewriter.melodymatrix.view.data.LicenseStatus
import be.codewriter.melodymatrix.view.stage.chart.ChartsStage
import be.codewriter.melodymatrix.view.stage.midi.MidiStage
import be.codewriter.melodymatrix.view.stage.piano.PianoStage
import be.codewriter.melodymatrix.view.video.DummyVideoRecorder
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.stage.Stage
import stage.drum.DrumStage
import stage.ledstrip.LedStripStage
import stage.scale.ScaleStage

class TestViewStages(
    val parentStage: Stage,
    val midiSimulator: MidiSimulator,
    licenseStatus: LicenseStatus
) : VBox() {

    init {
        spacing = 10.0

        children.setAll(
            Label("Open one or more views"),
            createButton("Midi") { MidiStage() },
            createButton("Piano") {
                PianoStage(licenseStatus, DummyVideoRecorder())
            },
            createButton("Charts") { ChartsStage() },
            createButton("Scale") { ScaleStage() },
            createButton("Drum") { DrumStage() },
            createButton("LED Strip") { LedStripStage() }
        )
    }

    private fun createButton(
        label: String,
        stageSupplier: () -> VisualizerStage
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
