package be.codewriter.melodymatrix.view.component

import javafx.scene.control.Slider

class TickerSlider : Slider() {

    init {
        isShowTickMarks = true
        isShowTickLabels = true
        isSnapToTicks = true
    }
}