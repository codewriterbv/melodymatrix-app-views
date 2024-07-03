package be.codewriter.melodymatrix.view.stage.chart

import atlantafx.base.controls.ToggleSwitch
import be.codewriter.melodymatrix.view.VisualizerStage
import be.codewriter.melodymatrix.view.data.MidiData
import be.codewriter.melodymatrix.view.data.PlayEvent
import be.codewriter.melodymatrix.view.stage.chart.component.*
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox


class ChartsStage : VisualizerStage() {

    var charts: MutableList<ChartVisualizer> = mutableListOf()
    var grid: GridPane = GridPane()

    init {
        grid.add(Button("Reset").apply {
            setOnAction {
                charts.forEach(ChartVisualizer::reset)
            }
        }, 0, 0)

        charts.add(PieChartPlayedNotes())
        charts.add(BarChartPlayedNotes())
        charts.add(ConcentricPlayedNotes())
        charts.add(CoxCombPlayedNotes())
        charts.add(RidgeLineChartPlayedNotes())
        charts.add(RadarChartPlayedNotes())

        val buttons = VBox().apply {
            spacing = 10.0
            children.addAll(
                createButton("Pie chart", pie, false),
                createButton("Bar chart", bar, false),
                createButton("Concentric chart", concentric, false),
                createButton("Cox Comb chart", coxcomb, false),
                createButton("Ridge Line chart", ridgeline, true),
                createButton("Radar Chart chart", radar, true)
            )
        }

        title = "See your music in charts..."
        scene = Scene(BorderPane().apply {
            left = buttons
            right = charts.get(0) as Node
        }, 1400.0, 820.0)

        setOnCloseRequest {
            // Nothing needed here, but must be defined or will cause a problem when closing the window
        }
    }


    fun createButton(label: String, chart: Node, defaultAdded: Boolean): ToggleSwitch {
        if (defaultAdded) {
            charts.children.add(chart)
        }
        return ToggleSwitch(label).apply {
            isSelected = defaultAdded
            selectedProperty().addListener { _, _, newValue ->
                if (newValue) {
                    charts.children.add(chart)
                } else {
                    charts.children.remove(chart)
                }
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