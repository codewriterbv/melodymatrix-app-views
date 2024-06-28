package be.codewriter.melodymatrix.view.definition

import javafx.scene.paint.Color


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
