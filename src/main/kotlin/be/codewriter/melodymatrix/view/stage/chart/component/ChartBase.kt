package be.codewriter.melodymatrix.view.stage.chart.component

import javafx.scene.Node
import javafx.scene.layout.Pane

open class ChartBase : Pane() {

    private var chart: Node? = null

    override fun layoutChildren() {
        println("Test")

        left = snappedLeftInset()
        right = snappedRightInset()
        top = snappedTopInset()
        bottom = snappedBottomInset()

        chart?.resizeRelocate(left, top, width - right - left, height - bottom - top)

        //super.layoutChildren()
    }

    fun addChart(chart: Node) {
        this.chart = chart
        children.add(chart)
        //chart.resizeRelocate(left, top, width - right, height - bottom)
    }

    companion object {
        var left: Double = 0.0
        var right: Double = 0.0
        var top: Double = 0.0
        var bottom: Double = 0.0
    }
}