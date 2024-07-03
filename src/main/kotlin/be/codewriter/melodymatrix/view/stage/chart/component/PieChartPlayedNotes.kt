package be.codewriter.melodymatrix.view.stage.chart.component

import be.codewriter.melodymatrix.view.definition.Note
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.scene.chart.PieChart
import javafx.scene.layout.BorderPane

class PieChartPlayedNotes : BorderPane(), ChartVisualizer {

    private val pieChartMainNotes: List<PieChart.Data> = FXCollections.emptyObservableList()

    init {
        val pieChart = PieChart()
        pieChart.data.addAll(pieChartMainNotes)

        children.add(pieChart)
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
