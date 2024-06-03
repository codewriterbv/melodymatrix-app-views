package be.codewriter.melodymatrix.view.stage.chart.component

import be.codewriter.melodymatrix.view.data.Note
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.scene.chart.PieChart
import javafx.scene.control.TitledPane

class PieChartPlayedNotes : TitledPane(), ChartVisualizer {

    private val pieChartMainNotes: List<PieChart.Data> = FXCollections.emptyObservableList()

    init {
        val pieChart = PieChart()
        pieChart.data.addAll(pieChartMainNotes)

        text = "Pie Chart"
        content = pieChart
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE)
    }

    override fun onNote(note: Note) {
        Platform.runLater {
            // todo
        }
    }

    override fun reset() {
        Platform.runLater {
            pieChartMainNotes.forEach { data ->
                data.pieValue = 0.0
            }
        }
    }
}
