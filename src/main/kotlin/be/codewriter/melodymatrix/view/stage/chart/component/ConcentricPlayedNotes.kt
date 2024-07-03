package be.codewriter.melodymatrix.view.stage.chart.component

import be.codewriter.melodymatrix.view.definition.MainNote
import be.codewriter.melodymatrix.view.definition.Note
import eu.hansolo.fx.charts.ConcentricRingChartBuilder
import eu.hansolo.fx.charts.data.ChartItem
import eu.hansolo.fx.charts.data.ChartItemBuilder
import eu.hansolo.fx.charts.tools.NumberFormat
import javafx.application.Platform
import javafx.scene.layout.BorderPane
import javafx.scene.paint.Color

class ConcentricPlayedNotes : BorderPane(), ChartVisualizer {

    private val chartItems: MutableList<ChartItem> = mutableListOf()

    init {
        MainNote.entries.stream()
            .forEach { mn ->
                run {
                    chartItems.add(
                        ChartItemBuilder.create()
                            .name(mn.name)
                            .value(0.0)
                            .fill(mn.chartColor)
                            .textFill(mn.labelColor)
                            .animated(true)
                            .build()
                    )
                }
            }

        val concentric = ConcentricRingChartBuilder.create()
            .prefSize(400.0, 400.0)
            .items(chartItems)
            .sorted(false)
            //.order(Order.DESCENDING)
            //.barBackgroundColor(Color.BLACK)
            .numberFormat(NumberFormat.NUMBER)
            .itemLabelFill(Color.WHITE)
            .build()

        children.add(concentric)
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE)
    }

    override fun onNote(note: Note) {
        Platform.runLater {
            chartItems.forEach { data ->
                if (data.name == note.mainNote.name) {
                    data.value++
                }
            }
        }
    }

    override fun reset() {
        Platform.runLater {
            chartItems.forEach { data ->
                data.value = 0.0
            }
        }
    }
}
