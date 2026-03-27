package be.codewriter.melodymatrix.view.view.piano.configurator

import atlantafx.base.controls.ToggleSwitch
import be.codewriter.melodymatrix.view.component.TickerSlider
import be.codewriter.melodymatrix.view.view.piano.data.PianoConfiguration
import javafx.geometry.HorizontalDirection
import javafx.scene.control.ColorPicker

/**
 * Settings panel for configuring the explosion particle effect.
 *
 * Provides controls to enable/disable the explosion, choose the colour (or random palette),
 * and tune the explosion radius, particle count, and particle size.
 * All controls are bidirectionally bound to [PianoConfiguration] explosion properties.
 *
 * @param config Observable configuration to bind to
 * @see PianoConfiguration
 */
class ExplosionConfigurator(config: PianoConfiguration) : BaseConfigurator() {

    init {
        val explosionVisible = ToggleSwitch().apply {
            textProperty().bind(selectedProperty().map { selected -> if (selected) "Visible" else "Hidden" })
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
            labeledControl("Explosion Radius", explosionRadius),
            labeledControl("Number of Particles", numParticles),
            labeledControl("Number of Tail Particles", tailNumParticles),
            labeledControl("Particle Size", particleSize),
            labeledControl("Vertical Lift", liftMultiplier),
            labeledControl("Random Color", randomColor),
            labeledControl("Fixed Color", color)
        )
    }
}