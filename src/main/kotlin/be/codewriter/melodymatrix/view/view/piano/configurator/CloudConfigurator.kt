package be.codewriter.melodymatrix.view.view.piano.configurator

import atlantafx.base.controls.ToggleSwitch
import be.codewriter.melodymatrix.view.component.TickerSlider
import be.codewriter.melodymatrix.view.view.piano.data.PianoConfiguration
import javafx.geometry.HorizontalDirection
import javafx.geometry.Pos
import javafx.scene.control.ColorPicker
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.beans.property.Property
import javafx.scene.paint.Color

/**
 * Settings panel for configuring the above-key smoke/steam particle effect.
 *
 * Provides a toggle, colour pickers, and sliders for all [CloudGenerator] parameters.
 * All controls are bidirectionally bound to [PianoConfiguration] cloud properties.
 *
 * @param config Observable configuration to bind to
 * @see PianoConfiguration
 * @see CloudGenerator
 */
class CloudConfigurator(config: PianoConfiguration) : BaseConfigurator() {

    init {
        val cloudVisible = ToggleSwitch().apply {
            textProperty().bind(selectedProperty().map { selected -> if (selected) "Visible" else "Hidden" })
            labelPosition = HorizontalDirection.RIGHT
            selectedProperty().bindBidirectional(config.cloudEnabled)
        }
        val particleCount = TickerSlider().apply {
            min = 8.0
            max = 80.0
            majorTickUnit = 8.0
            minorTickCount = 1
            valueProperty().bindBidirectional(config.cloudParticleCount)
        }
        val particleSize = TickerSlider().apply {
            min = 40.0
            max = 320.0
            majorTickUnit = 40.0
            minorTickCount = 3
            valueProperty().bindBidirectional(config.cloudParticleSize)
        }
        val driftSpeed = TickerSlider().apply {
            min = 0.0
            max = 80.0
            majorTickUnit = 20.0
            minorTickCount = 3
            valueProperty().bindBidirectional(config.cloudDriftSpeed)
        }
        val wobbleAmplitude = TickerSlider().apply {
            min = 0.0
            max = 50.0
            majorTickUnit = 10.0
            minorTickCount = 1
            valueProperty().bindBidirectional(config.cloudWobbleAmplitude)
        }
        val opacity = TickerSlider().apply {
            min = 0.02
            max = 0.6
            majorTickUnit = 0.1
            minorTickCount = 1
            valueProperty().bindBidirectional(config.cloudOpacity)
        }
        val spawnRadius = TickerSlider().apply {
            min = 40.0
            max = 400.0
            majorTickUnit = 80.0
            minorTickCount = 3
            valueProperty().bindBidirectional(config.cloudSpawnRadius)
        }

        contentBox.children.addAll(
            cloudVisible,
            labeledControl("Smoke Color", HBox(5.0,
                labeledPicker("Start", config.cloudColorStart),
                labeledPicker("End", config.cloudColorEnd)
            ).apply { alignment = Pos.CENTER_LEFT }),
            labeledControl("Particle Count", particleCount),
            labeledControl("Cloud Size", particleSize),
            labeledControl("Drift Speed", driftSpeed),
            labeledControl("Wobble Amplitude", wobbleAmplitude),
            labeledControl("Opacity", opacity),
            labeledControl("Spawn Radius (on key press)", spawnRadius)
        )
    }

    private fun labeledPicker(label: String, prop: Property<Color>) = VBox(2.0).apply {
        children.addAll(
            Label(label).apply { style = "-fx-font-size: 10px;" },
            ColorPicker().apply { valueProperty().bindBidirectional(prop) }
        )
    }
}

