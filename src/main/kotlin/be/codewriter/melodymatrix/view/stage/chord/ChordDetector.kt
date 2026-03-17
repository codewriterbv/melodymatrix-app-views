package be.codewriter.melodymatrix.view.stage.chord

import be.codewriter.melodymatrix.view.definition.Note

enum class ChordQuality(val label: String) {
    MAJOR("major"),
    MINOR("minor")
}

data class ChordMatch(
    val rootPitchClass: Int,
    val quality: ChordQuality
) {
    val label: String
        get() = "${PITCH_CLASS_LABELS.getValue(rootPitchClass)} ${quality.label}"

    companion object {
        private val PITCH_CLASS_LABELS = mapOf(
            0 to "C",
            1 to "C#",
            2 to "D",
            3 to "D#",
            4 to "E",
            5 to "F",
            6 to "F#",
            7 to "G",
            8 to "G#",
            9 to "A",
            10 to "A#",
            11 to "B"
        )
    }
}

object ChordDetector {

    private val MAJOR_TRIAD = setOf(0, 4, 7)
    private val MINOR_TRIAD = setOf(0, 3, 7)

    fun detect(notes: Collection<Note>, rootPriority: List<Int> = emptyList()): ChordMatch? {
        val pitchClasses = notes
            .filter { it != Note.UNDEFINED }
            .map { pitchClass(it.byteValue) }
            .toSet()

        return detectPitchClasses(pitchClasses, rootPriority)
    }

    fun detectPitchClasses(pitchClasses: Set<Int>, rootPriority: List<Int> = emptyList()): ChordMatch? {
        if (pitchClasses.size < 3) {
            return null
        }

        val normalizedPitchClasses = pitchClasses.map(::pitchClass).toSet()
        val rootCandidates = orderedRootCandidates(normalizedPitchClasses, rootPriority)

        for (root in rootCandidates) {
            if (normalizedPitchClasses.containsAll(transposeIntervals(root, MAJOR_TRIAD))) {
                return ChordMatch(root, ChordQuality.MAJOR)
            }
            if (normalizedPitchClasses.containsAll(transposeIntervals(root, MINOR_TRIAD))) {
                return ChordMatch(root, ChordQuality.MINOR)
            }
        }

        return null
    }

    private fun orderedRootCandidates(pitchClasses: Set<Int>, rootPriority: List<Int>): List<Int> {
        val normalizedPriority = rootPriority.map(::pitchClass)
        val remaining = pitchClasses.sorted().filterNot { normalizedPriority.contains(it) }
        return (normalizedPriority + remaining).distinct().filter { pitchClasses.contains(it) }
    }

    private fun transposeIntervals(root: Int, intervals: Set<Int>): Set<Int> {
        return intervals.map { pitchClass(root + it) }.toSet()
    }

    private fun pitchClass(midiValue: Int): Int {
        return ((midiValue % 12) + 12) % 12
    }
}

