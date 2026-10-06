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
 * Pattern lists are ordered from most-complex (most notes) to least-complex so that
 * the detector can check richer chords first and fall back to simpler ones.
 *
 * Detection order (by note count):
 *  - Dyads (2 notes)
 *  - Thirteenth chords (7 notes)
 *  - Eleventh chords (6 notes)
 *  - Extended / altered dominant chords (5 notes)
 *  - Ninth chords (5 notes)
 *  - Sixth, add9, 7sus4 (4 notes)
 *  - Seventh chords (4 notes)
 *  - Triads (3 notes)
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
     * Thirteenth-chord detection patterns (7 notes).
     *
     * Checked before eleventh- and ninth-chord patterns so that a fully-voiced 13th
     * chord is not mis-identified as a simpler chord whose notes are a strict subset.
     */
    val THIRTEENTH_PATTERNS: List<ChordDetectionPattern> = listOf(
        // 13#11  – dominant 13th with sharp 11th  {0,2,4,6,7,9,10}
        ChordDetectionPattern(setOf(0, 2, 4, 6, 7, 9, 10), ChordQuality.DOMINANT, ChordExtension.THIRTEENTH_SHARP_ELEVENTH),
        // maj13   {0,2,4,5,7,9,11}
        ChordDetectionPattern(setOf(0, 2, 4, 5, 7, 9, 11), ChordQuality.MAJOR, ChordExtension.MAJOR_THIRTEENTH),
        // dom13   {0,2,4,5,7,9,10}
        ChordDetectionPattern(setOf(0, 2, 4, 5, 7, 9, 10), ChordQuality.DOMINANT, ChordExtension.DOMINANT_THIRTEENTH),
        // m13     {0,2,3,5,7,9,10}
        ChordDetectionPattern(setOf(0, 2, 3, 5, 7, 9, 10), ChordQuality.MINOR, ChordExtension.MINOR_THIRTEENTH)
    )

    /**
     * Eleventh-chord detection patterns (6 notes).
     */
    val ELEVENTH_PATTERNS: List<ChordDetectionPattern> = listOf(
        // 9#11    {0,2,4,6,7,10}
        ChordDetectionPattern(setOf(0, 2, 4, 6, 7, 10), ChordQuality.DOMINANT, ChordExtension.NINTH_SHARP_ELEVENTH),
        // 9add13  {0,2,4,7,9,10}
        ChordDetectionPattern(setOf(0, 2, 4, 7, 9, 10), ChordQuality.DOMINANT, ChordExtension.NINTH_ADD_THIRTEENTH),
        // maj11   {0,2,4,5,7,11}
        ChordDetectionPattern(setOf(0, 2, 4, 5, 7, 11), ChordQuality.MAJOR, ChordExtension.MAJOR_ELEVENTH),
        // dom11   {0,2,4,5,7,10}
        ChordDetectionPattern(setOf(0, 2, 4, 5, 7, 10), ChordQuality.DOMINANT, ChordExtension.DOMINANT_ELEVENTH),
        // m11     {0,2,3,5,7,10}
        ChordDetectionPattern(setOf(0, 2, 3, 5, 7, 10), ChordQuality.MINOR, ChordExtension.MINOR_ELEVENTH)
    )

    /**
     * Extended and altered dominant patterns (5 notes) checked before basic ninth patterns.
     *
     * Covers 7b9, 7#9, 7#11, 7add11, 7add13, 9sus4 and dim9.
     */
    val EXTENDED_DOMINANT_PATTERNS: List<ChordDetectionPattern> = listOf(
        // 7b9   {0,1,4,7,10}
        ChordDetectionPattern(setOf(0, 1, 4, 7, 10), ChordQuality.DOMINANT, ChordExtension.SEVENTH_FLAT_NINTH),
        // 7#9   {0,3,4,7,10}  (Hendrix / purple-haze chord)
        ChordDetectionPattern(setOf(0, 3, 4, 7, 10), ChordQuality.DOMINANT, ChordExtension.SEVENTH_SHARP_NINTH),
        // 7#11  {0,4,6,7,10}
        ChordDetectionPattern(setOf(0, 4, 6, 7, 10), ChordQuality.DOMINANT, ChordExtension.SEVENTH_SHARP_ELEVENTH),
        // 7add11 {0,4,5,7,10}
        ChordDetectionPattern(setOf(0, 4, 5, 7, 10), ChordQuality.DOMINANT, ChordExtension.SEVENTH_ADD_ELEVENTH),
        // 7add13 {0,4,7,9,10}
        ChordDetectionPattern(setOf(0, 4, 7, 9, 10), ChordQuality.DOMINANT, ChordExtension.SEVENTH_ADD_THIRTEENTH),
        // 9sus4  {0,2,5,7,10}
        ChordDetectionPattern(setOf(0, 2, 5, 7, 10), ChordQuality.SUSPENDED_FOURTH, ChordExtension.NINTH_SUSPENDED_FOURTH),
        // dim9   {0,1,3,6,9}
        ChordDetectionPattern(setOf(0, 1, 3, 6, 9), ChordQuality.DIMINISHED, ChordExtension.DIMINISHED_NINTH)
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
     * Four-note patterns for sixth and add9 chords, and 7sus4, checked before generic seventh patterns.
     */
    val SIXTH_AND_ADD_PATTERNS: List<ChordDetectionPattern> = listOf(
        // 7sus4        {0,5,7,10}
        ChordDetectionPattern(setOf(0, 5, 7, 10), ChordQuality.SUSPENDED_FOURTH, ChordExtension.MINOR_SEVENTH),
        // major 6th    {0,4,7,9}
        ChordDetectionPattern(setOf(0, 4, 7, 9), ChordQuality.MAJOR, ChordExtension.SIXTH),
        // minor 6th    {0,3,7,9}
        ChordDetectionPattern(setOf(0, 3, 7, 9), ChordQuality.MINOR, ChordExtension.SIXTH),
        // add9         {0,2,4,7}
        ChordDetectionPattern(setOf(0, 2, 4, 7), ChordQuality.MAJOR, ChordExtension.ADD_NINTH)
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
     * These patterns match three-note chords: major, minor, augmented, diminished,
     * suspended fourth and suspended second triads.
     */
    val TRIAD_PATTERNS: List<ChordDetectionPattern> = listOf(
        ChordDetectionPattern(setOf(0, 4, 7), ChordQuality.MAJOR),
        ChordDetectionPattern(setOf(0, 3, 7), ChordQuality.MINOR),
        ChordDetectionPattern(setOf(0, 4, 8), ChordQuality.AUGMENTED),
        ChordDetectionPattern(setOf(0, 3, 6), ChordQuality.DIMINISHED),
        ChordDetectionPattern(setOf(0, 5, 7), ChordQuality.SUSPENDED_FOURTH),
        ChordDetectionPattern(setOf(0, 2, 7), ChordQuality.SUSPENDED_SECOND)
    )
}
