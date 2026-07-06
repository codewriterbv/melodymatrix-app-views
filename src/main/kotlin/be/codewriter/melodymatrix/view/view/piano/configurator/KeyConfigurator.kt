package be.codewriter.melodymatrix.view.view.piano.configurator

import be.codewriter.melodymatrix.view.component.TickerSlider
import be.codewriter.melodymatrix.view.view.piano.data.PianoConfiguration
import javafx.geometry.HorizontalDirection
import javafx.scene.control.ColorPicker
import javafx.scene.layout.HBox

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
        val noteNameVisible = stableToggle(visibleHiddenBinding(config.pianoKeyNameVisible)).apply {
            selectedProperty().bindBidirectional(config.pianoKeyNameVisible)
            labelPosition = HorizontalDirection.RIGHT
        }
        val solfegeVisible = stableToggle(visibleHiddenBinding(config.pianoKeySolfegeVisible)).apply {
            selectedProperty().bindBidirectional(config.pianoKeySolfegeVisible)
            labelPosition = HorizontalDirection.RIGHT
        }

        contentBox.children.addAll(
            sectionTitle("keyConfig.section.white_key"),
            twoLabeledControls("keyConfig.normal", whiteNormalColor, "keyConfig.active", whiteActiveColor),
            labeledControl("keyConfig.depth", whiteDepth),
            sectionTitle("keyConfig.section.black_key"),
            twoLabeledControls("keyConfig.normal", blackNormalColor, "keyConfig.active", blackActiveColor),
            labeledControl("keyConfig.depth", blackDepth),
            sectionTitle("keyConfig.section.note_names"),
            HBox(10.0).apply {
                children.addAll(
                    labeledControl("keyConfig.style_letters", noteNameVisible),
                    labeledControl("keyConfig.style_solfege", solfegeVisible)
                )
            },
            labeledControl("keyConfig.color", noteNameColor),
            labeledControl("keyConfig.font_size", noteNameFontSize)
        )
    }
}
