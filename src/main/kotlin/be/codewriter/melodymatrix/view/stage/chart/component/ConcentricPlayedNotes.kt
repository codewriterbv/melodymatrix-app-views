package be.codewriter.melodymatrix.view.stage.chart.component

import eu.hansolo.fx.charts.ConcentricRingChartBuilder
import eu.hansolo.fx.charts.data.ChartItem
import eu.hansolo.fx.charts.data.ChartItemBuilder
import eu.hansolo.fx.charts.tools.NumberFormat
import javafx.application.Platform
import javafx.scene.paint.Color

class ConcentricPlayedNotes : be.codewriter.melodymatrix.view.stage.chart.component.ChartBase(),
    be.codewriter.melodymatrix.view.stage.chart.component.ChartVisualizer {

    private val chartItems: MutableList<ChartItem> = mutableListOf()

    init {
        be.codewriter.melodymatrix.view.definition.MainNote.entries.stream()
            .forEach { mn ->
                run {
                    chartItems.add(
                        ChartItemBuilder.create()
                            .name(mn.label)
                            .value(0.0)
                            .fill(mn.chartColor)
                            .textFill(mn.labelColor)
                            .animated(true)
                            .build()
                    )
                }
            }

        addChart(
            ConcentricRingChartBuilder.create()
                //.prefSize(Double.MAX_VALUE, Double.MAX_VALUE)
                .items(chartItems)
                .sorted(false)
                //.order(Order.DESCENDING)
                //.barBackgroundColor(Color.BLACK)
                .valueVisible(true)
                .numberFormat(NumberFormat.NUMBER)
                .itemLabelFill(Color.WHITE)
                .build()
        )
    }

    override fun onNote(note: be.codewriter.melodymatrix.view.definition.Note) {
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
