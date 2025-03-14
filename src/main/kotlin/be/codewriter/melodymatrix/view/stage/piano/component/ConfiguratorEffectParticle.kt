package be.codewriter.melodymatrix.view.stage.piano.component

import atlantafx.base.controls.ToggleSwitch
import com.almasb.fxgl.core.math.FXGLMath
import com.almasb.fxgl.dsl.FXGL.Companion.getb
import com.almasb.fxgl.dsl.FXGL.Companion.getdp
import com.almasb.fxgl.dsl.FXGL.Companion.getop
import com.almasb.fxgl.dsl.getbp
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.HorizontalDirection
import javafx.geometry.Pos
import javafx.scene.Node
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
        private val colorMode = ToggleSwitch()
        private val color = ColorPicker()
        private val tailNumParticles = Slider()
    }

    private var isFireworksSelected = true
    private var isRandomColor = true

    private fun updateColorUI() {
        children.removeIf { it.id == "colorLabel" || it.id == "colorBox" }

        if (!isRandomColor) {
            children.add(Label("Colors").apply { id = "colorLabel" })
            children.add(HBox(color).apply {
                id = "colorBox"
                spacing = 10.0
                alignment = Pos.CENTER_LEFT
            })
        }
    }

    init {
        explosionVisible.apply {
            isSelected = true
            textProperty().bind(selectedProperty().map { selected -> if (selected) "Visible" else "Hidden" })
            labelPosition = HorizontalDirection.RIGHT
        }
        explosionType.apply {
            isSelected = true
            isFireworksSelected = isSelected
            textProperty().bind(selectedProperty().map { selected -> if (selected) "Fireworks" else "Splash" })

            selectedProperty().addListener { _ ->
                if (isSelected) {
                    explosionRadius.min = 10.0
                    explosionRadius.max = 30.0
                    particleSize.min = 1.0
                    particleSize.max = 5.0
                } else {
                    explosionRadius.min = 2.0
                    explosionRadius.max = 9.5
                    particleSize.min = 0.9
                    particleSize.max = 3.0
                }

                explosionRadius.value = explosionRadius.min
                particleSize.value = particleSize.min
                numParticles.value = numParticles.min
            }
            labelPosition = HorizontalDirection.RIGHT
        }
        explosionRadius.apply {
            min = if (isFireworksSelected) 10.0 else 2.0
            max = if (isFireworksSelected) 30.0 else 9.5
        }
        numParticles.apply {
            min = 80.0
            max = 150.0
        }
        particleSize.apply {
            min = if (isFireworksSelected) 1.0 else 0.9
            max = if (isFireworksSelected) 5.0 else 3.0
        }
        colorMode.apply {
            isSelected = false
            isRandomColor = isSelected
            labelPosition = HorizontalDirection.RIGHT
            selectedProperty().addListener { _, _, selected ->
                isRandomColor = selected
                updateColorUI()
            }
        }

        // Ensure correct initial state
        color.value = Color.YELLOW

        children.addAll(
            Label("Show Explosion"), explosionVisible,
            Label("Animation Type"), explosionType,
            Label("Explosion Radius"), explosionRadius,
            Label("Number of Particles"), numParticles,
            Label("Particle Size"), particleSize,
            Label("Random Color"), colorMode
        )

        updateColorUI() // Call after adding initial UI elements
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
            explosionType.selectedProperty()
                .bindBidirectional(getbp(PianoProperty.EXPLOSION_TYPE.name))
            color.valueProperty()
                .bindBidirectional(getop<Color>(PianoProperty.EXPLOSION_COLOR.name))
            colorMode.selectedProperty()
                .bindBidirectional(getbp(PianoProperty.EXPLOSION_RANDOM_COLOR.name))
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