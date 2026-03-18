package be.codewriter.melodymatrix.view.stage.piano.keyboard

import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.event.MidiDataEvent
import be.codewriter.melodymatrix.view.stage.piano.data.PianoConfiguration
import javafx.geometry.Point2D
import javafx.scene.Node
import javafx.scene.layout.StackPane

/**
 * JavaFX pane that renders all 88 piano keys as a full-width keyboard.
 *
 * White keys are placed consecutively from left to right; black keys are overlaid at
 * the correct horizontal offset relative to the preceding white key. Each key is stored
 * in a map so it can be looked up and updated when a MIDI note event arrives.
 *
 * @param config Observable configuration for key colours and note-name visibility
 * @see Key
 * @see KeyWhite
 * @see KeyBlack
 * @see PianoStage
 */
class KeyboardView(config: PianoConfiguration) : StackPane() {

    private val keys: MutableMap<Note, Key> = mutableMapOf()

    init {
        var counterWhiteKeys = 0
        var previousWhiteKeyX = 0.0

        Note.pianoKeys().forEach { note ->
            if (note.mainNote.isSharp) {
                val x = previousWhiteKeyX + PIANO_WHITE_KEY_WIDTH - (PIANO_BLACK_KEY_WIDTH / 2)
                val key = KeyBlack(config, note, x)
                keys[note] = key
                addUINode(key, x)
            } else {
                val x = counterWhiteKeys * PIANO_WHITE_KEY_WIDTH
                val key = KeyWhite(config, note, x)
                keys[note] = key
                addUINode(key, x)
                counterWhiteKeys++
                previousWhiteKeyX = x
            }
        }
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
            PIANO_BLACK_KEY_WIDTH / 2
        } else {
            PIANO_WHITE_KEY_WIDTH / 2
        }
        return Point2D(centerX, 0.0)
    }

    companion object {
        /** Width of a single white key in pixels. */
        const val PIANO_WHITE_KEY_WIDTH = 24.66

        /** Height of a white key in pixels. */
        const val PIANO_WHITE_KEY_HEIGHT = 120.0

        /** Width of a black key in pixels. */
        const val PIANO_BLACK_KEY_WIDTH = 18.0

        /** Height of a black key in pixels. */
        const val PIANO_BLACK_KEY_HEIGHT = 80.0
    }
}