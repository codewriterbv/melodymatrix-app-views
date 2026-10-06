package be.codewriter.melodymatrix.view.view.piano.animation

import be.codewriter.melodymatrix.view.view.piano.data.CHANNEL_PALETTE
import be.codewriter.melodymatrix.view.view.piano.data.NoteBlockColorMode
import javafx.scene.paint.Color

/**
 * Immutable snapshot of the note-block appearance and timing settings, pushed to
 * [AnimationCalculator] each frame by the JavaFX renderer.
 *
 * Keeping this as a plain data class (no JavaFX observable properties) lets the
 * off-thread calculator read the values without touching the JavaFX toolkit.
 *
 * @property lookAheadSeconds       Time window a falling block travels top\u2192keyboard, and
 *                                  the equivalent scroll rate applied to rising blocks
 * @property colorMode              How each block's fill colour is chosen
 * @property fixedColor             Fill colour when [colorMode] == [NoteBlockColorMode.FIXED]
 * @property lowVelocityColor       Interpolation endpoint (velocity 1) for BY_VELOCITY
 * @property highVelocityColor      Interpolation endpoint (velocity 127) for BY_VELOCITY
 * @property opacity                Fill opacity (0.0\u20131.0) baked into each [NoteBlockData]
 */
data class NoteBlockConfig(
    val lookAheadSeconds: Double,
    val colorMode: NoteBlockColorMode,
    val fixedColor: Color,
    val lowVelocityColor: Color,
    val highVelocityColor: Color,
    val opacity: Double
) {
    /**
     * Resolves the fill colour for a block given the current [colorMode].
     *
     * @param velocity MIDI velocity 0..127 (only used for BY_VELOCITY)
     * @param channel  MIDI channel 0..15 (only used for BY_CHANNEL)
     */
    fun resolveColor(velocity: Int, channel: Int): Color = when (colorMode) {
        NoteBlockColorMode.FIXED -> fixedColor
        NoteBlockColorMode.BY_VELOCITY -> {
            val t = (velocity.coerceIn(1, 127) - 1) / 126.0
            interpolate(lowVelocityColor, highVelocityColor, t)
        }
        NoteBlockColorMode.BY_CHANNEL -> {
            val idx = ((channel % CHANNEL_PALETTE.size) + CHANNEL_PALETTE.size) % CHANNEL_PALETTE.size
            CHANNEL_PALETTE[idx]
        }
    }

    private fun interpolate(a: Color, b: Color, t: Double): Color {
        val clamped = t.coerceIn(0.0, 1.0)
        return Color.color(
            a.red + (b.red - a.red) * clamped,
            a.green + (b.green - a.green) * clamped,
            a.blue + (b.blue - a.blue) * clamped,
            a.opacity + (b.opacity - a.opacity) * clamped
        )
    }

    companion object {
        val DEFAULT: NoteBlockConfig = NoteBlockConfig(
            lookAheadSeconds = 3.0,
            colorMode = NoteBlockColorMode.FIXED,
            fixedColor = Color.web("#4d66cc"),
            lowVelocityColor = Color.web("#2e7dff"),
            highVelocityColor = Color.web("#ff3b3b"),
            opacity = 0.9
        )
    }
}
