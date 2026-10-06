package be.codewriter.melodymatrix.view.view.piano.configurator

import atlantafx.base.controls.ToggleSwitch
import be.codewriter.melodymatrix.view.component.TickerSlider
import be.codewriter.melodymatrix.view.view.piano.data.PianoConfiguration
import javafx.geometry.HorizontalDirection
import javafx.scene.control.ColorPicker
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory

/**
 * Settings panel for configuring the particle cloud effect.
 *
 * @see PianoConfiguration
 */
class ParticlesConfigurator(config: PianoConfiguration) : BaseConfigurator() {

    init {
        val visible = ToggleSwitch().apply {
            textProperty().bind(visibleHiddenBinding(config.particlesEnabled))
            labelPosition = HorizontalDirection.RIGHT
            selectedProperty().bindBidirectional(config.particlesEnabled)
        }
        val numParticles = Spinner<Int>().apply {
            isEditable = true
            valueFactory = SpinnerValueFactory.IntegerSpinnerValueFactory(
                50, 1000, config.particlesParticleCount.get()
            ).also { it.valueProperty().bindBidirectional(config.particlesParticleCount.asObject()) }
        }
        val particleSize = TickerSlider().apply {
            min = 1.0
            max = 20.0
            valueProperty().bindBidirectional(config.particlesParticleSize)
        }
        val spreadRadius = TickerSlider().apply {
            min = 5.0
            max = 200.0
            valueProperty().bindBidirectional(config.particlesSpreadRadius)
        }
        val upSpeed = TickerSlider().apply {
            min = 20.0
            max = 300.0
            valueProperty().bindBidirectional(config.particlesUpSpeed)
        }
        val swirlSpeed = TickerSlider().apply {
            min = 0.0
            max = 150.0
            valueProperty().bindBidirectional(config.particlesSwirlSpeed)
        }
        val liftMultiplier = TickerSlider().apply {
            min = 0.6
            max = 1.8
            majorTickUnit = 0.2
            minorTickCount = 1
            valueProperty().bindBidirectional(config.particlesLiftMultiplier)
        }
        val randomColor = ToggleSwitch().apply {
            labelPosition = HorizontalDirection.RIGHT
            selectedProperty().bindBidirectional(config.particlesRandomColor)
        }
        val color = ColorPicker().apply {
            valueProperty().bindBidirectional(config.particlesColor)
            disableProperty().bind(randomColor.selectedProperty())
        }

        contentBox.children.addAll(
            visible,
            labeledControl("particlesConfig.num_particles", numParticles),
            labeledControl("particlesConfig.particle_size", particleSize),
            labeledControl("particlesConfig.spread_radius", spreadRadius),
            labeledControl("particlesConfig.up_speed", upSpeed),
            labeledControl("particlesConfig.swirl_speed", swirlSpeed),
            labeledControl("particlesConfig.lift_multiplier", liftMultiplier),
            labeledControl("particlesConfig.random_color", randomColor),
            labeledControl("particlesConfig.fixed_color", color)
        )
    }
}
