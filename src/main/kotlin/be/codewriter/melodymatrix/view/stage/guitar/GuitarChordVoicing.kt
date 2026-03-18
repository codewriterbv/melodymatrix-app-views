package be.codewriter.melodymatrix.view.stage.guitar

import be.codewriter.melodymatrix.view.definition.Chord
import be.codewriter.melodymatrix.view.definition.ChordAlteration
import be.codewriter.melodymatrix.view.definition.ChordExtension
import be.codewriter.melodymatrix.view.definition.ChordQuality
import be.codewriter.melodymatrix.view.helper.ChordFingersLoader

internal object GuitarChordVoicing {

    private val openPitchClassByString = intArrayOf(4, 9, 2, 7, 11, 4)

    data class Voicing(
        val fretsByString: IntArray
    ) {
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

    fun forChord(chord: Chord): Voicing {
        findCsvVoicing(chord)?.let { return it }
        return autoToneBasedVoicing(chord)
    }

    private fun findCsvVoicing(chord: Chord): Voicing? {
        return ChordFingersLoader.voicingsFor(chord)
            .firstOrNull { voicing -> voicing.all { it in -1..MAX_CSV_FRET } }
            ?.let(::Voicing)
    }

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

    private fun lowERootFret(pitchClass: Int): Int {
        return ((pitchClass - 4) % 12 + 12) % 12
    }

    private val STRING_LABELS = listOf("E", "A", "D", "G", "B", "e")
    private const val MAX_CSV_FRET = 12
}

