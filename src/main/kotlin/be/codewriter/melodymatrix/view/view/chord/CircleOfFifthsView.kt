package be.codewriter.melodymatrix.view.view.chord

import be.codewriter.melodymatrix.view.component.ZoomableNode
import be.codewriter.melodymatrix.view.definition.Chord
import be.codewriter.melodymatrix.view.definition.ChordQuality
import be.codewriter.melodymatrix.view.event.ChordEvent
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.MmxEventType
import be.codewriter.melodymatrix.view.view.MmxView
import be.codewriter.melodymatrix.view.view.MmxViewMetadata
import javafx.animation.Animation
import javafx.animation.Interpolator
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.ArcType
import javafx.scene.shape.StrokeLineCap
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import javafx.util.Duration
import kotlin.math.cos
import kotlin.math.sin

/**
 * Renders a Circle of Fifths (Kwintencirkel) on a Canvas and highlights the
 * currently detected chord.
 *
 * The outer ring lists the 12 major keys in fifths order (C → G → D … clockwise
 * from 12 o'clock).  The inner ring shows the relative minor key at the same position.
 * The active chord position animates in with a colour-blend highlight.
 *
 * @see ChordRelationView
 */
class CircleOfFifthsView : MmxView() {

    companion object : MmxViewMetadata {
        override fun getViewTitle(): String = "Circle of Fifths"
        override fun getViewDescription(): String =
            "Highlights the detected chord on the Circle of Fifths (Kwintencirkel)."
        override fun getViewImagePath(): String = "/view/chord-relation.png"

        // ── Canvas geometry ─────────────────────────────────────────────────
        const val SIZE = 680.0
        private const val CX = SIZE / 2.0
        private const val CY = SIZE / 2.0

        /** Outer boundary of the major-chord ring. */
        private const val OUTER_R = 308.0

        /** Boundary between the major and the minor ring. */
        private const val MID_R = 205.0

        /** Inner boundary of the minor ring (centre disk begins here). */
        private const val INNER_R = 92.0

        /** Radius for major-chord label text. */
        private const val MAJOR_LABEL_R = 258.0

        /** Radius for minor-chord label text. */
        private const val MINOR_LABEL_R = 150.0

        /** Half-gap (°) between adjacent segments (so 2× this is taken out of 30°). */
        private const val SEG_GAP = 1.5

        // ── Colours ──────────────────────────────────────────────────────────
        private val COL_MAJOR_BASE   = Color.web("#1E3A8A")
        private val COL_MAJOR_ACTIVE = Color.web("#3B82F6")
        private val COL_MINOR_BASE   = Color.web("#7C2D12")
        private val COL_MINOR_ACTIVE = Color.web("#EA580C")
        private val COL_CENTER       = Color.web("#0F172A")
        private val COL_DIVIDER      = Color.web("#080C15")
        private val COL_BORDER       = Color.web("#334155")
        private val COL_TEXT_MAJOR   = Color.web("#E2E8F0")
        private val COL_TEXT_MINOR   = Color.web("#94A3B8")
        private val COL_TEXT_CENTER  = Color.web("#64748B")
        private val COL_TEXT_ACTIVE  = Color.WHITE

        // ── Slice data ───────────────────────────────────────────────────────
        /** One slice per key, clockwise from C at 12 o'clock. */
        private data class Slice(
            /** Clockwise position index 0–11, where 0 = top (C major / A minor). */
            val position: Int,
            val majorPitchClass: Int,
            val majorLabel: String,
            val minorPitchClass: Int,
            val minorLabel: String
        )

        private val SLICES = listOf(
            Slice(0,   0, "C",    9, "Am"),
            Slice(1,   7, "G",    4, "Em"),
            Slice(2,   2, "D",   11, "Bm"),
            Slice(3,   9, "A",    6, "F#m"),
            Slice(4,   4, "E",    1, "C#m"),
            Slice(5,  11, "B",    8, "G#m"),
            Slice(6,   6, "F#",   3, "D#m"),
            Slice(7,   1, "C#",  10, "A#m"),
            Slice(8,   8, "G#",   5, "Fm"),
            Slice(9,   3, "D#",   0, "Cm"),
            Slice(10, 10, "A#",   7, "Gm"),
            Slice(11,  5, "F",    2, "Dm")
        )

        // ── Helpers ──────────────────────────────────────────────────────────

        /**
         * Convert a clockwise-from-top angle (degrees) to the JFX Canvas arc angle.
         *
         * JavaFX `gc.arc()` uses 0° = right (3 o'clock) with the raw `(cx + r·cos, cy + r·sin)`
         * formula in a Y-down coordinate system, so 270° = top (12 o'clock).
         * CW-from-top → JFX: subtract 90.
         */
        private fun cwToJfx(cwDeg: Double): Double = cwDeg - 90.0

        /** Clockwise-from-12-o'clock degrees → X coordinate on the canvas. */
        private fun cwX(cx: Double, r: Double, cwDeg: Double) =
            cx + r * cos(Math.toRadians(cwDeg - 90.0))

        /** Clockwise-from-12-o'clock degrees → Y coordinate on the canvas. */
        private fun cwY(cy: Double, r: Double, cwDeg: Double) =
            cy + r * sin(Math.toRadians(cwDeg - 90.0))

        /** Linear colour interpolation between [from] and [to] by parameter [t] ∈ [0,1]. */
        private fun blend(from: Color, to: Color, t: Double): Color {
            val s = t.coerceIn(0.0, 1.0)
            return Color(
                from.red   + (to.red   - from.red)   * s,
                from.green + (to.green - from.green) * s,
                from.blue  + (to.blue  - from.blue)  * s,
                1.0
            )
        }

        /**
         * Draw a single chord slice as a ring arc.
         *
         * Using `strokeArc` with a controlled thickness avoids fill-winding artefacts
         * and keeps each segment geometry easy to reason about.
         */
        private fun drawRingSliceArc(
            gc: GraphicsContext,
            cx: Double,
            cy: Double,
            innerR: Double,
            outerR: Double,
            startCw: Double,
            sweepCw: Double,
            color: Color
        ) {
            val centerR = (innerR + outerR) / 2.0
            val thickness = (outerR - innerR)
            val diameter = centerR * 2.0

            gc.stroke = color
            gc.lineWidth = thickness
            gc.lineCap = StrokeLineCap.BUTT
            gc.strokeArc(
                cx - centerR,
                cy - centerR,
                diameter,
                diameter,
                cwToJfx(startCw),
                sweepCw,
                ArcType.OPEN
            )
        }

        /** Draw a full circle outline. */
        private fun strokeCircle(gc: GraphicsContext, cx: Double, cy: Double, r: Double) {
            gc.strokeOval(cx - r, cy - r, r * 2, r * 2)
        }
    }

    // ── State ────────────────────────────────────────────────────────────────

    private val canvas = Canvas(SIZE, SIZE)

    /** Currently highlighted major pitch class, or –1 for none. */
    private var activeMajorPitch = -1

    /** Currently highlighted minor pitch class, or –1 for none. */
    private var activeMinorPitch = -1

    /** Current chord label shown in the centre disk. */
    private var activeLabel = ""

    /** Animation progress 0 → 1 driving the highlight blend. */
    private val highlightAlpha = SimpleDoubleProperty(0.0)

    private var highlightTimeline: Timeline? = null

    // ── Init ─────────────────────────────────────────────────────────────────

    init {
        highlightAlpha.addListener { _, _, _ -> redraw() }

        val zoomable = ZoomableNode(
            content = StackPane(canvas),
            naturalWidth = SIZE,
            naturalHeight = SIZE,
            minWidthValue = 200.0,
            minHeightValue = 200.0,
            fitMode = ZoomableNode.FitMode.CONTAIN
        )

        setupSurface(
            rootNode = zoomable,
            naturalWidth = SIZE,
            naturalHeight = SIZE,
            captureNode = canvas,
            captureWidth = SIZE.toInt(),
            captureHeight = SIZE.toInt()
        )

        redraw()
    }

    // ── Events ───────────────────────────────────────────────────────────────

    override fun onEvent(event: MmxEvent) {
        if (event.type != MmxEventType.CHORD) return
        val chordEvent = event as? ChordEvent ?: return
        Platform.runLater {
            if (chordEvent.on && chordEvent.chord != Chord.UNDEFINED) {
                activate(chordEvent.chord)
            } else {
                deactivate()
            }
        }
    }

    private fun activate(chord: Chord) {
        val isMajorLike = chord.quality in setOf(
            ChordQuality.MAJOR,
            ChordQuality.DOMINANT,
            ChordQuality.AUGMENTED,
            ChordQuality.SUSPENDED_FOURTH,
            ChordQuality.SUSPENDED_SECOND
        )
        activeMajorPitch = if (isMajorLike) chord.pitchClass else -1
        activeMinorPitch = if (!isMajorLike) chord.pitchClass else -1
        activeLabel = chord.label
        animateTo(1.0, durationMs = 260.0)
    }

    private fun deactivate() {
        activeLabel = ""
        animateTo(0.0, durationMs = 700.0) {
            activeMajorPitch = -1
            activeMinorPitch = -1
        }
    }

    private fun animateTo(target: Double, durationMs: Double, onFinished: (() -> Unit)? = null) {
        highlightTimeline?.stop()
        val tl = Timeline(
            KeyFrame(Duration.ZERO, KeyValue(highlightAlpha, highlightAlpha.get(), Interpolator.EASE_BOTH)),
            KeyFrame(Duration.millis(durationMs), KeyValue(highlightAlpha, target, Interpolator.EASE_BOTH))
        )
        if (onFinished != null) tl.setOnFinished { onFinished() }
        highlightTimeline = tl
        tl.play()
    }

    // ── Drawing ──────────────────────────────────────────────────────────────

    private fun redraw() {
        val gc = canvas.graphicsContext2D
        gc.clearRect(0.0, 0.0, SIZE, SIZE)
        val alpha = highlightAlpha.get()

        drawSegments(gc, alpha)
        drawDividers(gc)
        drawBorders(gc)
        drawLabels(gc, alpha)
        drawCenterDisk(gc, alpha)
    }

    private fun drawSegments(gc: GraphicsContext, alpha: Double) {
        for (slice in SLICES) {
            val startCw = slice.position * 30.0 - 15.0 + SEG_GAP
            val sweep   = 30.0 - SEG_GAP * 2

            // ── Outer (major) ring
            val majorHighlight = slice.majorPitchClass == activeMajorPitch
            val majorColor = if (majorHighlight) blend(COL_MAJOR_BASE, COL_MAJOR_ACTIVE, alpha) else COL_MAJOR_BASE
            drawRingSliceArc(gc, CX, CY, MID_R, OUTER_R, startCw, sweep, majorColor)

            // Glow overlay for active major segment
            if (majorHighlight && alpha > 0.01) {
                val glow = COL_MAJOR_ACTIVE.deriveColor(0.0, 1.0, 1.3, alpha * 0.20)
                drawRingSliceArc(gc, CX, CY, MID_R - 6.0, OUTER_R + 6.0, startCw - 0.5, sweep + 1.0, glow)
            }

            // ── Inner (minor) ring
            val minorHighlight = slice.minorPitchClass == activeMinorPitch
            val minorColor = if (minorHighlight) blend(COL_MINOR_BASE, COL_MINOR_ACTIVE, alpha) else COL_MINOR_BASE
            drawRingSliceArc(gc, CX, CY, INNER_R, MID_R, startCw, sweep, minorColor)

            // Glow overlay for active minor segment
            if (minorHighlight && alpha > 0.01) {
                val glow = COL_MINOR_ACTIVE.deriveColor(0.0, 1.0, 1.3, alpha * 0.20)
                drawRingSliceArc(gc, CX, CY, INNER_R - 5.0, MID_R + 5.0, startCw - 0.5, sweep + 1.0, glow)
            }
        }
    }

    private fun drawDividers(gc: GraphicsContext) {
        gc.stroke = COL_DIVIDER
        gc.lineWidth = 1.5
        for (i in 0 until 12) {
            val cwDeg = i * 30.0 - 15.0   // spoke at gap centre
            gc.strokeLine(
                cwX(CX, INNER_R, cwDeg), cwY(CY, INNER_R, cwDeg),
                cwX(CX, OUTER_R, cwDeg), cwY(CY, OUTER_R, cwDeg)
            )
        }
    }

    private fun drawBorders(gc: GraphicsContext) {
        gc.stroke = COL_BORDER
        gc.lineWidth = 1.5
        strokeCircle(gc, CX, CY, OUTER_R)
        gc.lineWidth = 1.0
        strokeCircle(gc, CX, CY, MID_R)
        gc.lineWidth = 1.5
        strokeCircle(gc, CX, CY, INNER_R)
    }

    private fun drawLabels(gc: GraphicsContext, alpha: Double) {
        gc.textAlign = TextAlignment.CENTER
        gc.textBaseline = javafx.geometry.VPos.CENTER

        for (slice in SLICES) {
            val cwDeg = slice.position * 30.0   // clockwise from top, segment centre
            val lx = cwX(CX, MAJOR_LABEL_R, cwDeg)
            val ly = cwY(CY, MAJOR_LABEL_R, cwDeg)
            val sx = cwX(CX, MINOR_LABEL_R, cwDeg)
            val sy = cwY(CY, MINOR_LABEL_R, cwDeg)

            val majorHit = slice.majorPitchClass == activeMajorPitch
            val minorHit = slice.minorPitchClass == activeMinorPitch

            // Major label
            val majorSize = if (majorHit) 14.0 + alpha * 2.0 else 13.0
            gc.font = Font.font("System", FontWeight.BOLD, majorSize)
            gc.fill = if (majorHit) blend(COL_TEXT_MAJOR, COL_TEXT_ACTIVE, alpha) else COL_TEXT_MAJOR
            gc.fillText(slice.majorLabel, lx, ly)

            // Minor label
            val minorSize = if (minorHit) 11.0 + alpha * 2.0 else 10.0
            gc.font = Font.font("System", FontWeight.SEMI_BOLD, minorSize)
            gc.fill = if (minorHit) blend(COL_TEXT_MINOR, Color.web("#FED7AA"), alpha) else COL_TEXT_MINOR
            gc.fillText(slice.minorLabel, sx, sy)
        }
    }

    private fun drawCenterDisk(gc: GraphicsContext, alpha: Double) {
        // Fill
        gc.fill = COL_CENTER
        gc.fillOval(CX - INNER_R, CY - INNER_R, INNER_R * 2, INNER_R * 2)

        // Border
        gc.stroke = COL_BORDER
        gc.lineWidth = 1.5
        strokeCircle(gc, CX, CY, INNER_R)

        gc.textAlign = TextAlignment.CENTER
        gc.textBaseline = javafx.geometry.VPos.CENTER

        if (activeLabel.isNotEmpty() && alpha > 0.01) {
            // Current chord name, scaled by alpha
            val labelSize = 13.0 + alpha * 4.0
            gc.font = Font.font("System", FontWeight.BOLD, labelSize)
            gc.fill = Color.WHITE.deriveColor(0.0, 1.0, 1.0, alpha)
            gc.fillText(activeLabel, CX, CY - 9.0)
            // Subtitle
            gc.font = Font.font("System", FontWeight.NORMAL, 10.0)
            gc.fill = COL_TEXT_CENTER.deriveColor(0.0, 1.0, 1.0, alpha * 0.8)
            gc.fillText("detected", CX, CY + 10.0)
        } else {
            // Idle label
            gc.font = Font.font("System", FontWeight.NORMAL, 11.0)
            gc.fill = COL_TEXT_CENTER
            gc.fillText("Circle\nof Fifths", CX, CY)
        }
    }
}

