package be.codewriter.melodymatrix.view.stage.chart.component

import eu.hansolo.fx.charts.YChart
import eu.hansolo.fx.charts.data.ValueChartItem
import javafx.scene.Node
import javafx.scene.layout.Pane

/**
 * Base pane class for all chart visualizer components.
 *
 * Extends [Pane] to provide a resizable container that stretches its single
 * child chart node to fill all available space after accounting for insets.
 * Subclasses add their specific chart via [addChart] or [addRadarChart].
 *
 * @see ChartVisualizer
 */
open class ChartBase : Pane() {

    private var chart: Node? = null

    /**
     * Lays out the child chart node to fill the full available area inside the pane's insets.
     */
    override fun layoutChildren() {
        left = snappedLeftInset()
        right = snappedRightInset()
        top = snappedTopInset()
        bottom = snappedBottomInset()

        chart?.resizeRelocate(left, top, width - right - left, height - bottom - top)

        //super.layoutChildren()
    }

    /**
     * Adds a generic JavaFX [Node] chart as the single child of this pane.
     *
     * @param chart The chart node to display
     */
    fun addChart(chart: Node) {
        this.chart = chart
        children.add(chart)
        //chart.resizeRelocate(left, top, width - right, height - bottom)
    }

    /**
     * Adds a Hansolo [YChart] radar/value chart as the single child of this pane.
     *
     * @param chart The radar chart to display, or null to clear
     */
    fun addRadarChart(chart: YChart<ValueChartItem>?) {
        this.chart = chart
        children.add(chart)
    }

    companion object {
        var left: Double = 0.0
        var right: Double = 0.0
        var top: Double = 0.0
        var bottom: Double = 0.0
    }
}