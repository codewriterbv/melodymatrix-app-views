package be.codewriter.melodymatrix.view.stage.chart

import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox

class ChartsStage : be.codewriter.melodymatrix.view.VisualizerStage() {

    init {
        title = "See your music in charts..."

        var buttons = HBox().apply {
            spacing = 10.0
            children.addAll(
                createChartSelection(
                    "Bar chart",
                    be.codewriter.melodymatrix.view.stage.chart.component.BarChartPlayedNotes()
                ),
                createChartSelection(
                    "Concentric chart",
                    be.codewriter.melodymatrix.view.stage.chart.component.ConcentricPlayedNotes()
                ),
                createChartSelection(
                    "Cox Comb chart",
                    be.codewriter.melodymatrix.view.stage.chart.component.CoxCombPlayedNotes()
                ),
                createChartSelection(
                    "Ridge Line chart",
                    be.codewriter.melodymatrix.view.stage.chart.component.RidgeLineChartPlayedNotes()
                ),
                createChartSelection(
                    "Radar Chart chart",
                    be.codewriter.melodymatrix.view.stage.chart.component.RadarChartPlayedNotes()
                )
            )
        }

        borderPane = BorderPane().apply {
            top = buttons
        }

        scene = Scene(borderPane, 900.0, 700.0)

        setOnCloseRequest {
            // Nothing needed here, but must be defined or will cause a problem when closing the window
        }
    }

    fun createChartSelection(
        label: String,
        chart: be.codewriter.melodymatrix.view.stage.chart.component.ChartVisualizer
    ): BorderPane {
        var chartHolder = ChartHolder(label, chart)
        chartHolders.add(chartHolder)
        return chartHolder
    }

    class ChartHolder(
        val label: String,
        val chart: be.codewriter.melodymatrix.view.stage.chart.component.ChartVisualizer
    ) : BorderPane() {
        init {
            prefWidth = 200.0
            prefHeight = 230.0
            top = Button(label).apply {
                prefWidth = 200.0
                setOnAction {
                    borderPane.center = (chart as Node)
                    for (chartHolder in chartHolders) {
                        if (chartHolder.label != label) {
                            chartHolder.resetView()
                        }
                    }
                }
            }
            resetView()
        }

        fun resetView() {
            center = (chart as Node).apply {
                prefWidth = 200.0
                prefHeight = 200.0
            }
        }
    }

    override fun onMidiData(midiData: be.codewriter.melodymatrix.view.data.MidiData) {
        if (midiData.event != be.codewriter.melodymatrix.view.definition.MidiEvent.NOTE_ON) {
            return
        }
        val note = midiData.note
        chartHolders.stream().forEach { v -> v.chart.onNote(note) }
    }

    override fun onPlayEvent(playEvent: be.codewriter.melodymatrix.view.data.PlayEvent) {
        // Not needed here
    }

    companion object {
        var chartHolders: MutableList<ChartHolder> = mutableListOf()
        lateinit var borderPane: BorderPane
    }
}