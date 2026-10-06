package be.codewriter.melodymatrix.view.view.piano

import be.codewriter.melodymatrix.view.component.ZoomableNode
import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.event.MidiDataEvent
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.MmxEventType
import be.codewriter.melodymatrix.view.event.NoteEventListener
import be.codewriter.melodymatrix.view.helper.SettingHelper
import be.codewriter.melodymatrix.view.view.MmxNoteDispatcher
import be.codewriter.melodymatrix.view.view.MmxView
import be.codewriter.melodymatrix.view.view.MmxViewMetadata
import be.codewriter.melodymatrix.view.view.piano.data.PianoConfiguration
import be.codewriter.melodymatrix.view.view.piano.keyboard.KeyboardView
import javafx.application.Platform
import javafx.scene.layout.StackPane
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * Compact visualizer that displays a single piano keyboard covering the centre octaves.
 *
 * Unlike [PianoWithEffectsView], this view contains no animation canvas or effect layers —
 * only the interactive [KeyboardView] scaled to fill the available space.
 * The keyboard spans from [DEFAULT_START_NOTE] to [DEFAULT_END_NOTE] (C3 – C6# by default,
 * 37 keys), mirroring the range of a typical small/portable keyboard. The range can be
 * overridden via the `view.piano.simple.startNote` / `view.piano.simple.endNote`
 * setting keys — for example, a learn-series configuration can shrink the keyboard to
 * a single octave.
 *
 * The view is wrapped in a [ZoomableNode] so it scales correctly inside host containers.
 *
 * @see MmxView
 * @see KeyboardView
 * @see PianoWithEffectsView
 */
class PianoSimpleView(
    settings: SettingHelper? = null
) : MmxView(), MmxNoteDispatcher {

    override val fitToViewport: Boolean = true

    private val config = PianoConfiguration("simple", settings)

    private val startNote: Note = resolveNote(settings, SETTING_KEY_START_NOTE, DEFAULT_START_NOTE)
    private val endNote: Note = resolveEndNote(settings, startNote)

    private val keyboardView: KeyboardView = KeyboardView(
        config,
        KEYBOARD_WIDTH,
        KEYBOARD_HEIGHT,
        startNote,
        endNote
    )

    /**
     * Optional listener that receives note events when the user clicks a key with the mouse.
     *
     * Assign an implementation from the host application to route keyboard interactions
     * to the engine (e.g., [sendNote]).
     * The listener is wired to [keyboardView] so all user interactions propagate.
     */
    override var noteEventListener: NoteEventListener
        get() = keyboardView.noteEventListener
        set(value) {
            keyboardView.noteEventListener = value
        }

    init {
        config.restoreSettings()
        config.pianoKeyNameVisible.set(true)

        val keyboardPane = StackPane(keyboardView).apply {
            prefWidth = KEYBOARD_WIDTH
            prefHeight = KEYBOARD_HEIGHT
            minWidth = KEYBOARD_WIDTH
            maxWidth = KEYBOARD_WIDTH
            minHeight = KEYBOARD_HEIGHT
            maxHeight = KEYBOARD_HEIGHT
        }

        val zoomableKeyboard = ZoomableNode(
            content = keyboardPane,
            naturalWidth = KEYBOARD_WIDTH,
            naturalHeight = KEYBOARD_HEIGHT,
            minWidthValue = 100.0,
            minHeightValue = 40.0,
            fitMode = ZoomableNode.FitMode.CONTAIN
        )

        setupSurface(
            rootNode = zoomableKeyboard,
            naturalWidth = KEYBOARD_WIDTH,
            naturalHeight = KEYBOARD_HEIGHT,
            captureNode = keyboardPane,
            captureWidth = KEYBOARD_WIDTH.toInt(),
            captureHeight = KEYBOARD_HEIGHT.toInt()
        )
    }

    private var firstNoteLogged = false

    /**
     * Handles incoming MelodyMatrix events.
     *
     * Reacts to MIDI note-on/off events by updating the keyboard key visuals.
     * All other event types are ignored.
     *
     * @param event The MelodyMatrix event to process
     */
    override fun onEvent(event: MmxEvent) {
        when (event.type) {
            MmxEventType.MIDI -> {
                val midiDataEvent = event as? MidiDataEvent ?: return
                Platform.runLater {
                    val note = midiDataEvent.note
                    val inRange = note.byteValue in startNote.byteValue..endNote.byteValue
                    // Log the first note at INFO so range-mismatch problems are visible
                    // without changing log configuration; subsequent notes stay at DEBUG.
                    if (!firstNoteLogged) {
                        firstNoteLogged = true
                        logger.info(
                            "First MIDI event received: note={} event={} inRange={} range={}..{}",
                            note,
                            midiDataEvent.event,
                            inRange,
                            startNote,
                            endNote
                        )
                    } else {
                        logger.debug(
                            "Received note {} {} (inRange={} range={}..{})",
                            note,
                            if (midiDataEvent.event == MidiEvent.NOTE_ON) "ON" else "OFF",
                            inRange,
                            startNote,
                            endNote
                        )
                    }
                    keyboardView.playNote(midiDataEvent)
                }
            }

            else -> { /* not needed */
            }
        }
    }

    companion object : MmxViewMetadata {
        override val bundleBaseName = "i18n/view/piano"
        override val bundleKeyPrefix = "simple."
        override fun getViewImagePath(): String = "/view/piano.png"

        private val logger: Logger = LogManager.getLogger(PianoSimpleView::class.java.name)

        /** Setting key that overrides the lowest displayed note. */
        const val SETTING_KEY_START_NOTE: String = "view.piano.simple.startNote"

        /** Setting key that overrides the highest displayed note. */
        const val SETTING_KEY_END_NOTE: String = "view.piano.simple.endNote"

        /** Default lowest note displayed on the keyboard when no setting override exists. */
        val DEFAULT_START_NOTE: Note = Note.C3

        /** Default highest note displayed on the keyboard when no setting override exists. */
        val DEFAULT_END_NOTE: Note = Note.C6_SHARP

        /** Natural pixel width of the keyboard view. */
        const val KEYBOARD_WIDTH = 900.0

        /** Natural pixel height of the keyboard view (= white key height). */
        const val KEYBOARD_HEIGHT = 160.0

        private fun resolveNote(settings: SettingHelper?, key: String, fallback: Note): Note {
            val raw = settings?.get(key)?.trim().orEmpty()
            if (raw.isEmpty()) return fallback
            return runCatching { Note.valueOf(raw) }
                .onFailure { logger.warn("Ignoring invalid note '{}' for setting {}", raw, key) }
                .getOrDefault(fallback)
        }

        private fun resolveEndNote(settings: SettingHelper?, startNote: Note): Note {
            val resolved = resolveNote(settings, SETTING_KEY_END_NOTE, DEFAULT_END_NOTE)
            if (resolved.byteValue < startNote.byteValue) {
                logger.warn(
                    "endNote {} is below startNote {}; falling back to default range",
                    resolved,
                    startNote
                )
                return DEFAULT_END_NOTE
            }
            return resolved
        }
    }
}
