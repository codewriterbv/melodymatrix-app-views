package be.codewriter.melodymatrix.view.stage.chord

import be.codewriter.melodymatrix.view.VisualizerStage
import be.codewriter.melodymatrix.view.definition.Chord
import be.codewriter.melodymatrix.view.definition.ChordQuality
import be.codewriter.melodymatrix.view.event.ChordEvent
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.MmxEventType
import javafx.animation.FadeTransition
import javafx.animation.ParallelTransition
import javafx.animation.ScaleTransition
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Label
import javafx.scene.control.ToggleButton
import javafx.scene.effect.BlurType
import javafx.scene.effect.DropShadow
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import javafx.util.Duration
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Visualizer stage that shows the **last detected chord** at the centre of a radial graph and
 * surrounds it with its harmonically related chords, colour-coded by quality and connected by
 * directed, labelled arrows.
 *
 * ## Layout
 * ```
 *   ┌─────────────────────────────────────────────────┐
 *   │  ⬡ Harmony Navigator   ●Major ●Minor ●Dim  [toggles] │
 *   │                  V / ♭III                       │
 *   │           vii°/♭VII  ╌╌  IV / V                │
 *   │              ii/ii°──[chord]──vi / iv           │
 *   │                     ╌╌  parallel                │
 *   └─────────────────────────────────────────────────┘
 * ```
 *
 * ## Animation
 * When a new chord event arrives the graph fades out and scales down, then the
 * new set fades in and scales up from the centre.
 *
 * ## Filtering
 * Toggle buttons in the header control whether **minor** and **diminished** related chords are
 * shown; the centre chord is always displayed regardless of its quality.
 *
 * @see ChordRelationMap
 * @see RelatedChord
 * @see RelationshipType
 */
class ChordRelationStage : VisualizerStage() {

    // ─── Geometry ─────────────────────────────────────────────────────────────

    private companion object {
        const val W = 920.0
        const val H = 710.0
        const val HEADER_H = 58.0
        const val GRAPH_H = H - HEADER_H

        const val CX = W / 2.0
        const val CY = GRAPH_H / 2.0 + 10.0

        /** Distance from centre to each orbit node centre. */
        const val ORBIT_R = 228.0

        /** Circumradius of the centre hexagon. */
        const val CENTER_HEX_R = 68.0

        /** Circumradius of each orbit hexagon. */
        const val ORBIT_HEX_R = 50.0

        const val CENTER_NODE_HALF = 80.0
        const val ORBIT_NODE_HALF = 63.0
    }

    // ─── State ────────────────────────────────────────────────────────────────

    private var currentChord: Chord = Chord.UNDEFINED
    private var showMinor = true
    private var showDiminished = false
    private var animating = false

    // ─── UI layers ────────────────────────────────────────────────────────────

    private val arrowCanvas = Canvas(W, GRAPH_H)

    private val nodesPane = Pane().apply {
        prefWidth = W
        prefHeight = GRAPH_H
        isMouseTransparent = true
    }

    /** The canvas + nodes pane are stacked and faded together during transitions. */
    private val contentGroup = StackPane(arrowCanvas, nodesPane).apply {
        prefWidth = W
        prefHeight = GRAPH_H
        maxWidth = W
        maxHeight = GRAPH_H
        style = "-fx-background-color: #0E1117;"
    }

    private val statusLabel = Label("Play a chord…").apply {
        style = "-fx-font-size: 12; -fx-text-fill: #64748B;"
    }

    // ─── Init ─────────────────────────────────────────────────────────────────

    init {
        title = "Chord Relationship"

        val root = BorderPane().apply {
            top = buildHeader()
            center = contentGroup
            //style = "-fx-background-color: #0E1117;"
        }

        scene = Scene(root, W, H)
        setOnCloseRequest { }

        drawIdleState()
    }

    // ─── Header ───────────────────────────────────────────────────────────────

    private fun buildHeader(): HBox {
        val minorToggle = styledToggle("Minor", showMinor) { v ->
            showMinor = v; redraw()
        }
        val dimToggle = styledToggle("Diminished", showDiminished) { v ->
            showDiminished = v; redraw()
        }

        val showLabel = Label("Show:").apply {
            style = "-fx-font-size: 12; -fx-text-fill: #94A3B8;"
        }

        val legend = HBox(10.0).apply {
            alignment = Pos.CENTER
            children.addAll(
                legendDot(majorFill(false), "Major"),
                legendDot(minorFill(false), "Minor"),
                legendDot(dimFill(false), "Dim.")
            )
        }

        val spacer = Region().also { HBox.setHgrow(it, Priority.ALWAYS) }

        return HBox(12.0, statusLabel, spacer, legend, showLabel, minorToggle, dimToggle).apply {
            padding = Insets(10.0, 16.0, 8.0, 16.0)
            alignment = Pos.CENTER_LEFT
            //style = "-fx-background-color: #161B22; -fx-border-color: #30363D; -fx-border-width: 0 0 1 0;"
            prefHeight = HEADER_H
        }
    }

    private fun styledToggle(text: String, initial: Boolean, onChange: (Boolean) -> Unit): ToggleButton {
        return ToggleButton(text).apply {
            isSelected = initial
            style = toggleStyle(initial)
            selectedProperty().addListener { _, _, v ->
                style = toggleStyle(v)
                Platform.runLater { onChange(v) }
            }
        }
    }

    private fun toggleStyle(on: Boolean) =
        if (on) "-fx-background-color: #1D4ED8; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-size: 12; -fx-padding: 4 10 4 10;"
        else "-fx-background-color: #1E293B; -fx-text-fill: #94A3B8; -fx-background-radius: 5; -fx-font-size: 12; -fx-padding: 4 10 4 10;"

    private fun legendDot(color: Color, label: String): HBox {
        val dot = Canvas(11.0, 11.0).also {
            it.graphicsContext2D.apply { fill = color; fillOval(0.0, 0.0, 11.0, 11.0) }
        }
        val lbl = Label(label).apply { style = "-fx-font-size: 11; -fx-text-fill: #94A3B8;" }
        return HBox(4.0, dot, lbl).apply { alignment = Pos.CENTER_LEFT }
    }

    // ─── Event handling ───────────────────────────────────────────────────────

    override fun onEvent(event: MmxEvent) {
        if (event.type != MmxEventType.CHORD) return
        val chordEvent = event as? ChordEvent ?: return
        Platform.runLater {
            val incoming = chordEvent.chord
            if (chordEvent.on && incoming != Chord.UNDEFINED && incoming != currentChord) {
                animateChordChange(incoming)
            }
        }
    }

    // ─── Animation ────────────────────────────────────────────────────────────

    private fun animateChordChange(newChord: Chord) {
        if (animating) {
            currentChord = newChord; return
        }

        if (nodesPane.children.isEmpty()) {
            // First chord – pop in
            currentChord = newChord
            redraw()
            contentGroup.opacity = 0.0
            contentGroup.scaleX = 0.85
            contentGroup.scaleY = 0.85
            val popIn = ParallelTransition(
                contentGroup,
                FadeTransition(Duration.millis(380.0)).apply { fromValue = 0.0; toValue = 1.0 },
                ScaleTransition(Duration.millis(380.0)).apply { fromX = 0.85; fromY = 0.85; toX = 1.0; toY = 1.0 }
            )
            popIn.play()
            return
        }

        animating = true
        val out = ParallelTransition(
            contentGroup,
            FadeTransition(Duration.millis(200.0)).apply { fromValue = 1.0; toValue = 0.0 },
            ScaleTransition(Duration.millis(200.0)).apply { toX = 0.93; toY = 0.93 }
        )
        out.setOnFinished {
            currentChord = newChord
            redraw()
            contentGroup.scaleX = 0.93
            contentGroup.scaleY = 0.93
            val inAnim = ParallelTransition(
                contentGroup,
                FadeTransition(Duration.millis(300.0)).apply { fromValue = 0.0; toValue = 1.0 },
                ScaleTransition(Duration.millis(300.0)).apply { fromX = 0.93; fromY = 0.93; toX = 1.0; toY = 1.0 }
            )
            inAnim.setOnFinished { animating = false }
            inAnim.play()
        }
        out.play()
    }

    // ─── Drawing ──────────────────────────────────────────────────────────────

    private fun redraw() {
        nodesPane.children.clear()
        val gc = arrowCanvas.graphicsContext2D
        gc.clearRect(0.0, 0.0, W, GRAPH_H)

        if (currentChord == Chord.UNDEFINED) {
            drawIdleState(); return
        }

        statusLabel.text = currentChord.label

        val allRelated = ChordRelationMap.getRelatedChords(currentChord)
        val visible = allRelated.filter { rel ->
            when (rel.chord.quality) {
                ChordQuality.MINOR -> showMinor
                ChordQuality.DIMINISHED, ChordQuality.HALF_DIMINISHED -> showDiminished
                else -> true
            }
        }

        // Draw subtle orbit ring
        gc.save()
        gc.stroke = Color.web("#1E293B", 0.9)
        gc.lineWidth = 1.0
        gc.setLineDashes(4.0, 7.0)
        gc.strokeOval(CX - ORBIT_R, CY - ORBIT_R, ORBIT_R * 2, ORBIT_R * 2)
        gc.setLineDashes()
        gc.restore()

        // Arrows behind nodes
        visible.forEach { rel ->
            val (rx, ry) = orbitXY(rel.angleDeg)
            drawArrow(gc, CX, CY, rx, ry, rel)
        }

        // Orbit nodes
        visible.forEach { rel ->
            val (rx, ry) = orbitXY(rel.angleDeg)
            nodesPane.children.add(buildChordNode(rel.chord, rel.relationship, false, rx, ry))
        }

        // Centre node last (on top)
        nodesPane.children.add(buildChordNode(currentChord, null, true, CX, CY))
    }

    private fun drawIdleState() {
        val gc = arrowCanvas.graphicsContext2D
        gc.clearRect(0.0, 0.0, W, GRAPH_H)
        drawHex(gc, CX, CY, CENTER_HEX_R, Color.web("#1C2333"), Color.web("#2D3F55"), 1.5)
        gc.save()
        gc.fill = Color.web("#475569")
        gc.font = Font.font("System", FontWeight.NORMAL, 15.0)
        gc.textAlign = TextAlignment.CENTER
        gc.textBaseline = VPos.CENTER
        gc.fillText("Play a chord…", CX, CY)
        gc.restore()
        statusLabel.text = "Waiting for chord…"
    }

    // ─── Arrow drawing ────────────────────────────────────────────────────────

    private fun drawArrow(
        gc: GraphicsContext,
        x1: Double, y1: Double,
        x2: Double, y2: Double,
        rel: RelatedChord
    ) {
        val dx = x2 - x1
        val dy = y2 - y1
        val dist = sqrt(dx * dx + dy * dy)
        val ux = dx / dist
        val uy = dy / dist

        // Trim to hex edges so lines don't overlap the nodes
        val sx = x1 + ux * (CENTER_HEX_R + 7)
        val sy = y1 + uy * (CENTER_HEX_R + 7)
        val ex = x2 - ux * (ORBIT_HEX_R + 7)
        val ey = y2 - uy * (ORBIT_HEX_R + 7)

        val col = rel.relationship.color

        gc.save()

        // Dashed shaft
        gc.stroke = col.deriveColor(0.0, 1.0, 1.0, 0.72)
        gc.lineWidth = 1.8
        gc.setLineDashes(7.0, 5.0)
        gc.strokeLine(sx, sy, ex, ey)
        gc.setLineDashes()

        // Filled arrowhead at orbit end
        val headLen = 11.0
        val angle = atan2(ey - sy, ex - sx)
        val ax1 = ex - headLen * cos(angle - Math.PI / 6)
        val ay1 = ey - headLen * sin(angle - Math.PI / 6)
        val ax2 = ex - headLen * cos(angle + Math.PI / 6)
        val ay2 = ey - headLen * sin(angle + Math.PI / 6)
        gc.fill = col.deriveColor(0.0, 1.0, 1.0, 0.88)
        gc.fillPolygon(doubleArrayOf(ex, ax1, ax2), doubleArrayOf(ey, ay1, ay2), 3)

        // Relationship label offset perpendicularly from the midpoint
        val midX = (sx + ex) / 2.0
        val midY = (sy + ey) / 2.0
        val perpX = -uy * 16.0
        val perpY = ux * 16.0
        gc.fill = col.deriveColor(0.0, 0.85, 1.5, 1.0)
        gc.font = Font.font("System", FontWeight.BOLD, 11.0)
        gc.textAlign = TextAlignment.CENTER
        gc.textBaseline = VPos.CENTER
        gc.fillText(rel.relationship.shortLabel, midX + perpX, midY + perpY)

        gc.restore()
    }

    // ─── Chord hex nodes ──────────────────────────────────────────────────────

    private fun buildChordNode(
        chord: Chord,
        relationship: RelationshipType?,
        isCurrent: Boolean,
        cx: Double, cy: Double
    ): StackPane {
        val half = if (isCurrent) CENTER_NODE_HALF else ORBIT_NODE_HALF
        val hexR = if (isCurrent) CENTER_HEX_R else ORBIT_HEX_R
        val size = half * 2

        // Canvas draws the hexagon background
        val canvas = Canvas(size, size)
        val fill = chordFill(chord, isCurrent)
        val stroke = chordStroke(chord, isCurrent)
        drawHex(canvas.graphicsContext2D, half, half, hexR, fill, stroke, if (isCurrent) 2.5 else 1.5)

        // Text content stacked over canvas
        val content = buildNodeLabels(chord, relationship, isCurrent, hexR)

        val node = StackPane(canvas, content).apply {
            prefWidth = size; prefHeight = size
            layoutX = cx - half; layoutY = cy - half
            if (isCurrent) effect = DropShadow(
                BlurType.GAUSSIAN,
                fill.deriveColor(0.0, 1.0, 1.6, 0.85), 26.0, 0.22, 0.0, 0.0
            )
        }
        StackPane.setAlignment(content, Pos.CENTER)
        return node
    }

    private fun buildNodeLabels(
        chord: Chord,
        relationship: RelationshipType?,
        isCurrent: Boolean,
        hexR: Double
    ): VBox {
        val maxW = hexR * 1.55
        val nameLabel = Label(chord.label).apply {
            style = "-fx-font-size: ${if (isCurrent) 14 else 11}; -fx-font-weight: bold; " +
                    "-fx-text-fill: white; -fx-wrap-text: true; -fx-text-alignment: center;"
            maxWidth = maxW; isWrapText = true; alignment = Pos.CENTER
        }
        val vbox = VBox(2.0).apply { alignment = Pos.CENTER }
        if (relationship != null) {
            vbox.children.add(Label(relationship.description).apply {
                style = "-fx-font-size: 9; -fx-text-fill: #CBD5E1; -fx-text-alignment: center;"
                maxWidth = maxW; isWrapText = true; alignment = Pos.CENTER
            })
        }
        vbox.children.add(nameLabel)
        return vbox
    }

    // ─── Hexagon helper ───────────────────────────────────────────────────────

    /** Draws a **pointy-top** hexagon centred at ([cx],[cy]) with circumradius [r]. */
    private fun drawHex(
        gc: GraphicsContext,
        cx: Double, cy: Double, r: Double,
        fill: Color, stroke: Color, strokeW: Double
    ) {
        val xs = DoubleArray(6)
        val ys = DoubleArray(6)
        for (i in 0..5) {
            val a = Math.toRadians(i * 60.0 - 90.0) // -90° → first vertex at top
            xs[i] = cx + r * cos(a); ys[i] = cy + r * sin(a)
        }
        gc.save()
        gc.fill = fill; gc.fillPolygon(xs, ys, 6)
        gc.stroke = stroke; gc.lineWidth = strokeW; gc.strokePolygon(xs, ys, 6)
        gc.restore()
    }

    // ─── Colour palette ───────────────────────────────────────────────────────

    private fun majorFill(current: Boolean) = if (current) Color.web("#2563EB") else Color.web("#1E3A8A")
    private fun minorFill(current: Boolean) = if (current) Color.web("#D97706") else Color.web("#78350F")
    private fun dimFill(current: Boolean) = if (current) Color.web("#DC2626") else Color.web("#7F1D1D")
    private fun dominantFill(current: Boolean) = if (current) Color.web("#7C3AED") else Color.web("#4C1D95")

    private fun chordFill(chord: Chord, current: Boolean) = when (chord.quality) {
        ChordQuality.MAJOR -> majorFill(current)
        ChordQuality.MINOR -> minorFill(current)
        ChordQuality.DOMINANT -> dominantFill(current)
        ChordQuality.DIMINISHED, ChordQuality.HALF_DIMINISHED -> dimFill(current)
    }

    private fun chordStroke(chord: Chord, current: Boolean) = if (current) when (chord.quality) {
        ChordQuality.MAJOR -> Color.web("#93C5FD")
        ChordQuality.MINOR -> Color.web("#FCD34D")
        ChordQuality.DOMINANT -> Color.web("#C4B5FD")
        ChordQuality.DIMINISHED, ChordQuality.HALF_DIMINISHED -> Color.web("#FCA5A5")
    } else Color.web("#334155")

    // ─── Geometry ─────────────────────────────────────────────────────────────

    /** Converts a clockwise-from-top [angleDeg] to canvas (x, y). */
    private fun orbitXY(angleDeg: Double): Pair<Double, Double> {
        val rad = Math.toRadians(angleDeg - 90.0)
        return Pair(CX + ORBIT_R * cos(rad), CY + ORBIT_R * sin(rad))
    }
}