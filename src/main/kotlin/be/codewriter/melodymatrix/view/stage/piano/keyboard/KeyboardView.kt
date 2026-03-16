package be.codewriter.melodymatrix.view.stage.piano.keyboard

import be.codewriter.melodymatrix.view.data.MidiData
import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.stage.piano.data.PianoConfiguration
import javafx.geometry.Point2D
import javafx.scene.Node
import javafx.scene.layout.StackPane

class KeyboardView(config: PianoConfiguration) : StackPane() {

    private val keys: MutableMap<Note, Key> = mutableMapOf()

    init {
        var counterWhiteKeys = 0
        var previousWhiteKeyX = 0.0

        Note.pianoKeys().forEach { note ->
            if (note.mainNote.isSharp) {
                val x = previousWhiteKeyX + PIANO_WHITE_KEY_WIDTH - (PIANO_BLACK_KEY_WIDTH / 2)
                val y = PIANO_BLACK_KEY_HEIGHT - PIANO_WHITE_KEY_HEIGHT
                val key = KeyBlack(config, note, x, y)
                keys[note] = key
                addUINode(key, x, y)
            } else {
                val x = counterWhiteKeys * PIANO_WHITE_KEY_WIDTH
                val y = PIANO_BLACK_KEY_HEIGHT - PIANO_WHITE_KEY_HEIGHT
                val key = KeyWhite(config, note, x, y)
                keys[note] = key
                addUINode(key, x, y)
                counterWhiteKeys++
                previousWhiteKeyX = x
            }
        }
    }

    private fun addUINode(key: Node, x: Double, y: Double) {
        key.translateX = x
        key.translateY = y
        children.add(key)
    }

    fun playNote(midiData: MidiData) {
        val key = keys[midiData.note] ?: return
        key.update(midiData.event == MidiEvent.NOTE_ON)
    }

    fun getEffectOrigin(note: Note): Point2D? {
        val key = keys[note] ?: return null
        val centerX = key.position().x + if (note.mainNote.isSharp) {
            PIANO_BLACK_KEY_WIDTH / 2
        } else {
            PIANO_WHITE_KEY_WIDTH / 2
        }
        return Point2D(centerX, key.position().y)
    }

    companion object {

        const val PIANO_WHITE_KEY_WIDTH = 24.66
        const val PIANO_WHITE_KEY_HEIGHT = 120.0
        const val PIANO_BLACK_KEY_WIDTH = 16.0
        const val PIANO_BLACK_KEY_HEIGHT = 80.0
    }
}