package be.codewriter.melodymatrix.view.helper

import be.codewriter.melodymatrix.view.definition.Chord
import be.codewriter.melodymatrix.view.definition.ChordAlteration
import be.codewriter.melodymatrix.view.definition.ChordExtension
import be.codewriter.melodymatrix.view.definition.ChordQuality

/**
 * CSV file retrieved from
 * https://www.kaggle.com/datasets/arnavsharma45/guitar-chord-fingers-dataset
 */
internal object ChordFingersLoader {

    private data class ChordFingerKey(
        val pitchClass: Int,
        val quality: ChordQuality,
        val extension: ChordExtension,
        val alteration: ChordAlteration
    )

    private val voicingsByChord: Map<ChordFingerKey, List<IntArray>> by lazy {
        val inputStream = FileLoader.getResource("/guitar/chord-fingers.csv") ?: return@lazy emptyMap()
        inputStream.bufferedReader().useLines(::loadVoicings)
    }

    fun voicingsFor(chord: Chord): List<IntArray> {
        return voicingsFor(chord, voicingsByChord)
    }

    internal fun voicingsFor(chord: Chord, lines: Sequence<String>): List<IntArray> {
        return voicingsFor(chord, loadVoicings(lines))
    }

    private fun voicingsFor(chord: Chord, voicingsByChord: Map<ChordFingerKey, List<IntArray>>): List<IntArray> {
        val key = ChordFingerKey(chord.pitchClass, chord.quality, chord.extension, chord.alteration)
        return voicingsByChord[key].orEmpty().map(IntArray::copyOf)
    }

    private fun loadVoicings(lines: Sequence<String>): Map<ChordFingerKey, List<IntArray>> {
        val groupedVoicings = linkedMapOf<ChordFingerKey, MutableList<IntArray>>()

        lines.drop(1).forEach { line ->
            val columns = parseCsvLine(line)
            if (columns.size < 4) {
                return@forEach
            }

            val chordKey = toChordKey(columns[0], columns[1]) ?: return@forEach
            val fretsByString = parseFretsByString(columns[3]) ?: return@forEach
            groupedVoicings.getOrPut(chordKey) { mutableListOf() }.add(fretsByString)
        }

        return groupedVoicings
    }

    private fun toChordKey(root: String, chordType: String): ChordFingerKey? {
        val pitchClass = pitchClassFromName(root) ?: return null
        val signature = when (chordType.trim()) {
            "maj" -> Triple(ChordQuality.MAJOR, ChordExtension.NONE, ChordAlteration.NONE)
            "m" -> Triple(ChordQuality.MINOR, ChordExtension.NONE, ChordAlteration.NONE)
            "7" -> Triple(ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, ChordAlteration.NONE)
            "m7" -> Triple(ChordQuality.MINOR, ChordExtension.MINOR_SEVENTH, ChordAlteration.NONE)
            "maj7" -> Triple(ChordQuality.MAJOR, ChordExtension.MAJOR_SEVENTH, ChordAlteration.NONE)
            "dim" -> Triple(ChordQuality.DIMINISHED, ChordExtension.NONE, ChordAlteration.NONE)
            "dim7" -> Triple(ChordQuality.DIMINISHED, ChordExtension.DIMINISHED_SEVENTH, ChordAlteration.NONE)
            "m7b5" -> Triple(ChordQuality.HALF_DIMINISHED, ChordExtension.MINOR_SEVENTH, ChordAlteration.NONE)
            "7b5" -> Triple(ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, ChordAlteration.FLAT_FIFTH)
            "7(#5)" -> Triple(ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, ChordAlteration.SHARP_FIFTH)
            else -> null
        } ?: return null

        return ChordFingerKey(pitchClass, signature.first, signature.second, signature.third)
    }

    private fun parseFretsByString(rawFingers: String): IntArray? {
        val tokens = rawFingers.split(",").map { it.trim() }
        if (tokens.size != 6) {
            return null
        }

        val frets = IntArray(tokens.size)
        tokens.forEachIndexed { index, token ->
            frets[index] = when {
                token.equals("x", ignoreCase = true) -> -1
                else -> token.toIntOrNull() ?: return null
            }
        }

        return frets
    }

    private fun parseCsvLine(line: String): List<String> {
        val values = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var index = 0

        while (index < line.length) {
            val char = line[index]
            when {
                char == '"' -> {
                    val hasEscapedQuote = inQuotes && index + 1 < line.length && line[index + 1] == '"'
                    if (hasEscapedQuote) {
                        current.append('"')
                        index++
                    } else {
                        inQuotes = !inQuotes
                    }
                }

                char == ';' && !inQuotes -> {
                    values.add(current.toString())
                    current.clear()
                }

                else -> current.append(char)
            }
            index++
        }

        values.add(current.toString())
        return values
    }

    private fun pitchClassFromName(noteName: String): Int? {
        val cleaned = noteName.trim()
        if (cleaned.isEmpty()) {
            return null
        }

        val basePitch = when (cleaned[0].uppercaseChar()) {
            'C' -> 0
            'D' -> 2
            'E' -> 4
            'F' -> 5
            'G' -> 7
            'A' -> 9
            'B' -> 11
            else -> return null
        }

        var pitchClass = basePitch
        for (position in 1 until cleaned.length) {
            when (cleaned[position]) {
                '#' -> pitchClass += 1
                'b', 'B' -> pitchClass -= 1
                else -> return null
            }
        }

        return ((pitchClass % 12) + 12) % 12
    }
}