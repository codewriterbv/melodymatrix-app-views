package be.codewriter.melodymatrix.view.definition

/**
 * Enumeration of chord quality types.
 *
 * Describes the basic chord quality, which determines the intervals of notes
 * that make up the chord. Combined with chord extensions, this fully defines a chord.
 *
 * @property label A human-readable label for the chord quality
 *
 * @see Chord
 * @see ChordExtension
 * @see ChordAlteration
 */
enum class ChordQuality(val label: String) {
    /** Major triad (root, major third, perfect fifth) */
    MAJOR("major"),

    /** Minor triad (root, minor third, perfect fifth) */
    MINOR("minor"),

    /** Dominant chord (major triad with minor seventh) */
    DOMINANT("dominant"),

    /** Diminished triad (root, minor third, diminished fifth) */
    DIMINISHED("diminished"),

    /** Half-diminished seventh (diminished triad with minor seventh) */
    HALF_DIMINISHED("half diminished")
}
