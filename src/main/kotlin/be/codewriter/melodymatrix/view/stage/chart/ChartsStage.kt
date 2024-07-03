package be.codewriter.melodymatrix.view.stage.chart

import be.codewriter.melodymatrix.view.VisualizerStage
import be.codewriter.melodymatrix.view.data.MidiData
import be.codewriter.melodymatrix.view.data.PlayEvent
import be.codewriter.melodymatrix.view.stage.chart.component.*
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox


class ChartsStage : VisualizerStage() {

    var charts: MutableList<ChartVisualizer> = mutableListOf()
    var stackPane = StackPane()

    init {
        val buttons = VBox().apply {
            spacing = 10.0
            prefWidth = 200.0
            minWidth = 200.0
            maxWidth = 200.0
            children.addAll(
                Button("Reset").apply {
                    prefWidth = 200.0
                    minWidth = 200.0
                    maxWidth = 200.0
                    setOnAction {
                        charts.forEach { c -> c.reset() }
                    }
                },
                createButton("Pie chart", PieChartPlayedNotes()),
                createButton("Bar chart", BarChartPlayedNotes()),
                createButton("Concentric chart", ConcentricPlayedNotes()),
                createButton("Cox Comb chart", CoxCombPlayedNotes()),
                createButton("Ridge Line chart", RidgeLineChartPlayedNotes()),
                createButton("Radar Chart chart", RadarChartPlayedNotes())
            )
        }

        title = "See your music in charts..."
        scene = Scene(BorderPane().apply {
            left = buttons
            right = stackPane
        }, 800.0, 800.0)

        setOnCloseRequest {
            // Nothing needed here, but must be defined or will cause a problem when closing the window
        }
    }


    fun createButton(label: String, chart: ChartVisualizer): Button {
        charts.add(chart)
        stackPane.children.add(chart as Node)
        return Button(label).apply {
            prefWidth = 200.0
            minWidth = 200.0
            maxWidth = 200.0
            setOnAction {
                chart.toFront()
            }
        }
    }

    override fun onMidiData(midiData: MidiData) {
        val note = midiData.note
        charts.stream().forEach { v -> v.onNote(note) }
    }

    override fun onPlayEvent(playEvent: PlayEvent) {
        // Not needed here
    }
}