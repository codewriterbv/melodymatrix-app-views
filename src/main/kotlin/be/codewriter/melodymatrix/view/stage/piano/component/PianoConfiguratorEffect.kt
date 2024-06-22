package be.codewriter.melodymatrix.view.stage.piano.component

import atlantafx.base.controls.ToggleSwitch
import com.almasb.fxgl.dsl.FXGL.Companion.getdp
import com.almasb.fxgl.dsl.FXGL.Companion.getop
import com.almasb.fxgl.dsl.getbp
import javafx.application.Platform
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
            min = 1.0
            max = 20.0
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

    /**
     * FXGL properties can only be used after FXGL has started.
     * So in the PianoGenerator/GameApplication class a callback is done in the initGame method to this method.
     */
    fun createBindings() {
        Platform.runLater {
            explosionVisible.selectedProperty()
                .bindBidirectional(getbp(PianoGenerator.PianoProperty.EXPLOSION_ENABLED.name))
            colorEnd.valueProperty()
                .bindBidirectional(getop<Color>(PianoGenerator.PianoProperty.EXPLOSION_COLOR_END.name))
            colorStart.valueProperty()
                .bindBidirectional(getop<Color>(PianoGenerator.PianoProperty.EXPLOSION_COLOR_START.name))
            explosionRadius.valueProperty()
                .bindBidirectional(getdp(PianoGenerator.PianoProperty.EXPLOSION_RADIUS.name))
            blendModeCombobox.valueProperty()
                .bindBidirectional(getop<BlendMode>(PianoGenerator.PianoProperty.EXPLOSION_BLENDMODE.name))
            numParticles.valueProperty()
                .bindBidirectional(getdp(PianoGenerator.PianoProperty.EXPLOSION_NUMBER_OF_PARTICLES.name))
            particleSize.valueProperty()
                .bindBidirectional(getdp(PianoGenerator.PianoProperty.EXPLOSION_PARTICLE_SIZE.name))
        }
    }
}