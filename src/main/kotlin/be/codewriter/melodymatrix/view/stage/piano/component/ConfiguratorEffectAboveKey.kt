package be.codewriter.melodymatrix.view.stage.piano.component

import atlantafx.base.controls.ToggleSwitch
import com.almasb.fxgl.dsl.FXGL.Companion.getop
import com.almasb.fxgl.dsl.getbp
import javafx.application.Platform
import javafx.geometry.HorizontalDirection
import javafx.geometry.Pos
import javafx.scene.control.ColorPicker
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color

class ConfiguratorEffectAboveKey : VBox() {
    companion object {
        private val aboveKeyVisible = ToggleSwitch()
        private val aboveKeyStart = ColorPicker()
        private val aboveKeyEnd = ColorPicker()
    }

    init {
        aboveKeyVisible.apply {
            isSelected = true
            textProperty().bind(selectedProperty().map { selected -> if (selected) "Visible" else "Hidden" })
            labelPosition = HorizontalDirection.RIGHT
        }
        aboveKeyStart.value = Color.YELLOW
        aboveKeyEnd.value = Color.DARKRED
        children.addAll(
            Label("Show particles above keys"),
            aboveKeyVisible,
            Label("Smoke Color"),
            HBox(aboveKeyStart).apply {
                spacing = 5.0
                alignment = Pos.CENTER_LEFT
            }
        )
        spacing = 5.0
    }

    /**
     * FXGL properties can only be used after FXGL has started.
     * So in the PianoGenerator/GameApplication class a callback is done in the initGame method to this method.
     */
    fun createBindings() {
        Platform.runLater {
            aboveKeyVisible.selectedProperty()
                .bindBidirectional(getbp(PianoProperty.ABOVE_KEY_ENABLED.name))
            aboveKeyStart.valueProperty()
                .bindBidirectional(getop<Color>(PianoProperty.ABOVE_KEY_COLOR_START.name))
            aboveKeyEnd.valueProperty()
                .bindBidirectional(getop<Color>(PianoProperty.ABOVE_KEY_COLOR_END.name))
        }
    }
}