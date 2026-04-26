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
    C_SHARP_HALF_DIMINISHED_SEVENTH(
        1,
        ChordQuality.HALF_DIMINISHED,
        ChordExtension.MINOR_SEVENTH,
        "C# half-diminished 7th"
    ),
    C_SHARP_DIMINISHED_SEVENTH(1, ChordQuality.DIMINISHED, ChordExtension.DIMINISHED_SEVENTH, "C# diminished 7th"),

    D_MAJOR_SEVENTH(2, ChordQuality.MAJOR, ChordExtension.MAJOR_SEVENTH, "D major 7th"),
    D_DOMINANT_SEVENTH(2, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "D dominant 7th"),
    D_MINOR_SEVENTH(2, ChordQuality.MINOR, ChordExtension.MINOR_SEVENTH, "D minor 7th"),
    D_HALF_DIMINISHED_SEVENTH(2, ChordQuality.HALF_DIMINISHED, ChordExtension.MINOR_SEVENTH, "D half-diminished 7th"),
    D_DIMINISHED_SEVENTH(2, ChordQuality.DIMINISHED, ChordExtension.DIMINISHED_SEVENTH, "D diminished 7th"),

    D_SHARP_MAJOR_SEVENTH(3, ChordQuality.MAJOR, ChordExtension.MAJOR_SEVENTH, "D# major 7th"),
    D_SHARP_DOMINANT_SEVENTH(3, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "D# dominant 7th"),
    D_SHARP_MINOR_SEVENTH(3, ChordQuality.MINOR, ChordExtension.MINOR_SEVENTH, "D# minor 7th"),
    D_SHARP_HALF_DIMINISHED_SEVENTH(
        3,
        ChordQuality.HALF_DIMINISHED,
        ChordExtension.MINOR_SEVENTH,
        "D# half-diminished 7th"
    ),
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
    F_SHARP_HALF_DIMINISHED_SEVENTH(
        6,
        ChordQuality.HALF_DIMINISHED,
        ChordExtension.MINOR_SEVENTH,
        "F# half-diminished 7th"
    ),
    F_SHARP_DIMINISHED_SEVENTH(6, ChordQuality.DIMINISHED, ChordExtension.DIMINISHED_SEVENTH, "F# diminished 7th"),

    G_MAJOR_SEVENTH(7, ChordQuality.MAJOR, ChordExtension.MAJOR_SEVENTH, "G major 7th"),
    G_DOMINANT_SEVENTH(7, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "G dominant 7th"),
    G_MINOR_SEVENTH(7, ChordQuality.MINOR, ChordExtension.MINOR_SEVENTH, "G minor 7th"),
    G_HALF_DIMINISHED_SEVENTH(7, ChordQuality.HALF_DIMINISHED, ChordExtension.MINOR_SEVENTH, "G half-diminished 7th"),
    G_DIMINISHED_SEVENTH(7, ChordQuality.DIMINISHED, ChordExtension.DIMINISHED_SEVENTH, "G diminished 7th"),

    G_SHARP_MAJOR_SEVENTH(8, ChordQuality.MAJOR, ChordExtension.MAJOR_SEVENTH, "G# major 7th"),
    G_SHARP_DOMINANT_SEVENTH(8, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, "G# dominant 7th"),
    G_SHARP_MINOR_SEVENTH(8, ChordQuality.MINOR, ChordExtension.MINOR_SEVENTH, "G# minor 7th"),
    G_SHARP_HALF_DIMINISHED_SEVENTH(
        8,
        ChordQuality.HALF_DIMINISHED,
        ChordExtension.MINOR_SEVENTH,
        "G# half-diminished 7th"
    ),
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
    C_SHARP_DOMINANT_SEVENTH_FLAT_FIFTH(
        1,
        ChordQuality.DOMINANT,
        ChordExtension.MINOR_SEVENTH,
        "C# dominant 7th b5",
        ChordAlteration.FLAT_FIFTH
    ),
    C_SHARP_DOMINANT_SEVENTH_SHARP_FIFTH(
        1,
        ChordQuality.DOMINANT,
        ChordExtension.MINOR_SEVENTH,
        "C# dominant 7th #5",
        ChordAlteration.SHARP_FIFTH
    ),
    D_DOMINANT_SEVENTH_FLAT_FIFTH(
        2,
        ChordQuality.DOMINANT,
        ChordExtension.MINOR_SEVENTH,
        "D dominant 7th b5",
        ChordAlteration.FLAT_FIFTH
    ),
    D_DOMINANT_SEVENTH_SHARP_FIFTH(
        2,
        ChordQuality.DOMINANT,
        ChordExtension.MINOR_SEVENTH,
        "D dominant 7th #5",
        ChordAlteration.SHARP_FIFTH
    ),
    D_SHARP_DOMINANT_SEVENTH_FLAT_FIFTH(
        3,
        ChordQuality.DOMINANT,
        ChordExtension.MINOR_SEVENTH,
        "D# dominant 7th b5",
        ChordAlteration.FLAT_FIFTH
    ),
    D_SHARP_DOMINANT_SEVENTH_SHARP_FIFTH(
        3,
        ChordQuality.DOMINANT,
        ChordExtension.MINOR_SEVENTH,
        "D# dominant 7th #5",
        ChordAlteration.SHARP_FIFTH
    ),
    E_DOMINANT_SEVENTH_FLAT_FIFTH(
        4,
        ChordQuality.DOMINANT,
        ChordExtension.MINOR_SEVENTH,
        "E dominant 7th b5",
        ChordAlteration.FLAT_FIFTH
    ),
    E_DOMINANT_SEVENTH_SHARP_FIFTH(
        4,
        ChordQuality.DOMINANT,
        ChordExtension.MINOR_SEVENTH,
        "E dominant 7th #5",
        ChordAlteration.SHARP_FIFTH
    ),
    F_DOMINANT_SEVENTH_FLAT_FIFTH(
        5,
        ChordQuality.DOMINANT,
        ChordExtension.MINOR_SEVENTH,
        "F dominant 7th b5",
        ChordAlteration.FLAT_FIFTH
    ),
    F_DOMINANT_SEVENTH_SHARP_FIFTH(
        5,
        ChordQuality.DOMINANT,
        ChordExtension.MINOR_SEVENTH,
        "F dominant 7th #5",
        ChordAlteration.SHARP_FIFTH
    ),
    F_SHARP_DOMINANT_SEVENTH_FLAT_FIFTH(
        6,
        ChordQuality.DOMINANT,
        ChordExtension.MINOR_SEVENTH,
        "F# dominant 7th b5",
        ChordAlteration.FLAT_FIFTH
    ),
    F_SHARP_DOMINANT_SEVENTH_SHARP_FIFTH(
        6,
        ChordQuality.DOMINANT,
        ChordExtension.MINOR_SEVENTH,
        "F# dominant 7th #5",
        ChordAlteration.SHARP_FIFTH
    ),
    G_DOMINANT_SEVENTH_FLAT_FIFTH(
        7,
        ChordQuality.DOMINANT,
        ChordExtension.MINOR_SEVENTH,
        "G dominant 7th b5",
        ChordAlteration.FLAT_FIFTH
    ),
    G_DOMINANT_SEVENTH_SHARP_FIFTH(
        7,
        ChordQuality.DOMINANT,
        ChordExtension.MINOR_SEVENTH,
        "G dominant 7th #5",
        ChordAlteration.SHARP_FIFTH
    ),
    G_SHARP_DOMINANT_SEVENTH_FLAT_FIFTH(
        8,
        ChordQuality.DOMINANT,
        ChordExtension.MINOR_SEVENTH,
        "G# dominant 7th b5",
        ChordAlteration.FLAT_FIFTH
    ),
    G_SHARP_DOMINANT_SEVENTH_SHARP_FIFTH(
        8,
        ChordQuality.DOMINANT,
        ChordExtension.MINOR_SEVENTH,
        "G# dominant 7th #5",
        ChordAlteration.SHARP_FIFTH
    ),
    A_DOMINANT_SEVENTH_FLAT_FIFTH(
        9,
        ChordQuality.DOMINANT,
        ChordExtension.MINOR_SEVENTH,
        "A dominant 7th b5",
        ChordAlteration.FLAT_FIFTH
    ),
    A_DOMINANT_SEVENTH_SHARP_FIFTH(
        9,
        ChordQuality.DOMINANT,
        ChordExtension.MINOR_SEVENTH,
        "A dominant 7th #5",
        ChordAlteration.SHARP_FIFTH
    ),
    A_SHARP_DOMINANT_SEVENTH_FLAT_FIFTH(
        10,
        ChordQuality.DOMINANT,
        ChordExtension.MINOR_SEVENTH,
        "A# dominant 7th b5",
        ChordAlteration.FLAT_FIFTH
    ),
    A_SHARP_DOMINANT_SEVENTH_SHARP_FIFTH(
        10,
        ChordQuality.DOMINANT,
        ChordExtension.MINOR_SEVENTH,
        "A# dominant 7th #5",
        ChordAlteration.SHARP_FIFTH
    ),
    B_DOMINANT_SEVENTH_FLAT_FIFTH(
        11,
        ChordQuality.DOMINANT,
        ChordExtension.MINOR_SEVENTH,
        "B dominant 7th b5",
        ChordAlteration.FLAT_FIFTH
    ),
    B_DOMINANT_SEVENTH_SHARP_FIFTH(
        11,
        ChordQuality.DOMINANT,
        ChordExtension.MINOR_SEVENTH,
        "B dominant 7th #5",
        ChordAlteration.SHARP_FIFTH
    ),

    // ── Augmented triads ──────────────────────────────────────────────────────
    C_AUGMENTED(0, ChordQuality.AUGMENTED, ChordExtension.NONE, "C augmented"),
    C_SHARP_AUGMENTED(1, ChordQuality.AUGMENTED, ChordExtension.NONE, "C# augmented"),
    D_AUGMENTED(2, ChordQuality.AUGMENTED, ChordExtension.NONE, "D augmented"),
    D_SHARP_AUGMENTED(3, ChordQuality.AUGMENTED, ChordExtension.NONE, "D# augmented"),
    E_AUGMENTED(4, ChordQuality.AUGMENTED, ChordExtension.NONE, "E augmented"),
    F_AUGMENTED(5, ChordQuality.AUGMENTED, ChordExtension.NONE, "F augmented"),
    F_SHARP_AUGMENTED(6, ChordQuality.AUGMENTED, ChordExtension.NONE, "F# augmented"),
    G_AUGMENTED(7, ChordQuality.AUGMENTED, ChordExtension.NONE, "G augmented"),
    G_SHARP_AUGMENTED(8, ChordQuality.AUGMENTED, ChordExtension.NONE, "G# augmented"),
    A_AUGMENTED(9, ChordQuality.AUGMENTED, ChordExtension.NONE, "A augmented"),
    A_SHARP_AUGMENTED(10, ChordQuality.AUGMENTED, ChordExtension.NONE, "A# augmented"),
    B_AUGMENTED(11, ChordQuality.AUGMENTED, ChordExtension.NONE, "B augmented"),

    // ── Diminished triads ─────────────────────────────────────────────────────
    C_DIMINISHED(0, ChordQuality.DIMINISHED, ChordExtension.NONE, "C diminished"),
    C_SHARP_DIMINISHED(1, ChordQuality.DIMINISHED, ChordExtension.NONE, "C# diminished"),
    D_DIMINISHED(2, ChordQuality.DIMINISHED, ChordExtension.NONE, "D diminished"),
    D_SHARP_DIMINISHED(3, ChordQuality.DIMINISHED, ChordExtension.NONE, "D# diminished"),
    E_DIMINISHED(4, ChordQuality.DIMINISHED, ChordExtension.NONE, "E diminished"),
    F_DIMINISHED(5, ChordQuality.DIMINISHED, ChordExtension.NONE, "F diminished"),
    F_SHARP_DIMINISHED(6, ChordQuality.DIMINISHED, ChordExtension.NONE, "F# diminished"),
    G_DIMINISHED(7, ChordQuality.DIMINISHED, ChordExtension.NONE, "G diminished"),
    G_SHARP_DIMINISHED(8, ChordQuality.DIMINISHED, ChordExtension.NONE, "G# diminished"),
    A_DIMINISHED(9, ChordQuality.DIMINISHED, ChordExtension.NONE, "A diminished"),
    A_SHARP_DIMINISHED(10, ChordQuality.DIMINISHED, ChordExtension.NONE, "A# diminished"),
    B_DIMINISHED(11, ChordQuality.DIMINISHED, ChordExtension.NONE, "B diminished"),

    // ── Suspended 4th triads ──────────────────────────────────────────────────
    C_SUSPENDED_FOURTH(0, ChordQuality.SUSPENDED_FOURTH, ChordExtension.NONE, "C sus4"),
    C_SHARP_SUSPENDED_FOURTH(1, ChordQuality.SUSPENDED_FOURTH, ChordExtension.NONE, "C# sus4"),
    D_SUSPENDED_FOURTH(2, ChordQuality.SUSPENDED_FOURTH, ChordExtension.NONE, "D sus4"),
    D_SHARP_SUSPENDED_FOURTH(3, ChordQuality.SUSPENDED_FOURTH, ChordExtension.NONE, "D# sus4"),
    E_SUSPENDED_FOURTH(4, ChordQuality.SUSPENDED_FOURTH, ChordExtension.NONE, "E sus4"),
    F_SUSPENDED_FOURTH(5, ChordQuality.SUSPENDED_FOURTH, ChordExtension.NONE, "F sus4"),
    F_SHARP_SUSPENDED_FOURTH(6, ChordQuality.SUSPENDED_FOURTH, ChordExtension.NONE, "F# sus4"),
    G_SUSPENDED_FOURTH(7, ChordQuality.SUSPENDED_FOURTH, ChordExtension.NONE, "G sus4"),
    G_SHARP_SUSPENDED_FOURTH(8, ChordQuality.SUSPENDED_FOURTH, ChordExtension.NONE, "G# sus4"),
    A_SUSPENDED_FOURTH(9, ChordQuality.SUSPENDED_FOURTH, ChordExtension.NONE, "A sus4"),
    A_SHARP_SUSPENDED_FOURTH(10, ChordQuality.SUSPENDED_FOURTH, ChordExtension.NONE, "A# sus4"),
    B_SUSPENDED_FOURTH(11, ChordQuality.SUSPENDED_FOURTH, ChordExtension.NONE, "B sus4"),

    // ── Suspended 2nd triads ──────────────────────────────────────────────────
    C_SUSPENDED_SECOND(0, ChordQuality.SUSPENDED_SECOND, ChordExtension.NONE, "C sus2"),
    C_SHARP_SUSPENDED_SECOND(1, ChordQuality.SUSPENDED_SECOND, ChordExtension.NONE, "C# sus2"),
    D_SUSPENDED_SECOND(2, ChordQuality.SUSPENDED_SECOND, ChordExtension.NONE, "D sus2"),
    D_SHARP_SUSPENDED_SECOND(3, ChordQuality.SUSPENDED_SECOND, ChordExtension.NONE, "D# sus2"),
    E_SUSPENDED_SECOND(4, ChordQuality.SUSPENDED_SECOND, ChordExtension.NONE, "E sus2"),
    F_SUSPENDED_SECOND(5, ChordQuality.SUSPENDED_SECOND, ChordExtension.NONE, "F sus2"),
    F_SHARP_SUSPENDED_SECOND(6, ChordQuality.SUSPENDED_SECOND, ChordExtension.NONE, "F# sus2"),
    G_SUSPENDED_SECOND(7, ChordQuality.SUSPENDED_SECOND, ChordExtension.NONE, "G sus2"),
    G_SHARP_SUSPENDED_SECOND(8, ChordQuality.SUSPENDED_SECOND, ChordExtension.NONE, "G# sus2"),
    A_SUSPENDED_SECOND(9, ChordQuality.SUSPENDED_SECOND, ChordExtension.NONE, "A sus2"),
    A_SHARP_SUSPENDED_SECOND(10, ChordQuality.SUSPENDED_SECOND, ChordExtension.NONE, "A# sus2"),
    B_SUSPENDED_SECOND(11, ChordQuality.SUSPENDED_SECOND, ChordExtension.NONE, "B sus2"),

    // ── Add 9 chords ───────────────────────────────────────────────────────────
    C_MAJOR_ADD_NINTH(0, ChordQuality.MAJOR, ChordExtension.ADD_NINTH, "C add9"),
    C_SHARP_MAJOR_ADD_NINTH(1, ChordQuality.MAJOR, ChordExtension.ADD_NINTH, "C# add9"),
    D_MAJOR_ADD_NINTH(2, ChordQuality.MAJOR, ChordExtension.ADD_NINTH, "D add9"),
    D_SHARP_MAJOR_ADD_NINTH(3, ChordQuality.MAJOR, ChordExtension.ADD_NINTH, "D# add9"),
    E_MAJOR_ADD_NINTH(4, ChordQuality.MAJOR, ChordExtension.ADD_NINTH, "E add9"),
    F_MAJOR_ADD_NINTH(5, ChordQuality.MAJOR, ChordExtension.ADD_NINTH, "F add9"),
    F_SHARP_MAJOR_ADD_NINTH(6, ChordQuality.MAJOR, ChordExtension.ADD_NINTH, "F# add9"),
    G_MAJOR_ADD_NINTH(7, ChordQuality.MAJOR, ChordExtension.ADD_NINTH, "G add9"),
    G_SHARP_MAJOR_ADD_NINTH(8, ChordQuality.MAJOR, ChordExtension.ADD_NINTH, "G# add9"),
    A_MAJOR_ADD_NINTH(9, ChordQuality.MAJOR, ChordExtension.ADD_NINTH, "A add9"),
    A_SHARP_MAJOR_ADD_NINTH(10, ChordQuality.MAJOR, ChordExtension.ADD_NINTH, "A# add9"),
    B_MAJOR_ADD_NINTH(11, ChordQuality.MAJOR, ChordExtension.ADD_NINTH, "B add9"),

    // ── Major 6th chords ──────────────────────────────────────────────────────
    C_MAJOR_SIXTH(0, ChordQuality.MAJOR, ChordExtension.SIXTH, "C 6"),
    C_SHARP_MAJOR_SIXTH(1, ChordQuality.MAJOR, ChordExtension.SIXTH, "C# 6"),
    D_MAJOR_SIXTH(2, ChordQuality.MAJOR, ChordExtension.SIXTH, "D 6"),
    D_SHARP_MAJOR_SIXTH(3, ChordQuality.MAJOR, ChordExtension.SIXTH, "D# 6"),
    E_MAJOR_SIXTH(4, ChordQuality.MAJOR, ChordExtension.SIXTH, "E 6"),
    F_MAJOR_SIXTH(5, ChordQuality.MAJOR, ChordExtension.SIXTH, "F 6"),
    F_SHARP_MAJOR_SIXTH(6, ChordQuality.MAJOR, ChordExtension.SIXTH, "F# 6"),
    G_MAJOR_SIXTH(7, ChordQuality.MAJOR, ChordExtension.SIXTH, "G 6"),
    G_SHARP_MAJOR_SIXTH(8, ChordQuality.MAJOR, ChordExtension.SIXTH, "G# 6"),
    A_MAJOR_SIXTH(9, ChordQuality.MAJOR, ChordExtension.SIXTH, "A 6"),
    A_SHARP_MAJOR_SIXTH(10, ChordQuality.MAJOR, ChordExtension.SIXTH, "A# 6"),
    B_MAJOR_SIXTH(11, ChordQuality.MAJOR, ChordExtension.SIXTH, "B 6"),

    // ── Minor 6th chords ──────────────────────────────────────────────────────
    C_MINOR_SIXTH(0, ChordQuality.MINOR, ChordExtension.SIXTH, "C minor 6"),
    C_SHARP_MINOR_SIXTH(1, ChordQuality.MINOR, ChordExtension.SIXTH, "C# minor 6"),
    D_MINOR_SIXTH(2, ChordQuality.MINOR, ChordExtension.SIXTH, "D minor 6"),
    D_SHARP_MINOR_SIXTH(3, ChordQuality.MINOR, ChordExtension.SIXTH, "D# minor 6"),
    E_MINOR_SIXTH(4, ChordQuality.MINOR, ChordExtension.SIXTH, "E minor 6"),
    F_MINOR_SIXTH(5, ChordQuality.MINOR, ChordExtension.SIXTH, "F minor 6"),
    F_SHARP_MINOR_SIXTH(6, ChordQuality.MINOR, ChordExtension.SIXTH, "F# minor 6"),
    G_MINOR_SIXTH(7, ChordQuality.MINOR, ChordExtension.SIXTH, "G minor 6"),
    G_SHARP_MINOR_SIXTH(8, ChordQuality.MINOR, ChordExtension.SIXTH, "G# minor 6"),
    A_MINOR_SIXTH(9, ChordQuality.MINOR, ChordExtension.SIXTH, "A minor 6"),
    A_SHARP_MINOR_SIXTH(10, ChordQuality.MINOR, ChordExtension.SIXTH, "A# minor 6"),
    B_MINOR_SIXTH(11, ChordQuality.MINOR, ChordExtension.SIXTH, "B minor 6"),

    // ── 7sus4 chords ──────────────────────────────────────────────────────────
    C_SEVENTH_SUSPENDED_FOURTH(0, ChordQuality.SUSPENDED_FOURTH, ChordExtension.MINOR_SEVENTH, "C 7sus4"),
    C_SHARP_SEVENTH_SUSPENDED_FOURTH(1, ChordQuality.SUSPENDED_FOURTH, ChordExtension.MINOR_SEVENTH, "C# 7sus4"),
    D_SEVENTH_SUSPENDED_FOURTH(2, ChordQuality.SUSPENDED_FOURTH, ChordExtension.MINOR_SEVENTH, "D 7sus4"),
    D_SHARP_SEVENTH_SUSPENDED_FOURTH(3, ChordQuality.SUSPENDED_FOURTH, ChordExtension.MINOR_SEVENTH, "D# 7sus4"),
    E_SEVENTH_SUSPENDED_FOURTH(4, ChordQuality.SUSPENDED_FOURTH, ChordExtension.MINOR_SEVENTH, "E 7sus4"),
    F_SEVENTH_SUSPENDED_FOURTH(5, ChordQuality.SUSPENDED_FOURTH, ChordExtension.MINOR_SEVENTH, "F 7sus4"),
    F_SHARP_SEVENTH_SUSPENDED_FOURTH(6, ChordQuality.SUSPENDED_FOURTH, ChordExtension.MINOR_SEVENTH, "F# 7sus4"),
    G_SEVENTH_SUSPENDED_FOURTH(7, ChordQuality.SUSPENDED_FOURTH, ChordExtension.MINOR_SEVENTH, "G 7sus4"),
    G_SHARP_SEVENTH_SUSPENDED_FOURTH(8, ChordQuality.SUSPENDED_FOURTH, ChordExtension.MINOR_SEVENTH, "G# 7sus4"),
    A_SEVENTH_SUSPENDED_FOURTH(9, ChordQuality.SUSPENDED_FOURTH, ChordExtension.MINOR_SEVENTH, "A 7sus4"),
    A_SHARP_SEVENTH_SUSPENDED_FOURTH(10, ChordQuality.SUSPENDED_FOURTH, ChordExtension.MINOR_SEVENTH, "A# 7sus4"),
    B_SEVENTH_SUSPENDED_FOURTH(11, ChordQuality.SUSPENDED_FOURTH, ChordExtension.MINOR_SEVENTH, "B 7sus4"),

    // ── 9sus4 chords ──────────────────────────────────────────────────────────
    C_NINTH_SUSPENDED_FOURTH(0, ChordQuality.SUSPENDED_FOURTH, ChordExtension.NINTH_SUSPENDED_FOURTH, "C 9sus4"),
    C_SHARP_NINTH_SUSPENDED_FOURTH(1, ChordQuality.SUSPENDED_FOURTH, ChordExtension.NINTH_SUSPENDED_FOURTH, "C# 9sus4"),
    D_NINTH_SUSPENDED_FOURTH(2, ChordQuality.SUSPENDED_FOURTH, ChordExtension.NINTH_SUSPENDED_FOURTH, "D 9sus4"),
    D_SHARP_NINTH_SUSPENDED_FOURTH(3, ChordQuality.SUSPENDED_FOURTH, ChordExtension.NINTH_SUSPENDED_FOURTH, "D# 9sus4"),
    E_NINTH_SUSPENDED_FOURTH(4, ChordQuality.SUSPENDED_FOURTH, ChordExtension.NINTH_SUSPENDED_FOURTH, "E 9sus4"),
    F_NINTH_SUSPENDED_FOURTH(5, ChordQuality.SUSPENDED_FOURTH, ChordExtension.NINTH_SUSPENDED_FOURTH, "F 9sus4"),
    F_SHARP_NINTH_SUSPENDED_FOURTH(6, ChordQuality.SUSPENDED_FOURTH, ChordExtension.NINTH_SUSPENDED_FOURTH, "F# 9sus4"),
    G_NINTH_SUSPENDED_FOURTH(7, ChordQuality.SUSPENDED_FOURTH, ChordExtension.NINTH_SUSPENDED_FOURTH, "G 9sus4"),
    G_SHARP_NINTH_SUSPENDED_FOURTH(8, ChordQuality.SUSPENDED_FOURTH, ChordExtension.NINTH_SUSPENDED_FOURTH, "G# 9sus4"),
    A_NINTH_SUSPENDED_FOURTH(9, ChordQuality.SUSPENDED_FOURTH, ChordExtension.NINTH_SUSPENDED_FOURTH, "A 9sus4"),
    A_SHARP_NINTH_SUSPENDED_FOURTH(
        10,
        ChordQuality.SUSPENDED_FOURTH,
        ChordExtension.NINTH_SUSPENDED_FOURTH,
        "A# 9sus4"
    ),
    B_NINTH_SUSPENDED_FOURTH(11, ChordQuality.SUSPENDED_FOURTH, ChordExtension.NINTH_SUSPENDED_FOURTH, "B 9sus4"),

    // ── Diminished ninth chords ───────────────────────────────────────────────
    C_DIMINISHED_NINTH(0, ChordQuality.DIMINISHED, ChordExtension.DIMINISHED_NINTH, "C diminished 9th"),
    C_SHARP_DIMINISHED_NINTH(1, ChordQuality.DIMINISHED, ChordExtension.DIMINISHED_NINTH, "C# diminished 9th"),
    D_DIMINISHED_NINTH(2, ChordQuality.DIMINISHED, ChordExtension.DIMINISHED_NINTH, "D diminished 9th"),
    D_SHARP_DIMINISHED_NINTH(3, ChordQuality.DIMINISHED, ChordExtension.DIMINISHED_NINTH, "D# diminished 9th"),
    E_DIMINISHED_NINTH(4, ChordQuality.DIMINISHED, ChordExtension.DIMINISHED_NINTH, "E diminished 9th"),
    F_DIMINISHED_NINTH(5, ChordQuality.DIMINISHED, ChordExtension.DIMINISHED_NINTH, "F diminished 9th"),
    F_SHARP_DIMINISHED_NINTH(6, ChordQuality.DIMINISHED, ChordExtension.DIMINISHED_NINTH, "F# diminished 9th"),
    G_DIMINISHED_NINTH(7, ChordQuality.DIMINISHED, ChordExtension.DIMINISHED_NINTH, "G diminished 9th"),
    G_SHARP_DIMINISHED_NINTH(8, ChordQuality.DIMINISHED, ChordExtension.DIMINISHED_NINTH, "G# diminished 9th"),
    A_DIMINISHED_NINTH(9, ChordQuality.DIMINISHED, ChordExtension.DIMINISHED_NINTH, "A diminished 9th"),
    A_SHARP_DIMINISHED_NINTH(10, ChordQuality.DIMINISHED, ChordExtension.DIMINISHED_NINTH, "A# diminished 9th"),
    B_DIMINISHED_NINTH(11, ChordQuality.DIMINISHED, ChordExtension.DIMINISHED_NINTH, "B diminished 9th"),

    // ── Major 11th chords ─────────────────────────────────────────────────────
    C_MAJOR_ELEVENTH(0, ChordQuality.MAJOR, ChordExtension.MAJOR_ELEVENTH, "C major 11th"),
    C_SHARP_MAJOR_ELEVENTH(1, ChordQuality.MAJOR, ChordExtension.MAJOR_ELEVENTH, "C# major 11th"),
    D_MAJOR_ELEVENTH(2, ChordQuality.MAJOR, ChordExtension.MAJOR_ELEVENTH, "D major 11th"),
    D_SHARP_MAJOR_ELEVENTH(3, ChordQuality.MAJOR, ChordExtension.MAJOR_ELEVENTH, "D# major 11th"),
    E_MAJOR_ELEVENTH(4, ChordQuality.MAJOR, ChordExtension.MAJOR_ELEVENTH, "E major 11th"),
    F_MAJOR_ELEVENTH(5, ChordQuality.MAJOR, ChordExtension.MAJOR_ELEVENTH, "F major 11th"),
    F_SHARP_MAJOR_ELEVENTH(6, ChordQuality.MAJOR, ChordExtension.MAJOR_ELEVENTH, "F# major 11th"),
    G_MAJOR_ELEVENTH(7, ChordQuality.MAJOR, ChordExtension.MAJOR_ELEVENTH, "G major 11th"),
    G_SHARP_MAJOR_ELEVENTH(8, ChordQuality.MAJOR, ChordExtension.MAJOR_ELEVENTH, "G# major 11th"),
    A_MAJOR_ELEVENTH(9, ChordQuality.MAJOR, ChordExtension.MAJOR_ELEVENTH, "A major 11th"),
    A_SHARP_MAJOR_ELEVENTH(10, ChordQuality.MAJOR, ChordExtension.MAJOR_ELEVENTH, "A# major 11th"),
    B_MAJOR_ELEVENTH(11, ChordQuality.MAJOR, ChordExtension.MAJOR_ELEVENTH, "B major 11th"),

    // ── Dominant 11th chords ──────────────────────────────────────────────────
    C_DOMINANT_ELEVENTH(0, ChordQuality.DOMINANT, ChordExtension.DOMINANT_ELEVENTH, "C dominant 11th"),
    C_SHARP_DOMINANT_ELEVENTH(1, ChordQuality.DOMINANT, ChordExtension.DOMINANT_ELEVENTH, "C# dominant 11th"),
    D_DOMINANT_ELEVENTH(2, ChordQuality.DOMINANT, ChordExtension.DOMINANT_ELEVENTH, "D dominant 11th"),
    D_SHARP_DOMINANT_ELEVENTH(3, ChordQuality.DOMINANT, ChordExtension.DOMINANT_ELEVENTH, "D# dominant 11th"),
    E_DOMINANT_ELEVENTH(4, ChordQuality.DOMINANT, ChordExtension.DOMINANT_ELEVENTH, "E dominant 11th"),
    F_DOMINANT_ELEVENTH(5, ChordQuality.DOMINANT, ChordExtension.DOMINANT_ELEVENTH, "F dominant 11th"),
    F_SHARP_DOMINANT_ELEVENTH(6, ChordQuality.DOMINANT, ChordExtension.DOMINANT_ELEVENTH, "F# dominant 11th"),
    G_DOMINANT_ELEVENTH(7, ChordQuality.DOMINANT, ChordExtension.DOMINANT_ELEVENTH, "G dominant 11th"),
    G_SHARP_DOMINANT_ELEVENTH(8, ChordQuality.DOMINANT, ChordExtension.DOMINANT_ELEVENTH, "G# dominant 11th"),
    A_DOMINANT_ELEVENTH(9, ChordQuality.DOMINANT, ChordExtension.DOMINANT_ELEVENTH, "A dominant 11th"),
    A_SHARP_DOMINANT_ELEVENTH(10, ChordQuality.DOMINANT, ChordExtension.DOMINANT_ELEVENTH, "A# dominant 11th"),
    B_DOMINANT_ELEVENTH(11, ChordQuality.DOMINANT, ChordExtension.DOMINANT_ELEVENTH, "B dominant 11th"),

    // ── Minor 11th chords ─────────────────────────────────────────────────────
    C_MINOR_ELEVENTH(0, ChordQuality.MINOR, ChordExtension.MINOR_ELEVENTH, "C minor 11th"),
    C_SHARP_MINOR_ELEVENTH(1, ChordQuality.MINOR, ChordExtension.MINOR_ELEVENTH, "C# minor 11th"),
    D_MINOR_ELEVENTH(2, ChordQuality.MINOR, ChordExtension.MINOR_ELEVENTH, "D minor 11th"),
    D_SHARP_MINOR_ELEVENTH(3, ChordQuality.MINOR, ChordExtension.MINOR_ELEVENTH, "D# minor 11th"),
    E_MINOR_ELEVENTH(4, ChordQuality.MINOR, ChordExtension.MINOR_ELEVENTH, "E minor 11th"),
    F_MINOR_ELEVENTH(5, ChordQuality.MINOR, ChordExtension.MINOR_ELEVENTH, "F minor 11th"),
    F_SHARP_MINOR_ELEVENTH(6, ChordQuality.MINOR, ChordExtension.MINOR_ELEVENTH, "F# minor 11th"),
    G_MINOR_ELEVENTH(7, ChordQuality.MINOR, ChordExtension.MINOR_ELEVENTH, "G minor 11th"),
    G_SHARP_MINOR_ELEVENTH(8, ChordQuality.MINOR, ChordExtension.MINOR_ELEVENTH, "G# minor 11th"),
    A_MINOR_ELEVENTH(9, ChordQuality.MINOR, ChordExtension.MINOR_ELEVENTH, "A minor 11th"),
    A_SHARP_MINOR_ELEVENTH(10, ChordQuality.MINOR, ChordExtension.MINOR_ELEVENTH, "A# minor 11th"),
    B_MINOR_ELEVENTH(11, ChordQuality.MINOR, ChordExtension.MINOR_ELEVENTH, "B minor 11th"),

    // ── Major 13th chords ─────────────────────────────────────────────────────
    C_MAJOR_THIRTEENTH(0, ChordQuality.MAJOR, ChordExtension.MAJOR_THIRTEENTH, "C major 13th"),
    C_SHARP_MAJOR_THIRTEENTH(1, ChordQuality.MAJOR, ChordExtension.MAJOR_THIRTEENTH, "C# major 13th"),
    D_MAJOR_THIRTEENTH(2, ChordQuality.MAJOR, ChordExtension.MAJOR_THIRTEENTH, "D major 13th"),
    D_SHARP_MAJOR_THIRTEENTH(3, ChordQuality.MAJOR, ChordExtension.MAJOR_THIRTEENTH, "D# major 13th"),
    E_MAJOR_THIRTEENTH(4, ChordQuality.MAJOR, ChordExtension.MAJOR_THIRTEENTH, "E major 13th"),
    F_MAJOR_THIRTEENTH(5, ChordQuality.MAJOR, ChordExtension.MAJOR_THIRTEENTH, "F major 13th"),
    F_SHARP_MAJOR_THIRTEENTH(6, ChordQuality.MAJOR, ChordExtension.MAJOR_THIRTEENTH, "F# major 13th"),
    G_MAJOR_THIRTEENTH(7, ChordQuality.MAJOR, ChordExtension.MAJOR_THIRTEENTH, "G major 13th"),
    G_SHARP_MAJOR_THIRTEENTH(8, ChordQuality.MAJOR, ChordExtension.MAJOR_THIRTEENTH, "G# major 13th"),
    A_MAJOR_THIRTEENTH(9, ChordQuality.MAJOR, ChordExtension.MAJOR_THIRTEENTH, "A major 13th"),
    A_SHARP_MAJOR_THIRTEENTH(10, ChordQuality.MAJOR, ChordExtension.MAJOR_THIRTEENTH, "A# major 13th"),
    B_MAJOR_THIRTEENTH(11, ChordQuality.MAJOR, ChordExtension.MAJOR_THIRTEENTH, "B major 13th"),

    // ── Dominant 13th chords ──────────────────────────────────────────────────
    C_DOMINANT_THIRTEENTH(0, ChordQuality.DOMINANT, ChordExtension.DOMINANT_THIRTEENTH, "C dominant 13th"),
    C_SHARP_DOMINANT_THIRTEENTH(1, ChordQuality.DOMINANT, ChordExtension.DOMINANT_THIRTEENTH, "C# dominant 13th"),
    D_DOMINANT_THIRTEENTH(2, ChordQuality.DOMINANT, ChordExtension.DOMINANT_THIRTEENTH, "D dominant 13th"),
    D_SHARP_DOMINANT_THIRTEENTH(3, ChordQuality.DOMINANT, ChordExtension.DOMINANT_THIRTEENTH, "D# dominant 13th"),
    E_DOMINANT_THIRTEENTH(4, ChordQuality.DOMINANT, ChordExtension.DOMINANT_THIRTEENTH, "E dominant 13th"),
    F_DOMINANT_THIRTEENTH(5, ChordQuality.DOMINANT, ChordExtension.DOMINANT_THIRTEENTH, "F dominant 13th"),
    F_SHARP_DOMINANT_THIRTEENTH(6, ChordQuality.DOMINANT, ChordExtension.DOMINANT_THIRTEENTH, "F# dominant 13th"),
    G_DOMINANT_THIRTEENTH(7, ChordQuality.DOMINANT, ChordExtension.DOMINANT_THIRTEENTH, "G dominant 13th"),
    G_SHARP_DOMINANT_THIRTEENTH(8, ChordQuality.DOMINANT, ChordExtension.DOMINANT_THIRTEENTH, "G# dominant 13th"),
    A_DOMINANT_THIRTEENTH(9, ChordQuality.DOMINANT, ChordExtension.DOMINANT_THIRTEENTH, "A dominant 13th"),
    A_SHARP_DOMINANT_THIRTEENTH(10, ChordQuality.DOMINANT, ChordExtension.DOMINANT_THIRTEENTH, "A# dominant 13th"),
    B_DOMINANT_THIRTEENTH(11, ChordQuality.DOMINANT, ChordExtension.DOMINANT_THIRTEENTH, "B dominant 13th"),

    // ── Minor 13th chords ─────────────────────────────────────────────────────
    C_MINOR_THIRTEENTH(0, ChordQuality.MINOR, ChordExtension.MINOR_THIRTEENTH, "C minor 13th"),
    C_SHARP_MINOR_THIRTEENTH(1, ChordQuality.MINOR, ChordExtension.MINOR_THIRTEENTH, "C# minor 13th"),
    D_MINOR_THIRTEENTH(2, ChordQuality.MINOR, ChordExtension.MINOR_THIRTEENTH, "D minor 13th"),
    D_SHARP_MINOR_THIRTEENTH(3, ChordQuality.MINOR, ChordExtension.MINOR_THIRTEENTH, "D# minor 13th"),
    E_MINOR_THIRTEENTH(4, ChordQuality.MINOR, ChordExtension.MINOR_THIRTEENTH, "E minor 13th"),
    F_MINOR_THIRTEENTH(5, ChordQuality.MINOR, ChordExtension.MINOR_THIRTEENTH, "F minor 13th"),
    F_SHARP_MINOR_THIRTEENTH(6, ChordQuality.MINOR, ChordExtension.MINOR_THIRTEENTH, "F# minor 13th"),
    G_MINOR_THIRTEENTH(7, ChordQuality.MINOR, ChordExtension.MINOR_THIRTEENTH, "G minor 13th"),
    G_SHARP_MINOR_THIRTEENTH(8, ChordQuality.MINOR, ChordExtension.MINOR_THIRTEENTH, "G# minor 13th"),
    A_MINOR_THIRTEENTH(9, ChordQuality.MINOR, ChordExtension.MINOR_THIRTEENTH, "A minor 13th"),
    A_SHARP_MINOR_THIRTEENTH(10, ChordQuality.MINOR, ChordExtension.MINOR_THIRTEENTH, "A# minor 13th"),
    B_MINOR_THIRTEENTH(11, ChordQuality.MINOR, ChordExtension.MINOR_THIRTEENTH, "B minor 13th"),

    // ── Dominant 7b9 chords ───────────────────────────────────────────────────
    C_DOMINANT_SEVENTH_FLAT_NINTH(0, ChordQuality.DOMINANT, ChordExtension.SEVENTH_FLAT_NINTH, "C dominant 7b9"),
    C_SHARP_DOMINANT_SEVENTH_FLAT_NINTH(1, ChordQuality.DOMINANT, ChordExtension.SEVENTH_FLAT_NINTH, "C# dominant 7b9"),
    D_DOMINANT_SEVENTH_FLAT_NINTH(2, ChordQuality.DOMINANT, ChordExtension.SEVENTH_FLAT_NINTH, "D dominant 7b9"),
    D_SHARP_DOMINANT_SEVENTH_FLAT_NINTH(3, ChordQuality.DOMINANT, ChordExtension.SEVENTH_FLAT_NINTH, "D# dominant 7b9"),
    E_DOMINANT_SEVENTH_FLAT_NINTH(4, ChordQuality.DOMINANT, ChordExtension.SEVENTH_FLAT_NINTH, "E dominant 7b9"),
    F_DOMINANT_SEVENTH_FLAT_NINTH(5, ChordQuality.DOMINANT, ChordExtension.SEVENTH_FLAT_NINTH, "F dominant 7b9"),
    F_SHARP_DOMINANT_SEVENTH_FLAT_NINTH(6, ChordQuality.DOMINANT, ChordExtension.SEVENTH_FLAT_NINTH, "F# dominant 7b9"),
    G_DOMINANT_SEVENTH_FLAT_NINTH(7, ChordQuality.DOMINANT, ChordExtension.SEVENTH_FLAT_NINTH, "G dominant 7b9"),
    G_SHARP_DOMINANT_SEVENTH_FLAT_NINTH(8, ChordQuality.DOMINANT, ChordExtension.SEVENTH_FLAT_NINTH, "G# dominant 7b9"),
    A_DOMINANT_SEVENTH_FLAT_NINTH(9, ChordQuality.DOMINANT, ChordExtension.SEVENTH_FLAT_NINTH, "A dominant 7b9"),
    A_SHARP_DOMINANT_SEVENTH_FLAT_NINTH(
        10,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_FLAT_NINTH,
        "A# dominant 7b9"
    ),
    B_DOMINANT_SEVENTH_FLAT_NINTH(11, ChordQuality.DOMINANT, ChordExtension.SEVENTH_FLAT_NINTH, "B dominant 7b9"),

    // ── Dominant 7#9 chords (Hendrix chord) ───────────────────────────────────
    C_DOMINANT_SEVENTH_SHARP_NINTH(0, ChordQuality.DOMINANT, ChordExtension.SEVENTH_SHARP_NINTH, "C dominant 7#9"),
    C_SHARP_DOMINANT_SEVENTH_SHARP_NINTH(
        1,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_SHARP_NINTH,
        "C# dominant 7#9"
    ),
    D_DOMINANT_SEVENTH_SHARP_NINTH(2, ChordQuality.DOMINANT, ChordExtension.SEVENTH_SHARP_NINTH, "D dominant 7#9"),
    D_SHARP_DOMINANT_SEVENTH_SHARP_NINTH(
        3,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_SHARP_NINTH,
        "D# dominant 7#9"
    ),
    E_DOMINANT_SEVENTH_SHARP_NINTH(4, ChordQuality.DOMINANT, ChordExtension.SEVENTH_SHARP_NINTH, "E dominant 7#9"),
    F_DOMINANT_SEVENTH_SHARP_NINTH(5, ChordQuality.DOMINANT, ChordExtension.SEVENTH_SHARP_NINTH, "F dominant 7#9"),
    F_SHARP_DOMINANT_SEVENTH_SHARP_NINTH(
        6,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_SHARP_NINTH,
        "F# dominant 7#9"
    ),
    G_DOMINANT_SEVENTH_SHARP_NINTH(7, ChordQuality.DOMINANT, ChordExtension.SEVENTH_SHARP_NINTH, "G dominant 7#9"),
    G_SHARP_DOMINANT_SEVENTH_SHARP_NINTH(
        8,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_SHARP_NINTH,
        "G# dominant 7#9"
    ),
    A_DOMINANT_SEVENTH_SHARP_NINTH(9, ChordQuality.DOMINANT, ChordExtension.SEVENTH_SHARP_NINTH, "A dominant 7#9"),
    A_SHARP_DOMINANT_SEVENTH_SHARP_NINTH(
        10,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_SHARP_NINTH,
        "A# dominant 7#9"
    ),
    B_DOMINANT_SEVENTH_SHARP_NINTH(11, ChordQuality.DOMINANT, ChordExtension.SEVENTH_SHARP_NINTH, "B dominant 7#9"),

    // ── Dominant 7#11 chords (Lydian dominant) ────────────────────────────────
    C_DOMINANT_SEVENTH_SHARP_ELEVENTH(
        0,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_SHARP_ELEVENTH,
        "C dominant 7#11"
    ),
    C_SHARP_DOMINANT_SEVENTH_SHARP_ELEVENTH(
        1,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_SHARP_ELEVENTH,
        "C# dominant 7#11"
    ),
    D_DOMINANT_SEVENTH_SHARP_ELEVENTH(
        2,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_SHARP_ELEVENTH,
        "D dominant 7#11"
    ),
    D_SHARP_DOMINANT_SEVENTH_SHARP_ELEVENTH(
        3,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_SHARP_ELEVENTH,
        "D# dominant 7#11"
    ),
    E_DOMINANT_SEVENTH_SHARP_ELEVENTH(
        4,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_SHARP_ELEVENTH,
        "E dominant 7#11"
    ),
    F_DOMINANT_SEVENTH_SHARP_ELEVENTH(
        5,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_SHARP_ELEVENTH,
        "F dominant 7#11"
    ),
    F_SHARP_DOMINANT_SEVENTH_SHARP_ELEVENTH(
        6,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_SHARP_ELEVENTH,
        "F# dominant 7#11"
    ),
    G_DOMINANT_SEVENTH_SHARP_ELEVENTH(
        7,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_SHARP_ELEVENTH,
        "G dominant 7#11"
    ),
    G_SHARP_DOMINANT_SEVENTH_SHARP_ELEVENTH(
        8,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_SHARP_ELEVENTH,
        "G# dominant 7#11"
    ),
    A_DOMINANT_SEVENTH_SHARP_ELEVENTH(
        9,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_SHARP_ELEVENTH,
        "A dominant 7#11"
    ),
    A_SHARP_DOMINANT_SEVENTH_SHARP_ELEVENTH(
        10,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_SHARP_ELEVENTH,
        "A# dominant 7#11"
    ),
    B_DOMINANT_SEVENTH_SHARP_ELEVENTH(
        11,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_SHARP_ELEVENTH,
        "B dominant 7#11"
    ),

    // ── Dominant 9#11 chords ──────────────────────────────────────────────────
    C_DOMINANT_NINTH_SHARP_ELEVENTH(0, ChordQuality.DOMINANT, ChordExtension.NINTH_SHARP_ELEVENTH, "C dominant 9#11"),
    C_SHARP_DOMINANT_NINTH_SHARP_ELEVENTH(
        1,
        ChordQuality.DOMINANT,
        ChordExtension.NINTH_SHARP_ELEVENTH,
        "C# dominant 9#11"
    ),
    D_DOMINANT_NINTH_SHARP_ELEVENTH(2, ChordQuality.DOMINANT, ChordExtension.NINTH_SHARP_ELEVENTH, "D dominant 9#11"),
    D_SHARP_DOMINANT_NINTH_SHARP_ELEVENTH(
        3,
        ChordQuality.DOMINANT,
        ChordExtension.NINTH_SHARP_ELEVENTH,
        "D# dominant 9#11"
    ),
    E_DOMINANT_NINTH_SHARP_ELEVENTH(4, ChordQuality.DOMINANT, ChordExtension.NINTH_SHARP_ELEVENTH, "E dominant 9#11"),
    F_DOMINANT_NINTH_SHARP_ELEVENTH(5, ChordQuality.DOMINANT, ChordExtension.NINTH_SHARP_ELEVENTH, "F dominant 9#11"),
    F_SHARP_DOMINANT_NINTH_SHARP_ELEVENTH(
        6,
        ChordQuality.DOMINANT,
        ChordExtension.NINTH_SHARP_ELEVENTH,
        "F# dominant 9#11"
    ),
    G_DOMINANT_NINTH_SHARP_ELEVENTH(7, ChordQuality.DOMINANT, ChordExtension.NINTH_SHARP_ELEVENTH, "G dominant 9#11"),
    G_SHARP_DOMINANT_NINTH_SHARP_ELEVENTH(
        8,
        ChordQuality.DOMINANT,
        ChordExtension.NINTH_SHARP_ELEVENTH,
        "G# dominant 9#11"
    ),
    A_DOMINANT_NINTH_SHARP_ELEVENTH(9, ChordQuality.DOMINANT, ChordExtension.NINTH_SHARP_ELEVENTH, "A dominant 9#11"),
    A_SHARP_DOMINANT_NINTH_SHARP_ELEVENTH(
        10,
        ChordQuality.DOMINANT,
        ChordExtension.NINTH_SHARP_ELEVENTH,
        "A# dominant 9#11"
    ),
    B_DOMINANT_NINTH_SHARP_ELEVENTH(11, ChordQuality.DOMINANT, ChordExtension.NINTH_SHARP_ELEVENTH, "B dominant 9#11"),

    // ── Dominant 13#11 chords ─────────────────────────────────────────────────
    C_DOMINANT_THIRTEENTH_SHARP_ELEVENTH(
        0,
        ChordQuality.DOMINANT,
        ChordExtension.THIRTEENTH_SHARP_ELEVENTH,
        "C dominant 13#11"
    ),
    C_SHARP_DOMINANT_THIRTEENTH_SHARP_ELEVENTH(
        1,
        ChordQuality.DOMINANT,
        ChordExtension.THIRTEENTH_SHARP_ELEVENTH,
        "C# dominant 13#11"
    ),
    D_DOMINANT_THIRTEENTH_SHARP_ELEVENTH(
        2,
        ChordQuality.DOMINANT,
        ChordExtension.THIRTEENTH_SHARP_ELEVENTH,
        "D dominant 13#11"
    ),
    D_SHARP_DOMINANT_THIRTEENTH_SHARP_ELEVENTH(
        3,
        ChordQuality.DOMINANT,
        ChordExtension.THIRTEENTH_SHARP_ELEVENTH,
        "D# dominant 13#11"
    ),
    E_DOMINANT_THIRTEENTH_SHARP_ELEVENTH(
        4,
        ChordQuality.DOMINANT,
        ChordExtension.THIRTEENTH_SHARP_ELEVENTH,
        "E dominant 13#11"
    ),
    F_DOMINANT_THIRTEENTH_SHARP_ELEVENTH(
        5,
        ChordQuality.DOMINANT,
        ChordExtension.THIRTEENTH_SHARP_ELEVENTH,
        "F dominant 13#11"
    ),
    F_SHARP_DOMINANT_THIRTEENTH_SHARP_ELEVENTH(
        6,
        ChordQuality.DOMINANT,
        ChordExtension.THIRTEENTH_SHARP_ELEVENTH,
        "F# dominant 13#11"
    ),
    G_DOMINANT_THIRTEENTH_SHARP_ELEVENTH(
        7,
        ChordQuality.DOMINANT,
        ChordExtension.THIRTEENTH_SHARP_ELEVENTH,
        "G dominant 13#11"
    ),
    G_SHARP_DOMINANT_THIRTEENTH_SHARP_ELEVENTH(
        8,
        ChordQuality.DOMINANT,
        ChordExtension.THIRTEENTH_SHARP_ELEVENTH,
        "G# dominant 13#11"
    ),
    A_DOMINANT_THIRTEENTH_SHARP_ELEVENTH(
        9,
        ChordQuality.DOMINANT,
        ChordExtension.THIRTEENTH_SHARP_ELEVENTH,
        "A dominant 13#11"
    ),
    A_SHARP_DOMINANT_THIRTEENTH_SHARP_ELEVENTH(
        10,
        ChordQuality.DOMINANT,
        ChordExtension.THIRTEENTH_SHARP_ELEVENTH,
        "A# dominant 13#11"
    ),
    B_DOMINANT_THIRTEENTH_SHARP_ELEVENTH(
        11,
        ChordQuality.DOMINANT,
        ChordExtension.THIRTEENTH_SHARP_ELEVENTH,
        "B dominant 13#11"
    ),

    // ── Dominant 7add11 chords ────────────────────────────────────────────────
    C_DOMINANT_SEVENTH_ADD_ELEVENTH(0, ChordQuality.DOMINANT, ChordExtension.SEVENTH_ADD_ELEVENTH, "C dominant 7add11"),
    C_SHARP_DOMINANT_SEVENTH_ADD_ELEVENTH(
        1,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_ADD_ELEVENTH,
        "C# dominant 7add11"
    ),
    D_DOMINANT_SEVENTH_ADD_ELEVENTH(2, ChordQuality.DOMINANT, ChordExtension.SEVENTH_ADD_ELEVENTH, "D dominant 7add11"),
    D_SHARP_DOMINANT_SEVENTH_ADD_ELEVENTH(
        3,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_ADD_ELEVENTH,
        "D# dominant 7add11"
    ),
    E_DOMINANT_SEVENTH_ADD_ELEVENTH(4, ChordQuality.DOMINANT, ChordExtension.SEVENTH_ADD_ELEVENTH, "E dominant 7add11"),
    F_DOMINANT_SEVENTH_ADD_ELEVENTH(5, ChordQuality.DOMINANT, ChordExtension.SEVENTH_ADD_ELEVENTH, "F dominant 7add11"),
    F_SHARP_DOMINANT_SEVENTH_ADD_ELEVENTH(
        6,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_ADD_ELEVENTH,
        "F# dominant 7add11"
    ),
    G_DOMINANT_SEVENTH_ADD_ELEVENTH(7, ChordQuality.DOMINANT, ChordExtension.SEVENTH_ADD_ELEVENTH, "G dominant 7add11"),
    G_SHARP_DOMINANT_SEVENTH_ADD_ELEVENTH(
        8,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_ADD_ELEVENTH,
        "G# dominant 7add11"
    ),
    A_DOMINANT_SEVENTH_ADD_ELEVENTH(9, ChordQuality.DOMINANT, ChordExtension.SEVENTH_ADD_ELEVENTH, "A dominant 7add11"),
    A_SHARP_DOMINANT_SEVENTH_ADD_ELEVENTH(
        10,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_ADD_ELEVENTH,
        "A# dominant 7add11"
    ),
    B_DOMINANT_SEVENTH_ADD_ELEVENTH(
        11,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_ADD_ELEVENTH,
        "B dominant 7add11"
    ),

    // ── Dominant 7add13 chords ────────────────────────────────────────────────
    C_DOMINANT_SEVENTH_ADD_THIRTEENTH(
        0,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_ADD_THIRTEENTH,
        "C dominant 7add13"
    ),
    C_SHARP_DOMINANT_SEVENTH_ADD_THIRTEENTH(
        1,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_ADD_THIRTEENTH,
        "C# dominant 7add13"
    ),
    D_DOMINANT_SEVENTH_ADD_THIRTEENTH(
        2,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_ADD_THIRTEENTH,
        "D dominant 7add13"
    ),
    D_SHARP_DOMINANT_SEVENTH_ADD_THIRTEENTH(
        3,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_ADD_THIRTEENTH,
        "D# dominant 7add13"
    ),
    E_DOMINANT_SEVENTH_ADD_THIRTEENTH(
        4,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_ADD_THIRTEENTH,
        "E dominant 7add13"
    ),
    F_DOMINANT_SEVENTH_ADD_THIRTEENTH(
        5,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_ADD_THIRTEENTH,
        "F dominant 7add13"
    ),
    F_SHARP_DOMINANT_SEVENTH_ADD_THIRTEENTH(
        6,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_ADD_THIRTEENTH,
        "F# dominant 7add13"
    ),
    G_DOMINANT_SEVENTH_ADD_THIRTEENTH(
        7,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_ADD_THIRTEENTH,
        "G dominant 7add13"
    ),
    G_SHARP_DOMINANT_SEVENTH_ADD_THIRTEENTH(
        8,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_ADD_THIRTEENTH,
        "G# dominant 7add13"
    ),
    A_DOMINANT_SEVENTH_ADD_THIRTEENTH(
        9,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_ADD_THIRTEENTH,
        "A dominant 7add13"
    ),
    A_SHARP_DOMINANT_SEVENTH_ADD_THIRTEENTH(
        10,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_ADD_THIRTEENTH,
        "A# dominant 7add13"
    ),
    B_DOMINANT_SEVENTH_ADD_THIRTEENTH(
        11,
        ChordQuality.DOMINANT,
        ChordExtension.SEVENTH_ADD_THIRTEENTH,
        "B dominant 7add13"
    ),

    // ── Dominant 9add13 chords ────────────────────────────────────────────────
    C_DOMINANT_NINTH_ADD_THIRTEENTH(0, ChordQuality.DOMINANT, ChordExtension.NINTH_ADD_THIRTEENTH, "C dominant 9add13"),
    C_SHARP_DOMINANT_NINTH_ADD_THIRTEENTH(
        1,
        ChordQuality.DOMINANT,
        ChordExtension.NINTH_ADD_THIRTEENTH,
        "C# dominant 9add13"
    ),
    D_DOMINANT_NINTH_ADD_THIRTEENTH(2, ChordQuality.DOMINANT, ChordExtension.NINTH_ADD_THIRTEENTH, "D dominant 9add13"),
    D_SHARP_DOMINANT_NINTH_ADD_THIRTEENTH(
        3,
        ChordQuality.DOMINANT,
        ChordExtension.NINTH_ADD_THIRTEENTH,
        "D# dominant 9add13"
    ),
    E_DOMINANT_NINTH_ADD_THIRTEENTH(4, ChordQuality.DOMINANT, ChordExtension.NINTH_ADD_THIRTEENTH, "E dominant 9add13"),
    F_DOMINANT_NINTH_ADD_THIRTEENTH(5, ChordQuality.DOMINANT, ChordExtension.NINTH_ADD_THIRTEENTH, "F dominant 9add13"),
    F_SHARP_DOMINANT_NINTH_ADD_THIRTEENTH(
        6,
        ChordQuality.DOMINANT,
        ChordExtension.NINTH_ADD_THIRTEENTH,
        "F# dominant 9add13"
    ),
    G_DOMINANT_NINTH_ADD_THIRTEENTH(7, ChordQuality.DOMINANT, ChordExtension.NINTH_ADD_THIRTEENTH, "G dominant 9add13"),
    G_SHARP_DOMINANT_NINTH_ADD_THIRTEENTH(
        8,
        ChordQuality.DOMINANT,
        ChordExtension.NINTH_ADD_THIRTEENTH,
        "G# dominant 9add13"
    ),
    A_DOMINANT_NINTH_ADD_THIRTEENTH(9, ChordQuality.DOMINANT, ChordExtension.NINTH_ADD_THIRTEENTH, "A dominant 9add13"),
    A_SHARP_DOMINANT_NINTH_ADD_THIRTEENTH(
        10,
        ChordQuality.DOMINANT,
        ChordExtension.NINTH_ADD_THIRTEENTH,
        "A# dominant 9add13"
    ),
    B_DOMINANT_NINTH_ADD_THIRTEENTH(
        11,
        ChordQuality.DOMINANT,
        ChordExtension.NINTH_ADD_THIRTEENTH,
        "B dominant 9add13"
    ),

    // ── Six-nine chords ───────────────────────────────────────────────────────
    C_MAJOR_SIX_NINE(0, ChordQuality.MAJOR, ChordExtension.SIX_NINE, "C 6/9"),
    C_MINOR_SIX_NINE(0, ChordQuality.MINOR, ChordExtension.SIX_NINE, "C minor 6/9"),
    C_SHARP_MAJOR_SIX_NINE(1, ChordQuality.MAJOR, ChordExtension.SIX_NINE, "C# 6/9"),
    C_SHARP_MINOR_SIX_NINE(1, ChordQuality.MINOR, ChordExtension.SIX_NINE, "C# minor 6/9"),
    D_MAJOR_SIX_NINE(2, ChordQuality.MAJOR, ChordExtension.SIX_NINE, "D 6/9"),
    D_MINOR_SIX_NINE(2, ChordQuality.MINOR, ChordExtension.SIX_NINE, "D minor 6/9"),
    D_SHARP_MAJOR_SIX_NINE(3, ChordQuality.MAJOR, ChordExtension.SIX_NINE, "D# 6/9"),
    D_SHARP_MINOR_SIX_NINE(3, ChordQuality.MINOR, ChordExtension.SIX_NINE, "D# minor 6/9"),
    E_MAJOR_SIX_NINE(4, ChordQuality.MAJOR, ChordExtension.SIX_NINE, "E 6/9"),
    E_MINOR_SIX_NINE(4, ChordQuality.MINOR, ChordExtension.SIX_NINE, "E minor 6/9"),
    F_MAJOR_SIX_NINE(5, ChordQuality.MAJOR, ChordExtension.SIX_NINE, "F 6/9"),
    F_MINOR_SIX_NINE(5, ChordQuality.MINOR, ChordExtension.SIX_NINE, "F minor 6/9"),
    F_SHARP_MAJOR_SIX_NINE(6, ChordQuality.MAJOR, ChordExtension.SIX_NINE, "F# 6/9"),
    F_SHARP_MINOR_SIX_NINE(6, ChordQuality.MINOR, ChordExtension.SIX_NINE, "F# minor 6/9"),
    G_MAJOR_SIX_NINE(7, ChordQuality.MAJOR, ChordExtension.SIX_NINE, "G 6/9"),
    G_MINOR_SIX_NINE(7, ChordQuality.MINOR, ChordExtension.SIX_NINE, "G minor 6/9"),
    G_SHARP_MAJOR_SIX_NINE(8, ChordQuality.MAJOR, ChordExtension.SIX_NINE, "G# 6/9"),
    G_SHARP_MINOR_SIX_NINE(8, ChordQuality.MINOR, ChordExtension.SIX_NINE, "G# minor 6/9"),
    A_MAJOR_SIX_NINE(9, ChordQuality.MAJOR, ChordExtension.SIX_NINE, "A 6/9"),
    A_MINOR_SIX_NINE(9, ChordQuality.MINOR, ChordExtension.SIX_NINE, "A minor 6/9"),
    A_SHARP_MAJOR_SIX_NINE(10, ChordQuality.MAJOR, ChordExtension.SIX_NINE, "A# 6/9"),
    A_SHARP_MINOR_SIX_NINE(10, ChordQuality.MINOR, ChordExtension.SIX_NINE, "A# minor 6/9"),
    B_MAJOR_SIX_NINE(11, ChordQuality.MAJOR, ChordExtension.SIX_NINE, "B 6/9"),
    B_MINOR_SIX_NINE(11, ChordQuality.MINOR, ChordExtension.SIX_NINE, "B minor 6/9"),

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
