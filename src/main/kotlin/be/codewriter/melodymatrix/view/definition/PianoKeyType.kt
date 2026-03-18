package be.codewriter.melodymatrix.view.definition

/**
 * Enumeration of piano key types for keyboard visualization.
 *
 * Describes the position and type of key on a piano keyboard:
 * - LEFT: White key on the left side of a black key pair
 * - RIGHT: White key on the right side of a black key pair
 * - BOTH: White key between two black keys (applies to D, G, and A)
 * - SHARP: Black key (sharp/flat note)
 * - NONE: Not a playable key on standard piano
 *
 * @see MainNote
 * @see PianoStage
 */
enum class PianoKeyType {
    /** White key positioned on the left */
    LEFT,

    /** White key positioned between two black keys */
    BOTH,

    /** White key positioned on the right */
    RIGHT,

    /** Black key (sharp or flat) */
    SHARP,

    /** No piano key (not applicable) */
    NONE
}