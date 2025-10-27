package be.codewriter.melodymatrix.view.stage.piano.keyboard

import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.stage.piano.data.PianoConfiguration
import be.codewriter.melodymatrix.view.stage.piano.keyboard.KeyboardView.Companion.PIANO_BLACK_KEY_HEIGHT
import be.codewriter.melodymatrix.view.stage.piano.keyboard.KeyboardView.Companion.PIANO_BLACK_KEY_WIDTH
import javafx.geometry.Point2D
import javafx.scene.layout.Region
import javafx.scene.shape.Rectangle


class KeyBlack(val config: PianoConfiguration, val note: Note, val x: Double, val y: Double) :
    Key, Region() {

    private var key: Rectangle = Rectangle(PIANO_BLACK_KEY_WIDTH, PIANO_BLACK_KEY_HEIGHT).apply {
        fill = config.pianoBlackKeyColor.value
    }

    override fun note(): Note {
        return note
    }

    override fun position(): Point2D {
        return Point2D(this.x, this.y)
    }

    override fun update(pressed: Boolean) {
        if (pressed) {
            key.fill = config.pianoBlackKeyActiveColor.value
        } else {
            key.fill = config.pianoBlackKeyColor.value
        }
    }
}