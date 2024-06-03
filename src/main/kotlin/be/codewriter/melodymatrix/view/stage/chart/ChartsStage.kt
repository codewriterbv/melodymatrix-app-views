package be.codewriter.melodymatrix.view.stage.chart

import atlantafx.base.controls.ToggleSwitch
import be.codewriter.melodymatrix.view.VisualizerStage
import be.codewriter.melodymatrix.view.data.MidiData
import be.codewriter.melodymatrix.view.stage.chart.component.*
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.FlowPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox

class ChartsStage : VisualizerStage() {

    private val charts: FlowPane = FlowPane()

    init {
        val pie = PieChartPlayedNotes()
        val bar = BarChartPlayedNotes()
        val concentric = ConcentricPlayedNotes()
        val coxcomb = CoxCombPlayedNotes()
        val ridgeline = RidgeLineChartPlayedNotes()
        val radar = RadarChartPlayedNotes()

        val controls = HBox().apply {
            spacing = 10.0
            padding = Insets(10.0)
            alignment = Pos.CENTER_LEFT
            children.addAll(
                Button("Reset").apply {
                    setOnAction {
                        for (visualizer in charts.children) {
                            if (visualizer is ChartVisualizer) {
                                visualizer.reset()
                            }
                        }
                    }
                },
                createToggle("Pie chart", pie, false),
                createToggle("Bar chart", bar, false),
                createToggle("Concentric chart", concentric, false),
                createToggle("Cox Comb chart", coxcomb, false),
                createToggle("Ridge Line chart", ridgeline, true),
                createToggle("Radar Chart chart", radar, true)
            )
        }

        title = "See your music in charts..."
        scene = Scene(
            VBox(controls, charts).apply {
                spacing = 5.0
                padding = Insets(10.0)
            }, 1400.0, 820.0
        )

        setOnCloseRequest {
            // Nothing needed here, but must be defined or will cause a problem when closing the window
        }
    }

    fun createToggle(label: String, chart: Node, defaultAdded: Boolean): ToggleSwitch {
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

    override fun onMidiDataReceived(midiData: MidiData) {
        val note = midiData.note
        for (visualizer in charts.children) {
            if (visualizer is ChartVisualizer) {
                visualizer.onNote(note)
            }
        }
    }
}