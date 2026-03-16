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

    private fun addUINode(key: Node, x: Double) {
        key.translateX = x
        children.add(key)
    }

    fun playNote(midiData: MidiData) {
        val key = keys[midiData.note] ?: return
        key.update(midiData.event == MidiEvent.NOTE_ON)
    }

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
        const val PIANO_WHITE_KEY_WIDTH = 24.66
        const val PIANO_WHITE_KEY_HEIGHT = 120.0
        const val PIANO_BLACK_KEY_WIDTH = 18.0
        const val PIANO_BLACK_KEY_HEIGHT = 80.0
    }
}