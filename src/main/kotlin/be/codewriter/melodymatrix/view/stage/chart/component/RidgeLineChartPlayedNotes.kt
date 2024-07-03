package be.codewriter.melodymatrix.view.stage.chart.component

import be.codewriter.melodymatrix.view.definition.MainNote
import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.definition.Octave
import eu.hansolo.fx.charts.ChartType
import eu.hansolo.fx.charts.XYPane
import eu.hansolo.fx.charts.data.XYChartItem
import eu.hansolo.fx.charts.series.XYSeries
import eu.hansolo.fx.charts.series.XYSeriesBuilder
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.Stop

class RidgeLineChartPlayedNotes : Pane(), ChartVisualizer {

    private val chartBox: VBox = VBox().apply {
        height = Double.MAX_VALUE
        width = Double.MAX_VALUE
    }

    private val gradient: LinearGradient = LinearGradient(
        0.0, 0.0, 1.0, 0.0, true, CycleMethod.NO_CYCLE,
        Stop(0.00, Color.rgb(0, 0, 200, 0.5)),
        Stop(0.25, Color.rgb(122, 0, 183, 0.5)),
        Stop(0.50, Color.rgb(255, 0, 0, 0.5)),
        Stop(0.75, Color.rgb(255, 175, 0, 0.5)),
        Stop(1.00, Color.rgb(255, 255, 0, 0.5))
    )

    private var chords: MutableMap<Octave, List<XYChartItem>> = mutableMapOf()

    init {

        for (octave in Note.usedAndSortedOctaves()) {
            val notes: MutableList<XYChartItem> = mutableListOf()
            var counter = 0.0
            MainNote.entries.stream()
                .forEach { mn ->
                    run {
                        notes.add(XYChartItem(counter, 0.0, mn.name))
                        counter++
                    }
                }
            chords.put(octave, notes)
        }

        for (chord in chords) {
            val xySeries: XYSeries<*> = XYSeriesBuilder.create()
                .items(chord.value)
                .chartType(ChartType.RIDGE_LINE)
                .fill(gradient)
                .stroke(Color.BLACK)
                .strokeWidth(1.0)
                .build()
            val ridgeLineChart = XYPane(xySeries).apply {
                setPrefSize(400.0, 400.0 / chords.size)
                upperBoundX = xySeries.items.size.toDouble()
            }
            chartBox.children.add(HBox().apply {
                alignment = Pos.CENTER_LEFT
                children.addAll(Label(chord.key.octave.toString()), ridgeLineChart)
            })
        }

        children.add(chartBox)
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE)
    }

    override fun onNote(note: Note) {
        Platform.runLater {
            val data = chords[note.octave]
            if (data != null) {
                for (item: XYChartItem in data) {
                    if (item.name.equals(note.mainNote.name)) {
                        item.y++
                    }
                }
            }
        }
    }

    override fun reset() {
        Platform.runLater {
            for (chord in chords) {
                for (value in chord.value) {
                    value.y = 0.0
                }
            }
        }
    }
}
