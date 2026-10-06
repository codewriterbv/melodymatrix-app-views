package be.codewriter.melodymatrix.view.definition

/**
 * Represents musical octaves (0-8) with associated visual gradient information.
 *
 * Each octave has a numeric value and a base colour used for visualisation.
 * The UNDEFINED octave is a sentinel value for cases where octave is not applicable.
 *
 * @property octave The numeric octave value (0-8), or 0 for UNDEFINED
 * @property rgb Base colour of this octave as 0xRRGGBB (null for UNDEFINED); the views module
 *  turns it into a JavaFX gradient
 *
 * @see Note
 * @see MainNote
 */
enum class Octave(val octave: Int, val rgb: Int?) {
    UNDEFINED(0, null),
    OCTAVE_0(0, 0xFF0000), // Red
    OCTAVE_1(1, 0xFF7F00), // Orange
    OCTAVE_2(2, 0xFFFF00), // Yellow
    OCTAVE_3(3, 0x00FF00), // Green
    OCTAVE_4(4, 0x0000FF), // Blue
    OCTAVE_5(5, 0x4B0082), // Indigo
    OCTAVE_6(6, 0x9400D3), // Violet
    OCTAVE_7(7, 0xFF0000), // Red
    OCTAVE_8(8, 0xFF7F00), // Orange
    OCTAVE_9(9, 0xFFFF00); // Yellow
}