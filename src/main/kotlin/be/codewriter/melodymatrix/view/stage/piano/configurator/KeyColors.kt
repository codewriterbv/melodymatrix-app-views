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

/**
 * Settings panel for configuring piano key colours.
 *
 * Provides colour pickers for white key normal/active colours, black key normal/active colours,
 * and a toggle to show or hide the note-name label on each key.
 * All pickers are bidirectionally bound to the corresponding [PianoConfiguration] properties.
 *
 * @param config Observable configuration to bind to
 * @see PianoStage
 * @see PianoConfiguration
 */
class KeyColors(config: PianoConfiguration) : VBox() {

    init {
        spacing = 5.0

        children.addAll(
            sectionLabel("White key"),
            colorRow(config.pianoWhiteKeyColor, config.pianoWhiteKeyActiveColor),
            sectionLabel("Black key"),
            colorRow(config.pianoBlackKeyColor, config.pianoBlackKeyActiveColor),
            sectionLabel("Key note name"),
            HBox(
                5.0,
                labeledPicker("Color", config.pianoKeyNameColor),
                ToggleSwitch().apply {
                    selectedProperty().bindBidirectional(config.pianoKeyNameVisible)
                    textProperty().bind(
                        selectedProperty().map { if (it) "Visible" else "Hidden" }
                    )
                    labelPosition = HorizontalDirection.RIGHT
                }
            ).apply { alignment = Pos.CENTER_LEFT }
        )
    }

    /** Bold label used as a section heading. */
    private fun sectionLabel(text: String) = Label(text).apply {
        style = "-fx-font-weight: bold;"
    }

    /**
     * A row with two labelled [ColorPicker]s — one for the resting colour and one for the
     * colour shown while a note is actively being played.
     */
    private fun colorRow(normalProp: Property<Color>, activeProp: Property<Color>) =
        HBox(
            5.0,
            labeledPicker("Normal", normalProp),
            labeledPicker("Active", activeProp)
        ).apply { alignment = Pos.CENTER_LEFT }

    /** A [ColorPicker] with a small caption label stacked above it. */
    private fun labeledPicker(label: String, prop: Property<Color>) =
        VBox(
            2.0,
            Label(label).apply { style = "-fx-font-size: 10px;" },
            ColorPicker().apply { valueProperty().bindBidirectional(prop) }
        )
}