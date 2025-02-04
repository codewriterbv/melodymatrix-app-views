package be.codewriter.melodymatrix.view.stage.piano.component

import atlantafx.base.controls.ToggleSwitch
import com.almasb.fxgl.dsl.FXGL.Companion.getdp
import com.almasb.fxgl.dsl.FXGL.Companion.getop
import com.almasb.fxgl.dsl.getbp
import javafx.application.Platform
import javafx.geometry.HorizontalDirection
import javafx.geometry.Pos
import javafx.scene.control.ColorPicker
import javafx.scene.control.Label
import javafx.scene.control.Slider
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color

class ConfiguratorEffectParticle : VBox() {
    companion object {
        private val explosionVisible = ToggleSwitch()
        private val explosionType = ToggleSwitch()
        private val explosionRadius = Slider()
        private val numParticles = Slider()
        private val particleSize = Slider()
        private val colorStart = ColorPicker()
        private val colorEnd = ColorPicker()
        private val tailNumParticles = Slider()
    }

    init {
        explosionVisible.apply {
            isSelected = true
            textProperty().bind(selectedProperty().map { selected -> if (selected) "Visible" else "Hidden" })
            labelPosition = HorizontalDirection.RIGHT
        }
        explosionType.apply {
            isSelected = true
            textProperty().bind(selectedProperty().map { selected -> if (selected) "Fireworks" else "Splash" })
            labelPosition = HorizontalDirection.RIGHT
        }
        explosionRadius.apply {
            min = 50.0 //1.0
            max = 400.0
        }
        numParticles.apply {
            min = 20.0 //10.0
            max = 50.0
        }
        particleSize.apply {
            min = 0.25 //1.0
            max = 5.0 //10.0
        }
        tailNumParticles.apply {
            min = 0.0
            max = 20.0
        }
        colorStart.value = Color.YELLOW
        colorEnd.value = Color.DARKRED
        children.addAll(
            Label("Show Explosion"),
            explosionVisible,
            Label("Animation Type"),
            explosionType,
            Label("Explosion Radius"),
            explosionRadius,
            Label("Number of Particles"),
            numParticles,
            Label("Particle Size"),
            particleSize,
            Label("Number of Tail Particles"),
            tailNumParticles,
            Label("Colors"),
            HBox(colorStart, colorEnd).apply {
                spacing = 5.0
                alignment = Pos.CENTER_LEFT
            }
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
                .bindBidirectional(getbp(PianoProperty.EXPLOSION_ENABLED.name))
            colorEnd.valueProperty()
                .bindBidirectional(getop<Color>(PianoProperty.EXPLOSION_COLOR_END.name))
            colorStart.valueProperty()
                .bindBidirectional(getop<Color>(PianoProperty.EXPLOSION_COLOR_START.name))
            explosionRadius.valueProperty()
                .bindBidirectional(getdp(PianoProperty.EXPLOSION_RADIUS.name))
            numParticles.valueProperty()
                .bindBidirectional(getdp(PianoProperty.EXPLOSION_NUMBER_OF_PARTICLES.name))
            particleSize.valueProperty()
                .bindBidirectional(getdp(PianoProperty.EXPLOSION_PARTICLE_SIZE.name))
            tailNumParticles.valueProperty()
                .bindBidirectional(getdp(PianoProperty.EXPLOSION_TAIL_NUMBER_OF_ARTICLES.name))
        }
    }
}