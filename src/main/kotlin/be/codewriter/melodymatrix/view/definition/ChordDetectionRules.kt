package be.codewriter.melodymatrix.view.definition

/**
 * Represents a chord detection pattern defined by a set of intervals and chord properties.
 *
 * A pattern is used to match a set of simultaneously played pitch-class intervals
 * against known chord shapes to identify the chord quality, extension and alteration.
 *
 * @property intervals The set of semitone intervals relative to the root (e.g., {0, 4, 7} for major triad)
 * @property quality The chord quality matched by this pattern
 * @property extension The chord extension matched by this pattern (default: NONE)
 * @property alteration The chord alteration matched by this pattern (default: NONE)
 *
 * @see ChordDetectionRules
 * @see ChordQuality
 * @see ChordExtension
 * @see ChordAlteration
 */
data class ChordDetectionPattern(
    val intervals: Set<Int>,
    val quality: ChordQuality,
    val extension: ChordExtension = ChordExtension.NONE,
    val alteration: ChordAlteration = ChordAlteration.NONE
)

/**
 * Defines the interval patterns used to detect chords from a set of played notes.
 *
 * Contains ordered lists of [ChordDetectionPattern] for dyads, ninth chords, seventh chords and triads.
 * Extended chord patterns are checked first to correctly identify richer chords before
 * falling back to triad patterns.
 *
 * @see ChordDetectionPattern
 */
object ChordDetectionRules {
    /**
     * Ordered list of dyad detection patterns.
     *
     * These patterns match two-note chords such as tritones.
     */
    val DYAD_PATTERNS: List<ChordDetectionPattern> = listOf(
        ChordDetectionPattern(setOf(0, 6), ChordQuality.TRITONE)
    )

    /**
     * Ordered list of ninth-chord detection patterns.
     *
     * These patterns match five-note chords including major, dominant and minor ninth chords,
     * and major/minor 6/9 chords (major 6th + major 9th, no 7th).
     */
    val NINTH_PATTERNS: List<ChordDetectionPattern> = listOf(
        ChordDetectionPattern(setOf(0, 4, 7, 11, 2), ChordQuality.MAJOR, ChordExtension.MAJOR_NINTH),
        ChordDetectionPattern(setOf(0, 4, 7, 10, 2), ChordQuality.DOMINANT, ChordExtension.DOMINANT_NINTH),
        ChordDetectionPattern(setOf(0, 3, 7, 10, 2), ChordQuality.MINOR, ChordExtension.MINOR_NINTH),
        ChordDetectionPattern(setOf(0, 4, 7, 9, 2), ChordQuality.MAJOR, ChordExtension.SIX_NINE),
        ChordDetectionPattern(setOf(0, 3, 7, 9, 2), ChordQuality.MINOR, ChordExtension.SIX_NINE)
    )

    /**
     * Ordered list of seventh-chord detection patterns.
     *
     * These patterns match four-note chords including major 7th, dominant 7th,
     * minor 7th, half-diminished, and fully diminished seventh chords.
     */
    val SEVENTH_PATTERNS: List<ChordDetectionPattern> = listOf(
        ChordDetectionPattern(setOf(0, 4, 7, 11), ChordQuality.MAJOR, ChordExtension.MAJOR_SEVENTH),
        ChordDetectionPattern(
            setOf(0, 4, 6, 10),
            ChordQuality.DOMINANT,
            ChordExtension.MINOR_SEVENTH,
            ChordAlteration.FLAT_FIFTH
        ),
        ChordDetectionPattern(
            setOf(0, 4, 8, 10),
            ChordQuality.DOMINANT,
            ChordExtension.MINOR_SEVENTH,
            ChordAlteration.SHARP_FIFTH
        ),
        ChordDetectionPattern(setOf(0, 4, 7, 10), ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH),
        ChordDetectionPattern(setOf(0, 3, 7, 10), ChordQuality.MINOR, ChordExtension.MINOR_SEVENTH),
        ChordDetectionPattern(setOf(0, 3, 6, 10), ChordQuality.HALF_DIMINISHED, ChordExtension.MINOR_SEVENTH),
        ChordDetectionPattern(setOf(0, 3, 6, 9), ChordQuality.DIMINISHED, ChordExtension.DIMINISHED_SEVENTH)
    )

    /**
     * Ordered list of triad detection patterns.
     *
     * These patterns match three-note chords: major and minor triads.
     */
    val TRIAD_PATTERNS: List<ChordDetectionPattern> = listOf(
        ChordDetectionPattern(setOf(0, 4, 7), ChordQuality.MAJOR),
        ChordDetectionPattern(setOf(0, 3, 7), ChordQuality.MINOR)
    )
}
