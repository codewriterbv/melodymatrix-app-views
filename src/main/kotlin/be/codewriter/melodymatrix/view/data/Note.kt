package be.codewriter.melodymatrix.view.data

import java.util.*

enum class Note(
    val mainNote: MainNote,
    val parentNote: Note? = null,
    val octave: Octave,
    val byteValue: Int,
    val showOnPiano: Boolean
) {

    A0(MainNote.A, null, Octave.OCTAVE_0, 21, true),
    A0_SHARP(MainNote.A_SHARP, A0, Octave.OCTAVE_0, 22, true),
    B0(MainNote.B, null, Octave.OCTAVE_0, 23, true),

    C1(MainNote.C, null, Octave.OCTAVE_1, 24, true),
    C1_SHARP(MainNote.C_SHARP, C1, Octave.OCTAVE_1, 25, true),
    D1(MainNote.D, null, Octave.OCTAVE_1, 26, true),
    D1_SHARP(MainNote.D_SHARP, D1, Octave.OCTAVE_1, 27, true),
    E1(MainNote.E, null, Octave.OCTAVE_1, 28, true),
    F1(MainNote.F, null, Octave.OCTAVE_1, 29, true),
    F1_SHARP(MainNote.F_SHARP, F1, Octave.OCTAVE_1, 30, true),
    G1(MainNote.G, null, Octave.OCTAVE_1, 31, true),
    G1_SHARP(MainNote.G_SHARP, G1, Octave.OCTAVE_1, 32, true),
    A1(MainNote.A, null, Octave.OCTAVE_1, 33, true),
    A1_SHARP(MainNote.A_SHARP, A1, Octave.OCTAVE_1, 34, true),
    B1(MainNote.B, null, Octave.OCTAVE_1, 35, true),

    C2(MainNote.C, null, Octave.OCTAVE_2, 36, true),
    C2_SHARP(MainNote.C_SHARP, C2, Octave.OCTAVE_2, 37, true),
    D2(MainNote.D, null, Octave.OCTAVE_2, 38, true),
    D2_SHARP(MainNote.D_SHARP, D2, Octave.OCTAVE_2, 39, true),
    E2(MainNote.E, null, Octave.OCTAVE_2, 40, true),
    F2(MainNote.F, null, Octave.OCTAVE_2, 41, true),
    F2_SHARP(MainNote.F_SHARP, F2, Octave.OCTAVE_2, 42, true),
    G2(MainNote.G, null, Octave.OCTAVE_2, 43, true),
    G2_SHARP(MainNote.G_SHARP, G2, Octave.OCTAVE_2, 44, true),
    A2(MainNote.A, null, Octave.OCTAVE_2, 45, true),
    A2_SHARP(MainNote.A_SHARP, A2, Octave.OCTAVE_2, 46, true),
    B2(MainNote.B, null, Octave.OCTAVE_2, 47, true),

    C3(MainNote.C, null, Octave.OCTAVE_3, 48, true),
    C3_SHARP(MainNote.C_SHARP, C3, Octave.OCTAVE_3, 49, true),
    D3(MainNote.D, null, Octave.OCTAVE_3, 50, true),
    D3_SHARP(MainNote.D_SHARP, D3, Octave.OCTAVE_3, 51, true),
    E3(MainNote.E, null, Octave.OCTAVE_3, 52, true),
    F3(MainNote.F, null, Octave.OCTAVE_3, 53, true),
    F3_SHARP(MainNote.F_SHARP, F3, Octave.OCTAVE_3, 54, true),
    G3(MainNote.G, null, Octave.OCTAVE_3, 55, true),
    G3_SHARP(MainNote.G_SHARP, G3, Octave.OCTAVE_3, 56, true),
    A3(MainNote.A, null, Octave.OCTAVE_3, 57, true),
    A3_SHARP(MainNote.A_SHARP, A3, Octave.OCTAVE_3, 58, true),
    B3(MainNote.B, null, Octave.OCTAVE_3, 59, true),

    C4(MainNote.C, null, Octave.OCTAVE_4, 60, true),
    C4_SHARP(MainNote.C_SHARP, C4, Octave.OCTAVE_4, 61, true),
    D4(MainNote.D, null, Octave.OCTAVE_4, 62, true),
    D4_SHARP(MainNote.D_SHARP, D4, Octave.OCTAVE_4, 63, true),
    E4(MainNote.E, null, Octave.OCTAVE_4, 64, true),
    F4(MainNote.F, null, Octave.OCTAVE_4, 65, true),
    F4_SHARP(MainNote.F_SHARP, F4, Octave.OCTAVE_4, 66, true),
    G4(MainNote.G, null, Octave.OCTAVE_4, 67, true),
    G4_SHARP(MainNote.G_SHARP, G4, Octave.OCTAVE_4, 68, true),
    A4(MainNote.A, null, Octave.OCTAVE_4, 69, true),
    A4_SHARP(MainNote.A_SHARP, A4, Octave.OCTAVE_4, 70, true),
    B4(MainNote.B, null, Octave.OCTAVE_4, 71, true),

    C5(MainNote.C, null, Octave.OCTAVE_5, 72, true),
    C5_SHARP(MainNote.C_SHARP, C5, Octave.OCTAVE_5, 73, true),
    D5(MainNote.D, null, Octave.OCTAVE_5, 74, true),
    D5_SHARP(MainNote.D_SHARP, D5, Octave.OCTAVE_5, 75, true),
    E5(MainNote.E, null, Octave.OCTAVE_5, 76, true),
    F5(MainNote.F, null, Octave.OCTAVE_5, 77, true),
    F5_SHARP(MainNote.F_SHARP, F5, Octave.OCTAVE_5, 78, true),
    G5(MainNote.G, null, Octave.OCTAVE_5, 79, true),
    G5_SHARP(MainNote.G_SHARP, G5, Octave.OCTAVE_5, 80, true),
    A5(MainNote.A, null, Octave.OCTAVE_5, 81, true),
    A5_SHARP(MainNote.A_SHARP, A5, Octave.OCTAVE_5, 82, true),
    B5(MainNote.B, null, Octave.OCTAVE_5, 83, true),

    C6(MainNote.C, null, Octave.OCTAVE_6, 84, true),
    C6_SHARP(MainNote.C_SHARP, C6, Octave.OCTAVE_6, 85, true),
    D6(MainNote.D, null, Octave.OCTAVE_6, 86, true),
    D6_SHARP(MainNote.D_SHARP, D6, Octave.OCTAVE_6, 87, true),
    E6(MainNote.E, null, Octave.OCTAVE_6, 88, true),
    F6(MainNote.F, null, Octave.OCTAVE_6, 89, true),
    F6_SHARP(MainNote.F_SHARP, F6, Octave.OCTAVE_6, 90, true),
    G6(MainNote.G, null, Octave.OCTAVE_6, 91, true),
    G6_SHARP(MainNote.G_SHARP, G6, Octave.OCTAVE_6, 92, true),
    A6(MainNote.A, null, Octave.OCTAVE_6, 93, true),
    A6_SHARP(MainNote.A_SHARP, A6, Octave.OCTAVE_6, 94, true),
    B6(MainNote.B, null, Octave.OCTAVE_6, 95, true),

    C7(MainNote.C, null, Octave.OCTAVE_7, 96, true),
    C7_SHARP(MainNote.C_SHARP, C7, Octave.OCTAVE_7, 97, true),
    D7(MainNote.D, null, Octave.OCTAVE_7, 98, true),
    D7_SHARP(MainNote.D_SHARP, D7, Octave.OCTAVE_7, 99, true),
    E7(MainNote.E, null, Octave.OCTAVE_7, 100, true),
    F7(MainNote.F, null, Octave.OCTAVE_7, 101, true),
    F7_SHARP(MainNote.F_SHARP, F7, Octave.OCTAVE_7, 102, true),
    G7(MainNote.G, null, Octave.OCTAVE_7, 103, true),
    G7_SHARP(MainNote.G_SHARP, G7, Octave.OCTAVE_7, 104, true),
    A7(MainNote.A, null, Octave.OCTAVE_7, 105, true),
    A7_SHARP(MainNote.A_SHARP, A7, Octave.OCTAVE_7, 106, true),
    B7(MainNote.B, null, Octave.OCTAVE_7, 107, true),

    C8(MainNote.C, null, Octave.OCTAVE_8, 108, true),
    C8_SHARP(MainNote.C_SHARP, C8, Octave.OCTAVE_8, 109, true),
    D8(MainNote.D, null, Octave.OCTAVE_8, 110, true),
    D8_SHARP(MainNote.D_SHARP, D8, Octave.OCTAVE_8, 111, true),
    E8(MainNote.E, null, Octave.OCTAVE_8, 112, true),
    F8(MainNote.F, null, Octave.OCTAVE_8, 113, true),
    F8_SHARP(MainNote.F_SHARP, F8, Octave.OCTAVE_8, 114, true),
    G8(MainNote.G, null, Octave.OCTAVE_8, 115, true),
    G8_SHARP(MainNote.G_SHARP, G8, Octave.OCTAVE_8, 116, true),
    A8(MainNote.A, null, Octave.OCTAVE_8, 117, true),
    A8_SHARP(MainNote.A_SHARP, A8, Octave.OCTAVE_8, 118, true),
    B8(MainNote.B, null, Octave.OCTAVE_8, 119, true),

    C9(MainNote.C, null, Octave.OCTAVE_9, 120, true),
    C9_SHARP(MainNote.C_SHARP, C9, Octave.OCTAVE_9, 121, false),
    D9(MainNote.D, null, Octave.OCTAVE_9, 122, false),
    D9_SHARP(MainNote.D_SHARP, D9, Octave.OCTAVE_9, 123, false),
    E9(MainNote.E, null, Octave.OCTAVE_9, 124, false),
    F9(MainNote.F, null, Octave.OCTAVE_9, 125, false),
    F9_SHARP(MainNote.F_SHARP, F9, Octave.OCTAVE_9, 126, false),
    G9(MainNote.G, null, Octave.OCTAVE_9, 127, false),
    G9_SHARP(MainNote.G_SHARP, G9, Octave.OCTAVE_9, 128, false),

    UNDEFINED(MainNote.UNDEFINED, null, Octave.UNDEFINED, 0, false);

    companion object {
        fun pianoKeys(): List<Note> {
            return entries.stream()
                .filter { it.showOnPiano }
                .toList()
        }

        fun numberOfNoneSharps(): Long {
            return entries.stream()
                .filter { !it.mainNote.isSharp }
                .count()
        }

        fun nonSharp(): List<Note> {
            return entries.stream()
                .filter { !it.mainNote.isSharp }
                .toList()
        }

        fun usedAndSortedOctaves(): List<Octave> {
            return entries.stream()
                .filter { o -> o.octave != Octave.UNDEFINED }
                .map { it.octave }
                .distinct()
                .sorted(Comparator.comparingInt(Octave::octave))
                .toList()
        }

        fun usedAndSortedMainNotes(): List<MainNote> {
            return entries.stream()
                .filter { o -> o.mainNote != MainNote.UNDEFINED }
                .map { it.mainNote }
                .distinct()
                .sorted(Comparator.comparingInt(MainNote::sorktingKey))
                .toList()
        }

        infix fun from(value: Byte): Note {
            return entries.stream()
                .filter { it.byteValue == value.toInt() }
                .findFirst()
                .orElse(UNDEFINED)
        }
    }
}
