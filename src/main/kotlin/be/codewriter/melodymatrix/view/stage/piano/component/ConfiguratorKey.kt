package be.codewriter.melodymatrix.view.stage.piano.component

import atlantafx.base.controls.ToggleSwitch
import com.almasb.fxgl.dsl.FXGL.Companion.getbp
import com.almasb.fxgl.dsl.FXGL.Companion.getop
import javafx.application.Platform
import javafx.geometry.HorizontalDirection
import javafx.geometry.Pos
import javafx.scene.control.ColorPicker
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox

class ConfiguratorKey : VBox() {
    companion object {
        private val whiteKeyColor = ColorPicker()
        private val whiteKeyActiveColor = ColorPicker()
        private val blackKeyColor = ColorPicker()
        private val blackKeyActiveColor = ColorPicker()
        private val keyNameVisible = ToggleSwitch()
        private val keyNameColor = ColorPicker()
    }

    init {
        keyNameVisible.apply {
            textProperty().bind(selectedProperty().map { selected -> if (selected) "Visible" else "Hidden" })
            labelPosition = HorizontalDirection.RIGHT
        }
        children.addAll(
            Label("White key"),
            HBox(whiteKeyColor, whiteKeyActiveColor).apply {
                spacing = 5.0
                alignment = Pos.CENTER_LEFT
            },
            Label("Key note name"),
            HBox(keyNameColor, keyNameVisible).apply {
                spacing = 5.0
                alignment = Pos.CENTER_LEFT
            },
            Label("Black key"),
            HBox(blackKeyColor, blackKeyActiveColor).apply {
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
            whiteKeyColor.valueProperty()
                .bindBidirectional(getop(be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.PIANO_WHITE_KEY_COLOR.name))
            whiteKeyActiveColor.valueProperty()
                .bindBidirectional(getop(be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.PIANO_WHITE_KEY_ACTIVE_COLOR.name))
            keyNameColor.valueProperty()
                .bindBidirectional(getop(be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.PIANO_WHITE_KEY_NAME_COLOR.name))
            keyNameVisible.selectedProperty()
                .bindBidirectional(getbp(be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.PIANO_WHITE_KEY_NAME_VISIBLE.name))
            blackKeyColor.valueProperty()
                .bindBidirectional(getop(be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.PIANO_BLACK_KEY_COLOR.name))
            blackKeyActiveColor.valueProperty()
                .bindBidirectional(getop(be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.PIANO_BLACK_KEY_ACTIVE_COLOR.name))
        }
    }
}