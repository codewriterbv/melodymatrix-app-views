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
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.RadialGradient

class RadarChartPlayedNotes : ChartBase(), ChartVisualizer {

    private var chords : MutableMap<Octave, YSeries<ValueChartItem>> = mutableMapOf()
    private var items  : MutableList<ValueChartItem>                 = mutableListOf()
    private var data   : YPane<ValueChartItem>?                      = null
    private var ychart : YChart<ValueChartItem>?                     = null

    init {
        for (octave in Note.usedAndSortedOctaves()) {
            val notes: MutableList<ValueChartItem> = mutableListOf()
            MainNote.entries.stream()
                .forEach { mn ->
                    run {
                        var item = ValueChartItem(0.0, mn.label)
                        notes.add(item)
                        items.add(item)
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

        this.data = YPane(
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

        this.ychart = YChart(data)
        addRadarChart(ychart)
        //addChart(YChart(data))
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE)
    }

    override fun onNote(note: Note) {
        Platform.runLater {
            val series = chords[note.octave]
            if (series != null) {
                /*
                for (item in series.items) {
                    if (item.name == note.mainNote.label) {
                        item.value++
                    }
                }
                */
                for (item in items) {
                    if (item.name == note.mainNote.label) {
                        item.value++
                        ychart?.refresh()
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
