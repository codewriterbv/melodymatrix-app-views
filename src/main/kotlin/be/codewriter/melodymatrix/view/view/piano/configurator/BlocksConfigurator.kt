package be.codewriter.melodymatrix.view.view.piano.configurator

import atlantafx.base.controls.ToggleSwitch
import be.codewriter.melodymatrix.view.component.TickerSlider
import be.codewriter.melodymatrix.view.i18n.I18n
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
 * @see PianoConfiguration
 */
class BlocksConfigurator(config: PianoConfiguration) : BaseConfigurator() {

    init {
        val blocksEnabled = ToggleSwitch().apply {
            textProperty().bind(enabledDisabledBinding(config.noteBlocksEnabled))
            labelPosition = HorizontalDirection.RIGHT
            selectedProperty().bindBidirectional(config.noteBlocksEnabled)
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

        val fixedColorPicker = labeledPicker("blocksConfig.fixed", config.noteBlockFixedColor)
        val velocityLowPicker = labeledPicker("blocksConfig.low_velocity", config.noteBlockLowVelocityColor)
        val velocityHighPicker = labeledPicker("blocksConfig.high_velocity", config.noteBlockHighVelocityColor)
        val channelPaletteInfo = Label().apply {
            textProperty().bind(I18n.binding(pianoBundle, "blocksConfig.channel_palette_info"))
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
            textProperty().bind(visibleHiddenBinding(config.noteBlockOutlineEnabled))
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
            labeledControl("blocksConfig.falling_playback", blocksEnabled),
            labeledControl("blocksConfig.look_ahead", lookAhead),
            labeledControl("blocksConfig.color_mode", colorMode),
            labeledControl("blocksConfig.block_color", colorPickerRow),
            labeledControl("blocksConfig.corner_radius", cornerRadius),
            labeledControl("blocksConfig.opacity", opacity),
            labeledControl("blocksConfig.outline", outlineEnabled),
            labeledControl("blocksConfig.outline_color", outlineColor),
            labeledControl("blocksConfig.outline_width", outlineWidth)
        )
    }

    private fun labeledPicker(labelKey: String, prop: Property<Color>) = VBox(2.0).apply {
        children.addAll(
            Label().apply {
                textProperty().bind(I18n.binding(pianoBundle, labelKey))
                style = "-fx-font-size: 10px;"
            },
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
