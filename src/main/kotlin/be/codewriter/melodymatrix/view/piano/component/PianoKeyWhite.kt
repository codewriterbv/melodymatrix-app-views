package be.codewriter.melodymatrix.view.piano.component

import be.codewriter.melodymatrix.app.data.Note
import be.codewriter.melodymatrix.app.data.PianoKeyType
import be.codewriter.melodymatrix.view.piano.component.PianoGenerator.Companion.PIANO_BLACK_KEY_HEIGHT
import be.codewriter.melodymatrix.view.piano.component.PianoGenerator.Companion.PIANO_WHITE_KEY_HEIGHT
import be.codewriter.melodymatrix.view.piano.component.PianoGenerator.Companion.PIANO_WHITE_KEY_WIDTH
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

        val bg = Rectangle(PIANO_WHITE_KEY_WIDTH, PIANO_WHITE_KEY_HEIGHT).apply {
            fill = Color.BLACK
        }
        children.add(bg)

        val key = Rectangle(PIANO_WHITE_KEY_WIDTH, PIANO_WHITE_KEY_HEIGHT).apply {
            fill = Color.WHITE
            strokeWidth = 1.5
            stroke = Color.BLACK
        }

        val cutoutLeft = Rectangle(cutoutBlackWidth, cutoutBlackHeight)
        cutoutLeft.translateX = 0.0
        val cutoutRight = Rectangle(cutoutBlackWidth, cutoutBlackHeight)
        cutoutRight.translateX = PIANO_WHITE_KEY_WIDTH - (cutoutBlackWidth)

        // Cut out the pieces that are filled with a sharp key
        when (cutOutType) {
            PianoKeyType.LEFT -> {
                keyShape = Shape.subtract(key, cutoutLeft)
            }

            PianoKeyType.RIGHT -> {
                keyShape = Shape.subtract(key, cutoutRight)
            }

            PianoKeyType.BOTH -> {
                val intermediateShape = Shape.subtract(key, cutoutLeft)
                keyShape = Shape.subtract(intermediateShape, cutoutRight)
            }

            PianoKeyType.NONE -> {
                keyShape = Shape.union(key, key)
            }

            PianoKeyType.SHARP -> {
                // Should not end up here...
                keyShape = Rectangle()
            }
        }

        keyShape.fillProperty().bind(
            Bindings.`when`(pressed)
                .then(getop<ObjectProperty<*>>(PianoGenerator.PianoProperty.PIANO_KEY_ACTIVE_COLOR.name) as ObjectProperty<Color>)
                .otherwise(getop<ObjectProperty<*>>(PianoGenerator.PianoProperty.PIANO_KEY_COLOR.name) as ObjectProperty<Color>)
        )

        keyShape.strokeWidth = 1.5
        keyShape.stroke = Color.BLACK

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
        }
        noteName.visibleProperty().bind(getbp(PianoGenerator.PianoProperty.PIANO_KEY_NAME_VISIBLE.name))

        children.addAll(keyShape, noteName)
    }

    override fun note(): Note {
        return note
    }

    override fun position(): Point2D {
        return Point2D(this.x, this.y)
    }

    override fun update(isPressed: Boolean) {
        logger.info(
            "Updating white key {}/{}, {}/{}, {}/{}",
            note,
            noteName.text,
            x,
            y,
            keyShape.layoutX,
            keyShape.layoutY
        )
        pressed.value = isPressed

        // For testing...
        val isTesting = false

        if (isTesting) {
            noteName.textFill = if (isPressed) Color.WHITE else Color.BLACK
            noteName.translateY = if (isPressed) PIANO_WHITE_KEY_HEIGHT - 50.0 else PIANO_WHITE_KEY_HEIGHT - 20.0
            val test = Rectangle(10.0, 40.0).apply {
                fill = Color.GREEN
                strokeWidth = 1.5
                stroke = Color.BLACK
                layoutX = 0.0
                layoutY = -50.0
            }
            children.add(test)
        }
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(PianoKeyWhite::class.java.name)
    }
}