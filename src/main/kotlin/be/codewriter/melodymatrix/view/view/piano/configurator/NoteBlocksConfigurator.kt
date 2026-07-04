package be.codewriter.melodymatrix.view.view.piano.configurator

import atlantafx.base.controls.ToggleSwitch
import be.codewriter.melodymatrix.view.component.TickerSlider
import be.codewriter.melodymatrix.view.view.piano.data.NoteBlockColorMode
import be.codewriter.melodymatrix.view.view.piano.data.PianoConfiguration
import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.Property
import javafx.collections.FXCollections
import javafx.geometry.HorizontalDirection
import javafx.geometry.Pos
import javafx.scene.control.ColorPicker
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color

/**
 * Settings panel for the Synthesia-style note-block visualisation (falling & rising blocks).
 *
 * Scope note: this configurator is used only from `PianoWithEffectsView`; `PianoSimpleView`
 * has no animation canvas and therefore does not expose these settings.
 *
 * @param config Observable configuration to bind to
 * @see PianoConfiguration
 * @see be.codewriter.melodymatrix.view.view.piano.animation.NoteBlockConfig
 */
class NoteBlocksConfigurator(config: PianoConfiguration) : BaseConfigurator() {

    init {
        val fallingEnabled = ToggleSwitch().apply {
            textProperty().bind(selectedProperty().map { if (it) "Enabled" else "Disabled" })
            labelPosition = HorizontalDirection.RIGHT
            selectedProperty().bindBidirectional(config.fallingBlocksEnabled)
        }
        val risingEnabled = ToggleSwitch().apply {
            textProperty().bind(selectedProperty().map { if (it) "Enabled" else "Disabled" })
            labelPosition = HorizontalDirection.RIGHT
            selectedProperty().bindBidirectional(config.risingBlocksEnabled)
        }

        val lookAhead = TickerSlider().apply {
            min = 0.5
            max = 8.0
            majorTickUnit = 1.0
            minorTickCount = 1
            valueProperty().bindBidirectional(config.noteBlockLookAheadSeconds)
        }

        val colorMode = ComboBox<NoteBlockColorMode>(
            FXCollections.observableArrayList(NoteBlockColorMode.entries)
        ).apply {
            valueProperty().bindBidirectional(config.noteBlockColorMode)
        }

        val fixedColorPicker = labeledPicker("Fixed", config.noteBlockFixedColor)
        val velocityLowPicker = labeledPicker("Low velocity", config.noteBlockLowVelocityColor)
        val velocityHighPicker = labeledPicker("High velocity", config.noteBlockHighVelocityColor)
        val channelPaletteInfo = Label("Uses a fixed 16-colour palette (one per MIDI channel).").apply {
            style = "-fx-font-size: 11px; -fx-text-fill: -color-fg-muted;"
            isWrapText = true
        }

        // Show only the picker(s) relevant to the selected colour mode.
        bindVisibleAndManaged(fixedColorPicker, config.noteBlockColorMode, NoteBlockColorMode.FIXED)
        bindVisibleAndManaged(velocityLowPicker, config.noteBlockColorMode, NoteBlockColorMode.BY_VELOCITY)
        bindVisibleAndManaged(velocityHighPicker, config.noteBlockColorMode, NoteBlockColorMode.BY_VELOCITY)
        bindVisibleAndManaged(channelPaletteInfo, config.noteBlockColorMode, NoteBlockColorMode.BY_CHANNEL)

        val cornerRadius = TickerSlider().apply {
            min = 0.0
            max = 20.0
            majorTickUnit = 5.0
            minorTickCount = 4
            valueProperty().bindBidirectional(config.noteBlockCornerRadius)
        }
        val opacity = TickerSlider().apply {
            min = 0.3
            max = 1.0
            majorTickUnit = 0.1
            minorTickCount = 1
            valueProperty().bindBidirectional(config.noteBlockOpacity)
        }

        val outlineEnabled = ToggleSwitch().apply {
            textProperty().bind(selectedProperty().map { if (it) "Visible" else "Hidden" })
            labelPosition = HorizontalDirection.RIGHT
            selectedProperty().bindBidirectional(config.noteBlockOutlineEnabled)
        }
        val outlineColor = ColorPicker().apply {
            valueProperty().bindBidirectional(config.noteBlockOutlineColor)
            disableProperty().bind(outlineEnabled.selectedProperty().not())
        }
        val outlineWidth = TickerSlider().apply {
            min = 0.5
            max = 4.0
            majorTickUnit = 0.5
            minorTickCount = 1
            valueProperty().bindBidirectional(config.noteBlockOutlineWidth)
            disableProperty().bind(outlineEnabled.selectedProperty().not())
        }

        val colorPickerRow = HBox(5.0, fixedColorPicker, velocityLowPicker, velocityHighPicker, channelPaletteInfo)
            .apply { alignment = Pos.CENTER_LEFT }

        contentBox.children.addAll(
            labeledControl("Show falling blocks (playback)", fallingEnabled),
            labeledControl("Show rising blocks (input)", risingEnabled),
            labeledControl("Look-ahead / trail (seconds)", lookAhead),
            labeledControl("Colour mode", colorMode),
            labeledControl("Block colour", colorPickerRow),
            labeledControl("Corner radius", cornerRadius),
            labeledControl("Opacity", opacity),
            labeledControl("Outline", outlineEnabled),
            labeledControl("Outline colour", outlineColor),
            labeledControl("Outline width", outlineWidth)
        )
    }

    private fun labeledPicker(label: String, prop: Property<Color>) = VBox(2.0).apply {
        children.addAll(
            Label(label).apply { style = "-fx-font-size: 10px;" },
            ColorPicker().apply { valueProperty().bindBidirectional(prop) }
        )
    }

    private fun bindVisibleAndManaged(
        node: javafx.scene.Node,
        modeProperty: javafx.beans.property.ObjectProperty<NoteBlockColorMode>,
        showFor: NoteBlockColorMode
    ) {
        val binding: BooleanBinding = Bindings.createBooleanBinding(
            { modeProperty.value == showFor },
            modeProperty
        )
        node.visibleProperty().bind(binding)
        node.managedProperty().bind(binding)
    }
}
