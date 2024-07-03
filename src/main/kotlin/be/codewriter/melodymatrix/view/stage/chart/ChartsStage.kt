package be.codewriter.melodymatrix.view.stage.chart

import be.codewriter.melodymatrix.view.VisualizerStage
import be.codewriter.melodymatrix.view.data.MidiData
import be.codewriter.melodymatrix.view.data.PlayEvent
import be.codewriter.melodymatrix.view.stage.chart.component.*
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox


class ChartsStage : VisualizerStage() {

    var charts: MutableList<ChartVisualizer> = mutableListOf()
    var stackPane = StackPane()

    init {
        val buttons = HBox().apply {
            spacing = 10.0
            prefHeight = 150.0
            minHeight = 200.0
            maxHeight = 200.0
            alignment = Pos.TOP_LEFT
            children.addAll(
                Button("Reset").apply {
                    prefWidth = 150.0
                    minWidth = 150.0
                    maxWidth = 150.0
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
            top = buttons
            center = stackPane
        }, 1200.0, 800.0)

        setOnCloseRequest {
            // Nothing needed here, but must be defined or will cause a problem when closing the window
        }
    }

    fun createButton(label: String, chart: ChartVisualizer): VBox {
        charts.add(chart)
        stackPane.children.add(chart as Node)

        val smallView = (chart as BorderPane).apply {
            maxWidth = 150.0
            maxHeight = 150.0
        }

        return VBox().apply {
            prefWidth = 150.0
            minWidth = 150.0
            maxWidth = 150.0
            children.addAll(
                Button(label).apply {
                    prefWidth = 150.0
                    minWidth = 150.0
                    maxWidth = 150.0
                    setOnAction {
                        chart.toFront()
                    }
                },
                smallView
            )
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