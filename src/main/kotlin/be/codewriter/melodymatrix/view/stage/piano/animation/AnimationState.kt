package be.codewriter.melodymatrix.view.stage.piano.animation

import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.stage.piano.particle.ParticleEmitter
import javafx.scene.paint.Color

data class AnimationState(
    val timestamp: Long,
    val particleEmitters: List<ParticleEmitter>,
    val fireEmitterState: FireState,
    val keyStates: Map<Note, KeyState>
)

data class ParticleData(val x: Double, val y: Double, val color: Color, val size: Double)
data class FireState(val x: Double, val y: Double, val intensity: Double)
data class KeyState(val isPressed: Boolean, val animationProgress: Double)