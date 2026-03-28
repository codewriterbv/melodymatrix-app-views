package be.codewriter.melodymatrix.view.view.piano.configurator

import atlantafx.base.controls.ToggleSwitch
import be.codewriter.melodymatrix.view.component.TickerSlider
import be.codewriter.melodymatrix.view.view.piano.data.PianoConfiguration
import javafx.geometry.HorizontalDirection
import javafx.scene.control.ColorPicker

/**
 * Settings panel for configuring piano key colours.
 *
 * Provides colour pickers for white key normal/active colours, black key normal/active colours,
 * and a toggle to show or hide the note-name label on each key.
 * All controls are bidirectionally bound to the corresponding [PianoConfiguration] properties.
 *
 * @param config Observable configuration to bind to
 * @see PianoConfiguration
 */
class KeyConfigurator(config: PianoConfiguration) : BaseConfigurator() {

    init {
        val whiteNormalColor = ColorPicker().apply {
            valueProperty().bindBidirectional(config.pianoWhiteKeyColor)
        }
        val whiteActiveColor = ColorPicker().apply {
            valueProperty().bindBidirectional(config.pianoWhiteKeyActiveColor)
        }
        val whiteDepth = TickerSlider().apply {
            min = 0.0
            max = 2.0
            majorTickUnit = 0.5
            minorTickCount = 4
            valueProperty().bindBidirectional(config.pianoWhiteKeyDepth)
        }

        val blackNormalColor = ColorPicker().apply {
            valueProperty().bindBidirectional(config.pianoBlackKeyColor)
        }
        val blackActiveColor = ColorPicker().apply {
            valueProperty().bindBidirectional(config.pianoBlackKeyActiveColor)
        }
        val blackDepth = TickerSlider().apply {
            min = 0.0
            max = 2.0
            majorTickUnit = 0.5
            minorTickCount = 4
            valueProperty().bindBidirectional(config.pianoBlackKeyDepth)
        }

        val noteNameColor = ColorPicker().apply {
            valueProperty().bindBidirectional(config.pianoKeyNameColor)
            disableProperty().bind(config.pianoKeyNameVisible.not())
        }
        val noteNameFontSize = TickerSlider().apply {
            min = 6.0
            max = 18.0
            majorTickUnit = 2.0
            minorTickCount = 1
            adaptiveTicks = false
            valueProperty().bindBidirectional(config.pianoKeyNameFontSize)
            disableProperty().bind(config.pianoKeyNameVisible.not())
        }
        val noteNameVisible = ToggleSwitch().apply {
            selectedProperty().bindBidirectional(config.pianoKeyNameVisible)
            textProperty().bind(
                selectedProperty().map { if (it) "Visible" else "Hidden" }
            )
            labelPosition = HorizontalDirection.RIGHT
        }

        contentBox.children.addAll(
            sectionTitle("White key"),
            twoLabeledControls("Normal", whiteNormalColor, "Active", whiteActiveColor),
            labeledControl("Depth", whiteDepth),
            sectionTitle("Black key"),
            twoLabeledControls("Normal", blackNormalColor, "Active", blackActiveColor),
            labeledControl("Depth", blackDepth),
            sectionTitle("Key note name"),
            labeledControl("Visible", noteNameVisible),
            labeledControl("Color", noteNameColor),
            labeledControl("Font size", noteNameFontSize)
        )
    }
}