package be.codewriter.melodymatrix.view.view.sheetmusic

import be.codewriter.melodymatrix.view.component.ZoomableNode
import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.event.MidiDataEvent
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.MmxEventType
import be.codewriter.melodymatrix.view.event.PlayEvent
import be.codewriter.melodymatrix.view.event.ScoreLoadedEvent
import be.codewriter.melodymatrix.view.i18n.I18n
import be.codewriter.melodymatrix.view.view.MmxView
import be.codewriter.melodymatrix.view.view.MmxViewMetadata
import be.codewriter.melodymatrix.view.view.sheet.SheetMusicAdapter
import com.sheetmusic4j.core.model.Score
import com.sheetmusic4j.engraving.glyph.MarkingCategory
import com.sheetmusic4j.fxviewer.SheetView
import javafx.application.Platform
import javafx.beans.property.SimpleIntegerProperty
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory
import javafx.scene.control.ToolBar
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.util.Duration as FxDuration

/**
 * Live "score-as-you-play" viewer. Starts with an empty grand staff (4/4, C major by
 * default); every completed NOTE_ON/NOTE_OFF pair is added to the sheet with a duration
 * proportional to how long the key was held.
 *
 * The time-signature numerator/denominator and the key signature (fifths, -7..7) can be
 * changed at any time via the toolbar; changing them re-engraves the score with the new
 * signature applied to the first measure.
 *
 * A "Clear" button removes all captured notes and returns the sheet to its empty state.
 */
class SheetMusicView : MmxView() {

    override val fitToViewport: Boolean = true

    private val bundle = I18n.registerBundle(BUNDLE_BASE_NAME)

    private val bpm = SimpleIntegerProperty(DEFAULT_BPM)
    private val timeBeats = SimpleIntegerProperty(4)
    private val timeBeatType = SimpleIntegerProperty(4)
    private val keyFifths = SimpleIntegerProperty(0)

    private val pendingByMidi = HashMap<Int, PendingNote>()
    private val captured = mutableListOf<PlayEvent>()

    /**
     * When non-null, [refreshScore] renders this pre-built score verbatim instead of
     * engraving one from the live [captured] events. Set when a recording file is
     * opened (MIDI/mmxr/MusicXML) via [ScoreLoadedEvent], cleared by [clear].
     */
    private var loadedScore: Score? = null

    private val sheet = SheetView().apply {
        setSystemWidth(SYSTEM_WIDTH)
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

    private var rebuildPending = false

    init {
        refreshScore()

        val toolbar = buildToolbar()

        val zoomable = ZoomableNode(
            content = sheet,
            naturalWidth = SYSTEM_WIDTH,
            naturalHeight = NATURAL_HEIGHT,
            minWidthValue = 240.0,
            minHeightValue = 120.0,
            fitMode = ZoomableNode.FitMode.CONTAIN
        )

        val root = BorderPane().apply {
            top = toolbar
            center = zoomable
            padding = Insets(0.0)
            style = "-fx-background-color: transparent;"
        }
        BorderPane.setMargin(zoomable, Insets(0.0))

        setupSurface(root, NATURAL_WIDTH, NATURAL_HEIGHT + TOOLBAR_HEIGHT, sheet)

        // Re-engrave whenever a signature changes.
        val rebuildListener = javafx.beans.value.ChangeListener<Any> { _, _, _ -> scheduleRebuild() }
        timeBeats.addListener(rebuildListener)
        timeBeatType.addListener(rebuildListener)
        keyFifths.addListener(rebuildListener)
    }

    private fun buildToolbar(): ToolBar {
        val timeLabel = Label().apply {
            textProperty().bind(I18n.binding(bundle, "sheetmusic.label.timesig"))
        }
        val beatsSpinner = Spinner<Int>().apply {
            valueFactory = SpinnerValueFactory.IntegerSpinnerValueFactory(1, 16, timeBeats.get())
            isEditable = false
            prefWidth = 70.0
            valueProperty().addListener { _, _, v -> timeBeats.set(v) }
        }
        val beatTypeCombo = ComboBox<Int>(FXCollections.observableArrayList(1, 2, 4, 8, 16)).apply {
            value = timeBeatType.get()
            prefWidth = 70.0
            valueProperty().addListener { _, _, v -> if (v != null) timeBeatType.set(v) }
        }
        val keyLabel = Label().apply {
            textProperty().bind(I18n.binding(bundle, "sheetmusic.label.key"))
        }
        val keyCombo = ComboBox<KeyOption>(FXCollections.observableArrayList(KeyOption.all)).apply {
            value = KeyOption.all.first { it.fifths == keyFifths.get() }
            prefWidth = 160.0
            valueProperty().addListener { _, _, v -> if (v != null) keyFifths.set(v.fifths) }
        }
        val bpmLabel = Label().apply {
            textProperty().bind(I18n.binding(bundle, "sheetmusic.label.bpm"))
        }
        val bpmSpinner = Spinner<Int>().apply {
            valueFactory = SpinnerValueFactory.IntegerSpinnerValueFactory(30, 240, bpm.get())
            isEditable = true
            prefWidth = 80.0
            valueProperty().addListener { _, _, v -> bpm.set(v) }
        }
        val clearButton = Button().apply {
            textProperty().bind(I18n.binding(bundle, "sheetmusic.action.clear"))
            setOnAction { clear() }
        }

        val leftGroup = HBox(6.0, timeLabel, beatsSpinner, Label("/"), beatTypeCombo, keyLabel, keyCombo)
            .apply { alignment = Pos.CENTER_LEFT }
        val spacer = Region().apply { HBox.setHgrow(this, Priority.ALWAYS) }
        val rightGroup = HBox(6.0, bpmLabel, bpmSpinner, clearButton).apply { alignment = Pos.CENTER_LEFT }

        return ToolBar(leftGroup, spacer, rightGroup)
    }

    override fun onEvent(event: MmxEvent) {
        when (event.type) {
            MmxEventType.MIDI -> {
                val midi = event as? MidiDataEvent ?: return
                if (midi.isDrum || midi.note == Note.UNDEFINED) return
                handleMidi(midi)
            }

            MmxEventType.PLAYBACK_STOP -> Platform.runLater { pendingByMidi.clear() }

            MmxEventType.SCORE_LOADED -> {
                val scoreEvent = event as? ScoreLoadedEvent ?: return
                Platform.runLater {
                    loadedScore = scoreEvent.score
                    captured.clear()
                    pendingByMidi.clear()
                    scoreEvent.bpm?.let { bpm.set(it) }
                    sheet.setScore(scoreEvent.score)
                }
            }

            MmxEventType.PLAY,
            MmxEventType.CHORD,
            MmxEventType.AUDIO_SPECTRUM -> {
                // Not used here
            }
        }
    }

    private fun handleMidi(midi: MidiDataEvent) {
        // When a full score has been loaded from a file, live MIDI events during
        // playback must not mutate the displayed sheet — the score is the source of
        // truth until the user clears it.
        if (loadedScore != null) return
        val midiNumber = midi.note.byteValue
        // `midi.timestamp` is emitted in nanoseconds when the event comes from recorded
        // playback (see DataLine) but in milliseconds for live/test input; using the JVM
        // wall-clock here keeps the two paths consistent for the live-capture view.
        val nowMs = System.currentTimeMillis()
        when (midi.event) {
            MidiEvent.NOTE_ON -> {
                if (midi.velocity == 0) {
                    finalizeNote(midiNumber, nowMs)
                } else {
                    pendingByMidi[midiNumber] = PendingNote(midi.note, nowMs, midi.velocity)
                }
            }

            MidiEvent.NOTE_OFF -> finalizeNote(midiNumber, nowMs)

            else -> { /* ignore */
            }
        }
    }

    private fun finalizeNote(midiNumber: Int, endMs: Long) {
        val pending = pendingByMidi.remove(midiNumber) ?: return
        val durationMs = (endMs - pending.startTimeMs).coerceAtLeast(1L)
        val playEvent = PlayEvent(
            note = pending.note,
            startTime = pending.startTimeMs,
            duration = FxDuration.millis(durationMs.toDouble()),
            velocity = pending.velocity
        )
        Platform.runLater {
            captured.add(playEvent)
            scheduleRebuild()
        }
    }

    private fun scheduleRebuild() {
        if (rebuildPending) return
        rebuildPending = true
        Platform.runLater {
            rebuildPending = false
            refreshScore()
        }
    }

    private fun refreshScore() {
        val preloaded = loadedScore
        if (preloaded != null) {
            sheet.setScore(preloaded)
            return
        }
        val score = SheetMusicBuilder.buildLiveScore(
            playEvents = captured.toList(),
            bpm = bpm.get(),
            beats = timeBeats.get(),
            beatType = timeBeatType.get(),
            fifths = keyFifths.get()
        )
        sheet.setScore(score)
    }

    private fun clear() {
        captured.clear()
        pendingByMidi.clear()
        loadedScore = null
        refreshScore()
    }

    private data class PendingNote(
        val note: Note,
        val startTimeMs: Long,
        val velocity: Int
    )

    companion object : MmxViewMetadata {
        override val bundleBaseName = BUNDLE_BASE_NAME
        override fun getViewImagePath(): String = "/view/scale.png"

        private const val BUNDLE_BASE_NAME: String = "i18n/view/sheetmusic"

        private const val SYSTEM_WIDTH: Double = 1400.0
        private const val NATURAL_WIDTH: Double = 1400.0
        private const val NATURAL_HEIGHT: Double = 320.0
        private const val TOOLBAR_HEIGHT: Double = 50.0
        private const val DEFAULT_BPM: Int = 100
    }
}

/**
 * Selectable key-signature option shown in the SheetMusicView toolbar. Uses the standard
 * "fifths" convention: negative = flats, positive = sharps.
 */
internal data class KeyOption(val fifths: Int, val label: String) {
    override fun toString(): String = label

    companion object {
        val all: List<KeyOption> = listOf(
            KeyOption(-7, "Cb / Ab minor (7♭)"),
            KeyOption(-6, "Gb / Eb minor (6♭)"),
            KeyOption(-5, "Db / Bb minor (5♭)"),
            KeyOption(-4, "Ab / F minor (4♭)"),
            KeyOption(-3, "Eb / C minor (3♭)"),
            KeyOption(-2, "Bb / G minor (2♭)"),
            KeyOption(-1, "F / D minor (1♭)"),
            KeyOption(0, "C / A minor"),
            KeyOption(1, "G / E minor (1♯)"),
            KeyOption(2, "D / B minor (2♯)"),
            KeyOption(3, "A / F♯ minor (3♯)"),
            KeyOption(4, "E / C♯ minor (4♯)"),
            KeyOption(5, "B / G♯ minor (5♯)"),
            KeyOption(6, "F♯ / D♯ minor (6♯)"),
            KeyOption(7, "C♯ / A♯ minor (7♯)")
        )
    }
}
