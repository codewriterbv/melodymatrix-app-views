package be.codewriter.melodymatrix.view.stage.piano.configurator

import atlantafx.base.controls.ToggleSwitch
import be.codewriter.melodymatrix.view.stage.piano.data.PianoConfiguration
import javafx.geometry.HorizontalDirection
import javafx.scene.Node
import javafx.scene.control.ColorPicker
import javafx.scene.control.Label
import javafx.scene.control.Slider
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory
import javafx.scene.layout.VBox

class EffectExplosion(config: PianoConfiguration) : VBox() {

    init {
        spacing = 5.0

        val explosionVisible = ToggleSwitch().apply {
            textProperty().bind(selectedProperty().map { selected -> if (selected) "Visible" else "Hidden" })
            labelPosition = HorizontalDirection.RIGHT
            selectedProperty().bindBidirectional(config.explosionEnabled)
        }
        val explosionRadius = Slider().apply {
            min = 2.0
            max = 9.5
            valueProperty().bindBidirectional(config.explosionRadius)
        }
        val numParticles = Slider().apply {
            min = 80.0
            max = 150.0
            isShowTickMarks = true
            isShowTickLabels = true
            majorTickUnit = 10.0
            minorTickCount = 9
            isSnapToTicks = true
            valueProperty().bindBidirectional(config.explosionNumberOfParticles)
        }
        val particleSize = Slider().apply {
            min = 0.9
            max = 3.0
            valueProperty().bindBidirectional(config.explosionParticleSize)
        }
        val randomColor = ToggleSwitch().apply {
            labelPosition = HorizontalDirection.RIGHT
            selectedProperty().bindBidirectional(config.explosionRandomColor)
        }
        val color = ColorPicker().apply {
            valueProperty().bindBidirectional(config.explosionColor)
            disableProperty().bind(randomColor.selectedProperty())
        }
        val tailNumParticles = Spinner<Int>().apply {
            isEditable = true
            valueFactory = SpinnerValueFactory.IntegerSpinnerValueFactory(
                0,
                100,
                config.explosionTailNumberOfParticles.get()
            ).also { factory ->
                factory.valueProperty().bindBidirectional(config.explosionTailNumberOfParticles.asObject())
            }
        }

        children.addAll(
            labeledControl("Show Explosion", explosionVisible),
            labeledControl("Explosion Radius", explosionRadius),
            labeledControl("Number of Particles", numParticles),
            labeledControl("Number of Tail Particles", tailNumParticles),
            labeledControl("Particle Size", particleSize),
            labeledControl("Random Color", randomColor),
            labeledControl("Fixed Color", color)
        )
    }

    private fun labeledControl(title: String, control: Node) = VBox(2.0).apply {
        children.addAll(
            Label(title),
            control
        )
    }
}