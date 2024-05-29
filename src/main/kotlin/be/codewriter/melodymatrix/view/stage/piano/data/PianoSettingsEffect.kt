package be.codewriter.melodymatrix.view.stage.piano.data

import javafx.scene.effect.BlendMode
import javafx.scene.paint.Color

data class PianoSettingsEffect(
    val showEffect: Boolean,
    val blendMode: BlendMode,
    val explosionRadius: Int,
    val numParticles: Int,
    val particleSize: Double,
    val colorStart: Color,
    val colorEnd: Color
)
