package be.codewriter.melodymatrix.view.definition

/**
 * Represents the 12 chromatic notes (C through B) with their visual and keyboard properties.
 *
 * Each main note has a sorting key for ordinal comparisons, a label, information about
 * its piano key type (white key, black key, or both), and color information for charts
 * and UI display.
 *
 * @property sortingKey An integer for ordering notes chromatically (1-12)
 * @property label The note name as a string (e.g., "C", "C#")
 * @property pianoKeyType The type of piano key this note corresponds to
 * @property chartColorHex The colour (CSS hex) used to represent this note in charts
 * @property labelColorHex The colour (CSS hex) of the label text on the piano keyboard
 *
 * @see Note
 * @see PianoKeyType
 * @see Octave
 */
enum class MainNote(
    val sortingKey: Int,
    val label: String,
    val pianoKeyType: PianoKeyType,
    val chartColorHex: String,
    val labelColorHex: String,
    val solfege: String = ""
) {
    C(1, "C", PianoKeyType.RIGHT, "#0197DE", "#000000", "Do"),
    C_SHARP(2, "C#", PianoKeyType.SHARP, "#0197DE", "#000000"),
    D(3, "D", PianoKeyType.BOTH, "#0197DE", "#000000", "Re"),
    D_SHARP(4, "D#", PianoKeyType.SHARP, "#0197DE", "#000000"),
    E(5, "E", PianoKeyType.LEFT, "#62BD4A", "#000000", "Mi"),
    F(6, "F", PianoKeyType.RIGHT, "#A1D490", "#000000", "Fa"),
    F_SHARP(7, "F#", PianoKeyType.SHARP, "#C5E3B9", "#000000"),
    G(8, "G", PianoKeyType.BOTH, "#EFE3BC", "#000000", "Sol"),
    G_SHARP(9, "G#", PianoKeyType.SHARP, "#FFD824", "#000000"),
    A(10, "A", PianoKeyType.BOTH, "#FCA300", "#000000", "La"),
    A_SHARP(11, "A#", PianoKeyType.SHARP, "#F3522C", "#000000"),
    B(12, "B", PianoKeyType.LEFT, "#D04625", "#000000", "Si"),
    UNDEFINED(0, "", PianoKeyType.NONE, "transparent", "#000000");

    val isSharp = pianoKeyType == PianoKeyType.SHARP
}
