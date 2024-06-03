package be.codewriter.melodymatrix.view.stage.chart.component

import be.codewriter.melodymatrix.view.data.MainNote
import be.codewriter.melodymatrix.view.data.Note
import be.codewriter.melodymatrix.view.data.Octave
import eu.hansolo.fx.charts.Category
import eu.hansolo.fx.charts.ChartType
import eu.hansolo.fx.charts.YChart
import eu.hansolo.fx.charts.YPane
import eu.hansolo.fx.charts.data.ValueChartItem
import eu.hansolo.fx.charts.series.YSeries
import javafx.application.Platform
import javafx.scene.control.TitledPane
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.RadialGradient

class RadarChartPlayedNotes : TitledPane(), ChartVisualizer {

    private var chords: MutableMap<Octave, YSeries<ValueChartItem>> = mutableMapOf()

    init {
        for (octave in Note.usedAndSortedOctaves()) {
            val notes: MutableList<ValueChartItem> = mutableListOf()
            MainNote.entries.stream()
                .forEach { mn ->
                    run {
                        notes.add(ValueChartItem(0.0, mn.name))
                    }
                }
            chords[octave] = YSeries<ValueChartItem>(
                notes,
                ChartType.SMOOTH_RADAR_POLYGON,
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
            categories.add(Category(mainNote.name))
        }

        val chart = YChart(YPane(categories, *chords.values.toTypedArray()))

        text = "Radar Chart"
        content = chart
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE)
    }

    override fun onNote(note: Note) {
        Platform.runLater {
            val data = chords[note.octave]
            if (data != null) {
                for (item in data.items) {
                    if (item.name.equals(note.mainNote.name)) {
                        item.value++
                    }
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
