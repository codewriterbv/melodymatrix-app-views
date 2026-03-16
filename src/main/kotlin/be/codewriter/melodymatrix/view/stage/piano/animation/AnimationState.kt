package be.codewriter.melodymatrix.view.stage.piano.animation

import be.codewriter.melodymatrix.view.definition.Note
import javafx.scene.paint.Color

data class AnimationState(
    val timestamp: Long,
    val particlePositions: List<ParticleData>,
    val aboveKeyParticles: List<AboveKeyParticleData>,
    val fireEmitterState: FireState,
    val keyStates: Map<Note, KeyState>
)

data class ParticleData(val x: Double, val y: Double, val color: Color, val size: Double, val opacity: Double)
data class AboveKeyParticleData(val x: Double, val y: Double, val color: Color, val size: Double, val opacity: Double)
data class FireState(val x: Double, val y: Double, val intensity: Double)
data class KeyState(val isPressed: Boolean, val animationProgress: Double)