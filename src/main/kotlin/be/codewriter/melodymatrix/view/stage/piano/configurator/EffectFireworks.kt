package be.codewriter.melodymatrix.view.stage.piano.configurator

import atlantafx.base.controls.ToggleSwitch
import be.codewriter.melodymatrix.view.stage.piano.data.FireworksExplosionType
import be.codewriter.melodymatrix.view.stage.piano.data.PianoConfiguration
import javafx.collections.FXCollections
import javafx.geometry.HorizontalDirection
import javafx.scene.control.ColorPicker
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.Slider
import javafx.scene.layout.VBox

class EffectFireworks(config: PianoConfiguration) : VBox() {

    init {
        val fireworksVisible = ToggleSwitch().apply {
            textProperty().bind(selectedProperty().map { selected -> if (selected) "Visible" else "Hidden" })
            labelPosition = HorizontalDirection.RIGHT
            selectedProperty().bindBidirectional(config.fireworksEnabled)
        }
        val fireworksRadius = Slider().apply {
            min = 2.0
            max = 50.0
            valueProperty().bindBidirectional(config.fireworksRadius)
        }
        val numParticles = Slider().apply {
            min = 80.0
            max = 150.0
            valueProperty().bindBidirectional(config.fireworksNumberOfParticles)
        }
        val particleSize = Slider().apply {
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
        }
        val tailNumParticles = Slider().apply {
            min = 0.9
            max = 3.0
            valueProperty().bindBidirectional(config.fireworksTailNumberOfParticles)
        }
        val launchHeightMultiplier = Slider().apply {
            min = 0.6
            max = 2.4
            valueProperty().bindBidirectional(config.fireworksLaunchHeightMultiplier)
        }
        val explosionType = ComboBox<FireworksExplosionType>(
            FXCollections.observableArrayList(FireworksExplosionType.values().toList())
        ).apply {
            value = config.fireworksExplosionType.value
            valueProperty().addListener { _, _, newValue ->
                if (newValue != null) {
                    config.fireworksExplosionType.value = newValue
                }
            }
            config.fireworksExplosionType.addListener { _, _, newValue ->
                if (newValue != null && value != newValue) {
                    value = newValue
                }
            }
        }

        children.addAll(
            Label("Show Fireworks"), fireworksVisible,
            Label("fireworks Radius"), fireworksRadius,
            Label("Number of Particles"), numParticles,
            Label("Number of Tail Particles"), tailNumParticles,
            Label("Launch Height"), launchHeightMultiplier,
            Label("Explosion Type"), explosionType,
            Label("Particle Size"), particleSize,
            Label("Random Color"), randomColor,
            Label("Fixed Color"), color
        )

        spacing = 5.0
    }
}