package be.codewriter.melodymatrix.view.stage.chart.component

import eu.hansolo.fx.charts.Category
import eu.hansolo.fx.charts.ChartType
import eu.hansolo.fx.charts.YChart
import eu.hansolo.fx.charts.YPane
import eu.hansolo.fx.charts.data.ValueChartItem
import eu.hansolo.fx.charts.series.YSeries
import javafx.application.Platform
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.RadialGradient

class RadarChartPlayedNotes : be.codewriter.melodymatrix.view.stage.chart.component.ChartBase(),
    be.codewriter.melodymatrix.view.stage.chart.component.ChartVisualizer {

    private var chords: MutableMap<be.codewriter.melodymatrix.view.definition.Octave, YSeries<ValueChartItem>> =
        mutableMapOf()

    init {

        for (octave in be.codewriter.melodymatrix.view.definition.Note.usedAndSortedOctaves()) {
            val notes: MutableList<ValueChartItem> = mutableListOf()
            be.codewriter.melodymatrix.view.definition.MainNote.entries.stream()
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
        for (mainNote in be.codewriter.melodymatrix.view.definition.Note.usedAndSortedMainNotes()) {
            categories.add(Category(mainNote.label))
        }

        val data = YPane(
            categories,
            chords[be.codewriter.melodymatrix.view.definition.Octave.OCTAVE_0],
            chords[be.codewriter.melodymatrix.view.definition.Octave.OCTAVE_1],
            chords[be.codewriter.melodymatrix.view.definition.Octave.OCTAVE_2],
            chords[be.codewriter.melodymatrix.view.definition.Octave.OCTAVE_3],
            chords[be.codewriter.melodymatrix.view.definition.Octave.OCTAVE_4],
            chords[be.codewriter.melodymatrix.view.definition.Octave.OCTAVE_5],
            chords[be.codewriter.melodymatrix.view.definition.Octave.OCTAVE_6],
            chords[be.codewriter.melodymatrix.view.definition.Octave.OCTAVE_7],
            chords[be.codewriter.melodymatrix.view.definition.Octave.OCTAVE_8],
            chords[be.codewriter.melodymatrix.view.definition.Octave.OCTAVE_9]
        )
        addChart(YChart(data))
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE)
    }

    override fun onNote(note: be.codewriter.melodymatrix.view.definition.Note) {
        Platform.runLater {
            val series = chords[note.octave]
            if (series != null) {
                for (item in series.items) {
                    if (item.name == note.mainNote.label) {
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
