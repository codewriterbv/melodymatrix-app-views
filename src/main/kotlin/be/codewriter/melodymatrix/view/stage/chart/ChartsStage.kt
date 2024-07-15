package be.codewriter.melodymatrix.view.stage.chart

import be.codewriter.melodymatrix.view.VisualizerStage
import be.codewriter.melodymatrix.view.data.MidiData
import be.codewriter.melodymatrix.view.data.PlayEvent
import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.stage.chart.component.*
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox

class ChartsStage : VisualizerStage() {

    init {
        title = "See your music in charts..."

        var buttons = HBox().apply {
            spacing = 10.0
            children.addAll(
                createChartSelection("Bar chart", BarChartPlayedNotes()),
                createChartSelection("Concentric chart", ConcentricPlayedNotes()),
                createChartSelection("Cox Comb chart", CoxCombPlayedNotes()),
                createChartSelection("Ridge Line chart", RidgeLineChartPlayedNotes()),
                createChartSelection("Radar Chart chart", RadarChartPlayedNotes())
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

    fun createChartSelection(label: String, chart: ChartVisualizer): BorderPane {
        var chartHolder = ChartHolder(label, chart)
        chartHolders.add(chartHolder)
        return chartHolder
    }

    class ChartHolder(val label: String, val chart: ChartVisualizer) : BorderPane() {
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

    override fun onMidiData(midiData: MidiData) {
        if (midiData.event != MidiEvent.NOTE_ON) {
            return
        }
        val note = midiData.note
        chartHolders.stream().forEach { v -> v.chart.onNote(note) }
    }

    override fun onPlayEvent(playEvent: PlayEvent) {
        // Not needed here
    }

    companion object {
        var chartHolders: MutableList<ChartHolder> = mutableListOf()
        lateinit var borderPane: BorderPane
    }
}