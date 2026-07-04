package be.codewriter.melodymatrix.view.view.piano.animation

import javafx.scene.paint.Color

/**
 * Positional and visual data for a single Synthesia-style note block, computed by the
 * off-thread [AnimationCalculator] and rendered on the JavaFX thread by `PianoCanvas`.
 *
 * The same shape is used for both falling (playback) and rising (input) blocks; the
 * two are transported in separate lists on [AnimationState] so the renderer can
 * apply per-mode styling if needed later.
 *
 * @property x       Left edge in scene coordinates
 * @property y       Top edge in scene coordinates
 * @property width   Block width in pixels
 * @property height  Block height in pixels
 * @property color   Fill colour before opacity is applied
 * @property opacity Fill opacity (0.0\u20131.0)
 */
data class NoteBlockData(
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double,
    val color: Color,
    val opacity: Double
)
