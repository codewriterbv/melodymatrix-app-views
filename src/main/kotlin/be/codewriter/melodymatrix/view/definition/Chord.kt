package be.codewriter.melodymatrix.view.definition

/**
 * Represents a detected chord, combining a root pitch class (0-11) with a quality (triad, seventh, or dyad).
 * Follows the same enum-with-properties pattern as Note and Octave.
 * 
 * <a href="https://hellomusictheory.com/learn/types-of-chords/">Types of Chords</a>
 */
enum class Chord(
    val pitchClass: Int,
    val quality: ChordQuality,
    val extension: ChordExtension,
    val label: String,
    val alteration: ChordAlteration = ChordAlteration.NONE
) {
    // ── Triads ───────────────────────────────────────────────────────────────
    C_MAJOR(0, ChordQuality.MAJOR, ChordExtension.NONE, "C major"),
    C_MINOR(0, ChordQuality.MINOR, ChordExtension.NONE, "C minor"),
    C_SHARP_MAJOR(1, ChordQuality.MAJOR, ChordExtension.NONE, "C# major"),
    C_SHARP_MINOR(1, ChordQuality.MINOR, ChordExtension.NONE, "C# minor"),
    D_MAJOR(2, ChordQuality.MAJOR, ChordExtension.NONE, "D major"),
    D_MINOR(2, ChordQuality.MINOR, ChordExtension.NONE, "D minor"),
    D_SHARP_MAJOR(3, ChordQuality.MAJOR, ChordExtension.NONE, "D# major"),
    D_SHARP_MINOR(3, ChordQuality.MINOR, ChordExtension.NONE, "D# minor"),
    E_MAJOR(4, ChordQuality.MAJOR, ChordExtension.NONE, "E major"),
    E_MINOR(4, ChordQuality.MINOR, ChordExtension.NONE, "E minor"),
    F_MAJOR(5, ChordQuality.MAJOR, ChordExtension.NONE, "F major"),
    F_MINOR(5, ChordQuality.MINOR, ChordExtension.NONE, "F minor"),
    F_SHARP_MAJOR(6, ChordQuality.MAJOR, ChordExtension.NONE, "F# major"),
    F_SHARP_MINOR(6, ChordQuality.MINOR, ChordExtension.NONE, "F# minor"),
    G_MAJOR(7, ChordQuality.MAJOR, ChordExtension.NONE, "G major"),
    G_MINOR(7, ChordQuality.MINOR, ChordExtension.NONE, "G minor"),
    G_SHARP_MAJOR(8, ChordQuality.MAJOR, ChordExtension.NONE, "G# major"),
    G_SHARP_MINOR(8, ChordQuality.MINOR, ChordExtension.NONE, "G# minor"),
    A_MAJOR(9, ChordQuality.MAJOR, ChordExtension.NONE, "A major"),
    A_MINOR(9, ChordQuality.MINOR, ChordExtension.NONE, "A minor"),
    A_SHARP_MAJOR(10, ChordQuality.MAJOR, ChordExtension.NONE, "A# major"),
    A_SHARP_MINOR(10, ChordQuality.MINOR, ChordExtension.NONE, "A# minor"),
    B_MAJOR(11, ChordQuality.MAJOR, ChordExtension.NONE, "B major"),
    B_MINOR(11, ChordQuality.MINOR, ChordExtension.NONE, "B minor"),

    // ── Dyads ───────────────────────────────────────────────────────────────
    C_TRITONE(0, ChordQuality.TRITONE, ChordExtension.NONE, "C tritone"),
    C_SHARP_TRITONE(1, ChordQuality.TRITONE, ChordExtension.NONE, "C# tritone"),
    D_TRITONE(2, ChordQuality.TRITONE, ChordExtension.NONE, "D tritone"),
    D_SHARP_TRITONE(3, ChordQuality.TRITONE, ChordExtension.NONE, "D# tritone"),
    E_TRITONE(4, ChordQuality.TRITONE, ChordExtension.NONE, "E tritone"),
    F_TRITONE(5, ChordQuality.TRITONE, ChordExtension.NONE, "F tritone"),
    F_SHARP_TRITONE(6, ChordQuality.TRITONE, ChordExtension.NONE, "F# tritone"),
    G_TRITONE(7, ChordQuality.TRITONE, ChordExtension.NONE, "G tritone"),
    G_SHARP_TRITONE(8, ChordQuality.TRITONE, ChordExtension.NONE, "G# tritone"),
    A_TRITONE(9, ChordQuality.TRITONE, ChordExtension.NONE, "A tritone"),
    A_SHARP_TRITONE(10, ChordQuality.TRITONE, ChordExtension.NONE, "A# tritone"),
    B_TRITONE(11, ChordQuality.TRITONE, ChordExtension.NONE, "B tritone"),

    // ── Seventh chords ───────────────────────────────────────────────────────
    C_MAJOR_SEVENTH(0, ChordQuality.MAJOR, ChordExtension.MAJOR_SEVENTH, "C major 7th"),
    C_DOMINANT_SEVENTH(0, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "C dominant 7th"),
    C_MINOR_SEVENTH(0, ChordQuality.MINOR, ChordExtension.MINOR_SEVENTH, "C minor 7th"),
    C_HALF_DIMINISHED_SEVENTH(0, ChordQuality.HALF_DIMINISHED, ChordExtension.MINOR_SEVENTH, "C half-diminished 7th"),
    C_DIMINISHED_SEVENTH(0, ChordQuality.DIMINISHED, ChordExtension.DIMINISHED_SEVENTH, "C diminished 7th"),

    C_SHARP_MAJOR_SEVENTH(1, ChordQuality.MAJOR, ChordExtension.MAJOR_SEVENTH, "C# major 7th"),
    C_SHARP_DOMINANT_SEVENTH(1, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "C# dominant 7th"),
    C_SHARP_MINOR_SEVENTH(1, ChordQuality.MINOR, ChordExtension.MINOR_SEVENTH, "C# minor 7th"),
    C_SHARP_HALF_DIMINISHED_SEVENTH(1, ChordQuality.HALF_DIMINISHED, ChordExtension.MINOR_SEVENTH, "C# half-diminished 7th"),
    C_SHARP_DIMINISHED_SEVENTH(1, ChordQuality.DIMINISHED, ChordExtension.DIMINISHED_SEVENTH, "C# diminished 7th"),

    D_MAJOR_SEVENTH(2, ChordQuality.MAJOR, ChordExtension.MAJOR_SEVENTH, "D major 7th"),
    D_DOMINANT_SEVENTH(2, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "D dominant 7th"),
    D_MINOR_SEVENTH(2, ChordQuality.MINOR, ChordExtension.MINOR_SEVENTH, "D minor 7th"),
    D_HALF_DIMINISHED_SEVENTH(2, ChordQuality.HALF_DIMINISHED, ChordExtension.MINOR_SEVENTH, "D half-diminished 7th"),
    D_DIMINISHED_SEVENTH(2, ChordQuality.DIMINISHED, ChordExtension.DIMINISHED_SEVENTH, "D diminished 7th"),

    D_SHARP_MAJOR_SEVENTH(3, ChordQuality.MAJOR, ChordExtension.MAJOR_SEVENTH, "D# major 7th"),
    D_SHARP_DOMINANT_SEVENTH(3, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "D# dominant 7th"),
    D_SHARP_MINOR_SEVENTH(3, ChordQuality.MINOR, ChordExtension.MINOR_SEVENTH, "D# minor 7th"),
    D_SHARP_HALF_DIMINISHED_SEVENTH(3, ChordQuality.HALF_DIMINISHED, ChordExtension.MINOR_SEVENTH, "D# half-diminished 7th"),
    D_SHARP_DIMINISHED_SEVENTH(3, ChordQuality.DIMINISHED, ChordExtension.DIMINISHED_SEVENTH, "D# diminished 7th"),

    E_MAJOR_SEVENTH(4, ChordQuality.MAJOR, ChordExtension.MAJOR_SEVENTH, "E major 7th"),
    E_DOMINANT_SEVENTH(4, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "E dominant 7th"),
    E_MINOR_SEVENTH(4, ChordQuality.MINOR, ChordExtension.MINOR_SEVENTH, "E minor 7th"),
    E_HALF_DIMINISHED_SEVENTH(4, ChordQuality.HALF_DIMINISHED, ChordExtension.MINOR_SEVENTH, "E half-diminished 7th"),
    E_DIMINISHED_SEVENTH(4, ChordQuality.DIMINISHED, ChordExtension.DIMINISHED_SEVENTH, "E diminished 7th"),

    F_MAJOR_SEVENTH(5, ChordQuality.MAJOR, ChordExtension.MAJOR_SEVENTH, "F major 7th"),
    F_DOMINANT_SEVENTH(5, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "F dominant 7th"),
    F_MINOR_SEVENTH(5, ChordQuality.MINOR, ChordExtension.MINOR_SEVENTH, "F minor 7th"),
    F_HALF_DIMINISHED_SEVENTH(5, ChordQuality.HALF_DIMINISHED, ChordExtension.MINOR_SEVENTH, "F half-diminished 7th"),
    F_DIMINISHED_SEVENTH(5, ChordQuality.DIMINISHED, ChordExtension.DIMINISHED_SEVENTH, "F diminished 7th"),

    F_SHARP_MAJOR_SEVENTH(6, ChordQuality.MAJOR, ChordExtension.MAJOR_SEVENTH, "F# major 7th"),
    F_SHARP_DOMINANT_SEVENTH(6, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "F# dominant 7th"),
    F_SHARP_MINOR_SEVENTH(6, ChordQuality.MINOR, ChordExtension.MINOR_SEVENTH, "F# minor 7th"),
    F_SHARP_HALF_DIMINISHED_SEVENTH(6, ChordQuality.HALF_DIMINISHED, ChordExtension.MINOR_SEVENTH, "F# half-diminished 7th"),
    F_SHARP_DIMINISHED_SEVENTH(6, ChordQuality.DIMINISHED, ChordExtension.DIMINISHED_SEVENTH, "F# diminished 7th"),

    G_MAJOR_SEVENTH(7, ChordQuality.MAJOR, ChordExtension.MAJOR_SEVENTH, "G major 7th"),
    G_DOMINANT_SEVENTH(7, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "G dominant 7th"),
    G_MINOR_SEVENTH(7, ChordQuality.MINOR, ChordExtension.MINOR_SEVENTH, "G minor 7th"),
    G_HALF_DIMINISHED_SEVENTH(7, ChordQuality.HALF_DIMINISHED, ChordExtension.MINOR_SEVENTH, "G half-diminished 7th"),
    G_DIMINISHED_SEVENTH(7, ChordQuality.DIMINISHED, ChordExtension.DIMINISHED_SEVENTH, "G diminished 7th"),

    G_SHARP_MAJOR_SEVENTH(8, ChordQuality.MAJOR, ChordExtension.MAJOR_SEVENTH, "G# major 7th"),
    G_SHARP_DOMINANT_SEVENTH(8, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "G# dominant 7th"),
    G_SHARP_MINOR_SEVENTH(8, ChordQuality.MINOR, ChordExtension.MINOR_SEVENTH, "G# minor 7th"),
    G_SHARP_HALF_DIMINISHED_SEVENTH(8, ChordQuality.HALF_DIMINISHED, ChordExtension.MINOR_SEVENTH, "G# half-diminished 7th"),
    G_SHARP_DIMINISHED_SEVENTH(8, ChordQuality.DIMINISHED, ChordExtension.DIMINISHED_SEVENTH, "G# diminished 7th"),

    A_MAJOR_SEVENTH(9, ChordQuality.MAJOR, ChordExtension.MAJOR_SEVENTH, "A major 7th"),
    A_DOMINANT_SEVENTH(9, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "A dominant 7th"),
    A_MINOR_SEVENTH(9, ChordQuality.MINOR, ChordExtension.MINOR_SEVENTH, "A minor 7th"),
    A_HALF_DIMINISHED_SEVENTH(9, ChordQuality.HALF_DIMINISHED, ChordExtension.MINOR_SEVENTH, "A half-diminished 7th"),
    A_DIMINISHED_SEVENTH(9, ChordQuality.DIMINISHED, ChordExtension.DIMINISHED_SEVENTH, "A diminished 7th"),

    A_SHARP_MAJOR_SEVENTH(10, ChordQuality.MAJOR, ChordExtension.MAJOR_SEVENTH, "A# major 7th"),
    A_SHARP_DOMINANT_SEVENTH(10, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "A# dominant 7th"),
    A_SHARP_MINOR_SEVENTH(10, ChordQuality.MINOR, ChordExtension.MINOR_SEVENTH, "A# minor 7th"),
    A_SHARP_HALF_DIMINISHED_SEVENTH(
        10,
        ChordQuality.HALF_DIMINISHED,
        ChordExtension.MINOR_SEVENTH,
        "A# half-diminished 7th"
    ),
    A_SHARP_DIMINISHED_SEVENTH(10, ChordQuality.DIMINISHED, ChordExtension.DIMINISHED_SEVENTH, "A# diminished 7th"),

    B_MAJOR_SEVENTH(11, ChordQuality.MAJOR, ChordExtension.MAJOR_SEVENTH, "B major 7th"),
    B_DOMINANT_SEVENTH(11, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "B dominant 7th"),
    B_MINOR_SEVENTH(11, ChordQuality.MINOR, ChordExtension.MINOR_SEVENTH, "B minor 7th"),
    B_HALF_DIMINISHED_SEVENTH(11, ChordQuality.HALF_DIMINISHED, ChordExtension.MINOR_SEVENTH, "B half-diminished 7th"),
    B_DIMINISHED_SEVENTH(11, ChordQuality.DIMINISHED, ChordExtension.DIMINISHED_SEVENTH, "B diminished 7th"),

    // ── Ninth chords ─────────────────────────────────────────────────────────
    C_MAJOR_NINTH(0, ChordQuality.MAJOR, ChordExtension.MAJOR_NINTH, "C major 9th"),
    C_DOMINANT_NINTH(0, ChordQuality.DOMINANT, ChordExtension.DOMINANT_NINTH, "C dominant 9th"),
    C_MINOR_NINTH(0, ChordQuality.MINOR, ChordExtension.MINOR_NINTH, "C minor 9th"),
    C_SHARP_MAJOR_NINTH(1, ChordQuality.MAJOR, ChordExtension.MAJOR_NINTH, "C# major 9th"),
    C_SHARP_DOMINANT_NINTH(1, ChordQuality.DOMINANT, ChordExtension.DOMINANT_NINTH, "C# dominant 9th"),
    C_SHARP_MINOR_NINTH(1, ChordQuality.MINOR, ChordExtension.MINOR_NINTH, "C# minor 9th"),
    D_MAJOR_NINTH(2, ChordQuality.MAJOR, ChordExtension.MAJOR_NINTH, "D major 9th"),
    D_DOMINANT_NINTH(2, ChordQuality.DOMINANT, ChordExtension.DOMINANT_NINTH, "D dominant 9th"),
    D_MINOR_NINTH(2, ChordQuality.MINOR, ChordExtension.MINOR_NINTH, "D minor 9th"),
    D_SHARP_MAJOR_NINTH(3, ChordQuality.MAJOR, ChordExtension.MAJOR_NINTH, "D# major 9th"),
    D_SHARP_DOMINANT_NINTH(3, ChordQuality.DOMINANT, ChordExtension.DOMINANT_NINTH, "D# dominant 9th"),
    D_SHARP_MINOR_NINTH(3, ChordQuality.MINOR, ChordExtension.MINOR_NINTH, "D# minor 9th"),
    E_MAJOR_NINTH(4, ChordQuality.MAJOR, ChordExtension.MAJOR_NINTH, "E major 9th"),
    E_DOMINANT_NINTH(4, ChordQuality.DOMINANT, ChordExtension.DOMINANT_NINTH, "E dominant 9th"),
    E_MINOR_NINTH(4, ChordQuality.MINOR, ChordExtension.MINOR_NINTH, "E minor 9th"),
    F_MAJOR_NINTH(5, ChordQuality.MAJOR, ChordExtension.MAJOR_NINTH, "F major 9th"),
    F_DOMINANT_NINTH(5, ChordQuality.DOMINANT, ChordExtension.DOMINANT_NINTH, "F dominant 9th"),
    F_MINOR_NINTH(5, ChordQuality.MINOR, ChordExtension.MINOR_NINTH, "F minor 9th"),
    F_SHARP_MAJOR_NINTH(6, ChordQuality.MAJOR, ChordExtension.MAJOR_NINTH, "F# major 9th"),
    F_SHARP_DOMINANT_NINTH(6, ChordQuality.DOMINANT, ChordExtension.DOMINANT_NINTH, "F# dominant 9th"),
    F_SHARP_MINOR_NINTH(6, ChordQuality.MINOR, ChordExtension.MINOR_NINTH, "F# minor 9th"),
    G_MAJOR_NINTH(7, ChordQuality.MAJOR, ChordExtension.MAJOR_NINTH, "G major 9th"),
    G_DOMINANT_NINTH(7, ChordQuality.DOMINANT, ChordExtension.DOMINANT_NINTH, "G dominant 9th"),
    G_MINOR_NINTH(7, ChordQuality.MINOR, ChordExtension.MINOR_NINTH, "G minor 9th"),
    G_SHARP_MAJOR_NINTH(8, ChordQuality.MAJOR, ChordExtension.MAJOR_NINTH, "G# major 9th"),
    G_SHARP_DOMINANT_NINTH(8, ChordQuality.DOMINANT, ChordExtension.DOMINANT_NINTH, "G# dominant 9th"),
    G_SHARP_MINOR_NINTH(8, ChordQuality.MINOR, ChordExtension.MINOR_NINTH, "G# minor 9th"),
    A_MAJOR_NINTH(9, ChordQuality.MAJOR, ChordExtension.MAJOR_NINTH, "A major 9th"),
    A_DOMINANT_NINTH(9, ChordQuality.DOMINANT, ChordExtension.DOMINANT_NINTH, "A dominant 9th"),
    A_MINOR_NINTH(9, ChordQuality.MINOR, ChordExtension.MINOR_NINTH, "A minor 9th"),
    A_SHARP_MAJOR_NINTH(10, ChordQuality.MAJOR, ChordExtension.MAJOR_NINTH, "A# major 9th"),
    A_SHARP_DOMINANT_NINTH(10, ChordQuality.DOMINANT, ChordExtension.DOMINANT_NINTH, "A# dominant 9th"),
    A_SHARP_MINOR_NINTH(10, ChordQuality.MINOR, ChordExtension.MINOR_NINTH, "A# minor 9th"),
    B_MAJOR_NINTH(11, ChordQuality.MAJOR, ChordExtension.MAJOR_NINTH, "B major 9th"),
    B_DOMINANT_NINTH(11, ChordQuality.DOMINANT, ChordExtension.DOMINANT_NINTH, "B dominant 9th"),
    B_MINOR_NINTH(11, ChordQuality.MINOR, ChordExtension.MINOR_NINTH, "B minor 9th"),

    // ── Altered dominant chords ──────────────────────────────────────────────
    C_DOMINANT_SEVENTH_FLAT_FIFTH(
        0,
        ChordQuality.DOMINANT,
        ChordExtension.MINOR_SEVENTH,
        "C dominant 7th b5",
        ChordAlteration.FLAT_FIFTH
    ),
    C_DOMINANT_SEVENTH_SHARP_FIFTH(
        0,
        ChordQuality.DOMINANT,
        ChordExtension.MINOR_SEVENTH,
        "C dominant 7th #5",
        ChordAlteration.SHARP_FIFTH
    ),
    C_SHARP_DOMINANT_SEVENTH_FLAT_FIFTH(1, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "C# dominant 7th b5", ChordAlteration.FLAT_FIFTH),
    C_SHARP_DOMINANT_SEVENTH_SHARP_FIFTH(1, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "C# dominant 7th #5", ChordAlteration.SHARP_FIFTH),
    D_DOMINANT_SEVENTH_FLAT_FIFTH(2, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "D dominant 7th b5", ChordAlteration.FLAT_FIFTH),
    D_DOMINANT_SEVENTH_SHARP_FIFTH(2, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "D dominant 7th #5", ChordAlteration.SHARP_FIFTH),
    D_SHARP_DOMINANT_SEVENTH_FLAT_FIFTH(3, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "D# dominant 7th b5", ChordAlteration.FLAT_FIFTH),
    D_SHARP_DOMINANT_SEVENTH_SHARP_FIFTH(3, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "D# dominant 7th #5", ChordAlteration.SHARP_FIFTH),
    E_DOMINANT_SEVENTH_FLAT_FIFTH(4, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "E dominant 7th b5", ChordAlteration.FLAT_FIFTH),
    E_DOMINANT_SEVENTH_SHARP_FIFTH(4, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "E dominant 7th #5", ChordAlteration.SHARP_FIFTH),
    F_DOMINANT_SEVENTH_FLAT_FIFTH(5, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "F dominant 7th b5", ChordAlteration.FLAT_FIFTH),
    F_DOMINANT_SEVENTH_SHARP_FIFTH(5, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "F dominant 7th #5", ChordAlteration.SHARP_FIFTH),
    F_SHARP_DOMINANT_SEVENTH_FLAT_FIFTH(6, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "F# dominant 7th b5", ChordAlteration.FLAT_FIFTH),
    F_SHARP_DOMINANT_SEVENTH_SHARP_FIFTH(6, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "F# dominant 7th #5", ChordAlteration.SHARP_FIFTH),
    G_DOMINANT_SEVENTH_FLAT_FIFTH(7, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "G dominant 7th b5", ChordAlteration.FLAT_FIFTH),
    G_DOMINANT_SEVENTH_SHARP_FIFTH(7, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "G dominant 7th #5", ChordAlteration.SHARP_FIFTH),
    G_SHARP_DOMINANT_SEVENTH_FLAT_FIFTH(8, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "G# dominant 7th b5", ChordAlteration.FLAT_FIFTH),
    G_SHARP_DOMINANT_SEVENTH_SHARP_FIFTH(8, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "G# dominant 7th #5", ChordAlteration.SHARP_FIFTH),
    A_DOMINANT_SEVENTH_FLAT_FIFTH(9, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "A dominant 7th b5", ChordAlteration.FLAT_FIFTH),
    A_DOMINANT_SEVENTH_SHARP_FIFTH(9, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "A dominant 7th #5", ChordAlteration.SHARP_FIFTH),
    A_SHARP_DOMINANT_SEVENTH_FLAT_FIFTH(10, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "A# dominant 7th b5", ChordAlteration.FLAT_FIFTH),
    A_SHARP_DOMINANT_SEVENTH_SHARP_FIFTH(10, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "A# dominant 7th #5", ChordAlteration.SHARP_FIFTH),
    B_DOMINANT_SEVENTH_FLAT_FIFTH(11, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "B dominant 7th b5", ChordAlteration.FLAT_FIFTH),
    B_DOMINANT_SEVENTH_SHARP_FIFTH(11, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "B dominant 7th #5", ChordAlteration.SHARP_FIFTH),

    UNDEFINED(-1, ChordQuality.MAJOR, ChordExtension.NONE, "");

    companion object {
        fun from(
            pitchClass: Int,
            quality: ChordQuality,
            extension: ChordExtension = ChordExtension.NONE,
            alteration: ChordAlteration = ChordAlteration.NONE
        ): Chord =
            entries.firstOrNull {
                it.pitchClass == pitchClass &&
                        it.quality == quality &&
                        it.extension == extension &&
                        it.alteration == alteration
            }
                ?: UNDEFINED
    }
}
