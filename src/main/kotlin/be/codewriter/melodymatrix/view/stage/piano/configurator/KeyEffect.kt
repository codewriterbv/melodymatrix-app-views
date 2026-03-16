package be.codewriter.melodymatrix.view.stage.piano.configurator

import atlantafx.base.controls.ToggleSwitch
import be.codewriter.melodymatrix.view.stage.piano.data.PianoConfiguration
import javafx.beans.property.Property
import javafx.geometry.HorizontalDirection
import javafx.geometry.Pos
import javafx.scene.control.ColorPicker
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color

class KeyEffect(config: PianoConfiguration) : VBox() {

    init {
        spacing = 5.0

        val aboveKeyVisible = ToggleSwitch().apply {
            textProperty().bind(selectedProperty().map { selected -> if (selected) "Visible" else "Hidden" })
            labelPosition = HorizontalDirection.RIGHT
            selectedProperty().bindBidirectional(config.aboveKeyEnabled)
        }

        children.addAll(
            Label("Show particles above keys"),
            aboveKeyVisible,
            Label("Smoke Color"),
            HBox(
                labeledPicker("Start", config.aboveKeyColorStart),
                labeledPicker("End", config.aboveKeyColorEnd)
            ).apply {
                spacing = 5.0
                alignment = Pos.CENTER_LEFT
            }
        )
    }

    private fun labeledPicker(label: String, prop: Property<Color>) = VBox(2.0).apply {
        children.addAll(
            Label(label).apply { style = "-fx-font-size: 10px;" },
            ColorPicker().apply { valueProperty().bindBidirectional(prop) }
        )
    }
}