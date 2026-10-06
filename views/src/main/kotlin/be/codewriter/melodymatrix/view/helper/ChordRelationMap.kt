package be.codewriter.melodymatrix.view.helper

import be.codewriter.melodymatrix.view.definition.Chord
import be.codewriter.melodymatrix.view.definition.ChordExtension
import be.codewriter.melodymatrix.view.definition.ChordQuality
import be.codewriter.melodymatrix.view.definition.RelationshipType

/**
 * Computes the harmonically related chords for any given [be.codewriter.melodymatrix.view.definition.Chord].
 *
 * All relationships are derived from the chord's [be.codewriter.melodymatrix.view.definition.Chord.pitchClass] (0-11) and [be.codewriter.melodymatrix.view.definition.Chord.quality]
 * using standard Western tonal harmony rules – no additional data needs to be stored on [be.codewriter.melodymatrix.view.definition.Chord].
 *
 * The returned list uses **clockwise-from-top** placement angles so the caller can position
 * nodes in a radial layout without further computation:
 *
 * ```
 *         0°  V / III
 *     300°          60°  IV / V
 *    250°   [chord]   120°  vi / iv
 *     240°          180°  parallel
 *         ii / ii°
 * ```
 */
object ChordRelationMap {

    /**
     * Returns a list of [be.codewriter.melodymatrix.view.definition.RelatedChord]s arranged at fixed angular positions.
     *
     * Results include all qualitative flavours (major, minor, diminished) so the caller
     * can decide which to display.  An empty list is returned for [be.codewriter.melodymatrix.view.definition.Chord.UNDEFINED].
     */
    fun getRelatedChords(chord: Chord): List<RelatedChord> {
        if (chord == Chord.UNDEFINED) return emptyList()
        val p = chord.pitchClass
        return when (chord.quality) {
            ChordQuality.MAJOR -> majorRelations(p)
            ChordQuality.DOMINANT -> majorRelations(p)   // treat like major
            ChordQuality.MINOR -> minorRelations(p)
            ChordQuality.DIMINISHED,
            ChordQuality.HALF_DIMINISHED,
            ChordQuality.TRITONE -> dimRelations(p)
            ChordQuality.AUGMENTED,
            ChordQuality.SUSPENDED_FOURTH,
            ChordQuality.SUSPENDED_SECOND -> majorRelations(p)  // treat as major-adjacent
        }
    }

    // ─── Major context ────────────────────────────────────────────────────────

    /**
     * Relationships for a **major** chord at pitch class [p].
     *
     * Angles (clockwise from 12 o'clock):
     * - 0°   V  – Dominant (perfect fifth above)
     * - 60°  IV – Subdominant (perfect fourth above / fifth below)
     * - 120° vi – Submediant / Relative minor
     * - 180° par – Parallel minor (same root)
     * - 240° ii  – Supertonic minor (whole step above)
     * - 300° vii° – Leading-tone diminished
     */
    private fun majorRelations(p: Int): List<RelatedChord> = listOfNotNull(
        rc((p + 7) % 12, ChordQuality.MAJOR, RelationshipType.DOMINANT, 0.0),
        rc((p + 5) % 12, ChordQuality.MAJOR, RelationshipType.SUBDOMINANT, 60.0),
        rc((p + 9) % 12, ChordQuality.MINOR, RelationshipType.RELATIVE_MINOR, 120.0),
        rc(p, ChordQuality.MINOR, RelationshipType.PARALLEL, 180.0),
        rc((p + 2) % 12, ChordQuality.MINOR, RelationshipType.SUPERTONIC, 240.0),
        rc((p + 11) % 12, ChordQuality.DIMINISHED, RelationshipType.LEADING_TONE, 300.0)
    )

    // ─── Minor context ────────────────────────────────────────────────────────

    /**
     * Relationships for a **minor** chord at pitch class [p].
     *
     * Angles (clockwise from 12 o'clock):
     * - 0°   ♭III – Relative major
     * - 60°  V    – Major dominant (harmonic minor)
     * - 120° iv   – Minor subdominant
     * - 180° par  – Parallel major (same root)
     * - 240° ii°  – Supertonic diminished
     * - 300° ♭VII – Subtonic major
     */
    private fun minorRelations(p: Int): List<RelatedChord> = listOfNotNull(
        rc((p + 3) % 12, ChordQuality.MAJOR, RelationshipType.RELATIVE_MAJOR, 0.0),
        rc((p + 7) % 12, ChordQuality.MAJOR, RelationshipType.MAJOR_DOMINANT, 60.0),
        rc((p + 5) % 12, ChordQuality.MINOR, RelationshipType.MINOR_SUBDOMINANT, 120.0),
        rc(p, ChordQuality.MAJOR, RelationshipType.PARALLEL, 180.0),
        rc((p + 2) % 12, ChordQuality.DIMINISHED, RelationshipType.SUPERTONIC_DIM, 240.0),
        rc((p + 10) % 12, ChordQuality.MAJOR, RelationshipType.FLAT_SEVENTH, 300.0)
    )

    // ─── Diminished context ───────────────────────────────────────────────────

    /**
     * Relationships for a **diminished** or **half-diminished** chord at pitch class [p].
     *
     * A diminished chord typically acts as a leading-tone surrogate, so we show the
     * two common resolution targets and the related minor chord from which it is built.
     */
    private fun dimRelations(p: Int): List<RelatedChord> = listOfNotNull(
        rc((p + 1) % 12, ChordQuality.MAJOR, RelationshipType.DOMINANT, 0.0),   // resolves up ½ step to major
        rc((p + 1) % 12, ChordQuality.MINOR, RelationshipType.DOMINANT, 60.0),  // resolves up ½ step to minor
        rc(p, ChordQuality.MINOR, RelationshipType.PARALLEL, 180.0) // related minor (same root)
    )

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Constructs a [RelatedChord] or returns `null` when the combination resolves to [Chord.UNDEFINED].
     */
    private fun rc(
        pitchClass: Int,
        quality: ChordQuality,
        type: RelationshipType,
        angle: Double
    ): RelatedChord? {
        val chord = Chord.from(pitchClass, quality, ChordExtension.NONE)
        return if (chord != Chord.UNDEFINED) RelatedChord(chord, type, angle) else null
    }
}