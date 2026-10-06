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
 * @see PianoConfiguration
 * @see FireworksExplosionType
 */
class FireworksConfigurator(config: PianoConfiguration) : BaseConfigurator() {

    init {
        val fireworksVisible = ToggleSwitch().apply {
            textProperty().bind(visibleHiddenBinding(config.fireworksEnabled))
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
            labeledControl("fireworksConfig.radius", fireworksRadius),
            labeledControl("fireworksConfig.num_particles", numParticles),
            labeledControl("fireworksConfig.num_tail_particles", tailNumParticles),
            labeledControl("fireworksConfig.launch_height", launchHeightMultiplier),
            labeledControl("fireworksConfig.vertical_lift", liftMultiplier),
            labeledControl("fireworksConfig.explosion_type", explosionType),
            labeledControl("fireworksConfig.particle_size", particleSize),
            labeledControl("fireworksConfig.random_color", randomColor),
            labeledControl("fireworksConfig.fixed_color", color)
        )
    }
}
