package be.codewriter.melodymatrix.view.stage.piano.component

import atlantafx.base.controls.ToggleSwitch
import com.almasb.fxgl.dsl.FXGL.Companion.getbp
import com.almasb.fxgl.dsl.FXGL.Companion.getop
import javafx.application.Platform
import javafx.geometry.HorizontalDirection
import javafx.scene.control.ColorPicker
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox

class PianoConfiguratorKey : VBox() {
    companion object {
        private val whiteKeyColor = ColorPicker()
        private val whiteKeyActiveColor = ColorPicker()
        private val blackKeyColor = ColorPicker()
        private val blackKeyActiveColor = ColorPicker()
        private val keyNameVisible = ToggleSwitch()
    }

    init {
        keyNameVisible.apply {
            textProperty().bind(selectedProperty().map { selected -> if (selected) "Visible" else "Hidden" })
            labelPosition = HorizontalDirection.RIGHT
        }
        children.addAll(
            Label("White key"),
            HBox(whiteKeyColor, whiteKeyActiveColor),
            Label("Black key"),
            HBox(blackKeyColor, blackKeyActiveColor),
            Label("Show key note"),
            keyNameVisible
        )
        spacing = 5.0
    }

    /**
     * FXGL properties can only be used after FXGL has started.
     * So in the PianoGenerator/GameApplication class a callback is done in the initGame method to this method.
     */
    fun createBindings() {
        Platform.runLater {
            whiteKeyColor.valueProperty().bindBidirectional(getop(PianoGenerator.PianoProperty.PIANO_KEY_COLOR.name))
            whiteKeyActiveColor.valueProperty()
                .bindBidirectional(getop(PianoGenerator.PianoProperty.PIANO_KEY_ACTIVE_COLOR.name))
            keyNameVisible.selectedProperty()
                .bindBidirectional(getbp(PianoGenerator.PianoProperty.PIANO_KEY_NAME_VISIBLE.name))
        }
    }
}