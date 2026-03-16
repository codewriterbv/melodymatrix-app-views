package be.codewriter.melodymatrix.view.stage.piano.configurator

import atlantafx.base.controls.ToggleSwitch
import be.codewriter.melodymatrix.view.stage.piano.data.PianoConfiguration
import javafx.geometry.HorizontalDirection
import javafx.geometry.Pos
import javafx.scene.control.ColorPicker
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox

class KeyEffect(config: PianoConfiguration) : VBox() {

    init {
        val aboveKeyVisible = ToggleSwitch().apply {
            isSelected = true
            textProperty().bind(selectedProperty().map { selected -> if (selected) "Visible" else "Hidden" })
            labelPosition = HorizontalDirection.RIGHT
            selectedProperty().bindBidirectional(config.aboveKeyEnabled)
        }
        val aboveKeyStart = ColorPicker().apply {
            valueProperty().bindBidirectional(config.aboveKeyColorStart)
        }
        val aboveKeyEnd = ColorPicker().apply {
            valueProperty().bindBidirectional(config.aboveKeyColorEnd)
        }

        children.addAll(
            Label("Show particles above keys"),
            aboveKeyVisible,
            Label("Smoke Color"),
            HBox(aboveKeyStart, aboveKeyEnd).apply {
                spacing = 5.0
                alignment = Pos.CENTER_LEFT
            }
        )

        spacing = 5.0
    }
}