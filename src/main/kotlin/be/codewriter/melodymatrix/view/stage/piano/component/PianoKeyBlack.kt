package be.codewriter.melodymatrix.view.stage.piano.component

import be.codewriter.melodymatrix.view.stage.piano.component.PianoGenerator.Companion.PIANO_BLACK_KEY_HEIGHT
import be.codewriter.melodymatrix.view.stage.piano.component.PianoGenerator.Companion.PIANO_BLACK_KEY_WIDTH
import com.almasb.fxgl.dsl.getop
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Point2D
import javafx.scene.Parent
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle

class PianoKeyBlack(val note: be.codewriter.melodymatrix.view.definition.Note, val x: Double, val y: Double) :
    be.codewriter.melodymatrix.view.stage.piano.component.PianoKey, Parent() {

    private val pressed = SimpleBooleanProperty(false)

    init {
        children.add(Rectangle(PIANO_BLACK_KEY_WIDTH, PIANO_BLACK_KEY_HEIGHT).apply {
            fillProperty().bind(
                Bindings.`when`(pressed)
                    .then(getop<Color>(be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.PIANO_BLACK_KEY_ACTIVE_COLOR.name))
                    .otherwise(getop<Color>(be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.PIANO_BLACK_KEY_COLOR.name))
            )
        })
    }

    override fun note(): be.codewriter.melodymatrix.view.definition.Note {
        return note
    }

    override fun position(): Point2D {
        return Point2D(this.x, this.y)
    }

    override fun update(isPressed: Boolean/*, pianoColors: PianoColors*/) {
        pressed.value = isPressed
    }
}