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
    MINOR_NINTH("minor 9th"),

    /** Six/nine extension (major 6th plus major 9th above root, no 7th) */
    SIX_NINE("6/9"),

    /** Add ninth – major 9th added to the triad, no 7th (0,2,4,7) */
    ADD_NINTH("add9"),

    /** Sixth – major 6th added to the triad, no 7th (0,4,7,9 for major; 0,3,7,9 for minor) */
    SIXTH("6th"),

    /** Major eleventh – major 7th + major 9th + perfect 11th (0,2,4,5,7,11) */
    MAJOR_ELEVENTH("major 11th"),

    /** Dominant eleventh – minor 7th + major 9th + perfect 11th (0,2,4,5,7,10) */
    DOMINANT_ELEVENTH("dominant 11th"),

    /** Minor eleventh – minor 7th + major 9th + perfect 11th over minor triad (0,2,3,5,7,10) */
    MINOR_ELEVENTH("minor 11th"),

    /** Major thirteenth – major 7th + 9th + 11th + major 13th (0,2,4,5,7,9,11) */
    MAJOR_THIRTEENTH("major 13th"),

    /** Dominant thirteenth – minor 7th + 9th + 11th + major 13th (0,2,4,5,7,9,10) */
    DOMINANT_THIRTEENTH("dominant 13th"),

    /** Minor thirteenth – minor 7th + 9th + 11th + major 13th over minor triad (0,2,3,5,7,9,10) */
    MINOR_THIRTEENTH("minor 13th"),

    /** Diminished ninth – diminished 7th + flat 9th (0,1,3,6,9) */
    DIMINISHED_NINTH("diminished 9th"),

    /** Dominant seventh with flat ninth (0,1,4,7,10) */
    SEVENTH_FLAT_NINTH("7b9"),

    /** Dominant seventh with sharp ninth / Hendrix chord (0,3,4,7,10) */
    SEVENTH_SHARP_NINTH("7#9"),

    /** Dominant seventh with sharp eleventh / Lydian dominant (0,4,6,7,10) */
    SEVENTH_SHARP_ELEVENTH("7#11"),

    /** Dominant ninth with sharp eleventh (0,2,4,6,7,10) */
    NINTH_SHARP_ELEVENTH("9#11"),

    /** Dominant thirteenth with sharp eleventh (0,2,4,6,7,9,10) */
    THIRTEENTH_SHARP_ELEVENTH("13#11"),

    /** Dominant seventh with added eleventh, no 9th (0,4,5,7,10) */
    SEVENTH_ADD_ELEVENTH("7add11"),

    /** Dominant seventh with added thirteenth, no 9th or 11th (0,4,7,9,10) */
    SEVENTH_ADD_THIRTEENTH("7add13"),

    /** Dominant ninth with added thirteenth, no 11th (0,2,4,7,9,10) */
    NINTH_ADD_THIRTEENTH("9add13"),

    /** Ninth suspended fourth – suspended 4th + minor 7th + major 9th (0,2,5,7,10) */
    NINTH_SUSPENDED_FOURTH("9sus4")
}
