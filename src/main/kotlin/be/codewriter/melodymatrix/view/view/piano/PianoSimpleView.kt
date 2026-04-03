package be.codewriter.melodymatrix.view.view.piano

import be.codewriter.melodymatrix.view.component.ZoomableNode
import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.event.MidiDataEvent
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.MmxEventType
import be.codewriter.melodymatrix.view.view.MmxView
import be.codewriter.melodymatrix.view.view.MmxViewMetadata
import be.codewriter.melodymatrix.view.view.piano.PianoSimpleView.Companion.END_NOTE
import be.codewriter.melodymatrix.view.view.piano.PianoSimpleView.Companion.START_NOTE
import be.codewriter.melodymatrix.view.view.piano.data.PianoConfiguration
import be.codewriter.melodymatrix.view.view.piano.keyboard.KeyboardView
import be.codewriter.melodymatrix.view.view.piano.keyboard.NoteEventListener
import javafx.application.Platform
import javafx.scene.layout.StackPane
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * Compact visualizer that displays a single piano keyboard covering the centre octaves.
 *
 * Unlike [PianoWithEffectsView], this view contains no animation canvas or effect layers —
 * only the interactive [KeyboardView] scaled to fill the available space.
 * The keyboard spans [START_NOTE] to [END_NOTE] (C3 – C6 by default, 37 keys), mirroring
 * the range of a typical small/portable keyboard.
 *
 * A minimal settings toolbar exposes the background colour and key-colour configuration.
 * The view is wrapped in a [ZoomableNode] so it scales correctly inside host containers.
 *
 * @see MmxView
 * @see KeyboardView
 * @see PianoWithEffectsView
 */
class PianoSimpleView : MmxView() {

    override val fitToViewport: Boolean = true

    private val config = PianoConfiguration("simple")

    private val keyboardView: KeyboardView = KeyboardView(
        config,
        KEYBOARD_WIDTH,
        KEYBOARD_HEIGHT,
        START_NOTE,
        END_NOTE
    )

    /**
     * Optional listener that receives note events when the user clicks a key with the mouse.
     *
     * Assign an implementation from the host application to route keyboard interactions
     * to the engine (e.g., [be.codewriter.melodymatrix.engine.midi.MidiService.sendNote]).
     * The listener is wired to [keyboardView] so all user interactions propagate.
     */
    var noteEventListener: NoteEventListener?
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
                    logger.debug(
                        "Received note {} {}",
                        midiDataEvent.note,
                        if (midiDataEvent.event == MidiEvent.NOTE_ON) "ON" else "OFF"
                    )
                    keyboardView.playNote(midiDataEvent)
                }
            }

            else -> { /* not needed */
            }
        }
    }

    companion object : MmxViewMetadata {
        override fun getViewTitle(): String = "Piano (Simple)"
        override fun getViewDescription(): String =
            "A compact piano keyboard showing the centre octaves (C3 – C6) without effects."

        override fun getViewImagePath(): String = "/view/piano.png"

        private val logger: Logger = LogManager.getLogger(PianoSimpleView::class.java.name)

        private const val TOOLBAR_CONTROL_HEIGHT = 40.0

        /** First note displayed on the keyboard (lowest pitch). */
        val START_NOTE: Note = Note.C3

        /** Last note displayed on the keyboard (highest pitch). */
        val END_NOTE: Note = Note.C6_SHARP

        /** Natural pixel width of the keyboard view. */
        const val KEYBOARD_WIDTH = 900.0

        /** Natural pixel height of the keyboard view (= white key height). */
        const val KEYBOARD_HEIGHT = 160.0
    }
}
