package be.codewriter.melodymatrix.view.view.piano.configurator

import atlantafx.base.controls.ToggleSwitch
import be.codewriter.melodymatrix.view.component.TickerSlider
import be.codewriter.melodymatrix.view.view.piano.data.FireworksExplosionType
import be.codewriter.melodymatrix.view.view.piano.data.PianoConfiguration
import javafx.collections.FXCollections
import javafx.geometry.HorizontalDirection
import javafx.scene.control.ColorPicker
import javafx.scene.control.ComboBox
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory

/**
 * Settings panel for configuring the fireworks particle effect.
 *
 * Provides controls to enable/disable the fireworks, choose the explosion type, colour
 * (or random palette), burst radius, particle count, particle size, tail particle count,
 * and launch height multiplier.
 * All controls are bidirectionally bound to [PianoConfiguration] fireworks properties.
 *
 * @param config Observable configuration to bind to
 * @see PianoConfiguration
 * @see FireworksExplosionType
 */
class FireworksConfigurator(config: PianoConfiguration) : BaseConfigurator() {

    init {
        val fireworksVisible = ToggleSwitch().apply {
            textProperty().bind(selectedProperty().map { selected -> if (selected) "Visible" else "Hidden" })
            labelPosition = HorizontalDirection.RIGHT
            selectedProperty().bindBidirectional(config.fireworksEnabled)
        }
        val fireworksRadius = TickerSlider().apply {
            min = 2.0
            max = 50.0
            valueProperty().bindBidirectional(config.fireworksRadius)
        }
        val numParticles = TickerSlider().apply {
            min = 80.0
            max = 150.0
            valueProperty().bindBidirectional(config.fireworksNumberOfParticles)
        }
        val particleSize = TickerSlider().apply {
            min = 0.9
            max = 3.0
            valueProperty().bindBidirectional(config.fireworksParticleSize)
        }
        val randomColor = ToggleSwitch().apply {
            labelPosition = HorizontalDirection.RIGHT
            selectedProperty().bindBidirectional(config.fireworksRandomColor)
        }
        val color = ColorPicker().apply {
            valueProperty().bindBidirectional(config.fireworksColor)
            disableProperty().bind(randomColor.selectedProperty())
        }
        val tailNumParticles = Spinner<Int>().apply {
            isEditable = true
            valueFactory = SpinnerValueFactory.IntegerSpinnerValueFactory(
                0,
                100,
                config.fireworksTailNumberOfParticles.get()
            ).also { factory ->
                factory.valueProperty().bindBidirectional(config.fireworksTailNumberOfParticles.asObject())
            }
        }
        val launchHeightMultiplier = TickerSlider().apply {
            min = 0.6
            max = 2.4
            valueProperty().bindBidirectional(config.fireworksLaunchHeightMultiplier)
        }
        val liftMultiplier = TickerSlider().apply {
            min = 0.6
            max = 1.8
            majorTickUnit = 0.2
            minorTickCount = 1
            valueProperty().bindBidirectional(config.fireworksLiftMultiplier)
        }
        val explosionType = ComboBox<FireworksExplosionType>(
            FXCollections.observableArrayList(FireworksExplosionType.entries)
        ).apply {
            valueProperty().bindBidirectional(config.fireworksExplosionType)
        }

        contentBox.children.addAll(
            fireworksVisible,
            labeledControl("Fireworks Radius", fireworksRadius),
            labeledControl("Number of Particles", numParticles),
            labeledControl("Number of Tail Particles", tailNumParticles),
            labeledControl("Launch Height", launchHeightMultiplier),
            labeledControl("Vertical Lift", liftMultiplier),
            labeledControl("Explosion Type", explosionType),
            labeledControl("Particle Size", particleSize),
            labeledControl("Random Color", randomColor),
            labeledControl("Fixed Color", color)
        )
    }
}