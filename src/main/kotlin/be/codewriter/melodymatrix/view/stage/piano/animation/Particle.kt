package be.codewriter.melodymatrix.view.stage.piano.animation

import javafx.scene.paint.Color

data class Particle(
    var x: Double,
    var y: Double,
    var velocityX: Double,
    var velocityY: Double,
    var age: Double = 0.0,
    val lifespan: Double,
    val startSize: Double,
    val endSize: Double,
    val startColor: Color,
    val endColor: Color
) {
    val isAlive: Boolean
        get() = age < lifespan

    fun update(deltaTime: Double) {
        x += velocityX * deltaTime
        y += velocityY * deltaTime
        age += deltaTime
    }

    fun getSize(): Double {
        val progress = age / lifespan
        return startSize + (endSize - startSize) * progress
    }

    fun getColor(): Color {
        val progress = age / lifespan
        return startColor.interpolate(endColor, progress)
    }

    fun getOpacity(): Double {
        val progress = age / lifespan
        return 1.0 - progress // Fade out over time
    }
}