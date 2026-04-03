package stage.drum

import be.codewriter.melodymatrix.view.component.ZoomableNode
import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.event.MidiDataEvent
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.MmxEventType
import be.codewriter.melodymatrix.view.event.NoteEventListener
import be.codewriter.melodymatrix.view.view.MmxNoteDispatcher
import be.codewriter.melodymatrix.view.view.MmxView
import be.codewriter.melodymatrix.view.view.MmxViewMetadata
import javafx.animation.ScaleTransition
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.control.Label
import javafx.scene.effect.DropShadow
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Ellipse
import javafx.scene.shape.Shape
import javafx.util.Duration
import kotlin.math.max


/**
 * Visualizer stage for drum note display.
 *
 * Shows a drum notation reference image and is designed to react to MIDI drum events.
 * Drum MIDI events arrive on channel 10 (index 9) and use note numbers to identify
 * individual drum instruments (kick, snare, hi-hat, etc.).
 *
 * @see MmxView
 * @see MidiDataEvent
 */
class DrumView : MmxView(), MmxNoteDispatcher {

    override val fitToViewport: Boolean = true
    override var noteEventListener: NoteEventListener = NoteEventListener { _, _ -> }
    override val midiChannel: Int = 9

    private val kitWidth = 800.0
    private val kitHeight = 500.0

    private enum class DrumPart(val title: String, val isCymbal: Boolean = false) {
        HI_HAT("Hi-hat", true),
        CRASH("Crash", true),
        RIDE("Ride", true),
        SNARE("Snare"),
        KICK("Kick"),
        LOW_FLOOR_TOM("Low floor tom"),
        HIGH_FLOOR_TOM("High floor tom"),
        LOW_TOM("Low tom"),
        HIGH_TOM("High tom")
    }

    private data class DrumNode(
        val container: StackPane,
        val surface: Shape,
        val baseFill: Color,
        val hitFill: Color
    )

    private val partToNote: Map<DrumPart, Note> = mapOf(
        DrumPart.KICK to (Note from 36.toByte()),
        DrumPart.SNARE to (Note from 38.toByte()),
        DrumPart.HI_HAT to (Note from 42.toByte()),
        DrumPart.LOW_FLOOR_TOM to (Note from 41.toByte()),
        DrumPart.HIGH_FLOOR_TOM to (Note from 43.toByte()),
        DrumPart.LOW_TOM to (Note from 45.toByte()),
        DrumPart.HIGH_TOM to (Note from 48.toByte()),
        DrumPart.CRASH to (Note from 49.toByte()),
        DrumPart.RIDE to (Note from 51.toByte()),
    )

    private val drumNodes: MutableMap<DrumPart, DrumNode> = mutableMapOf()
    private val activeHitsByPart: MutableMap<DrumPart, Int> = mutableMapOf()
    private val noteToPart: Map<Int, DrumPart> = mapOf(
        // Kick drum
        35 to DrumPart.KICK,
        36 to DrumPart.KICK,
        // Snare family
        37 to DrumPart.SNARE,
        38 to DrumPart.SNARE,
        39 to DrumPart.SNARE,
        40 to DrumPart.SNARE,
        // Hi-hat
        42 to DrumPart.HI_HAT,
        44 to DrumPart.HI_HAT,
        46 to DrumPart.HI_HAT,
        // Toms
        41 to DrumPart.LOW_FLOOR_TOM,
        43 to DrumPart.HIGH_FLOOR_TOM,
        45 to DrumPart.LOW_TOM,
        47 to DrumPart.LOW_TOM,
        48 to DrumPart.HIGH_TOM,
        50 to DrumPart.HIGH_TOM,
        // Cymbals
        49 to DrumPart.CRASH,
        51 to DrumPart.RIDE,
        53 to DrumPart.RIDE,
        57 to DrumPart.CRASH,
        59 to DrumPart.RIDE
    )

    init {
        val pane = Pane().apply {
            prefWidth = kitWidth
            prefHeight = kitHeight
        }

        registerNode(pane, DrumPart.CRASH, 95.0, 72.0, 95.0, 24.0, Color.web("#cfa62f"), Color.web("#fce57c"))
        registerNode(pane, DrumPart.RIDE, 580.0, 72.0, 110.0, 26.0, Color.web("#c6a13a"), Color.web("#ffe28f"))
        registerNode(pane, DrumPart.HI_HAT, 75.0, 175.0, 82.0, 20.0, Color.web("#b89537"), Color.web("#f8e072"))
        registerNode(pane, DrumPart.HIGH_TOM, 300.0, 140.0, 112.0, 52.0, Color.web("#3a5f8e"), Color.web("#66afff"))
        registerNode(pane, DrumPart.LOW_TOM, 430.0, 180.0, 115.0, 55.0, Color.web("#365988"), Color.web("#60a8f6"))
        registerNode(
            pane,
            DrumPart.HIGH_FLOOR_TOM,
            560.0,
            245.0,
            120.0,
            58.0,
            Color.web("#2f507d"),
            Color.web("#5f9de9")
        )
        registerNode(
            pane,
            DrumPart.LOW_FLOOR_TOM,
            530.0,
            330.0,
            128.0,
            62.0,
            Color.web("#2d496c"),
            Color.web("#5f95d8")
        )
        registerNode(pane, DrumPart.SNARE, 170.0, 265.0, 124.0, 58.0, Color.web("#9b4b55"), Color.web("#ff7488"))
        registerNode(pane, DrumPart.KICK, 310.0, 312.0, 180.0, 138.0, Color.web("#4e6783"), Color.web("#7ab6ff"))

        val zoomable = ZoomableNode(
            content = pane,
            naturalWidth = kitWidth,
            naturalHeight = kitHeight,
            minWidthValue = 100.0,
            minHeightValue = 100.0,
            fitMode = ZoomableNode.FitMode.CONTAIN
        )

        setupSurface(
            rootNode = zoomable,
            naturalWidth = kitWidth,
            naturalHeight = kitHeight,
            captureNode = pane,
            captureWidth = kitWidth.toInt(),
            captureHeight = kitHeight.toInt()
        )
    }

    /**
     * Handles incoming MelodyMatrix events.
     *
     * Currently responds to MIDI events only; PLAY and CHORD events are ignored.
     *
     * @param event The MelodyMatrix event to process
     */
    override fun onEvent(event: MmxEvent) {
        when (event.type) {
            MmxEventType.MIDI -> {
                val midiDataEvent = event as? MidiDataEvent ?: return
                Platform.runLater {
                    handleMidiEvent(midiDataEvent)
                }
            }

            MmxEventType.PLAY -> {
                // Not needed here
            }

            MmxEventType.CHORD -> {
                // Not needed here
            }
        }
    }

    private fun registerNode(
        stage: Pane,
        part: DrumPart,
        x: Double,
        y: Double,
        width: Double,
        height: Double,
        baseFill: Color,
        hitFill: Color
    ) {
        val surface = Ellipse(width / 2.0, height / 2.0).apply {
            fill = baseFill
            stroke = Color.web("#d7deef")
            strokeWidth = if (part.isCymbal) 1.5 else 2.0
            opacity = if (part.isCymbal) 0.95 else 1.0
        }

        val label = Label(part.title).apply {
            textFill = Color.web("#f6f8ff")
            style = "-fx-font-weight: bold; -fx-font-size: 12px;"
        }

        val note = partToNote[part] ?: Note.UNDEFINED

        val node = StackPane(surface, label).apply {
            layoutX = x
            layoutY = y
            prefWidth = width
            prefHeight = height
            alignment = Pos.CENTER
            cursor = Cursor.HAND
            setOnMousePressed {
                triggerHit(part, 64)
                noteEventListener.onNote(note, true)
            }
            setOnMouseReleased {
                releaseHit(part)
                noteEventListener.onNote(note, false)
            }
            setOnMouseExited { event ->
                if (event.isPrimaryButtonDown) {
                    releaseHit(part)
                    noteEventListener.onNote(note, false)
                }
            }
        }

        stage.children.add(node)
        drumNodes[part] = DrumNode(node, surface, baseFill, hitFill)
        activeHitsByPart[part] = 0
    }

    private fun handleMidiEvent(midiDataEvent: MidiDataEvent) {
        if (!midiDataEvent.isDrum || midiDataEvent.note.byteValue < 0) {
            return
        }

        val midiNote = midiDataEvent.bytes[1].toInt() and 0x7f
        val part = noteToPart[midiNote] ?: return

        when (midiDataEvent.event) {
            MidiEvent.NOTE_ON -> triggerHit(part, midiDataEvent.velocity)
            MidiEvent.NOTE_OFF -> releaseHit(part)
            else -> Unit
        }
    }

    private fun triggerHit(part: DrumPart, velocity: Int) {
        val node = drumNodes[part] ?: return
        val activeHitCount = (activeHitsByPart[part] ?: 0) + 1
        activeHitsByPart[part] = activeHitCount

        node.surface.fill = node.hitFill
        node.container.effect = DropShadow(30.0, node.hitFill)

        val velocityFactor = (velocity.coerceIn(0, 127) / 127.0)
        val maxScaleBoost = if (part.isCymbal) 0.15 else 0.2
        val targetScale = 1.0 + (0.08 + (velocityFactor * maxScaleBoost))

        val scalePulse = ScaleTransition(Duration.millis(90.0), node.container).apply {
            toX = targetScale
            toY = targetScale
            setAutoReverse(true)
            cycleCount = 2
        }

        scalePulse.play()
    }

    private fun releaseHit(part: DrumPart) {
        val node = drumNodes[part] ?: return
        val activeHitCount = max(0, (activeHitsByPart[part] ?: 0) - 1)
        activeHitsByPart[part] = activeHitCount

        if (activeHitCount == 0) {
            node.surface.fill = node.baseFill
            node.container.effect = null
            node.container.scaleX = 1.0
            node.container.scaleY = 1.0
        }
    }

    companion object : MmxViewMetadata {
        override fun getViewTitle(): String = "Hi Drummer!"
        override fun getViewDescription(): String = "Shows a drum notation reference and reacts to drum MIDI events."
        override fun getViewImagePath(): String = "/view/drum.png"
    }
}