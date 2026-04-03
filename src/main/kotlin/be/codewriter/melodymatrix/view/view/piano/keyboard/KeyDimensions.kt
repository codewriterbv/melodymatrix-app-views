package be.codewriter.melodymatrix.view.view.piano.keyboard

/**
 * Holds the calculated pixel dimensions for piano keys within a [KeyboardView].
 *
 * Dimensions are derived from the total available width/height and the number of
 * white keys in the rendered note range, so both full-keyboard and partial-range
 * views can share the same [KeyWhite] / [KeyBlack] rendering logic.
 *
 * @property whiteKeyWidth  Width of a single white key in pixels
 * @property whiteKeyHeight Height of a white key in pixels (equals the total keyboard height)
 * @property blackKeyWidth  Width of a black key in pixels
 * @property blackKeyHeight Height of a black key in pixels
 *
 * @see KeyboardView
 * @see KeyWhite
 * @see KeyBlack
 */
data class KeyDimensions(
    val whiteKeyWidth: Double,
    val whiteKeyHeight: Double,
    val blackKeyWidth: Double,
    val blackKeyHeight: Double
)

