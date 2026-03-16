package be.codewriter.melodymatrix.view.stage.piano.keyboard

import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.stage.piano.data.PianoConfiguration
import be.codewriter.melodymatrix.view.stage.piano.keyboard.KeyboardView.Companion.PIANO_BLACK_KEY_HEIGHT
import be.codewriter.melodymatrix.view.stage.piano.keyboard.KeyboardView.Companion.PIANO_BLACK_KEY_WIDTH
import javafx.scene.layout.Region
import javafx.scene.shape.Rectangle

class KeyBlack(val config: PianoConfiguration, val note: Note, val x: Double) :
    Region(), Key {

    private var key: Rectangle = Rectangle(PIANO_BLACK_KEY_WIDTH, PIANO_BLACK_KEY_HEIGHT).apply {
        fill = config.pianoBlackKeyColor.value
    }

    private var pressed: Boolean = false

    init {
        children.add(key)

        config.pianoBlackKeyColor.addListener { _, _, _ ->
            applyFill()
        }

        config.pianoBlackKeyActiveColor.addListener { _, _, _ ->
            applyFill()
        }
    }

    override fun note(): Note {
        return note
    }

    override fun keyX(): Double {
        return x
    }

    override fun update(pressed: Boolean) {
        this.pressed = pressed
        applyFill()
    }

    private fun applyFill() {
        key.fill = if (pressed) {
            config.pianoBlackKeyActiveColor.value
        } else {
            config.pianoBlackKeyColor.value
        }
    }
}