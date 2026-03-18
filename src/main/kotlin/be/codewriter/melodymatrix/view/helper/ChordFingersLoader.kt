package be.codewriter.melodymatrix.view.helper

import be.codewriter.melodymatrix.view.definition.Chord
import be.codewriter.melodymatrix.view.definition.ChordAlteration
import be.codewriter.melodymatrix.view.definition.ChordExtension
import be.codewriter.melodymatrix.view.definition.ChordQuality

/**
 * Loads guitar chord fingerings from a CSV file.
 *
 * This object loads chord voicings (finger positions) from a CSV dataset.
 * The dataset is retrieved from Kaggle: https://www.kaggle.com/datasets/arnavsharma45/guitar-chord-fingers-dataset
 *
 * @see Chord
 * @see GuitarChordVoicing
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

    /**
     * Gets all available voicings for a given chord.
     *
     * @param chord The chord to find voicings for
     * @return A list of voicing fret arrays for the given chord, or an empty list if not found
     */
    fun voicingsFor(chord: Chord): List<IntArray> {
        return voicingsFor(chord, voicingsByChord)
    }

    /**
     * Gets voicings for a chord from a sequence of CSV lines.
     *
     * Internal method used for testing and flexibility.
     *
     * @param chord The chord to find voicings for
     * @param lines A sequence of CSV lines to parse
     * @return A list of voicing fret arrays for the given chord
     */
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

    /**
     * Converts root note and chord type strings to a ChordFingerKey.
     *
     * @param root The root note name (e.g., "C", "F#", "Bb")
     * @param chordType The chord type string (e.g., "maj", "m", "7", "m7")
     * @return A ChordFingerKey if the input is valid, null otherwise
     */
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

    /**
     * Parses a comma-separated string of fret positions into an IntArray.
     *
     * The input should contain 6 values (one per string). 'x' or 'X' represents muted (-1),
     * and numeric values represent fret numbers.
     *
     * @param rawFingers A comma-separated string of fret positions (e.g., "0,3,2,1,0,x")
     * @return An IntArray of length 6 with fret positions, or null if parsing fails
     */
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

    /**
     * Parses a CSV line, handling quoted values and escaped quotes.
     *
     * @param line The CSV line to parse
     * @return A list of parsed values
     */
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

    /**
     * Converts a note name to its pitch class (0-11).
     *
     * Supports note names with sharps (#) and flats (b/B).
     * For example: "C" = 0, "C#" = 1, "Db" = 1, "G" = 7.
     *
     * @param noteName The note name (e.g., "C", "F#", "Bb")
     * @return The pitch class (0-11), or null if the note name is invalid
     */
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