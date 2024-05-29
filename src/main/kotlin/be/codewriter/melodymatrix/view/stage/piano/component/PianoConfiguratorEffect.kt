package be.codewriter.melodymatrix.view.stage.piano.component

import atlantafx.base.controls.ToggleSwitch
import be.codewriter.melodymatrix.view.stage.piano.data.PianoSettingsEffect
import javafx.geometry.HorizontalDirection
import javafx.scene.control.ColorPicker
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.Slider
import javafx.scene.effect.BlendMode
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color

class PianoConfiguratorEffect : VBox() {
    companion object {
        private val explosionVisible = ToggleSwitch()
        private val blendModeCombobox = ComboBox<BlendMode>()
        private val explosionRadius = Slider()
        private val numParticles = Slider()
        private val particleSize = Slider()
        private val colorStart = ColorPicker()
        private val colorEnd = ColorPicker()
    }

    init {
        explosionVisible.apply {
            isSelected = true
            textProperty().bind(selectedProperty().map { selected -> if (selected) "Visible" else "Hidden" })
            labelPosition = HorizontalDirection.RIGHT
        }
        blendModeCombobox.apply {
            items.addAll(BlendMode.entries)
            value = BlendMode.ADD
        }
        explosionRadius.apply {
            min = 1.0
            max = 400.0
        }
        numParticles.apply {
            min = 10.0
            max = 100.0
        }
        particleSize.apply {
            min = 1.0
            max = 10.0
        }
        colorStart.value = Color.YELLOW
        colorEnd.value = Color.DARKRED
        children.addAll(
            Label("Show explosion"),
            explosionVisible,
            Label("Blend mode"),
            blendModeCombobox,
            Label("Explosion radius"),
            explosionRadius,
            Label("Number of particles"),
            numParticles,
            Label("Particle size"),
            particleSize,
            Label("Colors"),
            HBox(colorStart, colorEnd)
        )
        spacing = 5.0
    }

    fun getPianoEffectSettings(): PianoSettingsEffect {
        return PianoSettingsEffect(
            explosionVisible.isSelected,
            blendModeCombobox.value,
            explosionRadius.value.toInt(),
            numParticles.value.toInt(),
            particleSize.value,
            colorStart.value,
            colorEnd.value
        )
    }
}