package be.codewriter.melodymatrix.view.stage.chart.component

import be.codewriter.melodymatrix.view.definition.MainNote
import be.codewriter.melodymatrix.view.definition.Note
import eu.hansolo.fx.charts.CoxcombChartBuilder
import eu.hansolo.fx.charts.data.ChartItem
import eu.hansolo.fx.charts.data.ChartItemBuilder
import javafx.application.Platform
import javafx.scene.paint.Color

class CoxCombPlayedNotes : ChartBase(), ChartVisualizer {

    private val chartItems: MutableList<ChartItem> = mutableListOf()

    init {
        MainNote.entries.stream()
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
            CoxcombChartBuilder.create()
                //.prefSize(Double.MAX_VALUE, Double.MAX_VALUE)
                .items(chartItems)
                .textColor(Color.WHITE)
                .autoTextColor(false)
                .useChartItemTextFill(false)
                .equalSegmentAngles(true)
                //.order(Order.ASCENDING)
                //.onMousePressed(onPressedHandler)
                //.onMouseMoved(onMoveHandler)
                .showPopup(false)
                .showItemName(true)
                .formatString("%.2f")
                //.selectedItemFill(Color.MAGENTA)
                .build()
        )
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE)
    }

    override fun onNote(note: Note) {
        Platform.runLater {
            chartItems.forEach { data ->
                if (data.name == note.mainNote.label) {
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
