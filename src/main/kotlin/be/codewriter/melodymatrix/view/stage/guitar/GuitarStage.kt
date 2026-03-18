package be.codewriter.melodymatrix.view.stage.guitar

import be.codewriter.melodymatrix.view.VisualizerStage
import be.codewriter.melodymatrix.view.definition.Chord
import be.codewriter.melodymatrix.view.event.ChordEvent
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.MmxEventType
import javafx.application.Platform
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.*

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
                createFretboard(),
                createLegend()
            ).apply {
                spacing = 10.0
                padding = Insets(12.0)
            },
            860.0,
            350.0
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

    private fun createGridLabel(text: String, labelStyle: String): Label {
        return Label(text).apply {
            style = labelStyle
            maxWidth = Double.MAX_VALUE
            maxHeight = Double.MAX_VALUE
            alignment = Pos.CENTER
        }
    }

    private fun clearFretboard() {
        fretMarkers.values.forEach { marker ->
            applyMarker(marker, Marker.EMPTY)
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
        return HBox(
            Label(marker.symbol).apply { style = marker.style },
            Label(description).apply { style = "-fx-font-size: 13;" }
        ).apply {
            spacing = 6.0
        }
    }

    private fun applyMarker(label: Label, marker: Marker) {
        label.text = marker.symbol
        label.style = marker.style
    }

    private enum class Marker(val symbol: String, val style: String) {
        EMPTY(".", "-fx-font-size: 18; -fx-text-fill: #8f8f8f;"),
        MUTED("X", "-fx-font-size: 18; -fx-text-fill: #8a1f11; -fx-font-weight: bold;"),
        OPEN("O", "-fx-font-size: 18; -fx-text-fill: #1a6f2b; -fx-font-weight: bold;"),
        PRESSED("●", "-fx-font-size: 18; -fx-text-fill: #1f4fa8; -fx-font-weight: bold;")
    }

    private companion object {
        const val MAX_FRET = 12
        val STRINGS = listOf("E2", "A2", "D3", "G3", "B3", "E4")
    }
}
