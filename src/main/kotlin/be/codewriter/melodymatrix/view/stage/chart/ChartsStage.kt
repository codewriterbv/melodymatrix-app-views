package be.codewriter.melodymatrix.view.stage.chart

import be.codewriter.melodymatrix.view.VisualizerStage
import be.codewriter.melodymatrix.view.data.MidiData
import be.codewriter.melodymatrix.view.stage.chart.component.*
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.FlowPane

class ChartsStage : VisualizerStage() {

    val charts: FlowPane

    init {
        charts = FlowPane()
        charts.children.add(Button("Reset").apply {
            setOnAction {
                for (visualizer in charts.children) {
                    if (visualizer is ChartVisualizer) {
                        visualizer.reset()
                    }
                }
            }
        })
        charts.children.add(PieChartPlayedNotes())
        charts.children.add(BarChartPlayedNotes())
        charts.children.add(ConcentricPlayedNotes())
        charts.children.add(CoxCombPlayedNotes())
        charts.children.add(RidgeLineChartPlayedNotes())
        charts.children.add(RadarChartPlayedNotes())

        title = "See your music in charts..."
        scene = Scene(charts, 1400.0, 820.0)

        setOnCloseRequest {
            // Nothing needed here, but must be defined or will cause a problem when closing the window
        }
    }

    override fun onMidiDataReceived(midiData: MidiData) {
        val note = midiData.note()
        for (visualizer in charts.children) {
            if (visualizer is ChartVisualizer) {
                visualizer.onNote(note)
            }
        }
    }
}