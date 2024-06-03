package be.codewriter.melodymatrix.view.stage.chart.component

import be.codewriter.melodymatrix.view.data.MainNote
import be.codewriter.melodymatrix.view.data.Note
import eu.hansolo.fx.charts.CoxcombChartBuilder
import eu.hansolo.fx.charts.data.ChartItem
import eu.hansolo.fx.charts.data.ChartItemBuilder
import javafx.application.Platform
import javafx.scene.control.TitledPane
import javafx.scene.paint.Color

class CoxCombPlayedNotes : TitledPane(), ChartVisualizer {

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

        val coxComb = CoxcombChartBuilder.create()
            .prefSize(400.0, 400.0)
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

        text = "Coxcomb"
        content = coxComb
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
