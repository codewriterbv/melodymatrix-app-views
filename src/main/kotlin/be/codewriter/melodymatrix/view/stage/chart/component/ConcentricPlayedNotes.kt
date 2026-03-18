package be.codewriter.melodymatrix.view.stage.chart.component

import be.codewriter.melodymatrix.view.definition.MainNote
import be.codewriter.melodymatrix.view.definition.Note
import eu.hansolo.fx.charts.ConcentricRingChartBuilder
import eu.hansolo.fx.charts.data.ChartItem
import eu.hansolo.fx.charts.data.ChartItemBuilder
import eu.hansolo.fx.charts.tools.NumberFormat
import javafx.application.Platform
import javafx.scene.paint.Color


/**
 * A concentric ring chart that counts and visualises how many times each main note has been played.
 *
 * Each ring segment represents one [MainNote] and grows as the note is played.
 * Colours are taken from [MainNote.chartColor] to distinguish the notes visually.
 *
 * @see ChartBase
 * @see ChartVisualizer
 */
class ConcentricPlayedNotes : ChartBase(), ChartVisualizer {

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

    /**
     * Increments the chart segment value for the main note matching the played note.
     *
     * @param note The note that was played (NOTE_ON)
     */
    override fun onNote(note: Note) {
        Platform.runLater {
            chartItems.forEach { data ->
                if (data.name == note.mainNote.name) {
                    data.value++
                }
            }
        }
    }

    /**
     * Resets all concentric ring segment values to zero.
     */
    override fun reset() {
        Platform.runLater {
            chartItems.forEach { data ->
                data.value = 0.0
            }
        }
    }
}