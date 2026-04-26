package be.codewriter.melodymatrix.view.definition

/**
 * Preset filter groups that control which [Chord]s are shown in the
 * chord-relation visualizer.  Designed for a segmented button bar so the user
 * selects one preset rather than toggling switches.
 *
 * | Preset   | Qualities included                                          | Extensions         |
 * |----------|-------------------------------------------------------------|--------------------|
 * | SIMPLE   | Major, Minor                                                | any                |
 * | ELEVENTH | Major, Minor, Dominant                                      | *_ELEVENTH, 7#11   |
 * | JAZZ     | Major, Minor, Dominant, Diminished, Half-diminished, Aug    | any                |
 * | ALL      | Every quality                                               | any                |
 *
 * @property label Human-readable display name used in the UI button
 * @property qualities The set of [ChordQuality] values that are enabled for this preset
 * @property extensions When non-empty, only chords whose [ChordExtension] is in this set are shown.
 *                      An empty set means "no extension filter – show all extensions".
 */
enum class ChordQualityPreset(
    val label: String,
    val qualities: Set<ChordQuality>,
    val extensions: Set<ChordExtension> = emptySet()
) {
    /** Only the two basic triad flavours – great for beginners. */
    SIMPLE(
        "Simple",
        setOf(
            ChordQuality.MAJOR,
            ChordQuality.MINOR
        )
    ),

    /** 11th chord flavours: major 11th, minor 11th, dominant 11th, and Lydian dominant (7#11). */
    ELEVENTH(
        "11th Chords",
        setOf(
            ChordQuality.MAJOR,
            ChordQuality.MINOR,
            ChordQuality.DOMINANT
        ),
        setOf(
            ChordExtension.MAJOR_ELEVENTH,
            ChordExtension.MINOR_ELEVENTH,
            ChordExtension.DOMINANT_ELEVENTH,
            ChordExtension.SEVENTH_SHARP_ELEVENTH
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

    /** Every quality and every extension. */
    ALL(
        "All",
        ChordQuality.entries.toSet()
    );

    /** Returns `true` when [chord] passes both the quality and extension filters of this preset. */
    fun includes(chord: Chord): Boolean =
        chord.quality in qualities && (extensions.isEmpty() || chord.extension in extensions)
}

