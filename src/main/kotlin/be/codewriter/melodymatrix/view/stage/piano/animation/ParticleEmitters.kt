package be.codewriter.melodymatrix.view.stage.piano.animation

import javafx.scene.paint.Color

object ParticleEmitters {
    fun newFireEmitter(): ParticleEmitter {
        return ParticleEmitter(
            x = 0.0,
            y = 0.0,
            emissionRate = 80.0,
            startColor = Color.rgb(255, 200, 50), // Bright yellow-orange
            endColor = Color.rgb(255, 69, 0, 0.0), // Orange-red fading to transparent
            minLifespan = 0.8,
            maxLifespan = 1.5,
            minVelocityX = -15.0,
            maxVelocityX = 15.0,
            minVelocityY = -100.0,
            maxVelocityY = -60.0,
            startSize = 10.0,
            endSize = 3.0,
            enabled = true,
            opacity = 0.0 // Start at 0 for fade-in
        )
    }
}