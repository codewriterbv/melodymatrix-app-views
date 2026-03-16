package be.codewriter.melodymatrix.view.stage.piano.keyboard

import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.stage.piano.data.PianoConfiguration
import be.codewriter.melodymatrix.view.stage.piano.keyboard.KeyboardView.Companion.PIANO_BLACK_KEY_HEIGHT
import be.codewriter.melodymatrix.view.stage.piano.keyboard.KeyboardView.Companion.PIANO_BLACK_KEY_WIDTH
import javafx.scene.layout.Region
import javafx.scene.shape.Rectangle

class KeyBlack(val config: PianoConfiguration, val note: Note, val x: Double) :
    Key, Region() {

    private var key: Rectangle = Rectangle(PIANO_BLACK_KEY_WIDTH, PIANO_BLACK_KEY_HEIGHT).apply {
        fill = config.pianoBlackKeyColor.value
    }

    init {
        children.add(key)
    }

    override fun note(): Note {
        return note
    }

    override fun keyX(): Double {
        return x
    }

    override fun update(pressed: Boolean) {
        if (pressed) {
            key.fill = config.pianoBlackKeyActiveColor.value
        } else {
            key.fill = config.pianoBlackKeyColor.value
        }
    }
}