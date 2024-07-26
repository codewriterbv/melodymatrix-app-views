package be.codewriter.melodymatrix.view.stage.chart.component

import javafx.application.Platform
import javafx.scene.chart.BarChart
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart

class BarChartPlayedNotes : be.codewriter.melodymatrix.view.stage.chart.component.ChartBase(),
    be.codewriter.melodymatrix.view.stage.chart.component.ChartVisualizer {

    private val xyChartMainNotes: XYChart.Series<String, Number> = XYChart.Series()

    init {
        be.codewriter.melodymatrix.view.definition.Note.usedAndSortedMainNotes().stream()
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

    override fun onNote(note: be.codewriter.melodymatrix.view.definition.Note) {
        Platform.runLater {
            xyChartMainNotes.data.forEachIndexed { i, data ->
                if (data.xValue == note.mainNote.label) {
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
