package be.codewriter.melodymatrix.view.definition

/**
 * Preset filter groups that control which [ChordQuality] types are shown in the
 * chord-relation visualizer.  Designed for a segmented button bar so the user
 * selects one preset rather than toggling nine individual switches.
 *
 * | Preset   | Qualities included                                                                  |
 * |----------|-------------------------------------------------------------------------------------|
 * | SIMPLE   | Major, Minor                                                                        |
 * | TRIADS   | Major, Minor, Augmented, Diminished, Sus4, Sus2                                     |
 * | JAZZ     | Major, Minor, Dominant, Diminished, Half-diminished, Augmented                      |
 * | ALL      | Every quality, including Suspended and Tritone                                      |
 *
 * @property label Human-readable display name used in the UI button
 * @property qualities The set of [ChordQuality] values that are enabled for this preset
 */
enum class ChordQualityPreset(
    val label: String,
    val qualities: Set<ChordQuality>
) {
    /** Only the two basic triad flavours – great for beginners. */
    SIMPLE(
        "Simple",
        setOf(
            ChordQuality.MAJOR,
            ChordQuality.MINOR
        )
    ),

    /** All triadic flavours: major, minor, augmented, diminished, sus4 and sus2. */
    TRIADS(
        "Triads",
        setOf(
            ChordQuality.MAJOR,
            ChordQuality.MINOR,
            ChordQuality.AUGMENTED,
            ChordQuality.DIMINISHED,
            ChordQuality.SUSPENDED_FOURTH,
            ChordQuality.SUSPENDED_SECOND
        )
    ),

    /** The palette used in jazz and blues: dominant, half-diminished and augmented added. */
    JAZZ(
        "Jazz",
        setOf(
            ChordQuality.MAJOR,
            ChordQuality.MINOR,
            ChordQuality.DOMINANT,
            ChordQuality.DIMINISHED,
            ChordQuality.HALF_DIMINISHED,
            ChordQuality.AUGMENTED,
            ChordQuality.SUSPENDED_FOURTH,
            ChordQuality.SUSPENDED_SECOND
        )
    ),

    /** Every quality, including tritone dyads. */
    ALL(
        "All",
        ChordQuality.entries.toSet()
    );

    /** Returns `true` when [quality] is included in this preset. */
    fun includes(quality: ChordQuality): Boolean = quality in qualities
}

