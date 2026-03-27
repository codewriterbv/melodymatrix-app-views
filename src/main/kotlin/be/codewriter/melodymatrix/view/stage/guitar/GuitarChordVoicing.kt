package be.codewriter.melodymatrix.view.stage.guitar

import be.codewriter.melodymatrix.view.definition.Chord
import be.codewriter.melodymatrix.view.definition.ChordAlteration
import be.codewriter.melodymatrix.view.definition.ChordExtension
import be.codewriter.melodymatrix.view.definition.ChordQuality
import be.codewriter.melodymatrix.view.helper.ChordFingersLoader
import be.codewriter.melodymatrix.view.stage.guitar.GuitarChordVoicing.MAX_CSV_FRET
import be.codewriter.melodymatrix.view.stage.guitar.GuitarChordVoicing.autoToneBasedVoicing

/**
 * Resolves a guitar chord [Voicing] for a given [Chord].
 *
 * First tries to find a voicing from a CSV fingering database via [ChordFingersLoader].
 * If none is found, falls back to an automatic tone-based algorithm that maps each chord
 * tone to the nearest fret on each string.
 *
 * @see GuitarChordView
 * @see ChordFingersLoader
 */
internal object GuitarChordVoicing {

    private val openPitchClassByString = intArrayOf(4, 9, 2, 7, 11, 4)

    /**
     * Represents a guitar chord voicing as fret numbers for each of the 6 strings.
     *
     * A value of -1 means the string is muted; 0 means open string.
     *
     * @property fretsByString Array of 6 fret numbers, one per string (low E to high e)
     */
    data class Voicing(
        val fretsByString: IntArray
    ) {
        /**
         * Returns a human-readable rendering of the voicing, e.g. "E: X | A: 3 | D: 2 | …".
         *
         * @return Formatted string showing each string's fret or X/O symbol
         */
        fun render(): String {
            return STRING_LABELS.indices.joinToString(" | ") { idx ->
                val fret = fretsByString[idx]
                when {
                    fret < 0 -> "${STRING_LABELS[idx]}: X"
                    fret == 0 -> "${STRING_LABELS[idx]}: O"
                    else -> "${STRING_LABELS[idx]}: $fret"
                }
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Voicing) return false
            return fretsByString.contentEquals(other.fretsByString)
        }

        override fun hashCode(): Int {
            return fretsByString.contentHashCode()
        }
    }

    /**
     * Returns the best available voicing for [chord].
     *
     * Prefers CSV-sourced fingerings; falls back to [autoToneBasedVoicing] if none match.
     *
     * @param chord The chord to look up
     * @return A [Voicing] for the chord; never null (returns muted strings if chord is unknown)
     */
    fun forChord(chord: Chord): Voicing {
        findCsvVoicing(chord)?.let { return it }
        return autoToneBasedVoicing(chord)
    }

    /**
     * Looks up a voicing from the CSV fingering database.
     *
     * Only returns a voicing if all fret numbers are within 0–[MAX_CSV_FRET].
     *
     * @param chord The chord to look up
     * @return A matching [Voicing], or null if none found in the database
     */
    private fun findCsvVoicing(chord: Chord): Voicing? {
        return ChordFingersLoader.voicingsFor(chord)
            .firstOrNull { voicing -> voicing.all { it in -1..MAX_CSV_FRET } }
            ?.let(::Voicing)
    }

    /**
     * Generates a voicing algorithmically by placing each chord tone on the nearest fret.
     *
     * Roots the voicing around the low-E string root position calculated from [chord.pitchClass].
     *
     * @param chord The chord to voice
     * @return A [Voicing] computed from chord tones
     */
    private fun autoToneBasedVoicing(chord: Chord): Voicing {
        val chordTones = chordTonePitchClasses(chord)
        if (chordTones.isEmpty()) {
            return Voicing(intArrayOf(-1, -1, -1, -1, -1, -1))
        }

        val target = lowERootFret(chord.pitchClass)
        val frets = IntArray(6) { -1 }

        for (stringIndex in openPitchClassByString.indices) {
            var bestFret = -1
            var bestDistance = Int.MAX_VALUE

            for (fret in 0..12) {
                val pitchClass = (openPitchClassByString[stringIndex] + fret) % 12
                if (!chordTones.contains(pitchClass)) {
                    continue
                }

                val distance = kotlin.math.abs(fret - target)
                if (distance < bestDistance || (distance == bestDistance && (bestFret == -1 || fret < bestFret))) {
                    bestDistance = distance
                    bestFret = fret
                }
            }

            frets[stringIndex] = bestFret
        }

        return Voicing(frets)
    }

    /**
     * Computes the set of pitch classes (0–11) that make up [chord].
     *
     * Combines quality intervals, extension intervals, and alteration adjustments,
     * then transposes them to the chord's root pitch class.
     *
     * @param chord The chord whose tones should be returned
     * @return A [Set] of pitch class integers (0–11)
     */
    private fun chordTonePitchClasses(chord: Chord): Set<Int> {
        val intervals = mutableSetOf<Int>()

        when (chord.quality) {
            ChordQuality.MAJOR, ChordQuality.DOMINANT -> intervals.addAll(listOf(0, 4, 7))
            ChordQuality.MINOR -> intervals.addAll(listOf(0, 3, 7))
            ChordQuality.DIMINISHED, ChordQuality.HALF_DIMINISHED -> intervals.addAll(listOf(0, 3, 6))
        }

        when (chord.extension) {
            ChordExtension.MAJOR_SEVENTH -> intervals.add(11)
            ChordExtension.MINOR_SEVENTH -> intervals.add(10)
            ChordExtension.DIMINISHED_SEVENTH -> intervals.add(9)
            ChordExtension.NONE -> {
                // No extension to add.
            }
        }

        when (chord.alteration) {
            ChordAlteration.FLAT_FIFTH -> {
                intervals.remove(7)
                intervals.add(6)
            }

            ChordAlteration.SHARP_FIFTH -> {
                intervals.remove(7)
                intervals.add(8)
            }

            ChordAlteration.NONE -> {
                // No alteration to apply.
            }
        }

        return intervals.map { (chord.pitchClass + it) % 12 }.toSet()
    }

    /**
     * Returns the fret number on the low-E string that plays the given [pitchClass].
     *
     * @param pitchClass The target pitch class (0–11)
     * @return Fret number in range 0–11
     */
    private fun lowERootFret(pitchClass: Int): Int {
        return ((pitchClass - 4) % 12 + 12) % 12
    }

    private val STRING_LABELS = listOf("E", "A", "D", "G", "B", "e")
    private const val MAX_CSV_FRET = 12
}
