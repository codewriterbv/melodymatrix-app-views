package be.codewriter.melodymatrix.view.view.piano.keyboard

import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.event.MidiDataEvent
import be.codewriter.melodymatrix.view.event.NoteEventListener
import be.codewriter.melodymatrix.view.view.piano.data.PianoConfiguration
import javafx.geometry.Point2D
import javafx.scene.Node
import javafx.scene.layout.Pane

/**
 * JavaFX pane that renders a piano keyboard for a configurable range of notes.
 *
 * Key dimensions are calculated from the supplied [width] and [height] so the keyboard
 * always fills exactly the requested area. Two constructors are provided:
 *
 * - `KeyboardView(config, width, height)` — renders all 88 standard piano keys.
 * - `KeyboardView(config, width, height, startNote, endNote)` — renders only the keys
 *   between [startNote] and [endNote] (inclusive), scaled to fill the same area.
 *
 * White keys are placed consecutively; black keys are overlaid at the correct horizontal
 * offset. Each key is stored in a map so it can be updated when a MIDI event arrives.
 *
 * @param config Observable configuration for key colours and note-name visibility
 * @see Key
 * @see KeyWhite
 * @see KeyBlack
 * @see KeyDimensions
 */
class KeyboardView private constructor(
    config: PianoConfiguration,
    notes: List<Note>,
    private val dims: KeyDimensions
) : Pane() {

    private val keys: MutableMap<Note, Key> = mutableMapOf()

    /**
     * Optional listener that receives note events when the user clicks a key with the mouse.
     *
     * Assign this from the host view to route key-click interactions to the engine without creating
     * a compile-time dependency on engine types inside the viewer module.
     *
     * The property can be set or cleared at any time; the lambda captured by each [Key]
     * reads the current value on every invocation, so late assignment works correctly.
     */
    var noteEventListener: NoteEventListener = NoteEventListener { _, _ -> }

    /**
     * Creates a keyboard displaying all 88 standard piano keys scaled to [width] × [height].
     *
     * @param config Observable configuration for key colours and note-name visibility
     * @param width  Total pixel width of the keyboard
     * @param height Total pixel height of the keyboard (white key height)
     */
    constructor(config: PianoConfiguration, width: Double, height: Double) :
            this(config, Note.pianoKeys(), buildDims(width, height, Note.pianoKeys()))

    /**
     * Creates a keyboard displaying only the keys from [startNote] to [endNote] (inclusive),
     * scaled to fill [width] × [height].
     *
     * @param config    Observable configuration for key colours and note-name visibility
     * @param width     Total pixel width of the keyboard
     * @param height    Total pixel height of the keyboard (white key height)
     * @param startNote First note to display (lowest pitch)
     * @param endNote   Last note to display (highest pitch)
     */
    constructor(config: PianoConfiguration, width: Double, height: Double, startNote: Note, endNote: Note) :
            this(
                config,
                notesInRange(startNote, endNote),
                buildDims(width, height, notesInRange(startNote, endNote))
            )

    init {
        var counterWhiteKeys = 0
        var previousWhiteKeyX = 0.0

        // Collect white and black keys separately so black keys can be added last,
        // ensuring they are always rendered on top and receive mouse events first.
        val blackKeyNodes = mutableListOf<Pair<KeyBlack, Double>>()

        notes.forEach { note ->
            if (note.mainNote.isSharp) {
                val x = previousWhiteKeyX + dims.whiteKeyWidth - (dims.blackKeyWidth / 2)
                val key = KeyBlack(config, note, x, dims)
                key.noteEventListener = NoteEventListener { n, isOn -> noteEventListener.onNote(n, isOn) }
                keys[note] = key
                blackKeyNodes.add(key to x)
            } else {
                val x = counterWhiteKeys * dims.whiteKeyWidth
                val key = KeyWhite(config, note, x, dims)
                key.noteEventListener = NoteEventListener { n, isOn -> noteEventListener.onNote(n, isOn) }
                keys[note] = key
                addUINode(key, x)
                counterWhiteKeys++
                previousWhiteKeyX = x
            }
        }

        // Add black keys after all white keys so they sit on top in z-order.
        blackKeyNodes.forEach { (key, x) -> addUINode(key, x) }
    }

    /**
     * Adds a key node to the [StackPane] at a fixed horizontal translation.
     *
     * @param key The key node to add
     * @param x   The desired X translation in pixels
     */
    private fun addUINode(key: Node, x: Double) {
        key.translateX = x
        children.add(key)
    }

    /**
     * Updates the visual state of the key corresponding to the event's note.
     *
     * @param midiDataEvent The MIDI event containing the note and on/off state
     */
    fun playNote(midiDataEvent: MidiDataEvent) {
        val key = keys[midiDataEvent.note] ?: return
        key.update(midiDataEvent.event == MidiEvent.NOTE_ON)
    }

    /**
     * Returns the on-screen X/Y origin for visual effects triggered by pressing [note].
     *
     * The origin is centred horizontally on the key and positioned at the top of the keyboard.
     *
     * @param note The note whose effect origin is requested
     * @return A [Point2D] representing the effect spawn point, or null if the note has no key
     */
    fun getEffectOrigin(note: Note): Point2D? {
        val key = keys[note] ?: return null
        val centerX = key.keyX() + if (note.mainNote.isSharp) {
            dims.blackKeyWidth / 2
        } else {
            dims.whiteKeyWidth / 2
        }
        return Point2D(centerX, 0.0)
    }

    companion object {
        /** Ratio of black-key width to white-key width (based on standard piano proportions). */
        private const val BLACK_WHITE_WIDTH_RATIO = 18.0 / 24.66

        /** Ratio of black-key height to white-key height (based on standard piano proportions). */
        private const val BLACK_WHITE_HEIGHT_RATIO = 80.0 / 120.0

        private fun notesInRange(startNote: Note, endNote: Note): List<Note> =
            Note.pianoKeys().filter { it.byteValue in startNote.byteValue..endNote.byteValue }

        private fun buildDims(width: Double, height: Double, notes: List<Note>): KeyDimensions {
            val numberOfWhiteKeys = notes.count { !it.mainNote.isSharp }
            val whiteKeyWidth = if (numberOfWhiteKeys > 0) width / numberOfWhiteKeys else width
            return KeyDimensions(
                whiteKeyWidth = whiteKeyWidth,
                whiteKeyHeight = height,
                blackKeyWidth = whiteKeyWidth * BLACK_WHITE_WIDTH_RATIO,
                blackKeyHeight = height * BLACK_WHITE_HEIGHT_RATIO
            )
        }
    }
}