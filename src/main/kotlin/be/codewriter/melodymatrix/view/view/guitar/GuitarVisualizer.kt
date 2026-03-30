package be.codewriter.melodymatrix.view.view.guitar

import atlantafx.base.theme.Styles
import be.codewriter.melodymatrix.view.definition.Chord
import be.codewriter.melodymatrix.view.definition.Note
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.*

/**
 * Reusable guitar fretboard component used by guitar-focused views.
 *
 * The component keeps the rendering logic in one place and exposes lightweight
 * API methods to display either a chord fingering or playable note positions.
 */
class GuitarVisualizer(private val mode: Mode) {

    enum class Mode {
        CHORD,
        NOTE
    }

    val rootNode: VBox
    /** When false (default), only one most playable position is shown for NOTE mode. */
    var showAllPositions: Boolean = false

    private val titleLabel = Label().apply {
        style = "-fx-font-size: 22; -fx-font-weight: bold;"
    }
    private val detailsLabel = Label().apply {
        style = "-fx-font-size: 15;"
    }
    private val fretMarkers: MutableMap<Pair<Int, Int>, Label> = mutableMapOf()

    init {
        rootNode = VBox(
            titleLabel,
            detailsLabel,
            createFretboard(),
            createLegend()
        ).apply {
            spacing = 10.0
            padding = Insets(12.0)
        }
        clear()
    }

    fun clear() {
        when (mode) {
            Mode.CHORD -> {
                titleLabel.text = "Chord: -"
                detailsLabel.text = "Finger setting: -"
            }

            Mode.NOTE -> {
                titleLabel.text = "Note: -"
                detailsLabel.text = "Positions: -"
            }
        }
        clearFretboard()
    }

    fun showChord(chord: Chord) {
        if (chord == Chord.UNDEFINED) {
            clear()
            return
        }

        val voicing = GuitarChordVoicing.forChord(chord)
        titleLabel.text = "Chord: ${chord.label}"
        detailsLabel.text = "Finger setting: ${voicing.render()}"
        drawVoicing(voicing)
    }

    fun showNote(note: Note) {
        if (note == Note.UNDEFINED) {
            clear()
            return
        }

        val allPositions = findPlayablePositions(note)
        val positions = if (showAllPositions) {
            allPositions
        } else {
            listOfNotNull(bestSinglePosition(allPositions))
        }
        titleLabel.text = "Note: ${note.mainNote.label}${note.octave.octave}"
        detailsLabel.text = if (allPositions.isEmpty()) {
            "Positions: not available in first $MAX_FRET frets"
        } else {
            "Positions: ${renderPositions(positions)}"
        }

        clearFretboard()
        positions.forEach { (stringIndex, fret) ->
            val marker = fretMarkers[stringIndex to fret] ?: return@forEach
            if (fret == 0) {
                applyMarker(marker, Marker.OPEN)
            } else {
                applyMarker(marker, Marker.PRESSED)
            }
        }
    }

    private fun bestSinglePosition(positions: List<Pair<Int, Int>>): Pair<Int, Int>? {
        // Prefer lower fret positions; for ties, prefer strings around the neck center.
        return positions.minWithOrNull(compareBy({ it.second }, { kotlin.math.abs(it.first - 2) }))
    }

    private fun findPlayablePositions(note: Note): List<Pair<Int, Int>> {
        val positions = mutableListOf<Pair<Int, Int>>()
        for (stringIndex in OPEN_MIDI_BY_STRING.indices) {
            val fret = note.byteValue - OPEN_MIDI_BY_STRING[stringIndex]
            if (fret in 0..MAX_FRET) {
                positions += stringIndex to fret
            }
        }
        return positions
    }

    private fun renderPositions(positions: List<Pair<Int, Int>>): String {
        return positions.joinToString(" | ") { (stringIndex, fret) ->
            val value = if (fret == 0) "O" else fret.toString()
            "${STRING_LABELS[stringIndex]}: $value"
        }
    }

    private fun createFretboard(): GridPane {
        val grid = GridPane().apply {
            hgap = 6.0
            vgap = 6.0
            padding = Insets(8.0)
            style = FRETBOARD_STYLE
        }

        grid.columnConstraints.add(ColumnConstraints(72.0).apply {
            halignment = HPos.CENTER
        })
        for (fret in 0..MAX_FRET) {
            grid.columnConstraints.add(ColumnConstraints(52.0).apply {
                halignment = HPos.CENTER
            })
        }

        repeat(STRING_NAMES.size + 1) {
            grid.rowConstraints.add(RowConstraints(30.0))
        }

        grid.add(createGridLabel("String/Fret", "-fx-font-weight: bold;"), 0, 0)

        for (fret in 0..MAX_FRET) {
            grid.add(createGridLabel(fret.toString(), "-fx-font-weight: bold;"), fret + 1, 0)
        }

        STRING_NAMES.forEachIndexed { stringIndex, stringName ->
            grid.add(createGridLabel(stringName, "-fx-font-weight: bold;"), 0, stringIndex + 1)
            for (fret in 0..MAX_FRET) {
                val marker = createGridLabel(Marker.EMPTY.symbol, Marker.EMPTY.baseStyle).apply {
                    prefWidth = 34.0
                }
                applyMarker(marker, Marker.EMPTY)
                fretMarkers[stringIndex to fret] = marker
                grid.add(marker, fret + 1, stringIndex + 1)
            }
        }

        return grid
    }

    private fun createGridLabel(text: String, labelStyle: String): Label {
        return Label(text).apply {
            style = labelStyle
            maxWidth = Double.MAX_VALUE
            maxHeight = Double.MAX_VALUE
            alignment = Pos.CENTER
        }
    }

    private fun clearFretboard() {
        fretMarkers.values.forEach { marker -> applyMarker(marker, Marker.EMPTY) }
    }

    private fun drawVoicing(voicing: GuitarChordVoicing.Voicing) {
        clearFretboard()

        voicing.fretsByString.forEachIndexed { stringIndex, fret ->
            if (fret !in -1..MAX_FRET) {
                return@forEachIndexed
            }
            val marker = fretMarkers[stringIndex to 0] ?: return@forEachIndexed
            when {
                fret < 0 -> applyMarker(marker, Marker.MUTED)
                fret == 0 -> applyMarker(marker, Marker.OPEN)
                else -> {
                    val fretMarker = fretMarkers[stringIndex to fret] ?: return@forEachIndexed
                    applyMarker(fretMarker, Marker.PRESSED)
                }
            }
        }
    }

    private fun createLegend(): HBox {
        return HBox(
            Label("Legend:").apply {
                style = "-fx-font-size: 13; -fx-font-weight: bold;"
            },
            createLegendItem(Marker.MUTED, "Muted string"),
            createLegendItem(Marker.OPEN, "Open string"),
            createLegendItem(Marker.PRESSED, "Press fret"),
            createLegendItem(Marker.EMPTY, "Empty position")
        ).apply {
            spacing = 16.0
            padding = Insets(4.0, 0.0, 0.0, 4.0)
        }
    }

    private fun createLegendItem(marker: Marker, description: String): HBox {
        val markerLabel = Label(marker.symbol)
        applyMarker(markerLabel, marker)
        return HBox(
            markerLabel,
            Label(description).apply { style = "-fx-font-size: 13;" }
        ).apply {
            spacing = 6.0
        }
    }

    private fun applyMarker(label: Label, marker: Marker) {
        label.text = marker.symbol
        label.style = marker.baseStyle
        label.styleClass.remove(Styles.DANGER)
        if (marker.useDangerStyle) {
            label.styleClass.add(Styles.DANGER)
        }
    }

    private enum class Marker(
        val symbol: String,
        val baseStyle: String,
        val useDangerStyle: Boolean = true
    ) {
        EMPTY(".", "-fx-font-size: 18; -fx-opacity: 0.45;"),
        MUTED("X", "-fx-font-size: 18; -fx-font-weight: bold;"),
        OPEN("O", "-fx-font-size: 18; -fx-font-weight: bold;"),
        PRESSED("●", "-fx-font-size: 18; -fx-font-weight: bold;")
    }

    companion object {
        private const val MAX_FRET = 12
        private val STRING_NAMES = listOf("E2", "A2", "D3", "G3", "B3", "E4")
        private val STRING_LABELS = listOf("E", "A", "D", "G", "B", "e")
        private val OPEN_MIDI_BY_STRING = intArrayOf(40, 45, 50, 55, 59, 64)

        private const val FRETBOARD_STYLE = "-fx-background-color: -fx-control-inner-background; " +
            "-fx-background-radius: 8; " +
            "-fx-border-color: -fx-box-border; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 8;"
    }
}

