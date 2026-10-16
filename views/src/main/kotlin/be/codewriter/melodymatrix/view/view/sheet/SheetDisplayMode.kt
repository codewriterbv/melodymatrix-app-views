package be.codewriter.melodymatrix.view.view.sheet

/**
 * How a notation view shows a song while it plays.
 */
enum class SheetDisplayMode {
    /** The whole score is visible from the start and a marker follows the playback. */
    FULL_SCORE,

    /** The staves start empty and every note appears when it is played. */
    NOTE_BY_NOTE
}
