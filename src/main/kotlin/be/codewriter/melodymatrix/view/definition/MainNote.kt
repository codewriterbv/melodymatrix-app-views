package be.codewriter.melodymatrix.view.definition

import javafx.scene.paint.Color


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
 * @property chartColor The color used to represent this note in charts
 * @property labelColor The color of the label text on the piano keyboard
 *
 * @see Note
 * @see PianoKeyType
 * @see Octave
 */
enum class MainNote(
    val sortingKey: Int,
    val label: String,
    val pianoKeyType: PianoKeyType,
    val chartColor: Color,
    val labelColor: Color
) {
    C(1, "C", PianoKeyType.RIGHT, Color.web("#0197DE"), Color.BLACK),
    C_SHARP(2, "C#", PianoKeyType.SHARP, Color.web("#0197DE"), Color.BLACK),
    D(3, "D", PianoKeyType.BOTH, Color.web("#0197DE"), Color.BLACK),
    D_SHARP(4, "D#", PianoKeyType.SHARP, Color.web("#0197DE"), Color.BLACK),
    E(5, "E", PianoKeyType.LEFT, Color.web("#62BD4A"), Color.BLACK),
    F(6, "F", PianoKeyType.RIGHT, Color.web("#A1D490"), Color.BLACK),
    F_SHARP(7, "F#", PianoKeyType.SHARP, Color.web("#C5E3B9"), Color.BLACK),
    G(8, "G", PianoKeyType.BOTH, Color.web("#EFE3BC"), Color.BLACK),
    G_SHARP(9, "G#", PianoKeyType.SHARP, Color.web("#FFD824"), Color.BLACK),
    A(10, "A", PianoKeyType.BOTH, Color.web("#FCA300"), Color.BLACK),
    A_SHARP(11, "A#", PianoKeyType.SHARP, Color.web("#F3522C"), Color.BLACK),
    B(12, "B", PianoKeyType.LEFT, Color.web("#D04625"), Color.BLACK),
    UNDEFINED(0, "", PianoKeyType.NONE, Color.TRANSPARENT, Color.BLACK);

    val isSharp = pianoKeyType == PianoKeyType.SHARP
}
