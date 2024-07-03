package be.codewriter.melodymatrix.view.stage.chart.component

import be.codewriter.melodymatrix.view.definition.MainNote
import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.definition.Octave
import eu.hansolo.fx.charts.Category
import eu.hansolo.fx.charts.ChartType
import eu.hansolo.fx.charts.YChart
import eu.hansolo.fx.charts.YPane
import eu.hansolo.fx.charts.data.ValueChartItem
import eu.hansolo.fx.charts.series.YSeries
import javafx.application.Platform
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.RadialGradient
import kotlin.random.Random

class RadarChartPlayedNotes : Pane(), ChartVisualizer {

    private var chords: MutableMap<Octave, YSeries<ValueChartItem>> = mutableMapOf()

    init {
        for (octave in Note.usedAndSortedOctaves()) {
            val notes: MutableList<ValueChartItem> = mutableListOf()
            MainNote.entries.stream()
                .forEach { mn ->
                    run {
                        notes.add(ValueChartItem(0.0, mn.label))
                    }
                }
            chords[octave] = YSeries<ValueChartItem>(
                notes,
                ChartType.RADAR_SECTOR,
                RadialGradient(
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    1.0,
                    true,
                    CycleMethod.NO_CYCLE,
                    *octave.gradientStops
                ),
                Color.TRANSPARENT
            )
        }

        val categories: MutableList<Category> = ArrayList()
        for (mainNote in Note.usedAndSortedMainNotes()) {
            categories.add(Category(mainNote.label))
        }

        val arr: Array<YSeries<ValueChartItem>> = chords.values.toTypedArray()
        /*for (series in chords.values) {
            arr.plus(series)
        }*/

        val data = YPane(categories, *arr)/*.apply {
            lowerBoundY = 0.0
            upperBoundY = 100.0
        }*/
        children.add(YChart(data))
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE)
    }

    override fun onNote(note: Note) {
        Platform.runLater {
            val series = chords[note.octave]
            if (series != null) {
                for (item in series.items) {
                    if (item.name == note.mainNote.label) {
                        item.value = Random.nextInt(0, 100 + 1).toDouble()

                    }
                }
                series.addSeriesEventListener {
                    println("test")
                }
            }
        }
    }

    override fun reset() {
        Platform.runLater {
            for (chord in chords) {
                for (item in chord.value.items) {
                    item.value = 0.0
                }
            }
        }
    }
}
