package be.codewriter.melodymatrix.view.piano.component

import be.codewriter.melodymatrix.app.data.Note
import be.codewriter.melodymatrix.view.piano.component.PianoGenerator.Companion.PIANO_BLACK_KEY_HEIGHT
import javafx.geometry.Point2D
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape

class PianoKeyBlack(val note: Note, val x: Double, val y: Double, width: Double, height: Double) : PianoKey, Parent() {

    var keyShape: Shape? = null
    var noteName: Label? = null

    init {
        val alignment = note.mainNote.pianoKeyType

        val cutoutBlackWidth = width / 5
        val cutoutBlackHeight = PIANO_BLACK_KEY_HEIGHT

        val bg = Rectangle(cutoutBlackWidth, cutoutBlackHeight)
        children.add(bg)
    }

    override fun note(): Note {
        return note
    }

    override fun position(): Point2D {
        return Point2D(this.x, this.y)
    }

    override fun update(isPressed: Boolean/*, pianoColors: PianoColors*/) {
        //keyShape!!.fill = if (pressed) pianoColors.blackKeySelected else pianoColors.blackKey
    }
}