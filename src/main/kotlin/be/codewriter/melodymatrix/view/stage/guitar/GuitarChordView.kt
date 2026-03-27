package be.codewriter.melodymatrix.view.stage.guitar

import be.codewriter.melodymatrix.view.definition.Chord
import be.codewriter.melodymatrix.view.event.ChordEvent
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.MmxEventType
import be.codewriter.melodymatrix.view.stage.MmxMmxView
import be.codewriter.melodymatrix.view.stage.MmxViewMetadata
import javafx.application.Platform
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.*

/**
 * Visualizer stage that displays guitar chord fingerings for detected chords.
 *
 * Shows a fretboard grid (6 strings × 13 frets) and marks the finger positions
 * for the current chord. When a chord event arrives the board is updated with the
 * best available voicing from [GuitarChordVoicing]. A legend explains the fretboard markers.
 *
 * @see MmxMmxView
 * @see GuitarChordVoicing
 * @see ChordEvent
 */
class GuitarChordView : MmxMmxView() {

    private val chordLabel = Label("Chord: -")
    private val voicingLabel = Label("Finger setting: -")
    private val fretMarkers: MutableMap<Pair<Int, Int>, Label> = mutableMapOf()

    init {
        chordLabel.style = "-fx-font-size: 22; -fx-font-weight: bold;"
        voicingLabel.style = "-fx-font-size: 15;"

        val root = VBox(
            chordLabel,
            voicingLabel,
            createFretboard(),
            createLegend()
        ).apply {
            spacing = 10.0
            padding = Insets(12.0)
        }

        setupSurface(root, 860.0, 350.0)
    }

    /**
     * Handles incoming events.
     *
     * Only CHORD events are processed; MIDI and PLAY events are ignored.
     * On a chord-on event the fretboard is updated with the matching voicing;
     * on chord-off the board is cleared.
     *
     * @param event The MelodyMatrix event to process
     */
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

    /**
     * Builds the fretboard [GridPane] with string/fret labels and empty marker cells.
     *
     * @return A fully initialised [GridPane] representing the guitar neck
     */
    private fun createFretboard(): GridPane {
        val grid = GridPane().apply {
            hgap = 6.0
            vgap = 6.0
            padding = Insets(8.0)
            style = "-fx-background-color: #f8f5ec; -fx-border-color: #9f8869; -fx-border-width: 1;"
        }

        grid.columnConstraints.add(ColumnConstraints(72.0).apply {
            halignment = HPos.CENTER
        })
        for (fret in 0..MAX_FRET) {
            grid.columnConstraints.add(ColumnConstraints(52.0).apply {
                halignment = HPos.CENTER
            })
        }

        repeat(STRINGS.size + 1) {
            grid.rowConstraints.add(RowConstraints(30.0))
        }

        grid.add(createGridLabel("String/Fret", "-fx-font-weight: bold;"), 0, 0)

        for (fret in 0..MAX_FRET) {
            grid.add(createGridLabel(fret.toString(), "-fx-font-weight: bold;"), fret + 1, 0)
        }

        STRINGS.forEachIndexed { stringIndex, stringName ->
            grid.add(createGridLabel(stringName, "-fx-font-weight: bold;"), 0, stringIndex + 1)
            for (fret in 0..MAX_FRET) {
                val marker = createGridLabel(Marker.EMPTY.symbol, Marker.EMPTY.style).apply {
                    prefWidth = 34.0
                }
                fretMarkers[stringIndex to fret] = marker
                grid.add(marker, fret + 1, stringIndex + 1)
            }
        }

        return grid
    }

    /**
     * Creates a centred label styled with [labelStyle] for use in the fretboard grid.
     *
     * @param text       The label text
     * @param labelStyle An inline CSS style string
     * @return A styled [Label]
     */
    private fun createGridLabel(text: String, labelStyle: String): Label {
        return Label(text).apply {
            style = labelStyle
            maxWidth = Double.MAX_VALUE
            maxHeight = Double.MAX_VALUE
            alignment = Pos.CENTER
        }
    }

    /** Resets all fretboard marker cells to [Marker.EMPTY]. */
    private fun clearFretboard() {
        fretMarkers.values.forEach { marker ->
            applyMarker(marker, Marker.EMPTY)
        }
    }

    /**
     * Applies the given [voicing] to the fretboard by setting markers on the correct cells.
     *
     * @param voicing The chord voicing to visualise
     */
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

    /**
     * Builds the legend row explaining each [Marker] symbol.
     *
     * @return An [HBox] containing the legend items
     */
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

    /**
     * Creates a single legend item composed of a marker label and a description label.
     *
     * @param marker      The marker to display
     * @param description Human-readable description of the marker
     * @return An [HBox] with the marker and description side by side
     */
    private fun createLegendItem(marker: Marker, description: String): HBox {
        return HBox(
            Label(marker.symbol).apply { style = marker.style },
            Label(description).apply { style = "-fx-font-size: 13;" }
        ).apply {
            spacing = 6.0
        }
    }

    /**
     * Applies the symbol and style of [marker] to [label].
     *
     * @param label  The label to update
     * @param marker The marker whose symbol and style to apply
     */
    private fun applyMarker(label: Label, marker: Marker) {
        label.text = marker.symbol
        label.style = marker.style
    }

    /**
     * Fretboard position markers with their display symbols and inline CSS styles.
     */
    private enum class Marker(val symbol: String, val style: String) {
        /** An unoccupied fret position */
        EMPTY(".", "-fx-font-size: 18; -fx-text-fill: #8f8f8f;"),

        /** A muted (not played) string */
        MUTED("X", "-fx-font-size: 18; -fx-text-fill: #8a1f11; -fx-font-weight: bold;"),

        /** An open (unfretted) string */
        OPEN("O", "-fx-font-size: 18; -fx-text-fill: #1a6f2b; -fx-font-weight: bold;"),

        /** A fretted (pressed) position */
        PRESSED("●", "-fx-font-size: 18; -fx-text-fill: #1f4fa8; -fx-font-weight: bold;")
    }

    companion object : MmxViewMetadata {
        override fun getViewTitle(): String = "Play your chord on guitar"
        override fun getViewDescription(): String = "Displays guitar fretboard finger settings for detected chords."
        override fun getViewImagePath(): String = "/stage/guitar-chord.png"
        const val MAX_FRET = 12
        val STRINGS = listOf("E2", "A2", "D3", "G3", "B3", "E4")
    }
}
