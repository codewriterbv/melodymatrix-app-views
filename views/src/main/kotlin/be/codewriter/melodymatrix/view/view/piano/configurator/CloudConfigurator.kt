package be.codewriter.melodymatrix.view.view.piano.configurator

import atlantafx.base.controls.ToggleSwitch
import be.codewriter.melodymatrix.view.component.TickerSlider
import be.codewriter.melodymatrix.view.i18n.I18n
import be.codewriter.melodymatrix.view.view.piano.data.PianoConfiguration
import javafx.beans.property.Property
import javafx.geometry.HorizontalDirection
import javafx.geometry.Pos
import javafx.scene.control.ColorPicker
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color

/**
 * Settings panel for configuring the above-key smoke/steam particle effect.
 *
 * @see PianoConfiguration
 */
class CloudConfigurator(config: PianoConfiguration) : BaseConfigurator() {

    init {
        val cloudVisible = ToggleSwitch().apply {
            textProperty().bind(visibleHiddenBinding(config.cloudEnabled))
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
            labeledControl(
                "cloudConfig.smoke_color", HBox(
                    5.0,
                    labeledPicker("cloudConfig.start", config.cloudColorStart),
                    labeledPicker("cloudConfig.end", config.cloudColorEnd)
                ).apply { alignment = Pos.CENTER_LEFT }
            ),
            labeledControl("cloudConfig.particle_count", particleCount),
            labeledControl("cloudConfig.cloud_size", particleSize),
            labeledControl("cloudConfig.drift_speed", driftSpeed),
            labeledControl("cloudConfig.wobble_amplitude", wobbleAmplitude),
            labeledControl("cloudConfig.opacity", opacity),
            labeledControl("cloudConfig.spawn_radius", spawnRadius)
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
}
