package be.codewriter.melodymatrix.view.stage.piano.animation

import be.codewriter.melodymatrix.view.definition.Note
import javafx.scene.paint.Color

/**
 * Immutable snapshot of the complete piano animation state at a single frame.
 *
 * @property timestamp          The frame timestamp in milliseconds since epoch
 * @property particlePositions  All active explosion/fireworks particle positions and visual properties
 * @property aboveKeyParticles  Particles rendered above the keyboard keys (smoke/steam effect)
 * @property fireEmitterState   State of the fire emitter effect
 * @property keyStates          Per-note pressed/animation state for the keyboard
 */
data class AnimationState(
    val timestamp: Long,
    val particlePositions: List<ParticleData>,
    val aboveKeyParticles: List<AboveKeyParticleData>,
    val fireEmitterState: FireState,
    val keyStates: Map<Note, KeyState>
)

/**
 * Positional and visual data for a single explosion or fireworks particle.
 *
 * @property x       Horizontal position in scene coordinates
 * @property y       Vertical position in scene coordinates
 * @property color   The particle colour
 * @property size    The particle diameter in pixels
 * @property opacity The particle opacity (0.0–1.0)
 */
data class ParticleData(val x: Double, val y: Double, val color: Color, val size: Double, val opacity: Double)

/**
 * Positional and visual data for a single above-key smoke/steam particle.
 *
 * @property x       Horizontal position in scene coordinates
 * @property y       Vertical position in scene coordinates
 * @property color   The particle colour
 * @property size    The particle diameter in pixels
 * @property opacity The particle opacity (0.0–1.0)
 */
data class AboveKeyParticleData(val x: Double, val y: Double, val color: Color, val size: Double, val opacity: Double)

/**
 * State of the fire emitter effect.
 *
 * @property x         Horizontal position of the emitter origin
 * @property y         Vertical position of the emitter origin
 * @property intensity Current intensity of the fire effect (0.0 = off, 1.0 = maximum)
 */
data class FireState(val x: Double, val y: Double, val intensity: Double)

/**
 * Per-key animation state.
 *
 * @property isPressed         Whether the key is currently pressed
 * @property animationProgress Progress of the key-press animation (0.0 = start, 1.0 = complete)
 */
data class KeyState(val isPressed: Boolean, val animationProgress: Double)