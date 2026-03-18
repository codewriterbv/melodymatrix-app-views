package be.codewriter.melodymatrix.view.stage.piano.keyboard

import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.definition.PianoKeyType
import be.codewriter.melodymatrix.view.stage.piano.data.PianoConfiguration
import be.codewriter.melodymatrix.view.stage.piano.keyboard.KeyboardView.Companion.PIANO_BLACK_KEY_HEIGHT
import be.codewriter.melodymatrix.view.stage.piano.keyboard.KeyboardView.Companion.PIANO_WHITE_KEY_HEIGHT
import be.codewriter.melodymatrix.view.stage.piano.keyboard.KeyboardView.Companion.PIANO_WHITE_KEY_WIDTH
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment


/**
 * Visual representation of a white (natural) piano key.
 *
 * The key shape is built by subtracting rectangular cutouts from a full-height rectangle
 * to leave room for adjacent black keys. The exact cutout depends on [PianoKeyType]:
 * left cutout only, right cutout only, both, or none for the edge keys.
 *
 * Fill colour switches between [PianoConfiguration.pianoWhiteKeyColor] and
 * [PianoConfiguration.pianoWhiteKeyActiveColor] when the key is pressed.
 * An optional note-name label is displayed at the bottom of the key.
 *
 * @param config Observable configuration for colours and label visibility
 * @param note   The musical note this key represents
 * @param x      The horizontal position of the key's left edge in keyboard coordinates
 * @see Key
 * @see KeyBlack
 * @see KeyboardView
 */
class KeyWhite(val config: PianoConfiguration, val note: Note, val x: Double) :
    Key, Region() {

    private var pressed = false
    private val key: Shape
    private val noteName: Label

    init {
        val cutOutType =
            if (note == Note.A0) PianoKeyType.RIGHT else if (note == Note.C8) PianoKeyType.NONE else note.mainNote.pianoKeyType

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
                key = Shape.subtract(fullKey, cutoutLeft)
            }

            PianoKeyType.RIGHT -> {
                key = Shape.subtract(fullKey, cutoutRight)
            }

            PianoKeyType.BOTH -> {
                key = Shape.subtract(Shape.subtract(fullKey, cutoutLeft), cutoutRight)
            }

            PianoKeyType.NONE -> {
                key = Shape.union(fullKey, fullKey)
            }

            PianoKeyType.SHARP -> {
                // Should not end up here...
                key = Rectangle()
            }
        }

        key.apply {
            fill = config.pianoWhiteKeyColor.value
            strokeWidth = 0.5
            stroke = Color.BLACK
        }

        noteName = Label(note.name).apply {
            prefWidth = PIANO_WHITE_KEY_WIDTH
            minWidth = PIANO_WHITE_KEY_WIDTH
            maxWidth = PIANO_WHITE_KEY_WIDTH
            font = Font.font(8.0)
            alignment = Pos.CENTER
            textAlignment = TextAlignment.CENTER
            translateY = PIANO_WHITE_KEY_HEIGHT - 20
            translateX = 0.0
            visibleProperty().bind(config.pianoKeyNameVisible)
            textFillProperty().bind(config.pianoKeyNameColor)
        }

        children.addAll(key, noteName)

        config.pianoWhiteKeyColor.addListener { _, _, _ -> setColor() }
        config.pianoWhiteKeyActiveColor.addListener { _, _, _ -> setColor() }
    }

    override fun note(): Note {
        return note
    }

    override fun keyX(): Double {
        return x
    }

    override fun update(pressed: Boolean) {
        this.pressed = pressed
        setColor()
    }

    private fun setColor() {
        if (pressed) {
            key.fill = config.pianoWhiteKeyActiveColor.value
        } else {
            key.fill = config.pianoWhiteKeyColor.value
        }
    }
}