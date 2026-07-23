package be.codewriter.melodymatrix.view.view.learnstrip

import be.codewriter.melodymatrix.view.component.ZoomableNode
import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.MmxEventType
import be.codewriter.melodymatrix.view.event.PlayEvent
import be.codewriter.melodymatrix.view.i18n.I18n
import be.codewriter.melodymatrix.view.view.MmxView
import be.codewriter.melodymatrix.view.view.MmxViewMetadata
import be.codewriter.melodymatrix.view.view.sheet.SheetMusicAdapter
import com.sheetmusic4j.core.model.MusicElement
import com.sheetmusic4j.fxviewer.StripSheetView
import javafx.animation.AnimationTimer
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.layout.BorderPane
import javafx.scene.paint.Color

/**
 * Scrolling one-line "learn" view: renders the currently-playing recording as a
 * horizontal strip of notation with a fixed cursor and live per-note highlighting.
 *
 * Wiring:
 *  - Buffers incoming `PLAY` events (dispatched all-at-once at playback start by
 *    [be.codewriter.melodymatrix.engine.recording.PlaybackEventScheduler]) and, once the
 *    burst settles, builds a strip score from the full timeline.
 *  - Drives the cursor from the wall-clock elapsed time since playback start converted
 *    to quarter notes using the same bpm ([SheetMusicAdapter.DEFAULT_BPM]) that
 *    quantized the timeline. Slight drift is possible but visually bounded.
 *  - Highlights an element for its notified `PLAY` duration (green), then clears it.
 *  - `PLAYBACK_STOP` resets everything.
 */
class LearnStripView : MmxView() {

    override val fitToViewport: Boolean = true

    private val bundle = I18n.registerBundle(BUNDLE_BASE_NAME)

    private val strip = StripSheetView().apply {
        setCursorScreenPosition(0.3)
        setCursorColor(Color.CRIMSON)
    }

    private val hintLabel = Label().apply {
        textProperty().bind(I18n.binding(bundle, "learnstrip.label.waiting"))
        style = "-fx-font-size: 13; -fx-text-fill: -color-fg-muted;"
        padding = Insets(4.0, 0.0, 4.0, 8.0)
    }

    // Buffered PLAY events: we build the score once the initial burst has settled.
    private val pendingEvents = mutableListOf<PlayEvent>()
    private var commitPending: Boolean = false

    // Live score state (available after commit).
    private var scoreCommitted: Boolean = false
    private var playbackStartWallClockMs: Long = 0L
    private var msPerQuarter: Double = 60_000.0 / SheetMusicAdapter.DEFAULT_BPM
    private var elementByPlayEvent: Map<PlayEvent, MusicElement> = emptyMap()
    private var activeHighlights: MutableSet<MusicElement> = HashSet()

    private val cursorAnimation = object : AnimationTimer() {
        override fun handle(now: Long) {
            if (!scoreCommitted) return
            val elapsedMs = System.currentTimeMillis() - playbackStartWallClockMs
            if (elapsedMs < 0) {
                strip.cursorTimeProperty().set(0.0)
                return
            }
            val quarters = elapsedMs / msPerQuarter
            strip.cursorTimeProperty().set(quarters)
        }
    }

    init {
        val zoomable = ZoomableNode(
            content = strip,
            naturalWidth = NATURAL_WIDTH,
            naturalHeight = NATURAL_HEIGHT,
            minWidthValue = 240.0,
            minHeightValue = 80.0,
            fitMode = ZoomableNode.FitMode.CONTAIN
        )
        val root = BorderPane().apply {
            top = hintLabel
            center = zoomable
            padding = Insets(0.0)
        }
        setupSurface(root, NATURAL_WIDTH, NATURAL_HEIGHT + HINT_HEIGHT, strip, onDispose = {
            cursorAnimation.stop()
        })
    }

    override fun onEvent(event: MmxEvent) {
        when (event.type) {
            MmxEventType.PLAY -> {
                val play = event as? PlayEvent ?: return
                Platform.runLater { handlePlayEvent(play) }
            }

            MmxEventType.PLAYBACK_STOP -> Platform.runLater { reset() }

            MmxEventType.MIDI,
            MmxEventType.CHORD,
            MmxEventType.AUDIO_SPECTRUM -> {
                // Not used here
            }
        }
    }

    private fun handlePlayEvent(play: PlayEvent) {
        if (play.note == Note.UNDEFINED) return

        if (!scoreCommitted) {
            pendingEvents.add(play)
            if (!commitPending) {
                commitPending = true
                // Wait one JavaFX pulse and one more Platform.runLater tick to let the whole
                // initial burst arrive before we build the score. In practice the PlaybackEventScheduler
                // dispatches every PlayEvent synchronously so a single defer is enough, but
                // we chain two to be safe.
                Platform.runLater { Platform.runLater { commitScoreIfPending() } }
            }
            return
        }

        // Score already committed → set the highlight for this specific event.
        val element = elementByPlayEvent[play] ?: return
        val highlights = strip.noteHighlights()
        highlights[element] = ACTIVE_COLOR
        activeHighlights.add(element)

        val durationMs = play.duration.toMillis().toLong().coerceAtLeast(1L)
        val startAtWallClockMs = play.startTime / 1_000_000L
        val clearAtWallClockMs = startAtWallClockMs + durationMs
        val delayMs = (clearAtWallClockMs - System.currentTimeMillis()).coerceAtLeast(1L)
        Thread.startVirtualThread {
            try {
                Thread.sleep(delayMs)
                Platform.runLater {
                    highlights.remove(element)
                    activeHighlights.remove(element)
                }
            } catch (_: InterruptedException) {
                // benign — playback was stopped
            }
        }
    }

    private fun commitScoreIfPending() {
        commitPending = false
        if (scoreCommitted || pendingEvents.isEmpty()) return

        val eventsSnapshot = pendingEvents.toList()
        val originStart = eventsSnapshot.minOf { it.startTime }
        // Convert absolute wall-clock nanos back to a 0-based timeline (ms) so
        // SheetMusicAdapter can quantize consistently.
        val zeroBasedEvents = eventsSnapshot.map { evt ->
            evt.copy(startTime = (evt.startTime - originStart) / 1_000_000L)
        }
        val originalToNormalized = eventsSnapshot.zip(zeroBasedEvents).toMap()

        val result = SheetMusicAdapter.playEventsToScore(zeroBasedEvents, bpm = SheetMusicAdapter.DEFAULT_BPM)
        elementByPlayEvent = eventsSnapshot.mapNotNull { orig ->
            val norm = originalToNormalized[orig] ?: return@mapNotNull null
            val el = result.elementByPlayEvent[norm] ?: return@mapNotNull null
            orig to el
        }.toMap()

        strip.setScore(result.score)
        strip.cursorTimeProperty().set(0.0)
        msPerQuarter = 60_000.0 / result.bpm.coerceAtLeast(1)
        playbackStartWallClockMs = originStart / 1_000_000L
        scoreCommitted = true
        pendingEvents.clear()
        cursorAnimation.start()
    }

    private fun reset() {
        cursorAnimation.stop()
        pendingEvents.clear()
        activeHighlights.clear()
        elementByPlayEvent = emptyMap()
        scoreCommitted = false
        commitPending = false
        strip.cursorTimeProperty().set(0.0)
        strip.noteHighlights().clear()
    }

    companion object : MmxViewMetadata {
        override val bundleBaseName = BUNDLE_BASE_NAME
        override fun getViewImagePath(): String = "/view/scale.png"

        private const val BUNDLE_BASE_NAME = "i18n/view/learnstrip"
        private const val NATURAL_WIDTH: Double = 1000.0
        private const val NATURAL_HEIGHT: Double = 200.0
        private const val HINT_HEIGHT: Double = 24.0

        private val ACTIVE_COLOR: Color = Color.LIMEGREEN
    }
}
