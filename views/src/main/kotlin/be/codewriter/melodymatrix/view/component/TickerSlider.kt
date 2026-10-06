package be.codewriter.melodymatrix.view.component

import javafx.scene.control.Slider
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

class TickerSlider : Slider() {

    /**
     * Enables automatic tick-unit tuning based on the current min/max range.
     * Set to false when a screen needs fully manual major/minor tick control.
     */
    var adaptiveTicks: Boolean = true

    init {
        isShowTickMarks = true
        isShowTickLabels = true
        isSnapToTicks = true

        // Keep labels readable by adapting tick density when ranges change.
        minProperty().addListener { _, _, _ -> updateAdaptiveTicks() }
        maxProperty().addListener { _, _, _ -> updateAdaptiveTicks() }
        updateAdaptiveTicks()
    }

    private fun updateAdaptiveTicks() {
        if (!adaptiveTicks) return

        val range = max - min
        if (!range.isFinite() || range <= 0.0) {
            majorTickUnit = 1.0
            minorTickCount = 0
            return
        }

        val targetMajorTickCount = 8.0
        val major = niceStep(range / targetMajorTickCount)
        majorTickUnit = major

        val estimatedMajorTicks = range / major
        minorTickCount = when {
            estimatedMajorTicks > 12.0 -> 0
            major >= 100.0 -> 0
            major >= 20.0 -> 1
            major >= 5.0 -> 2
            else -> 4
        }
    }

    private fun niceStep(rawStep: Double): Double {
        if (!rawStep.isFinite() || rawStep <= 0.0) return 1.0

        val exponent = floor(log10(rawStep))
        val base = 10.0.pow(exponent)
        val fraction = rawStep / base
        val niceFraction = when {
            fraction <= 1.0 -> 1.0
            fraction <= 2.0 -> 2.0
            fraction <= 5.0 -> 5.0
            else -> 10.0
        }
        return niceFraction * base
    }
}