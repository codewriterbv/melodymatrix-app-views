package be.codewriter.melodymatrix.view.stage.piano.component

import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.definition.PianoKeyType
import be.codewriter.melodymatrix.view.stage.piano.component.PianoGenerator.Companion.PIANO_BLACK_KEY_HEIGHT
import be.codewriter.melodymatrix.view.stage.piano.component.PianoGenerator.Companion.PIANO_WHITE_KEY_HEIGHT
import be.codewriter.melodymatrix.view.stage.piano.component.PianoGenerator.Companion.PIANO_WHITE_KEY_WIDTH
import com.almasb.fxgl.dsl.FXGL.Companion.getbp
import com.almasb.fxgl.dsl.getop
import javafx.beans.binding.Bindings
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Point2D
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class PianoKeyWhite(val note: Note, val x: Double, val y: Double) : PianoKey, Parent() {

    val keyShape: Shape
    val noteName: Label

    private val pressed = SimpleBooleanProperty(false)

    init {
        val cutOutType =
            if (note == Note.A0) PianoKeyType.RIGHT else if (note == Note.C9) PianoKeyType.NONE else note.mainNote.pianoKeyType

        val cutoutBlackWidth = PIANO_WHITE_KEY_WIDTH / 5
        val cutoutBlackHeight = PIANO_BLACK_KEY_HEIGHT

        val fullKey = Rectangle(PIANO_WHITE_KEY_WIDTH, PIANO_WHITE_KEY_HEIGHT)

        val cutoutLeft = Rectangle(cutoutBlackWidth, cutoutBlackHeight).apply {
            translateX = 0.0
        }
        val cutoutRight = Rectangle(cutoutBlackWidth, cutoutBlackHeight).apply {
            translateX = PIANO_WHITE_KEY_WIDTH - (cutoutBlackWidth)
        }

        // Cut out the pieces that are filled with a sharp key
        when (cutOutType) {
            PianoKeyType.LEFT -> {
                keyShape = Shape.subtract(fullKey, cutoutLeft)
            }

            PianoKeyType.RIGHT -> {
                keyShape = Shape.subtract(fullKey, cutoutRight)
            }

            PianoKeyType.BOTH -> {
                keyShape = Shape.subtract(Shape.subtract(fullKey, cutoutLeft), cutoutRight)
            }

            PianoKeyType.NONE -> {
                keyShape = Shape.union(fullKey, fullKey)
            }

            PianoKeyType.SHARP -> {
                // Should not end up here...
                keyShape = Rectangle()
            }
        }

        keyShape.apply {
            fillProperty().bind(
                Bindings.`when`(pressed)
                    .then(getop<ObjectProperty<*>>(PianoGenerator.PianoProperty.PIANO_WHITE_KEY_ACTIVE_COLOR.name) as ObjectProperty<Color>)
                    .otherwise(getop<ObjectProperty<*>>(PianoGenerator.PianoProperty.PIANO_WHITE_KEY_COLOR.name) as ObjectProperty<Color>)
            )
            strokeWidth = 0.5
            stroke = Color.BLACK
        }

        noteName = Label(note.name).apply {
            prefWidth = PIANO_WHITE_KEY_WIDTH
            minWidth = PIANO_WHITE_KEY_WIDTH
            maxWidth = PIANO_WHITE_KEY_WIDTH
            font = Font.font(8.0)
            textFill = Color.BLACK
            alignment = Pos.CENTER
            textAlignment = TextAlignment.CENTER
            translateY = PIANO_WHITE_KEY_HEIGHT - 20
            translateX = 0.0
            visibleProperty().bind(getbp(PianoGenerator.PianoProperty.PIANO_WHITE_KEY_NAME_VISIBLE.name))
        }

        children.addAll(keyShape, noteName)
    }

    override fun note(): Note {
        return note
    }

    override fun position(): Point2D {
        return Point2D(this.x, this.y)
    }

    override fun update(isPressed: Boolean) {
        logger.debug(
            "Updating white key {}/{}, {}/{}, {}/{}",
            note,
            noteName.text,
            x,
            y,
            keyShape.layoutX,
            keyShape.layoutY
        )

        pressed.value = isPressed
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(PianoKeyWhite::class.java.name)
    }
}