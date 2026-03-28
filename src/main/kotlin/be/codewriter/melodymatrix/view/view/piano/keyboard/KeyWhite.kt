package be.codewriter.melodymatrix.view.view.piano.keyboard

import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.definition.PianoKeyType
import be.codewriter.melodymatrix.view.view.piano.data.PianoConfiguration
import be.codewriter.melodymatrix.view.view.piano.keyboard.KeyboardView.Companion.PIANO_BLACK_KEY_HEIGHT
import be.codewriter.melodymatrix.view.view.piano.keyboard.KeyboardView.Companion.PIANO_WHITE_KEY_HEIGHT
import be.codewriter.melodymatrix.view.view.piano.keyboard.KeyboardView.Companion.PIANO_WHITE_KEY_WIDTH
import javafx.animation.Interpolator
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import javafx.util.Duration
import javafx.scene.transform.Rotate


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
    private var pressAnimation: Timeline? = null
    private val sideShadowLeft = Rectangle(2.2, PIANO_WHITE_KEY_HEIGHT).apply {
        fill = Color.BLACK
        opacity = 0.0
        translateX = 0.0
    }
    private val sideShadowRight = Rectangle(2.2, PIANO_WHITE_KEY_HEIGHT).apply {
        fill = Color.BLACK
        opacity = 0.0
        translateX = PIANO_WHITE_KEY_WIDTH - width
    }
    private val frontShadow = Rectangle(PIANO_WHITE_KEY_WIDTH, 9.0).apply {
        fill = Color.BLACK
        opacity = 0.0
        translateY = PIANO_WHITE_KEY_HEIGHT - height
    }
    private val topHighlight = Rectangle(PIANO_WHITE_KEY_WIDTH - 1.0, 7.0).apply {
        fill = Color.WHITE
        opacity = 0.20
        translateX = 0.5
        translateY = 0.8
    }
    private val pressRotate = Rotate(
        0.0,
        PIANO_WHITE_KEY_WIDTH / 2.0,
        0.0,
        0.0,
        Rotate.X_AXIS
    )

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
            font = Font.font(config.pianoKeyNameFontSize.value)
            alignment = Pos.CENTER
            textAlignment = TextAlignment.CENTER
            translateY = PIANO_WHITE_KEY_HEIGHT - 20
            translateX = 0.0
            visibleProperty().bind(config.pianoKeyNameVisible)
            textFillProperty().bind(config.pianoKeyNameColor)
        }

        children.addAll(key, topHighlight, sideShadowLeft, sideShadowRight, frontShadow, noteName)
        transforms.add(pressRotate)

        config.pianoWhiteKeyColor.addListener { _, _, _ -> setColor() }
        config.pianoWhiteKeyActiveColor.addListener { _, _, _ -> setColor() }
        // Re-apply current visual intensities immediately when depth slider changes.
        config.pianoWhiteKeyDepth.addListener { _, _, _ -> animatePress(this.pressed) }
        // Update label font size when the slider changes.
        config.pianoKeyNameFontSize.addListener { _, _, newSize ->
            noteName.font = Font.font(newSize.toDouble())
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
        setColor()
        animatePress(pressed)
    }

    /**
     * Applies the correct fill colour based on the current pressed state and configuration.
     */
    private fun setColor() {
        if (pressed) {
            key.fill = config.pianoWhiteKeyActiveColor.value
        } else {
            key.fill = config.pianoWhiteKeyColor.value
        }
    }

    /**
     * Animates the key with a top-hinged rotation and stronger shading cues to emphasize depth.
     */
    private fun animatePress(pressed: Boolean) {
        pressAnimation?.stop()
        val depth = config.pianoWhiteKeyDepth.value.coerceIn(0.0, 2.0)

        val targetAngle = if (pressed) -10.0 * depth else 0.0
        val leftShadowOpacity = if (pressed) 0.30 * depth else 0.0
        val rightShadowOpacity = if (pressed) 0.22 * depth else 0.0
        val frontShadowOpacity = if (pressed) 0.26 * depth else 0.0
        val topHighlightOpacity = if (pressed) (0.20 - (0.12 * depth)).coerceIn(0.04, 0.20) else 0.20

        pressAnimation = Timeline(
            KeyFrame(
                if (pressed) Duration.millis(55.0) else Duration.millis(130.0),
                KeyValue(
                    pressRotate.angleProperty(),
                    targetAngle,
                    if (pressed) Interpolator.EASE_OUT else Interpolator.EASE_BOTH
                ),
                KeyValue(
                    sideShadowLeft.opacityProperty(),
                    leftShadowOpacity,
                    if (pressed) Interpolator.EASE_OUT else Interpolator.EASE_BOTH
                ),
                KeyValue(
                    sideShadowRight.opacityProperty(),
                    rightShadowOpacity,
                    if (pressed) Interpolator.EASE_OUT else Interpolator.EASE_BOTH
                ),
                KeyValue(
                    frontShadow.opacityProperty(),
                    frontShadowOpacity,
                    if (pressed) Interpolator.EASE_OUT else Interpolator.EASE_BOTH
                ),
                KeyValue(
                    topHighlight.opacityProperty(),
                    topHighlightOpacity,
                    if (pressed) Interpolator.EASE_OUT else Interpolator.EASE_BOTH
                )
            )
        )
        pressAnimation?.play()
    }
}