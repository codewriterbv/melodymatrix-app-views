package be.codewriter.melodymatrix.view.stage.chart

import be.codewriter.melodymatrix.view.VisualizerStage
import be.codewriter.melodymatrix.view.data.MidiData
import be.codewriter.melodymatrix.view.data.PlayEvent
import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.stage.chart.component.*
import javafx.scene.Scene
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.Pane

class ChartsStage : VisualizerStage() {
    var charts: MutableList<ChartVisualizer> = mutableListOf()

    init {
        title = "See your music in charts..."
        scene = Scene(TabPane().apply {
            setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE)
            tabs.addAll(
                createTab("Bar chart", BarChartPlayedNotes()),
                createTab("Concentric chart", ConcentricPlayedNotes()),
                createTab("Cox Comb chart", CoxCombPlayedNotes()),
                createTab("Ridge Line chart", RidgeLineChartPlayedNotes()),
                createTab("Radar Chart chart", RadarChartPlayedNotes())
            )
        }, 900.0, 700.0)

        setOnCloseRequest {
            // Nothing needed here, but must be defined or will cause a problem when closing the window
        }
    }

    fun createTab(label: String, chart: ChartVisualizer): Tab {
        charts.add(chart)
        return Tab(label, chart as Pane)
    }

    override fun onMidiData(midiData: MidiData) {
        if (midiData.event != MidiEvent.NOTE_ON) {
            return
        }
        val note = midiData.note
        charts.stream().forEach { v -> v.onNote(note) }
    }

    override fun onPlayEvent(playEvent: PlayEvent) {
        // Not needed here
    }
}