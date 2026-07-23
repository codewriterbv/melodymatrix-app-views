package be.codewriter.melodymatrix.view.view.staff

import be.codewriter.melodymatrix.view.component.ZoomableNode
import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.event.MidiDataEvent
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.MmxEventType
import be.codewriter.melodymatrix.view.view.MmxView
import be.codewriter.melodymatrix.view.view.MmxViewMetadata
import be.codewriter.melodymatrix.view.view.sheet.SheetMusicAdapter
import com.sheetmusic4j.core.model.Accidental
import com.sheetmusic4j.core.model.Note as SmNote
import com.sheetmusic4j.engraving.glyph.MarkingCategory
import com.sheetmusic4j.fxviewer.SheetView
import javafx.application.Platform
import javafx.scene.paint.Color

/**
 * Visualizer stage that displays a fixed two-octave grand staff and highlights the played
 * notes on it in real time.
 *
 * The reference score is a diatonic naturals-only grand staff engraved by sheetmusic4j's
 * [SheetView]. On a NOTE_ON we resolve the target [SmNote] via [SheetMusicAdapter] and
 * drive three of [SheetView]'s live-observable maps:
 *  - [SheetView.noteHighlights] recolours the notehead + stem + accidental,
 *  - [SheetView.noteBackgrounds] paints a rounded, semi-transparent rectangle behind the
 *    notehead (including the accidental slot) for a strong "played right now" pop, and
 *  - [SheetView.noteAccidentals] overlays a SMuFL accidental glyph next to the notehead
 *    for sharps — the reference score itself carries only naturals, so a sharp is drawn
 *    as `♯` next to its natural parent's notehead.
 *
 * All rendering happens inside the [SheetView]'s own canvas, so the view is a single
 * JavaFX node deep and needs no overlay pane.
 *
 * @see MmxView
 * @see MidiDataEvent
 */
class StaffView : MmxView() {

    override val fitToViewport: Boolean = true

    private val sheet = SheetView().apply {
        setSystemWidth(REFERENCE_SYSTEM_WIDTH)
        // Reference staff only shows clef + notes; hide page-level texts and directional
        // markings so nothing but the notation is drawn.
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

    private val mapping: Map<Note, SmNote>

    /** Highlight bookkeeping keyed by app [Note] so releases can undo exactly what NOTE_ON added. */
    private val activeHighlights: MutableMap<Note, HighlightEntry> = HashMap()

    init {
        val result = SheetMusicAdapter.buildReferenceStaff()
        mapping = result.notesByApp
        sheet.setScore(result.score)

        // Read the actual engraved dimensions after setScore so the ZoomableNode's
        // natural size matches what the SheetView is really drawing. sheetmusic4j
        // 0.0.1 grows `layout.height()` to cover ledger lines below the last staff,
        // so we no longer need to pad the canvas manually.
        val layout = sheet.layout
        val naturalWidth = layout?.width()?.coerceAtLeast(REFERENCE_SYSTEM_WIDTH) ?: REFERENCE_SYSTEM_WIDTH
        val naturalHeight = layout?.height()?.coerceAtLeast(REFERENCE_MIN_NATURAL_HEIGHT)
            ?: REFERENCE_MIN_NATURAL_HEIGHT

        val zoomable = ZoomableNode(
            content = sheet,
            naturalWidth = naturalWidth,
            naturalHeight = naturalHeight,
            minWidthValue = 200.0,
            minHeightValue = 120.0,
            fitMode = ZoomableNode.FitMode.CONTAIN
        )

        setupSurface(zoomable, naturalWidth, naturalHeight, sheet)
    }

    override fun onEvent(event: MmxEvent) {
        when (event.type) {
            MmxEventType.MIDI -> {
                val midi = event as? MidiDataEvent ?: return
                val note = midi.note
                if (note == Note.UNDEFINED) return
                Platform.runLater {
                    when (midi.event) {
                        MidiEvent.NOTE_ON -> highlightOn(note)
                        MidiEvent.NOTE_OFF -> highlightOff(note)
                        else -> { /* ignore */
                        }
                    }
                }
            }

            MmxEventType.PLAY,
            MmxEventType.CHORD,
            MmxEventType.AUDIO_SPECTRUM,
            MmxEventType.PLAYBACK_STOP -> {
                // Not needed here
            }
        }
    }

    private fun highlightOn(app: Note) {
        // Ignore double NOTE_ONs (same key pressed while already active).
        if (activeHighlights.containsKey(app)) return

        val element = SheetMusicAdapter.staffElementForApp(app, mapping) ?: return

        val isSharp = app.mainNote.isSharp
        val baseColor = if (isSharp) SHARP_HIGHLIGHT else NATURAL_HIGHLIGHT

        // 1. Notehead recolour via SheetView's built-in highlight map.
        sheet.noteHighlights()[element] = baseColor

        // 2. Semi-transparent yellow rounded rectangle behind the notehead via
        //    SheetView's built-in background map. The engraver already pads the
        //    rectangle so it covers the accidental slot; we just hand it a colour.
        sheet.noteBackgrounds()[element] = HIGHLIGHT_BG_COLOR

        // 3. Floating "♯" accidental drawn by the SheetView itself (0.0.1+). Uses the
        //    same SMuFL Bravura glyph the engraver would draw for a real MusicXML
        //    `<accidental>` and the same left-of-notehead offset, so it visually
        //    matches engraved accidentals in every other view.
        if (isSharp) {
            sheet.noteAccidentals()[element] = Accidental.SHARP
        }

        activeHighlights[app] = HighlightEntry(element, isSharp)
    }

    private fun highlightOff(app: Note) {
        val entry = activeHighlights.remove(app) ?: return

        // Only clear the notehead recolour / background / accidental when no other
        // still-pressed app note points at the same underlying SmNote (a natural and
        // its sharp share the same notehead when both are held). If a sharp is still
        // holding the parent's notehead, promote its colour so the visible tint matches
        // the remaining active mode; the background colour is identical for both modes
        // so it stays as-is. The accidental overlay follows the sharp: it's cleared
        // unless at least one still-active entry for this element is a sharp.
        val stillActive = activeHighlights.values.filter { it.element === entry.element }
        when {
            stillActive.isEmpty() -> {
                sheet.noteHighlights().remove(entry.element)
                sheet.noteBackgrounds().remove(entry.element)
                sheet.noteAccidentals().remove(entry.element)
            }

            stillActive.any { it.isSharp } -> {
                sheet.noteHighlights()[entry.element] = SHARP_HIGHLIGHT
                sheet.noteAccidentals()[entry.element] = Accidental.SHARP
            }

            else -> {
                sheet.noteHighlights()[entry.element] = NATURAL_HIGHLIGHT
                sheet.noteAccidentals().remove(entry.element)
            }
        }
    }

    /** Bookkeeping row per NOTE_ON so releases can undo exactly what was added. */
    private data class HighlightEntry(
        val element: SmNote,
        val isSharp: Boolean
    )

    companion object : MmxViewMetadata {
        override val bundleBaseName = "i18n/view/staff"
        override fun getViewImagePath(): String = "/view/scale.png"

        /**
         * Layout width fed to [SheetView.setSystemWidth]. With 7 measures (bass phase +
         * handoff + treble phase) at the engraver's minimum measure width, a system
         * around 1600 layout units gives comfortable spacing without over-stretching.
         */
        private const val REFERENCE_SYSTEM_WIDTH: Double = 1600.0

        /**
         * Fallback minimum height used only when the sheet has not been engraved yet
         * (defensive; [SheetView.setScore] is called during `init` so `sheet.layout`
         * should always be non-null by the time the [ZoomableNode] is created).
         */
        private const val REFERENCE_MIN_NATURAL_HEIGHT: Double = 280.0

        private val NATURAL_HIGHLIGHT: Color = Color.CRIMSON
        private val SHARP_HIGHLIGHT: Color = Color.MEDIUMBLUE

        /**
         * Fill colour handed to [SheetView.noteBackgrounds]. The engraver draws it as a
         * rounded rectangle behind the notehead (including the accidental slot) with the
         * given opacity honoured.
         */
        private val HIGHLIGHT_BG_COLOR: Color = Color.GOLD.deriveColor(0.0, 1.0, 1.0, 0.55)
    }
}
