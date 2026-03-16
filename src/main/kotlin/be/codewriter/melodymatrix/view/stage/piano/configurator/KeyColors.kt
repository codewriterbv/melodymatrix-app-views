package be.codewriter.melodymatrix.view.stage.piano.configurator

import atlantafx.base.controls.ToggleSwitch
import javafx.geometry.HorizontalDirection
import javafx.geometry.Pos
import javafx.scene.control.ColorPicker
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox

class KeyColors(val config: PianoConfiguration) : VBox() {

    init {
        val whiteKeyColor = ColorPicker().apply {
            valueProperty().bindBidirectional(config.pianoWhiteKeyColor)
        }
        val whiteKeyActiveColor = ColorPicker().apply {
            valueProperty().bindBidirectional(config.pianoWhiteKeyActiveColor)
        }
        val blackKeyColor = ColorPicker().apply {
            valueProperty().bindBidirectional(config.pianoBlackKeyColor)
        }
        val blackKeyActiveColor = ColorPicker().apply {
            valueProperty().bindBidirectional(config.pianoBlackKeyActiveColor)
        }
        val keyNameColor = ColorPicker().apply {
            valueProperty().bindBidirectional(config.pianoKeyNameColor)
        }
        val keyNameVisible = ToggleSwitch().apply {
            selectedProperty().bindBidirectional(config.pianoKeyNameVisible)
            textProperty().bind(
                selectedProperty()
                    .map { selected -> if (selected) "Visible" else "Hidden" }
            )
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
}