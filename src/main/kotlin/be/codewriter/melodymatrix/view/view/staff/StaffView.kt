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
import com.sheetmusic4j.core.model.Note as SmNote
import com.sheetmusic4j.engraving.glyph.MarkingCategory
import com.sheetmusic4j.engraving.layout.NoteAnchor
import com.sheetmusic4j.fxviewer.SheetView
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment

/**
 * Visualizer stage that displays a fixed two-octave grand staff and highlights the played
 * notes on it in real time.
 *
 * The reference score is a diatonic naturals-only grand staff engraved by sheetmusic4j's
 * [SheetView]. On a NOTE_ON we resolve the [NoteAnchor] for that pitch (via
 * [SheetView.getLayout]) and overlay two decorations on top of the score canvas:
 *  - a rounded, semi-transparent highlight rectangle behind the notehead for a strong
 *    "background colour" pop, and
 *  - for sharps, a floating "\u266f" glyph next to the natural parent's notehead so the
 *    user sees the accidental without the reference staff having to carry every chromatic
 *    step. This keeps the engraved score simple and side-steps the fxviewer's current
 *    limitation for accidentals on isolated whole/quarter notes.
 *
 * The sheet is wrapped in a [ZoomableNode] and the view opts in to
 * [MmxView.fitToViewport] so both the notation and the overlays scale together with the
 * host viewport.
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

    /**
     * Absolute-positioned overlay above [sheet]; hosts highlight rectangles and floating
     * accidental glyphs. Marked mouse-transparent so the underlying score canvas keeps its
     * hit-testing (currently unused, but future-proof for tooltips on the sheet).
     */
    private val overlayPane = Pane().apply {
        isMouseTransparent = true
        isPickOnBounds = false
    }

    private val mapping: Map<Note, SmNote>

    /** Highlight bookkeeping keyed by app [Note] so releases can undo exactly what NOTE_ON added. */
    private val activeHighlights: MutableMap<Note, HighlightEntry> = HashMap()

    init {
        val result = SheetMusicAdapter.buildReferenceStaff()
        mapping = result.notesByApp
        sheet.setScore(result.score)

        // Read the actual engraved dimensions after setScore so the ZoomableNode's
        // natural size matches what the SheetView is really drawing. This keeps every
        // notehead inside the visible area (no left/right cut-off) and preserves the
        // aspect ratio when scaled to the host viewport.
        val layout = sheet.layout
        val naturalWidth = layout?.width()?.coerceAtLeast(REFERENCE_SYSTEM_WIDTH) ?: REFERENCE_SYSTEM_WIDTH
        val naturalHeight = layout?.height()?.coerceAtLeast(REFERENCE_MIN_NATURAL_HEIGHT)
            ?: REFERENCE_MIN_NATURAL_HEIGHT

        val sheetHost = StackPane(sheet, overlayPane).apply {
            // Align both children to the top-left so the overlay pane's coordinate
            // space matches the sheet's engraved layout coordinates. StackPane's
            // default Pos.CENTER would offset the sheet horizontally whenever the
            // engraved layout is narrower than the pane, breaking overlay alignment.
            alignment = Pos.TOP_LEFT
            isPickOnBounds = false
        }

        val zoomable = ZoomableNode(
            content = sheetHost,
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
        val anchor = anchorFor(element) ?: return

        val isSharp = app.mainNote.isSharp
        val baseColor = if (isSharp) SHARP_HIGHLIGHT else NATURAL_HIGHLIGHT

        // 1. Notehead recolour via SheetView's built-in highlight map.
        sheet.noteHighlights()[element] = baseColor

        // 2. Semi-transparent yellow rounded rectangle behind the notehead for a strong
        //    "played right now" pop. Extra left padding shifts the highlight a bit to the
        //    left of the notehead so accidental space (used by the floating ♯ glyph for
        //    sharps) also sits inside the highlight.
        val background = Rectangle(
            anchor.x() - HIGHLIGHT_PAD_LEFT,
            anchor.y() - HIGHLIGHT_PAD_Y,
            anchor.width() + HIGHLIGHT_PAD_LEFT + HIGHLIGHT_PAD_RIGHT,
            anchor.height() + 2.0 * HIGHLIGHT_PAD_Y
        ).apply {
            fill = HIGHLIGHT_BG_COLOR.deriveColor(0.0, 1.0, 1.0, HIGHLIGHT_BG_OPACITY)
            stroke = baseColor
            strokeWidth = 1.5
            arcWidth = HIGHLIGHT_ARC
            arcHeight = HIGHLIGHT_ARC
            isMouseTransparent = true
        }

        val overlays = mutableListOf<Node>(background)

        // 3. Floating "♯" glyph for sharps, positioned just left of the natural parent's
        //    notehead. Drawn as a bold Text node so it scales cleanly with the ZoomableNode
        //    Scale transform on the enclosing StackPane.
        if (isSharp) {
            val glyph = Text(SHARP_GLYPH).apply {
                font = Font.font("Serif", FontWeight.BOLD, SHARP_GLYPH_FONT_SIZE)
                fill = baseColor
                textAlignment = TextAlignment.CENTER
                isMouseTransparent = true
                x = anchor.x() - SHARP_GLYPH_OFFSET_X
                // Text.y is the baseline; align roughly with the notehead's vertical centre.
                y = anchor.y() + anchor.height() * SHARP_GLYPH_BASELINE_FRACTION
            }
            overlays.add(glyph)
        }

        overlayPane.children.addAll(overlays)
        activeHighlights[app] = HighlightEntry(element, overlays, isSharp)
    }

    private fun highlightOff(app: Note) {
        val entry = activeHighlights.remove(app) ?: return
        overlayPane.children.removeAll(entry.overlays)

        // Only clear the notehead recolour when no other still-pressed app note points at
        // the same underlying SmNote (a natural and its sharp share the same notehead when
        // both are held). If a sharp is still holding the parent's notehead, promote its
        // colour so the visible tint matches the remaining active mode.
        val stillActive = activeHighlights.values.filter { it.element === entry.element }
        when {
            stillActive.isEmpty() -> sheet.noteHighlights().remove(entry.element)
            stillActive.any { it.isSharp } -> sheet.noteHighlights()[entry.element] = SHARP_HIGHLIGHT
            else -> sheet.noteHighlights()[entry.element] = NATURAL_HIGHLIGHT
        }
    }

    private fun anchorFor(element: SmNote): NoteAnchor? =
        sheet.layout?.noteAnchors()?.firstOrNull { it.elementRef() === element }

    /** Bookkeeping row per NOTE_ON so releases can undo exactly what was added. */
    private data class HighlightEntry(
        val element: SmNote,
        val overlays: List<Node>,
        val isSharp: Boolean
    )

    companion object : MmxViewMetadata {
        override val bundleBaseName = "i18n/view/staff"
        override fun getViewImagePath(): String = "/view/scale.png"

        /**
         * Layout width fed to [SheetView.setSystemWidth]. Wide enough to fit the entire
         * two-octave scale on a single system so the view visually reads as one continuous
         * grand staff, matching the original implementation's shape.
         */
        private const val REFERENCE_SYSTEM_WIDTH: Double = 2000.0

        /**
         * Fallback minimum height used only when the sheet has not been engraved yet
         * (defensive; [SheetView.setScore] is called during `init` so `sheet.layout`
         * should always be non-null by the time the [ZoomableNode] is created).
         */
        private const val REFERENCE_MIN_NATURAL_HEIGHT: Double = 280.0

        private val NATURAL_HIGHLIGHT: Color = Color.CRIMSON
        private val SHARP_HIGHLIGHT: Color = Color.MEDIUMBLUE

        /**
         * Padding around the notehead bounding box for the background rectangle.
         *
         * Left padding is larger than the right so the highlight also covers the space
         * where the floating ♯ glyph is drawn for sharps.
         */
        private const val HIGHLIGHT_PAD_LEFT: Double = 22.0
        private const val HIGHLIGHT_PAD_RIGHT: Double = 6.0
        private const val HIGHLIGHT_PAD_Y: Double = 4.0
        private const val HIGHLIGHT_ARC: Double = 10.0
        private const val HIGHLIGHT_BG_OPACITY: Double = 0.55
        private val HIGHLIGHT_BG_COLOR: Color = Color.GOLD

        private const val SHARP_GLYPH: String = "\u266F"        // ♯
        private const val SHARP_GLYPH_FONT_SIZE: Double = 26.0
        private const val SHARP_GLYPH_OFFSET_X: Double = 18.0    // move left of the notehead
        private const val SHARP_GLYPH_BASELINE_FRACTION: Double = 0.85
    }
}
