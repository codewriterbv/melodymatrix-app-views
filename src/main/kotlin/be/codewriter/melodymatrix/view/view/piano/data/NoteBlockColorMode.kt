package be.codewriter.melodymatrix.view.view.piano.data

import javafx.scene.paint.Color

/**
 * Determines how a Synthesia-style note block is coloured.
 *
 * @see be.codewriter.melodymatrix.view.view.piano.animation.NoteBlockData
 */
enum class NoteBlockColorMode {
    /** All blocks use the single configured fixed colour. */
    FIXED,

    /** Blocks are coloured by interpolating between a low- and high-velocity colour. */
    BY_VELOCITY,

    /** Blocks are coloured by MIDI channel (0..15) using [CHANNEL_PALETTE]. */
    BY_CHANNEL
}

/**
 * Default 16-slot channel palette used when [NoteBlockColorMode.BY_CHANNEL] is selected.
 *
 * v1: fixed palette, not persisted and not user-editable.
 */
val CHANNEL_PALETTE: List<Color> = listOf(
    Color.web("#4d66cc"), // 0
    Color.web("#ff3b3b"), // 1
    Color.web("#3bff77"), // 2
    Color.web("#ffb400"), // 3
    Color.web("#b04dff"), // 4
    Color.web("#00c8d1"), // 5
    Color.web("#ff5ea8"), // 6
    Color.web("#66d95a"), // 7
    Color.web("#ff8547"), // 8
    Color.web("#4dc0ff"), // 9
    Color.web("#ffe14d"), // 10
    Color.web("#a0ff5a"), // 11
    Color.web("#ff4d78"), // 12
    Color.web("#7a86ff"), // 13
    Color.web("#5affd3"), // 14
    Color.web("#c94dff")  // 15
)
