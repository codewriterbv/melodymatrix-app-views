package be.codewriter.melodymatrix.view.view.chord

import be.codewriter.melodymatrix.view.component.ZoomableNode
import be.codewriter.melodymatrix.view.definition.Chord
import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.event.ChordEvent
import be.codewriter.melodymatrix.view.event.MidiDataEvent
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.MmxEventType
import be.codewriter.melodymatrix.view.i18n.I18n
import be.codewriter.melodymatrix.view.view.MmxView
import be.codewriter.melodymatrix.view.view.MmxViewMetadata
import be.codewriter.melodymatrix.view.view.sheet.SheetMusicAdapter
import com.sheetmusic4j.engraving.glyph.MarkingCategory
import com.sheetmusic4j.fxviewer.SheetView
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color

/**
 * Visualizer stage that shows the currently played notes on a small grand-staff snippet
 * plus the detected chord name.
 *
 * The staff is rendered by sheetmusic4j: on every MIDI or chord event we rebuild a
 * one-measure snippet score containing the active notes as chords of whole notes
 * (treble = notes >= C4, bass = notes < C4). Real accidentals are drawn by the engraver.
 *
 * The sheet is wrapped in a [ZoomableNode] and the view opts in to
 * [MmxView.fitToViewport] so the notation scales with the host viewport rather than
 * rendering at a fixed pixel size.
 *
 * @see MmxView
 * @see ChordEvent
 * @see MidiDataEvent
 */
class ChordView : MmxView() {

    override val fitToViewport: Boolean = true

    private val activeNotes: MutableSet<Note> = linkedSetOf()
    private val bundle = I18n.registerBundle("i18n/view/chord")
    private val chordLabel = Label()
    private val chordNotesLabel = Label()

    private val sheet = SheetView().apply {
        setSystemWidth(SNIPPET_SYSTEM_WIDTH)
        hiddenTextCategoriesProperty().addAll(
            listOf(
                MarkingCategory.TITLE,
                MarkingCategory.SUBTITLE,
                MarkingCategory.CREATOR,
                MarkingCategory.LYRIC,
                MarkingCategory.DIRECTION,
                MarkingCategory.TEMPO,
                MarkingCategory.DYNAMIC,
                MarkingCategory.REHEARSAL,
                MarkingCategory.CHORD_SYMBOL,
                MarkingCategory.PART_LABEL
            )
        )
    }

    private var snippetRebuildPending = false

    init {
        chordLabel.text = defaultChordText()
        chordNotesLabel.text = defaultNotesText()
        I18n.currentLocale.addListener { _, _, _ ->
            if (activeNotes.isEmpty()) chordNotesLabel.text = defaultNotesText()
            if (chordLabel.text.endsWith("-") || chordLabel.text.isBlank()) chordLabel.text = defaultChordText()
        }
        chordLabel.style = "-fx-font-size: 22; -fx-font-weight: bold;"
        chordNotesLabel.style = "-fx-font-size: 16;"

        // Prime the sheet with an empty snippet so clef + staves show even when idle.
        refreshSnippet()

        val header = VBox(chordLabel, chordNotesLabel).apply {
            spacing = 4.0
            padding = Insets(12.0, 12.0, 4.0, 20.0)
        }
        val zoomable = ZoomableNode(
            content = sheet,
            naturalWidth = SNIPPET_SYSTEM_WIDTH,
            naturalHeight = SNIPPET_NATURAL_HEIGHT,
            minWidthValue = 240.0,
            minHeightValue = 120.0,
            fitMode = ZoomableNode.FitMode.CONTAIN
        )
        val root = BorderPane().apply {
            top = header
            center = zoomable
        }
        setupSurface(root, SNIPPET_SYSTEM_WIDTH, SNIPPET_NATURAL_HEIGHT + HEADER_HEIGHT, sheet)
    }

    override fun onEvent(event: MmxEvent) {
        when (event.type) {
            MmxEventType.MIDI -> {
                val midi = event as? MidiDataEvent ?: return
                handleMidiEvent(midi)
            }

            MmxEventType.CHORD -> {
                val chordEvent = event as? ChordEvent ?: return
                Platform.runLater {
                    val isChordOn = (chordEvent.chord != Chord.UNDEFINED) && chordEvent.on
                    chordLabel.text =
                        if (isChordOn) "${chordPrefix()} ${chordEvent.chord.label}" else defaultChordText()
                    chordNotesLabel.text = if (isChordOn) notesText() else defaultNotesText()
                }
            }

            MmxEventType.PLAY,
            MmxEventType.AUDIO_SPECTRUM,
            MmxEventType.PLAYBACK_STOP -> {
                // Not needed here
            }
        }
    }

    private fun handleMidiEvent(midi: MidiDataEvent) {
        if (midi.isDrum || midi.note == Note.UNDEFINED) return

        Platform.runLater {
            val changed = when (midi.event) {
                MidiEvent.NOTE_ON -> activeNotes.add(midi.note)
                MidiEvent.NOTE_OFF -> activeNotes.remove(midi.note)
                else -> return@runLater
            }
            if (changed) {
                chordNotesLabel.text = notesText()
                scheduleSnippetRebuild()
            }
        }
    }

    /**
     * Coalesce snippet rebuilds inside a single JavaFX pulse so a burst of near-simultaneous
     * NOTE_ON events (chord press) doesn't trigger N re-engraves.
     */
    private fun scheduleSnippetRebuild() {
        if (snippetRebuildPending) return
        snippetRebuildPending = true
        Platform.runLater {
            snippetRebuildPending = false
            refreshSnippet()
        }
    }

    private fun refreshSnippet() {
        val result = SheetMusicAdapter.buildSnippet(activeNotes.toSet())
        sheet.setScore(result.score)
        val highlights = sheet.noteHighlights()
        highlights.clear()
        result.notesByApp.forEach { (_, smNote) -> highlights[smNote] = ACTIVE_HIGHLIGHT }
    }

    private fun notesText(): String {
        val labels = activeNotes
            .asSequence()
            .filter { it != Note.UNDEFINED }
            .sortedBy { it.byteValue }
            .map { "${it.mainNote.label}${it.octave.octave}" }
            .toList()

        return if (labels.isEmpty()) defaultNotesText() else "${notesPrefix()} ${labels.joinToString(", ")}"
    }

    private fun chordPrefix(): String = I18n.get(bundle, "chord.label.chord")
    private fun notesPrefix(): String = I18n.get(bundle, "chord.label.notes")
    private fun defaultChordText(): String = "${chordPrefix()} -"
    private fun defaultNotesText(): String = "${notesPrefix()} -"

    companion object : MmxViewMetadata {
        override val bundleBaseName = "i18n/view/chord"
        override val bundleKeyPrefix = "chord."
        override fun getViewImagePath(): String = "/view/chord.png"

        private const val SNIPPET_SYSTEM_WIDTH: Double = 640.0
        private const val SNIPPET_NATURAL_HEIGHT: Double = 260.0
        private const val HEADER_HEIGHT: Double = 80.0

        private val ACTIVE_HIGHLIGHT: Color = Color.CRIMSON
    }
}
