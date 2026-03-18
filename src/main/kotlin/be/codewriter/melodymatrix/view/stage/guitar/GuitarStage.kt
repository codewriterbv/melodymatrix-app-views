package be.codewriter.melodymatrix.view.stage.guitar

import be.codewriter.melodymatrix.view.VisualizerStage
import be.codewriter.melodymatrix.view.definition.Chord
import be.codewriter.melodymatrix.view.definition.ChordAlteration
import be.codewriter.melodymatrix.view.definition.ChordExtension
import be.codewriter.melodymatrix.view.definition.ChordQuality
import be.codewriter.melodymatrix.view.event.ChordEvent
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.MmxEventType
import be.codewriter.melodymatrix.view.helper.FileLoader
import javafx.application.Platform
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox

class GuitarStage : VisualizerStage() {

    private val chordLabel = Label("Chord: -")
    private val voicingLabel = Label("Finger setting: -")
    private val fretMarkers: MutableMap<Pair<Int, Int>, Label> = mutableMapOf()

    init {
        title = "Play your chord on guitar"

        chordLabel.style = "-fx-font-size: 22; -fx-font-weight: bold;"
        voicingLabel.style = "-fx-font-size: 15;"

        scene = Scene(
            VBox(
                chordLabel,
                voicingLabel,
                createFretboard()
            ).apply {
                spacing = 10.0
                padding = Insets(12.0)
            },
            860.0,
            300.0
        )

        setOnCloseRequest {
            // Must be defined or the stage close flow is unstable on some systems.
        }
    }

    override fun onEvent(event: MmxEvent) {
        if (event.type != MmxEventType.CHORD) {
            return
        }

        val chordEvent = event as? ChordEvent ?: return
        Platform.runLater {
            val showChord = chordEvent.on && chordEvent.chord != Chord.UNDEFINED
            if (!showChord) {
                chordLabel.text = "Chord: -"
                voicingLabel.text = "Finger setting: -"
                clearFretboard()
                return@runLater
            }

            val voicing = GuitarChordVoicing.forChord(chordEvent.chord)
            chordLabel.text = "Chord: ${chordEvent.chord.label}"
            voicingLabel.text = "Finger setting: ${voicing.render()}"
            drawVoicing(voicing)
        }
    }

    private fun createFretboard(): GridPane {
        val grid = GridPane().apply {
            hgap = 6.0
            vgap = 6.0
            padding = Insets(8.0)
            style = "-fx-background-color: #f8f5ec; -fx-border-color: #9f8869; -fx-border-width: 1;"
        }

        grid.add(Label("String/Fret").apply { style = "-fx-font-weight: bold;" }, 0, 0)

        for (fret in 0..MAX_FRET) {
            grid.columnConstraints.add(ColumnConstraints(52.0).apply {
                halignment = HPos.CENTER
            })
            grid.add(Label(fret.toString()).apply { style = "-fx-font-weight: bold;" }, fret + 1, 0)
        }

        STRINGS.forEachIndexed { stringIndex, stringName ->
            grid.add(Label(stringName).apply { style = "-fx-font-weight: bold;" }, 0, stringIndex + 1)
            for (fret in 0..MAX_FRET) {
                val marker = Label(".").apply {
                    prefWidth = 34.0
                    style = "-fx-font-size: 18; -fx-text-fill: #8f8f8f;"
                }
                fretMarkers[stringIndex to fret] = marker
                grid.add(marker, fret + 1, stringIndex + 1)
            }
        }

        return grid
    }

    private fun clearFretboard() {
        fretMarkers.values.forEach {
            it.text = "."
            it.style = "-fx-font-size: 18; -fx-text-fill: #8f8f8f;"
        }
    }

    private fun drawVoicing(voicing: GuitarChordVoicing.Voicing) {
        clearFretboard()

        voicing.fretsByString.forEachIndexed { stringIndex, fret ->
            if (fret !in -1..MAX_FRET) {
                return@forEachIndexed
            }
            val marker = fretMarkers[stringIndex to 0] ?: return@forEachIndexed
            when {
                fret < 0 -> {
                    marker.text = "X"
                    marker.style = "-fx-font-size: 18; -fx-text-fill: #8a1f11; -fx-font-weight: bold;"
                }

                fret == 0 -> {
                    marker.text = "O"
                    marker.style = "-fx-font-size: 18; -fx-text-fill: #1a6f2b; -fx-font-weight: bold;"
                }

                else -> {
                    val fretMarker = fretMarkers[stringIndex to fret] ?: return@forEachIndexed
                    fretMarker.text = "●"
                    fretMarker.style = "-fx-font-size: 18; -fx-text-fill: #1f4fa8; -fx-font-weight: bold;"
                }
            }
        }
    }

    private companion object {
        const val MAX_FRET = 12
        val STRINGS = listOf("E2", "A2", "D3", "G3", "B3", "E4")
    }
}

internal object GuitarChordVoicing {

    private val openPitchClassByString = intArrayOf(4, 9, 2, 7, 11, 4)

    /**
     * Standard guitar voicing patterns relative to root position.
     * Using -1 for muted strings, 0 for open strings, and positive numbers for fret positions.
     * Format: [E string, A string, D string, G string, B string, e string]
     */
    data class VoicingPattern(
        val fretOffsets: IntArray
    ) {
        fun toVoicing(rootPitchClass: Int): Voicing {
            val rootFret = lowERootFret(rootPitchClass)
            return Voicing(fretOffsets.map { offset ->
                when {
                    offset < 0 -> -1 // Muted
                    else -> rootFret + offset
                }
            }.toIntArray())
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is VoicingPattern) return false
            return fretOffsets.contentEquals(other.fretOffsets)
        }

        override fun hashCode(): Int {
            return fretOffsets.contentHashCode()
        }
    }

    // Comprehensive chord voicing database
    // Key structure: Triple of (ChordQuality, ChordExtension, ChordAlteration)
    private val CHORD_VOICING_DATABASE = mapOf(
        // MAJOR chords
        Triple(ChordQuality.MAJOR, ChordExtension.NONE, ChordAlteration.NONE) to
                VoicingPattern(intArrayOf(0, 0, 2, 2, 1, 0)), // Standard major voicing

        // MINOR chords
        Triple(ChordQuality.MINOR, ChordExtension.NONE, ChordAlteration.NONE) to
                VoicingPattern(intArrayOf(0, 0, 2, 2, 0, -1)), // Standard minor voicing (3 notes)

        // DOMINANT 7th chords
        Triple(ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, ChordAlteration.NONE) to
                VoicingPattern(intArrayOf(0, 0, 2, 1, 0, -1)), // Dominant 7th voicing

        // MINOR 7th chords
        Triple(ChordQuality.MINOR, ChordExtension.MINOR_SEVENTH, ChordAlteration.NONE) to
                VoicingPattern(intArrayOf(0, 0, 2, 0, 0, -1)), // Minor 7th voicing (4 notes)

        // MAJOR 7th chords
        Triple(ChordQuality.MAJOR, ChordExtension.MAJOR_SEVENTH, ChordAlteration.NONE) to
                VoicingPattern(intArrayOf(0, 0, 2, 1, 1, 0)), // Major 7th voicing

        // DIMINISHED chords
        Triple(ChordQuality.DIMINISHED, ChordExtension.NONE, ChordAlteration.NONE) to
                VoicingPattern(intArrayOf(0, -1, 2, 2, 0, -1)), // Diminished voicing

        // DIMINISHED 7th chords
        Triple(ChordQuality.DIMINISHED, ChordExtension.DIMINISHED_SEVENTH, ChordAlteration.NONE) to
                VoicingPattern(intArrayOf(0, -1, 2, 1, 0, -1)), // Diminished 7th voicing

        // HALF-DIMINISHED (minor 7b5) chords
        Triple(ChordQuality.HALF_DIMINISHED, ChordExtension.MINOR_SEVENTH, ChordAlteration.NONE) to
                VoicingPattern(intArrayOf(0, -1, 2, 0, 0, -1)), // Half-diminished voicing

        // MAJOR with FLAT_FIFTH
        Triple(ChordQuality.MAJOR, ChordExtension.NONE, ChordAlteration.FLAT_FIFTH) to
                VoicingPattern(intArrayOf(0, -1, 2, 1, 0, -1)), // Major flat 5

        // MAJOR with SHARP_FIFTH
        Triple(ChordQuality.MAJOR, ChordExtension.NONE, ChordAlteration.SHARP_FIFTH) to
                VoicingPattern(intArrayOf(0, 0, 2, 2, 1, -1)), // Major sharp 5

        // MINOR with FLAT_FIFTH
        Triple(ChordQuality.MINOR, ChordExtension.NONE, ChordAlteration.FLAT_FIFTH) to
                VoicingPattern(intArrayOf(0, -1, 2, 1, 0, -1)), // Minor flat 5 (diminished)

        // MINOR with SHARP_FIFTH
        Triple(ChordQuality.MINOR, ChordExtension.NONE, ChordAlteration.SHARP_FIFTH) to
                VoicingPattern(intArrayOf(0, 0, 2, 2, 1, -1)), // Minor sharp 5

        // DOMINANT 7th with FLAT_FIFTH
        Triple(ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, ChordAlteration.FLAT_FIFTH) to
                VoicingPattern(intArrayOf(0, -1, 2, 0, 0, -1)), // Dominant 7 flat 5

        // DOMINANT 7th with SHARP_FIFTH
        Triple(ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH, ChordAlteration.SHARP_FIFTH) to
                VoicingPattern(intArrayOf(0, 0, 2, 1, 1, -1)), // Dominant 7 sharp 5

        // MAJOR 7th with FLAT_FIFTH
        Triple(ChordQuality.MAJOR, ChordExtension.MAJOR_SEVENTH, ChordAlteration.FLAT_FIFTH) to
                VoicingPattern(intArrayOf(0, -1, 2, 0, 1, -1)), // Major 7 flat 5

        // MAJOR 7th with SHARP_FIFTH
        Triple(ChordQuality.MAJOR, ChordExtension.MAJOR_SEVENTH, ChordAlteration.SHARP_FIFTH) to
                VoicingPattern(intArrayOf(0, 0, 2, 2, 1, -1)), // Major 7 sharp 5

        // MINOR 7th with FLAT_FIFTH
        Triple(ChordQuality.MINOR, ChordExtension.MINOR_SEVENTH, ChordAlteration.FLAT_FIFTH) to
                VoicingPattern(intArrayOf(0, -1, 2, 0, 0, -1)), // Minor 7 flat 5 (half-diminished)

        // MINOR 7th with SHARP_FIFTH
        Triple(ChordQuality.MINOR, ChordExtension.MINOR_SEVENTH, ChordAlteration.SHARP_FIFTH) to
                VoicingPattern(intArrayOf(0, 0, 2, 1, 0, -1)), // Minor 7 sharp 5
    )

    private data class CsvChordKey(
        val pitchClass: Int,
        val quality: ChordQuality,
        val extension: ChordExtension,
        val alteration: ChordAlteration
    )

    private val csvVoicingsByChord: Map<CsvChordKey, List<Voicing>> by lazy {
        loadVoicingsFromCsv()
    }

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

        val rootPitchClass = chord.pitchClass
        val voicing = findStandardVoicing(rootPitchClass, chord.quality, chord.extension, chord.alteration)
        if (voicing != null) {
            return voicing
        }

        return autoToneBasedVoicing(chord)
    }

    private fun findCsvVoicing(chord: Chord): Voicing? {
        val key = CsvChordKey(chord.pitchClass, chord.quality, chord.extension, chord.alteration)
        return csvVoicingsByChord[key]
            ?.firstOrNull { voicing -> voicing.fretsByString.all { it in -1..MAX_CSV_FRET } }
    }

    private fun loadVoicingsFromCsv(): Map<CsvChordKey, List<Voicing>> {
        val inputStream = FileLoader.getResource("/guitar/chord-fingers.csv") ?: return emptyMap()
        val groupedVoicings = linkedMapOf<CsvChordKey, MutableList<Voicing>>()

        inputStream.bufferedReader().useLines { lines ->
            lines.drop(1).forEach { line ->
                val columns = parseCsvLine(line)
                if (columns.size < 4) {
                    return@forEach
                }

                val chordKey = toChordKey(columns[0], columns[1]) ?: return@forEach
                val fretsByString = parseFretsByString(columns[3]) ?: return@forEach

                groupedVoicings.getOrPut(chordKey) { mutableListOf() }.add(Voicing(fretsByString))
            }
        }

        return groupedVoicings
    }

    private fun toChordKey(root: String, chordType: String): CsvChordKey? {
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

        return CsvChordKey(pitchClass, signature.first, signature.second, signature.third)
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

    private fun findStandardVoicing(
        rootPitchClass: Int,
        quality: ChordQuality,
        extension: ChordExtension,
        alteration: ChordAlteration
    ): Voicing? {
        val voicingPattern = CHORD_VOICING_DATABASE[Triple(quality, extension, alteration)] ?: return null
        return voicingPattern.toVoicing(rootPitchClass)
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
