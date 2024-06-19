package be.codewriter.melodymatrix.view.definition

import javafx.scene.paint.Color


enum class MainNote(
    val sortingKey: Int,
    val pianoKeyType: PianoKeyType,
    val chartColor: Color,
    val labelColor: Color
) {
    C(1, PianoKeyType.RIGHT, Color.web("#0197DE"), Color.BLACK),
    C_SHARP(2, PianoKeyType.SHARP, Color.web("#0197DE"), Color.BLACK),
    D(3, PianoKeyType.BOTH, Color.web("#0197DE"), Color.BLACK),
    D_SHARP(4, PianoKeyType.SHARP, Color.web("#0197DE"), Color.BLACK),
    E(5, PianoKeyType.LEFT, Color.web("#62BD4A"), Color.BLACK),
    F(6, PianoKeyType.RIGHT, Color.web("#A1D490"), Color.BLACK),
    F_SHARP(7, PianoKeyType.SHARP, Color.web("#C5E3B9"), Color.BLACK),
    G(8, PianoKeyType.BOTH, Color.web("#EFE3BC"), Color.BLACK),
    G_SHARP(9, PianoKeyType.SHARP, Color.web("#FFD824"), Color.BLACK),
    A(10, PianoKeyType.BOTH, Color.web("#FCA300"), Color.BLACK),
    A_SHARP(11, PianoKeyType.SHARP, Color.web("#F3522C"), Color.BLACK),
    B(12, PianoKeyType.LEFT, Color.web("#D04625"), Color.BLACK),
    UNDEFINED(0, PianoKeyType.NONE, Color.TRANSPARENT, Color.BLACK);

    val isSharp = pianoKeyType == PianoKeyType.SHARP
}
