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
 * Contains ordered lists of [ChordDetectionPattern] for seventh chords and triads.
 * Seventh chord patterns are checked first to correctly identify extended chords before
 * falling back to triad patterns.
 *
 * @see ChordDetectionPattern
 */
object ChordDetectionRules {
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
