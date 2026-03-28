package be.codewriter.melodymatrix.view.definition

/**
 * Enumeration of chord extensions.
 *
 * Describes the extensions added to the basic chord quality (seventh notes, etc.).
 * Combined with chord quality and alteration, this fully defines a chord.
 *
 * @property label A human-readable label for the chord extension
 *
 * @see Chord
 * @see ChordQuality
 * @see ChordAlteration
 */
enum class ChordExtension(val label: String) {
    /** No extension (basic triad) */
    NONE(""),

    /** Major seventh extension (major 7th above root) */
    MAJOR_SEVENTH("major 7th"),

    /** Minor seventh extension (minor 7th above root) */
    MINOR_SEVENTH("minor 7th"),

    /** Diminished seventh extension (diminished 7th above root) */
    DIMINISHED_SEVENTH("diminished 7th"),

    /** Dominant ninth extension (minor 7th plus major 9th above root) */
    DOMINANT_NINTH("dominant 9th"),

    /** Major ninth extension (major 7th plus major 9th above root) */
    MAJOR_NINTH("major 9th"),

    /** Minor ninth extension (minor 7th plus major 9th above root) */
    MINOR_NINTH("minor 9th")
}

