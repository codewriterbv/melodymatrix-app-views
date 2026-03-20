package be.codewriter.melodymatrix.view.stage.chord

import be.codewriter.melodymatrix.view.VisualizerStage
import be.codewriter.melodymatrix.view.definition.Chord
import be.codewriter.melodymatrix.view.definition.ChordQuality
import be.codewriter.melodymatrix.view.definition.RelationshipType
import be.codewriter.melodymatrix.view.event.ChordEvent
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.MmxEventType
import be.codewriter.melodymatrix.view.helper.ChordRelationMap
import be.codewriter.melodymatrix.view.helper.RelatedChord
import javafx.animation.*
import javafx.application.Platform
import javafx.beans.property.SimpleDoubleProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Label
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
 * ## Visibility
 * All related chords are shown at all times; inter-related arrows are density-capped
 * so the graph stays readable during transitions.
 *
 * @see be.codewriter.melodymatrix.view.helper.ChordRelationMap
 * @see be.codewriter.melodymatrix.view.definition.RelatedChord
 * @see be.codewriter.melodymatrix.view.definition.RelationshipType
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

        const val CENTER_SPRING_OVERSHOOT = 24.0
        const val CENTER_SPRING_SCALE = 1.12
        const val SWIRL_AMPLITUDE = 30.0
        const val PULSE_PERIOD_MS = 940.0
        const val MAX_INTER_RELATED_EDGES = 12
        const val MAX_OUTGOING_INTER_EDGES_PER_ORBIT = 2
        const val MAX_TRAIL_NODES = 9

        val MOVE_INTERPOLATOR: Interpolator = Interpolator.SPLINE(0.2, 0.8, 0.2, 1.0)
    }

    private enum class NodeRole { CENTER, ORBIT, TRAIL }

    private data class NodeTarget(
        val chord: Chord,
        val role: NodeRole,
        val relationship: RelationshipType?,
        val x: Double,
        val y: Double,
        val trailSlot: Int? = null
    )

    private data class HarmonyKey(val pitchClass: Int, val quality: ChordQuality)

    private data class EdgeKey(val from: HarmonyKey, val to: HarmonyKey)

    private data class GraphSnapshot(
        val center: Chord,
        val nodes: Map<Chord, NodeTarget>,
        val edges: Map<EdgeKey, RelationshipType>
    )

    private data class NodeView(
        var chord: Chord,
        val pane: StackPane,
        var role: NodeRole,
        var relationship: RelationshipType?,
        var half: Double,
        var trailSlot: Int? = null
    )

    private data class EdgeView(
        val key: EdgeKey,
        var relationship: RelationshipType,
        val alpha: SimpleDoubleProperty = SimpleDoubleProperty(1.0),
        val pulse: SimpleDoubleProperty = SimpleDoubleProperty(0.0)
    )

    // ─── State ────────────────────────────────────────────────────────────────

    private var currentChord: Chord = Chord.UNDEFINED
    private var animating = false
    private var currentSnapshot: GraphSnapshot? = null
    private var queuedSnapshot: GraphSnapshot? = null
    private val playedTrail = mutableListOf<Chord>()

    private val nodeViews = mutableMapOf<Chord, NodeView>()
    private val edgeViews = mutableMapOf<EdgeKey, EdgeView>()
    private val pulseClock = SimpleDoubleProperty(0.0)

    private val pulseTimeline = Timeline(
        KeyFrame(Duration.ZERO, KeyValue(pulseClock, 0.0, Interpolator.LINEAR)),
        KeyFrame(Duration.millis(PULSE_PERIOD_MS), KeyValue(pulseClock, 1.0, Interpolator.LINEAR))
    ).apply {
        cycleCount = Animation.INDEFINITE
    }

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
        pulseClock.addListener { _, _, _ -> redrawArrows() }
        pulseTimeline.play()

        drawIdleState()
    }

    // ─── Header ───────────────────────────────────────────────────────────────

    private fun buildHeader(): HBox {
        val infoLabel = Label("All qualities visible").apply {
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

        return HBox(12.0, statusLabel, spacer, legend, infoLabel).apply {
            padding = Insets(10.0, 16.0, 8.0, 16.0)
            alignment = Pos.CENTER_LEFT
            //style = "-fx-background-color: #161B22; -fx-border-color: #30363D; -fx-border-width: 0 0 1 0;"
            prefHeight = HEADER_H
        }
    }

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
            if (chordEvent.on && incoming != Chord.UNDEFINED) {
                enqueueSnapshot(buildSnapshot(incoming))
            }
        }
    }

    // ─── Graph snapshots & transition queue ──────────────────────────────────

    private fun enqueueSnapshot(snapshot: GraphSnapshot) {
        if (animating) {
            queuedSnapshot = snapshot
            return
        }
        animateToSnapshot(snapshot)
    }

    private fun buildSnapshot(center: Chord): GraphSnapshot {
        updatePlayedTrail(center)
        val related = ChordRelationMap.getRelatedChords(center)

        val nodes = linkedMapOf<Chord, NodeTarget>()
        nodes[center] = NodeTarget(center, NodeRole.CENTER, null, CX, CY)
        related.forEach { rel ->
            val (x, y) = orbitXY(rel.angleDeg)
            nodes[rel.chord] = NodeTarget(rel.chord, NodeRole.ORBIT, rel.relationship, x, y)
        }

        playedTrail.take(MAX_TRAIL_NODES).forEachIndexed { slot, trailChord ->
            val clashesWithMainGraph = harmonyKey(trailChord) == harmonyKey(center) ||
                    nodes.keys.any { existing -> harmonyKey(existing) == harmonyKey(trailChord) }
            if (!clashesWithMainGraph) {
                val (x, y) = trailXY(slot)
                nodes[trailChord] = NodeTarget(trailChord, NodeRole.TRAIL, null, x, y, slot)
            }
        }

        val edges = linkedMapOf<EdgeKey, RelationshipType>()
        related.forEach { rel -> edges[EdgeKey(harmonyKey(center), harmonyKey(rel.chord))] = rel.relationship }

        val visibleRelatedKeys = related.map { harmonyKey(it.chord) }.toSet()
        addInterRelatedEdges(related, visibleRelatedKeys, edges)

        return GraphSnapshot(center, nodes, edges)
    }

    private fun addInterRelatedEdges(
        visibleRelated: List<RelatedChord>,
        visibleRelatedKeys: Set<HarmonyKey>,
        edges: MutableMap<EdgeKey, RelationshipType>
    ) {
        var totalInterEdges = 0
        visibleRelated.forEach { source ->
            if (totalInterEdges >= MAX_INTER_RELATED_EDGES) return
            val sourceKey = harmonyKey(source.chord)
            var outgoingForSource = 0
            ChordRelationMap.getRelatedChords(source.chord)
                .asSequence()
                .filter { candidate -> harmonyKey(candidate.chord) in visibleRelatedKeys }
                .forEach { candidate ->
                    if (outgoingForSource >= MAX_OUTGOING_INTER_EDGES_PER_ORBIT || totalInterEdges >= MAX_INTER_RELATED_EDGES) {
                        return@forEach
                    }
                    val targetKey = harmonyKey(candidate.chord)
                    if (targetKey !in visibleRelatedKeys || sourceKey == targetKey) return@forEach
                    val inserted = edges.putIfAbsent(EdgeKey(sourceKey, targetKey), candidate.relationship) == null
                    if (inserted) {
                        outgoingForSource++
                        totalInterEdges++
                    }
                }
        }
    }

    private fun animateToSnapshot(target: GraphSnapshot) {
        currentChord = target.center
        statusLabel.text = target.center.label

        if (nodeViews.isEmpty()) {
            currentSnapshot = target
            materializeInitialSnapshot(target)
            return
        }

        animating = true
        val centerAlreadyVisible = findNodeEntryByHarmony(target.center) != null
        val duration = Duration.millis(if (centerAlreadyVisible) 620.0 else 760.0)
        val previousRoles = nodeViews.entries.associate { harmonyKey(it.key) to it.value.role }

        val anchorCenter = if (centerAlreadyVisible) {
            val v = findNodeEntryByHarmony(target.center)!!.value
            Pair(nodeCenterX(v), nodeCenterY(v))
        } else Pair(CX, CY)

        target.nodes.values.forEach { targetNode ->
            val existingEntry: Pair<Chord, NodeView>? = nodeViews[targetNode.chord]?.let { targetNode.chord to it }
                ?: findNodeEntryByHarmony(targetNode.chord)?.let { it.key to it.value }
            val existing = existingEntry?.second
            if (existing == null) {
                val startHalf = nodeHalf(targetNode.role)
                val pane = buildNodePane(targetNode.chord, targetNode.relationship, targetNode.role)
                val view = NodeView(
                    targetNode.chord,
                    pane,
                    targetNode.role,
                    targetNode.relationship,
                    startHalf,
                    targetNode.trailSlot
                )
                nodeViews[targetNode.chord] = view
                nodesPane.children.add(pane)
                setNodeCenter(view, anchorCenter.first, anchorCenter.second)
                pane.opacity = 0.0
                pane.scaleX = 0.55
                pane.scaleY = 0.55
                pane.rotate =
                    if (targetNode.role == NodeRole.CENTER) 0.0 else ((targetNode.chord.pitchClass % 3) - 1) * 10.0
                attachNodeListeners(view)
            } else {
                val existingKey = existingEntry.first
                if (existingKey != targetNode.chord) {
                    nodeViews.remove(existingKey)
                    nodeViews[targetNode.chord] = existing
                }

                val requiresRebuild = existing.chord != targetNode.chord ||
                        existing.role != targetNode.role ||
                        existing.relationship != targetNode.relationship ||
                        existing.trailSlot != targetNode.trailSlot
                if (requiresRebuild) {
                    existing.chord = targetNode.chord
                    applyNodeVisual(existing, targetNode.role, targetNode.relationship)
                    existing.trailSlot = targetNode.trailSlot
                }
            }
        }

        target.edges.forEach { (key, rel) ->
            val edge = edgeViews[key]
            if (edge == null) {
                val created = EdgeView(key, rel, SimpleDoubleProperty(0.0), SimpleDoubleProperty(0.0))
                created.alpha.addListener { _, _, _ -> redrawArrows() }
                created.pulse.addListener { _, _, _ -> redrawArrows() }
                edgeViews[key] = created
            } else {
                edge.relationship = rel
            }
        }

        val timeline = Timeline()
        val start = KeyFrame(Duration.ZERO)
        timeline.keyFrames.add(start)

        target.nodes.values.forEach { targetNode ->
            val view = findNodeEntryByHarmony(targetNode.chord)?.value ?: return@forEach
            val targetLayoutX = targetNode.x - view.half
            val targetLayoutY = targetNode.y - view.half

            val startCx = nodeCenterX(view)
            val startCy = nodeCenterY(view)
            val prevRole = previousRoles[harmonyKey(targetNode.chord)]
            val enteringOrbit = prevRole == null && targetNode.role == NodeRole.ORBIT
            val promotedToCenter =
                prevRole != null && prevRole != NodeRole.CENTER && targetNode.role == NodeRole.CENTER

            if (enteringOrbit) {
                val dx = targetNode.x - anchorCenter.first
                val dy = targetNode.y - anchorCenter.second
                val d = sqrt(dx * dx + dy * dy)
                val ux = if (d > 0.0001) dx / d else 1.0
                val uy = if (d > 0.0001) dy / d else 0.0
                val nx = -uy
                val ny = ux
                val swirlDir = if (targetNode.chord.pitchClass % 2 == 0) 1.0 else -1.0
                val midCx = anchorCenter.first + dx * 0.58 + nx * SWIRL_AMPLITUDE * swirlDir
                val midCy = anchorCenter.second + dy * 0.58 + ny * SWIRL_AMPLITUDE * swirlDir

                timeline.keyFrames.add(
                    KeyFrame(
                        Duration.millis(duration.toMillis() * 0.58),
                        KeyValue(view.pane.layoutXProperty(), midCx - view.half, MOVE_INTERPOLATOR),
                        KeyValue(view.pane.layoutYProperty(), midCy - view.half, MOVE_INTERPOLATOR),
                        KeyValue(view.pane.opacityProperty(), 0.92, Interpolator.EASE_OUT),
                        KeyValue(view.pane.scaleXProperty(), 1.04, Interpolator.EASE_OUT),
                        KeyValue(view.pane.scaleYProperty(), 1.04, Interpolator.EASE_OUT),
                        KeyValue(view.pane.rotateProperty(), 12.0 * swirlDir, Interpolator.EASE_OUT)
                    )
                )
            }

            if (promotedToCenter) {
                val dx = CX - startCx
                val dy = CY - startCy
                val d = sqrt(dx * dx + dy * dy)
                val ux = if (d > 0.0001) dx / d else 0.0
                val uy = if (d > 0.0001) dy / d else -1.0
                val overshootCx = CX + ux * CENTER_SPRING_OVERSHOOT
                val overshootCy = CY + uy * CENTER_SPRING_OVERSHOOT
                timeline.keyFrames.add(
                    KeyFrame(
                        Duration.millis(duration.toMillis() * 0.72),
                        KeyValue(view.pane.layoutXProperty(), overshootCx - view.half, MOVE_INTERPOLATOR),
                        KeyValue(view.pane.layoutYProperty(), overshootCy - view.half, MOVE_INTERPOLATOR),
                        KeyValue(view.pane.opacityProperty(), 1.0, Interpolator.EASE_BOTH),
                        KeyValue(view.pane.scaleXProperty(), CENTER_SPRING_SCALE, Interpolator.EASE_OUT),
                        KeyValue(view.pane.scaleYProperty(), CENTER_SPRING_SCALE, Interpolator.EASE_OUT),
                        KeyValue(view.pane.rotateProperty(), 0.0, Interpolator.EASE_OUT)
                    )
                )
            }

            timeline.keyFrames.add(
                KeyFrame(
                    duration,
                    KeyValue(view.pane.layoutXProperty(), targetLayoutX, MOVE_INTERPOLATOR),
                    KeyValue(view.pane.layoutYProperty(), targetLayoutY, MOVE_INTERPOLATOR),
                    KeyValue(view.pane.opacityProperty(), 1.0, Interpolator.EASE_BOTH),
                    KeyValue(view.pane.scaleXProperty(), 1.0, Interpolator.EASE_OUT),
                    KeyValue(view.pane.scaleYProperty(), 1.0, Interpolator.EASE_OUT),
                    KeyValue(view.pane.rotateProperty(), 0.0, Interpolator.EASE_OUT)
                )
            )
        }

        val targetHarmony = target.nodes.keys.map { harmonyKey(it) }.toSet()
        val removedNodes = nodeViews.keys.filter { harmonyKey(it) !in targetHarmony }
        removedNodes.forEach { chord ->
            val view = nodeViews[chord] ?: return@forEach
            val cx = nodeCenterX(view)
            val cy = nodeCenterY(view)
            val dx = cx - CX
            val dy = cy - CY
            val dist = sqrt(dx * dx + dy * dy)
            val ux = if (dist > 0.0001) dx / dist else cos(Math.toRadians((chord.pitchClass * 30.0) - 90.0))
            val uy = if (dist > 0.0001) dy / dist else sin(Math.toRadians((chord.pitchClass * 30.0) - 90.0))
            val travel = if (centerAlreadyVisible) 140.0 else 220.0
            val outCx = cx + ux * travel
            val outCy = cy + uy * travel

            timeline.keyFrames.add(
                KeyFrame(
                    duration,
                    KeyValue(view.pane.layoutXProperty(), outCx - view.half, Interpolator.EASE_IN),
                    KeyValue(view.pane.layoutYProperty(), outCy - view.half, Interpolator.EASE_IN),
                    KeyValue(view.pane.opacityProperty(), 0.0, Interpolator.EASE_IN),
                    KeyValue(view.pane.scaleXProperty(), 0.38, Interpolator.EASE_IN),
                    KeyValue(view.pane.scaleYProperty(), 0.38, Interpolator.EASE_IN),
                    KeyValue(
                        view.pane.rotateProperty(),
                        if (chord.pitchClass % 2 == 0) 22.0 else -22.0,
                        Interpolator.EASE_IN
                    )
                )
            )
        }

        edgeViews.values.forEach { edge ->
            val targetAlpha = if (target.edges.containsKey(edge.key)) 1.0 else 0.0
            val targetPulse = if (targetAlpha > 0.0 && edge.relationship.isDominantLike()) 1.0 else 0.0
            timeline.keyFrames.add(
                KeyFrame(duration, KeyValue(edge.alpha, targetAlpha, Interpolator.EASE_BOTH))
            )
            timeline.keyFrames.add(
                KeyFrame(duration, KeyValue(edge.pulse, targetPulse, Interpolator.EASE_BOTH))
            )
        }

        timeline.setOnFinished {
            removedNodes.forEach { chord ->
                val removed = nodeViews.remove(chord)
                if (removed != null) nodesPane.children.remove(removed.pane)
            }

            edgeViews.entries.removeIf { (_, edge) -> edge.alpha.get() <= 0.01 }
            currentSnapshot = target
            animating = false
            redrawArrows()

            val queued = queuedSnapshot
            queuedSnapshot = null
            if (queued != null && queued != currentSnapshot) {
                animateToSnapshot(queued)
            }
        }

        timeline.play()
    }

    private fun materializeInitialSnapshot(snapshot: GraphSnapshot) {
        val timeline = Timeline()

        snapshot.nodes.values.forEach { targetNode ->
            val pane = buildNodePane(targetNode.chord, targetNode.relationship, targetNode.role)
            val half = nodeHalf(targetNode.role)
            val view = NodeView(
                targetNode.chord,
                pane,
                targetNode.role,
                targetNode.relationship,
                half,
                targetNode.trailSlot
            )

            nodeViews[targetNode.chord] = view
            nodesPane.children.add(pane)
            setNodeCenter(view, CX, CY)
            pane.opacity = 0.0
            pane.scaleX = 0.45
            pane.scaleY = 0.45
            attachNodeListeners(view)

            timeline.keyFrames.add(
                KeyFrame(
                    Duration.millis(320.0),
                    KeyValue(
                        pane.layoutXProperty(),
                        if (targetNode.role == NodeRole.ORBIT) {
                            val dx = targetNode.x - CX
                            val dy = targetNode.y - CY
                            val d = sqrt(dx * dx + dy * dy)
                            val uy = if (d > 0.0001) dy / d else 0.0
                            val nx = -uy
                            val swirlDir = if (targetNode.chord.pitchClass % 2 == 0) 1.0 else -1.0
                            (CX + dx * 0.55 + nx * SWIRL_AMPLITUDE * swirlDir) - half
                        } else targetNode.x - half,
                        MOVE_INTERPOLATOR
                    ),
                    KeyValue(
                        pane.layoutYProperty(),
                        if (targetNode.role == NodeRole.ORBIT) {
                            val dx = targetNode.x - CX
                            val dy = targetNode.y - CY
                            val d = sqrt(dx * dx + dy * dy)
                            val ux = if (d > 0.0001) dx / d else 1.0
                            val ny = ux
                            val swirlDir = if (targetNode.chord.pitchClass % 2 == 0) 1.0 else -1.0
                            (CY + dy * 0.55 + ny * SWIRL_AMPLITUDE * swirlDir) - half
                        } else targetNode.y - half,
                        MOVE_INTERPOLATOR
                    ),
                    KeyValue(
                        pane.rotateProperty(),
                        if (targetNode.role == NodeRole.ORBIT) 9.0 else 0.0,
                        Interpolator.EASE_OUT
                    ),
                    KeyValue(pane.scaleXProperty(), 0.98, Interpolator.EASE_OUT),
                    KeyValue(pane.scaleYProperty(), 0.98, Interpolator.EASE_OUT),
                    KeyValue(pane.opacityProperty(), 0.9, Interpolator.EASE_OUT)
                )
            )

            timeline.keyFrames.add(
                KeyFrame(
                    Duration.millis(620.0),
                    KeyValue(pane.layoutXProperty(), targetNode.x - half, MOVE_INTERPOLATOR),
                    KeyValue(pane.layoutYProperty(), targetNode.y - half, MOVE_INTERPOLATOR),
                    KeyValue(pane.opacityProperty(), 1.0, Interpolator.EASE_OUT),
                    KeyValue(pane.scaleXProperty(), 1.0, Interpolator.EASE_OUT),
                    KeyValue(pane.scaleYProperty(), 1.0, Interpolator.EASE_OUT),
                    KeyValue(pane.rotateProperty(), 0.0, Interpolator.EASE_OUT)
                )
            )
        }

        snapshot.edges.forEach { (key, rel) ->
            val edge = EdgeView(key, rel, SimpleDoubleProperty(0.0), SimpleDoubleProperty(0.0))
            edge.alpha.addListener { _, _, _ -> redrawArrows() }
            edge.pulse.addListener { _, _, _ -> redrawArrows() }
            edgeViews[key] = edge
            timeline.keyFrames.add(KeyFrame(Duration.millis(620.0), KeyValue(edge.alpha, 1.0, Interpolator.EASE_OUT)))
            timeline.keyFrames.add(
                KeyFrame(
                    Duration.millis(620.0),
                    KeyValue(edge.pulse, if (rel.isDominantLike()) 1.0 else 0.0, Interpolator.EASE_OUT)
                )
            )
        }

        timeline.setOnFinished { redrawArrows() }
        timeline.play()
    }

    private fun attachNodeListeners(view: NodeView) {
        view.pane.layoutXProperty().addListener { _, _, _ -> redrawArrows() }
        view.pane.layoutYProperty().addListener { _, _, _ -> redrawArrows() }
        view.pane.opacityProperty().addListener { _, _, _ -> redrawArrows() }
        view.pane.scaleXProperty().addListener { _, _, _ -> redrawArrows() }
        view.pane.scaleYProperty().addListener { _, _, _ -> redrawArrows() }
    }

    // ─── Drawing ──────────────────────────────────────────────────────────────

    private fun redrawArrows() {
        val gc = arrowCanvas.graphicsContext2D
        gc.clearRect(0.0, 0.0, W, GRAPH_H)

        if (currentChord == Chord.UNDEFINED || nodeViews.isEmpty()) {
            drawIdleState()
            return
        }

        val orbitAlpha = nodeViews.values
            .filter { it.role == NodeRole.ORBIT || it.role == NodeRole.TRAIL }
            .maxOfOrNull { it.pane.opacity.coerceIn(0.0, 1.0) }
            ?: 0.0

        gc.save()
        gc.stroke = Color.web("#1E293B", 0.25 + orbitAlpha * 0.65)
        gc.lineWidth = 1.0
        gc.setLineDashes(4.0, 7.0)
        gc.strokeOval(CX - ORBIT_R, CY - ORBIT_R, ORBIT_R * 2, ORBIT_R * 2)
        gc.setLineDashes()
        gc.restore()

        drawTrailChain(gc)

        edgeViews.values.forEach { edge ->
            val alpha = edge.alpha.get().coerceIn(0.0, 1.0)
            if (alpha <= 0.01) return@forEach
            val from = findNodeByHarmonyKey(edge.key.from) ?: return@forEach
            val to = findNodeByHarmonyKey(edge.key.to) ?: return@forEach
            val pulseIntensity = edge.pulse.get().coerceIn(0.0, 1.0)
            val trailPhase = ((pulseClock.get() + (edge.key.to.pitchClass / 12.0)) % 1.0)
            drawArrow(
                gc,
                nodeCenterX(from),
                nodeCenterY(from),
                nodeCenterX(to),
                nodeCenterY(to),
                edge.relationship,
                from.half,
                to.half,
                alpha,
                pulseIntensity,
                trailPhase
            )
        }
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

    private fun drawTrailChain(gc: GraphicsContext) {
        val orderedTrail = nodeViews.values
            .filter { it.role == NodeRole.TRAIL && it.trailSlot != null }
            .sortedBy { it.trailSlot }

        if (orderedTrail.isEmpty()) return

        val center = nodeViews.values.firstOrNull { it.role == NodeRole.CENTER } ?: return

        gc.save()
        gc.stroke = Color.web("#94A3B8", 0.45)
        gc.lineWidth = 1.5
        gc.setLineDashes(2.0, 8.0)
        gc.strokeLine(
            nodeCenterX(center),
            nodeCenterY(center),
            nodeCenterX(orderedTrail.first()),
            nodeCenterY(orderedTrail.first())
        )

        gc.stroke = Color.web("#64748B", 0.56)
        gc.lineWidth = 1.35
        orderedTrail.zipWithNext().forEach { (a, b) ->
            val ax = nodeCenterX(a)
            val ay = nodeCenterY(a)
            val bx = nodeCenterX(b)
            val by = nodeCenterY(b)
            gc.strokeLine(ax, ay, bx, by)

            // Tiny directional arrowhead toward older nodes to emphasize timeline flow.
            val dx = bx - ax
            val dy = by - ay
            val dist = sqrt(dx * dx + dy * dy)
            if (dist > 0.001) {
                val ux = dx / dist
                val uy = dy / dist
                val hx = bx - ux * 8.0
                val hy = by - uy * 8.0
                val nx = -uy
                val ny = ux
                gc.fill = Color.web("#94A3B8", 0.55)
                gc.fillPolygon(
                    doubleArrayOf(bx, hx + nx * 3.2, hx - nx * 3.2),
                    doubleArrayOf(by, hy + ny * 3.2, hy - ny * 3.2),
                    3
                )
            }
        }
        gc.setLineDashes()
        gc.restore()
    }

    // ─── Arrow drawing ────────────────────────────────────────────────────────

    private fun drawArrow(
        gc: GraphicsContext,
        x1: Double, y1: Double,
        x2: Double, y2: Double,
        relationship: RelationshipType,
        fromHalf: Double,
        toHalf: Double,
        alpha: Double,
        pulseIntensity: Double,
        trailPhase: Double
    ) {
        val dx = x2 - x1
        val dy = y2 - y1
        val dist = sqrt(dx * dx + dy * dy)
        if (dist < 0.001) return
        val ux = dx / dist
        val uy = dy / dist

        val sx = x1 + ux * (fromHalf - 10.0)
        val sy = y1 + uy * (fromHalf - 10.0)
        val ex = x2 - ux * (toHalf - 12.0)
        val ey = y2 - uy * (toHalf - 12.0)

        val col = relationship.color

        gc.save()

        gc.stroke = col.deriveColor(0.0, 1.0, 1.0, alpha * 0.75)
        gc.lineWidth = 1.6 + alpha * 0.8
        gc.setLineDashes(7.0, 5.0)
        gc.strokeLine(sx, sy, ex, ey)
        gc.setLineDashes()

        if (pulseIntensity > 0.01) {
            gc.stroke = col.deriveColor(0.0, 1.0, 1.3, alpha * (0.25 + pulseIntensity * 0.45))
            gc.lineWidth = 3.5 + pulseIntensity * 2.8
            gc.strokeLine(sx, sy, ex, ey)
        }

        val headLen = 11.0
        val angle = atan2(ey - sy, ex - sx)
        val ax1 = ex - headLen * cos(angle - Math.PI / 6)
        val ay1 = ey - headLen * sin(angle - Math.PI / 6)
        val ax2 = ex - headLen * cos(angle + Math.PI / 6)
        val ay2 = ey - headLen * sin(angle + Math.PI / 6)
        gc.fill = col.deriveColor(0.0, 1.0, 1.0, alpha * 0.9)
        gc.fillPolygon(doubleArrayOf(ex, ax1, ax2), doubleArrayOf(ey, ay1, ay2), 3)

        val midX = (sx + ex) / 2.0
        val midY = (sy + ey) / 2.0
        val perpX = -uy * 16.0
        val perpY = ux * 16.0
        val labelPulse = if (pulseIntensity > 0.0) {
            pulseIntensity * (0.55 + 0.45 * sin(Math.PI * 2.0 * trailPhase))
        } else 0.0

        if (labelPulse > 0.01) {
            for (step in 1..3) {
                val shift = (12.0 * step) + trailPhase * 9.0
                gc.fill = col.deriveColor(0.0, 0.85, 1.35, alpha * labelPulse * (0.2 / step))
                gc.font = Font.font("System", FontWeight.BOLD, 11.0 + labelPulse * 1.2)
                gc.fillText(
                    relationship.shortLabel,
                    midX + perpX - ux * shift,
                    midY + perpY - uy * shift
                )
            }
        }

        gc.fill = col.deriveColor(0.0, 0.85, 1.5, (alpha + labelPulse * 0.35).coerceAtMost(1.0))
        gc.font = Font.font("System", FontWeight.BOLD, 11.0 + labelPulse * 1.4)
        gc.textAlign = TextAlignment.CENTER
        gc.textBaseline = VPos.CENTER
        gc.fillText(relationship.shortLabel, midX + perpX, midY + perpY)

        gc.restore()
    }

    // ─── Chord hex nodes ──────────────────────────────────────────────────────

    private fun buildNodePane(
        chord: Chord,
        relationship: RelationshipType?,
        role: NodeRole,
        trailSlot: Int? = null
    ): StackPane {
        val isCurrent = role == NodeRole.CENTER
        val isTrail = role == NodeRole.TRAIL
        val half = when (role) {
            NodeRole.CENTER -> CENTER_NODE_HALF
            NodeRole.ORBIT -> ORBIT_NODE_HALF
            NodeRole.TRAIL -> ORBIT_NODE_HALF * 0.66
        }
        val hexR = when (role) {
            NodeRole.CENTER -> CENTER_HEX_R
            NodeRole.ORBIT -> ORBIT_HEX_R
            NodeRole.TRAIL -> ORBIT_HEX_R * 0.62
        }
        val size = half * 2

        val canvas = Canvas(size, size)
        val fill = chordFill(chord, isCurrent)
        val stroke = chordStroke(chord, isCurrent)
        drawHex(canvas.graphicsContext2D, half, half, hexR, fill, stroke, if (isCurrent) 2.5 else 1.5)

        val content = buildNodeLabels(chord, relationship, isCurrent, isTrail, hexR)
        val trailAlpha = trailOpacity(trailSlot)
        return StackPane(canvas, content).apply {
            prefWidth = size
            prefHeight = size
            if (isCurrent) {
                effect = DropShadow(
                    BlurType.GAUSSIAN,
                    fill.deriveColor(0.0, 1.0, 1.6, 0.85),
                    26.0,
                    0.22,
                    0.0,
                    0.0
                )
            } else if (isTrail) {
                opacity = trailAlpha
            }
        }
    }

    private fun applyNodeVisual(view: NodeView, role: NodeRole, relationship: RelationshipType?) {
        val cx = nodeCenterX(view)
        val cy = nodeCenterY(view)
        view.role = role
        view.relationship = relationship
        view.half = nodeHalf(role)

        val rebuilt = buildNodePane(view.chord, relationship, role, view.trailSlot)
        view.pane.children.setAll(rebuilt.children)
        view.pane.prefWidth = rebuilt.prefWidth
        view.pane.prefHeight = rebuilt.prefHeight
        view.pane.effect = rebuilt.effect
        if (role == NodeRole.TRAIL) {
            view.pane.opacity = trailOpacity(view.trailSlot)
        }
        setNodeCenter(view, cx, cy)
    }

    private fun setNodeCenter(view: NodeView, cx: Double, cy: Double) {
        view.pane.layoutX = cx - view.half
        view.pane.layoutY = cy - view.half
    }

    private fun nodeCenterX(view: NodeView): Double = view.pane.layoutX + view.half

    private fun nodeCenterY(view: NodeView): Double = view.pane.layoutY + view.half

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
        val content = buildNodeLabels(chord, relationship, isCurrent, false, hexR)

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
        isTrail: Boolean,
        hexR: Double
    ): VBox {
        val maxW = hexR * 1.55
        val nameLabel = Label(chord.label).apply {
            val fontSize = when {
                isCurrent -> 14
                isTrail -> 9
                else -> 11
            }
            style = "-fx-font-size: $fontSize; -fx-font-weight: bold; " +
                    "-fx-text-fill: white; -fx-wrap-text: true; -fx-text-alignment: center;"
            maxWidth = maxW; isWrapText = true; alignment = Pos.CENTER
        }
        val vbox = VBox(2.0).apply { alignment = Pos.CENTER }
        if (relationship != null && !isTrail) {
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

    private fun trailXY(slot: Int): Pair<Double, Double> {
        val safeSlot = slot.coerceIn(0, MAX_TRAIL_NODES - 1)
        val left = CX - 300.0
        val spacing = 75.0
        val y = CY + ORBIT_R + 58.0
        return Pair(left + safeSlot * spacing, y)
    }

    private fun trailOpacity(slot: Int?): Double {
        if (slot == null) return 0.9
        val t = slot.coerceIn(0, MAX_TRAIL_NODES - 1).toDouble() / (MAX_TRAIL_NODES - 1).coerceAtLeast(1)
        return 0.95 - (0.55 * t)
    }

    private fun nodeHalf(role: NodeRole): Double = when (role) {
        NodeRole.CENTER -> CENTER_NODE_HALF
        NodeRole.ORBIT -> ORBIT_NODE_HALF
        NodeRole.TRAIL -> ORBIT_NODE_HALF * 0.66
    }

    private fun findNodeEntryByHarmony(chord: Chord): MutableMap.MutableEntry<Chord, NodeView>? {
        val key = harmonyKey(chord)
        return nodeViews.entries.firstOrNull { harmonyKey(it.key) == key }
    }

    private fun findNodeByHarmonyKey(key: HarmonyKey): NodeView? =
        nodeViews.entries.firstOrNull { harmonyKey(it.key) == key }?.value

    private fun harmonyKey(chord: Chord): HarmonyKey =
        HarmonyKey(chord.pitchClass, normalizedQuality(chord.quality))

    private fun normalizedQuality(quality: ChordQuality): ChordQuality = when (quality) {
        ChordQuality.DOMINANT -> ChordQuality.MAJOR
        ChordQuality.HALF_DIMINISHED -> ChordQuality.DIMINISHED
        else -> quality
    }

    private fun updatePlayedTrail(nextCenter: Chord) {
        val previous = currentChord
        if (previous == Chord.UNDEFINED || previous == nextCenter) return
        if (!areRelated(previous, nextCenter)) return

        val previousKey = harmonyKey(previous)
        playedTrail.removeAll { harmonyKey(it) == previousKey }
        playedTrail.add(0, previous)
        if (playedTrail.size > MAX_TRAIL_NODES) {
            playedTrail.subList(MAX_TRAIL_NODES, playedTrail.size).clear()
        }
    }

    private fun areRelated(a: Chord, b: Chord): Boolean {
        val aKey = harmonyKey(a)
        val bKey = harmonyKey(b)
        val aToB = ChordRelationMap.getRelatedChords(a).any { harmonyKey(it.chord) == bKey }
        val bToA = ChordRelationMap.getRelatedChords(b).any { harmonyKey(it.chord) == aKey }
        return aToB || bToA
    }


    private fun RelationshipType.isDominantLike(): Boolean =
        this == RelationshipType.DOMINANT ||
                this == RelationshipType.MAJOR_DOMINANT ||
                this == RelationshipType.MINOR_DOMINANT
}