package be.codewriter.melodymatrix.view.definition

import javafx.scene.paint.Color


enum class MainNote(
    val sortingKey: Int,
    val label: String,
    val pianoKeyType: be.codewriter.melodymatrix.view.definition.PianoKeyType,
    val chartColor: Color,
    val labelColor: Color
) {
    C(1, "C", be.codewriter.melodymatrix.view.definition.PianoKeyType.RIGHT, Color.web("#0197DE"), Color.BLACK),
    C_SHARP(2, "C#", be.codewriter.melodymatrix.view.definition.PianoKeyType.SHARP, Color.web("#0197DE"), Color.BLACK),
    D(3, "D", be.codewriter.melodymatrix.view.definition.PianoKeyType.BOTH, Color.web("#0197DE"), Color.BLACK),
    D_SHARP(4, "D#", be.codewriter.melodymatrix.view.definition.PianoKeyType.SHARP, Color.web("#0197DE"), Color.BLACK),
    E(5, "E", be.codewriter.melodymatrix.view.definition.PianoKeyType.LEFT, Color.web("#62BD4A"), Color.BLACK),
    F(6, "F", be.codewriter.melodymatrix.view.definition.PianoKeyType.RIGHT, Color.web("#A1D490"), Color.BLACK),
    F_SHARP(7, "F#", be.codewriter.melodymatrix.view.definition.PianoKeyType.SHARP, Color.web("#C5E3B9"), Color.BLACK),
    G(8, "G", be.codewriter.melodymatrix.view.definition.PianoKeyType.BOTH, Color.web("#EFE3BC"), Color.BLACK),
    G_SHARP(9, "G#", be.codewriter.melodymatrix.view.definition.PianoKeyType.SHARP, Color.web("#FFD824"), Color.BLACK),
    A(10, "A", be.codewriter.melodymatrix.view.definition.PianoKeyType.BOTH, Color.web("#FCA300"), Color.BLACK),
    A_SHARP(11, "A#", be.codewriter.melodymatrix.view.definition.PianoKeyType.SHARP, Color.web("#F3522C"), Color.BLACK),
    B(12, "B", be.codewriter.melodymatrix.view.definition.PianoKeyType.LEFT, Color.web("#D04625"), Color.BLACK),
    UNDEFINED(0, "", be.codewriter.melodymatrix.view.definition.PianoKeyType.NONE, Color.TRANSPARENT, Color.BLACK);

    val isSharp = pianoKeyType == be.codewriter.melodymatrix.view.definition.PianoKeyType.SHARP
}
