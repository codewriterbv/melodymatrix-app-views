package be.codewriter.melodymatrix.view.stage.chart.component

import be.codewriter.melodymatrix.view.definition.Note
import javafx.application.Platform
import javafx.scene.chart.BarChart
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.control.TitledPane

class BarChartPlayedNotes : TitledPane(), ChartVisualizer {

    private val xyChartMainNotes: XYChart.Series<String, Number> = XYChart.Series()

    init {
        Note.usedAndSortedMainNotes().stream()
            .forEach { mn ->
                run {
                    xyChartMainNotes.data.add(XYChart.Data(mn.name, 0))
                }
            }

        val xAxis = CategoryAxis()
        val yAxis = NumberAxis()
        yAxis.label = "Number of times"
        val barChart: BarChart<String, Number> = BarChart(xAxis, yAxis).apply {
            title = "Played Notes"
            isLegendVisible = false
        }
        barChart.data.addAll(xyChartMainNotes)

        text = "Bar Chart"
        content = barChart
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE)
    }

    override fun onNote(note: Note) {
        Platform.runLater {
            xyChartMainNotes.data.forEachIndexed { i, data ->
                if (data.xValue == note.mainNote.name) {
                    xyChartMainNotes.data[i].yValue = xyChartMainNotes.data[i].yValue.toInt() + 1
                }
            }
        }
    }

    override fun reset() {
        Platform.runLater {
            xyChartMainNotes.data.forEachIndexed { i, data ->
                xyChartMainNotes.data[i].yValue = 0
            }
        }
    }
}
