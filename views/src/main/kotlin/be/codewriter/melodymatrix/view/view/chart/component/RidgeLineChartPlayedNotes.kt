package be.codewriter.melodymatrix.view.view.chart.component

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
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.Stop

/**
 * A ridge-line chart that visualises how often each main note has been played, grouped by octave.
 *
 * One horizontal ridge line is drawn per octave. Each ridge shows the relative play count for
 * every [MainNote] along the X-axis. A rainbow-style linear gradient is used to colour the area
 * under each ridge.
 *
 * @see ChartBase
 * @see ChartVisualizer
 */
class RidgeLineChartPlayedNotes : ChartBase(), ChartVisualizer {

    private val chartBox: VBox = VBox().apply {
        prefHeight = Double.MAX_VALUE
        prefWidth = Double.MAX_VALUE
        minHeight = 0.0
        //style = "-fx-border-color: black; -fx-border-width: 2px; -fx-border-style: solid; -fx-border-radius: 5px;"
    }

    private val gradient: LinearGradient = LinearGradient(
        0.0, 0.0, 1.0, 0.0, true, CycleMethod.NO_CYCLE,
        Stop(0.00, Color.rgb(0, 0, 200, 0.5)),
        Stop(0.25, Color.rgb(122, 0, 183, 0.5)),
        Stop(0.50, Color.rgb(255, 0, 0, 0.5)),
        Stop(0.75, Color.rgb(255, 175, 0, 0.5)),
        Stop(1.00, Color.rgb(255, 255, 0, 0.5))
    )

    private var chords: MutableMap<Octave, List<XYChartItem>> =
        mutableMapOf()

    /** The ridge of every octave; they share one vertical scale so the octaves stay comparable. */
    private val ridgePanes = mutableListOf<XYPane<*>>()

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
                // The last note sits at x = size - 1: with size as the bound the ridge stopped short of the right edge
                upperBoundX = xySeries.items.size - 1.0
                upperBoundY = MIN_RANGE
                prefWidth = Double.MAX_VALUE
                // Fill the height of the row, like the other charts fill the area they are expanded into
                minHeight = 0.0
                maxHeight = Double.MAX_VALUE
                //style = "-fx-border-color: red; -fx-border-width: 2px; -fx-border-style: solid; -fx-border-radius: 5px;"
                HBox.setHgrow(this, Priority.ALWAYS)
            }

            ridgePanes.add(ridgeLineChart)
            chartBox.children.add(HBox().apply {
                alignment = Pos.CENTER_LEFT
                prefWidth = Double.MAX_VALUE
                // Every octave gets an equal share of the height when the chart is enlarged
                minHeight = 0.0
                maxHeight = Double.MAX_VALUE
                VBox.setVgrow(this, Priority.ALWAYS)
                //style =
                //    "-fx-border-color: blue; -fx-border-width: 2px; -fx-border-style: solid; -fx-border-radius: 5px;"
                children.addAll(
                    Label(chord.key.octave.toString()).apply {
                        //style = "-fx-font-size: 12px; -fx-font-weight: bold;"
                    },
                    ridgeLineChart
                )
            })
        }

        addChart(chartBox)
    }

    /**
     * Increments the ridge-line data point for the main note and octave matching the played note.
     *
     * @param note The note that was played (NOTE_ON)
     */
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
            rescale()
        }
    }

    /** Fits the vertical scale to the highest count, like the other charts do, so the ridges use the room they get. */
    private fun rescale() {
        val peak = chords.values.maxOfOrNull { items -> items.maxOfOrNull { it.y } ?: 0.0 } ?: 0.0
        val range = maxOf(MIN_RANGE, peak * HEADROOM)
        ridgePanes.forEach { it.upperBoundYProperty().set(range) }
    }

    /**
     * Resets all ridge-line values to zero across all octaves.
     */
    override fun reset() {
        Platform.runLater {
            for (chord in chords) {
                for (value in chord.value) {
                    value.y = 0.0
                }
            }
            rescale()
        }
    }

    private companion object {
        /** Smallest vertical range, so the first few notes do not fill the whole row. */
        const val MIN_RANGE = 5.0

        /** Room above the highest ridge. */
        const val HEADROOM = 1.15
    }
}