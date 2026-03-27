package be.codewriter.melodymatrix.view.view.chart.component

import be.codewriter.melodymatrix.view.definition.Note
import javafx.application.Platform
import javafx.scene.chart.BarChart
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart


/**
 * A bar chart that counts and visualises how many times each main note has been played.
 *
 * One bar per [MainNote] is shown on the X-axis; the Y-axis shows the cumulative
 * count of NOTE_ON events for that note. The chart is initialised with zero values
 * for every main note used across the full note range.
 *
 * @see ChartBase
 * @see ChartVisualizer
 */
class BarChartPlayedNotes : ChartBase(), ChartVisualizer {

    private val xyChartMainNotes: XYChart.Series<String, Number> = XYChart.Series()

    init {
        Note.usedAndSortedMainNotes().stream()
            .forEach { mn ->
                run {
                    xyChartMainNotes.data.add(XYChart.Data(mn.label, 0))
                }
            }

        val xAxis = CategoryAxis()
        val yAxis = NumberAxis()
        yAxis.label = "Number of times"

        addChart(BarChart(xAxis, yAxis).apply {
            isLegendVisible = false
            data.addAll(xyChartMainNotes)
        })
    }

    /**
     * Increments the bar value for the main note matching the played note.
     *
     * @param note The note that was played (NOTE_ON)
     */
    override fun onNote(note: Note) {
        Platform.runLater {
            xyChartMainNotes.data.forEachIndexed { i, data ->
                if (data.xValue == note.mainNote.label) {
                    xyChartMainNotes.data[i].yValue = xyChartMainNotes.data[i].yValue.toInt() + 1
                }
            }
        }
    }

    /**
     * Resets all bar values to zero.
     */
    override fun reset() {
        Platform.runLater {
            xyChartMainNotes.data.forEachIndexed { i, data ->
                xyChartMainNotes.data[i].yValue = 0
            }
        }
    }
}