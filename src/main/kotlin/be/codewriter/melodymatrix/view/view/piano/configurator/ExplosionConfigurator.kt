package be.codewriter.melodymatrix.view.view.piano.configurator

import atlantafx.base.controls.ToggleSwitch
import be.codewriter.melodymatrix.view.component.TickerSlider
import be.codewriter.melodymatrix.view.view.piano.data.PianoConfiguration
import javafx.geometry.HorizontalDirection
import javafx.scene.control.ColorPicker

/**
 * Settings panel for configuring the explosion particle effect.
 *
 * @see PianoConfiguration
 */
class ExplosionConfigurator(config: PianoConfiguration) : BaseConfigurator() {

    init {
        val explosionVisible = ToggleSwitch().apply {
            textProperty().bind(visibleHiddenBinding(config.explosionEnabled))
            labelPosition = HorizontalDirection.RIGHT
            selectedProperty().bindBidirectional(config.explosionEnabled)
        }
        val explosionRadius = TickerSlider().apply {
            min = 1.0
            max = 16.0
            valueProperty().bindBidirectional(config.explosionRadius)
        }
        val numParticles = TickerSlider().apply {
            min = 80.0
            max = 150.0
            valueProperty().bindBidirectional(config.explosionNumberOfParticles)
        }
        val particleSize = TickerSlider().apply {
            min = 0.5
            max = 20.0
            valueProperty().bindBidirectional(config.explosionParticleSize)
        }
        val liftMultiplier = TickerSlider().apply {
            min = 0.6
            max = 1.8
            majorTickUnit = 0.2
            minorTickCount = 1
            valueProperty().bindBidirectional(config.explosionLiftMultiplier)
        }
        val tailNumParticles = TickerSlider().apply {
            min = 0.0
            max = 300.0
            majorTickUnit = 50.0
            minorTickCount = 4
            valueProperty().bindBidirectional(config.explosionTailNumberOfParticles)
        }
        val randomColor = ToggleSwitch().apply {
            labelPosition = HorizontalDirection.RIGHT
            selectedProperty().bindBidirectional(config.explosionRandomColor)
        }
        val color = ColorPicker().apply {
            valueProperty().bindBidirectional(config.explosionColor)
            disableProperty().bind(randomColor.selectedProperty())
        }

        contentBox.children.addAll(
            explosionVisible,
            labeledControl("explosionConfig.radius", explosionRadius),
            labeledControl("explosionConfig.num_particles", numParticles),
            labeledControl("explosionConfig.num_tail_particles", tailNumParticles),
            labeledControl("explosionConfig.particle_size", particleSize),
            labeledControl("explosionConfig.vertical_lift", liftMultiplier),
            labeledControl("explosionConfig.random_color", randomColor),
            labeledControl("explosionConfig.fixed_color", color)
        )
    }
}
