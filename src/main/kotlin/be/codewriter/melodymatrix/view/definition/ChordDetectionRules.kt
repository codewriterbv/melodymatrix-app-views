package be.codewriter.melodymatrix.view.definition

data class ChordDetectionPattern(
    val intervals: Set<Int>,
    val quality: ChordQuality,
    val extension: ChordExtension = ChordExtension.NONE,
    val alteration: ChordAlteration = ChordAlteration.NONE
)

object ChordDetectionRules {
    val SEVENTH_PATTERNS: List<ChordDetectionPattern> = listOf(
        ChordDetectionPattern(setOf(0, 4, 7, 11), ChordQuality.MAJOR, ChordExtension.MAJOR_SEVENTH),
        ChordDetectionPattern(setOf(0, 4, 6, 10), ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, ChordAlteration.FLAT_FIFTH),
        ChordDetectionPattern(setOf(0, 4, 8, 10), ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, ChordAlteration.SHARP_FIFTH),
        ChordDetectionPattern(setOf(0, 4, 7, 10), ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH),
        ChordDetectionPattern(setOf(0, 3, 7, 10), ChordQuality.MINOR, ChordExtension.MINOR_SEVENTH),
        ChordDetectionPattern(setOf(0, 3, 6, 10), ChordQuality.HALF_DIMINISHED, ChordExtension.MINOR_SEVENTH),
        ChordDetectionPattern(setOf(0, 3, 6, 9), ChordQuality.DIMINISHED, ChordExtension.DIMINISHED_SEVENTH)
    )

    val TRIAD_PATTERNS: List<ChordDetectionPattern> = listOf(
        ChordDetectionPattern(setOf(0, 4, 7), ChordQuality.MAJOR),
        ChordDetectionPattern(setOf(0, 3, 7), ChordQuality.MINOR)
    )
}

