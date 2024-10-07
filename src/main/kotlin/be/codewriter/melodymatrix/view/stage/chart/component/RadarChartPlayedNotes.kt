package be.codewriter.melodymatrix.view.stage.chart.component

import be.codewriter.melodymatrix.view.definition.MainNote
import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.definition.Octave
import eu.hansolo.fx.charts.Category
import eu.hansolo.fx.charts.ChartType
import eu.hansolo.fx.charts.YChart
import eu.hansolo.fx.charts.YPane
import eu.hansolo.fx.charts.event.ChartEvt
import eu.hansolo.fx.charts.data.ChartItemBuilder
import eu.hansolo.fx.charts.data.ValueChartItem
import eu.hansolo.fx.charts.series.YSeries
import javafx.application.Platform
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.RadialGradient
import kotlin.random.Random

class RadarChartPlayedNotes : ChartBase(), ChartVisualizer {

    private var chords: MutableMap<Octave, YSeries<ValueChartItem>> = mutableMapOf()

    init {
        for (octave in Note.usedAndSortedOctaves()) {
            val notes: MutableList<ValueChartItem> = mutableListOf()
            MainNote.entries.stream()
                .forEach { mn ->
                    run {
                        var item = ValueChartItem(0.0, mn.label)
                        item.valueProperty().addListener { _, ov, nv -> println(item.name + " changed from " + ov + " to " + nv)}
                        notes.add(item)
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

        val categories: MutableList<Category> = mutableListOf()
        for (mainNote in Note.usedAndSortedMainNotes()) {
            categories.add(Category(mainNote.label))
        }

        var data = YPane(
            categories,
            true,
            chords[Octave.OCTAVE_0],
            chords[Octave.OCTAVE_1],
            chords[Octave.OCTAVE_2],
            chords[Octave.OCTAVE_3],
            chords[Octave.OCTAVE_4],
            chords[Octave.OCTAVE_5],
            chords[Octave.OCTAVE_6],
            chords[Octave.OCTAVE_7],
            chords[Octave.OCTAVE_8],
            chords[Octave.OCTAVE_9]
        )
        addChart(YChart(data))
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE)
    }

    override fun onNote(note: Note) {
        Platform.runLater {
            val series = chords[note.octave]
            if (series != null) {
                for (item in series.items) {
                    if (item.name == note.mainNote.label) {
                        var newValue = item.value
                        newValue++
                        item.value = newValue
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
