package be.codewriter.melodymatrix.view.view.piano.keyboard

import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.view.piano.data.PianoConfiguration
import be.codewriter.melodymatrix.view.view.piano.keyboard.KeyboardView.Companion.PIANO_BLACK_KEY_HEIGHT
import be.codewriter.melodymatrix.view.view.piano.keyboard.KeyboardView.Companion.PIANO_BLACK_KEY_WIDTH
import javafx.animation.Interpolator
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.transform.Rotate
import javafx.util.Duration

/**
 * Visual representation of a black (sharp/flat) piano key.
 *
 * Rendered as a filled rectangle whose fill colour switches between
 * [PianoConfiguration.pianoBlackKeyColor] and [PianoConfiguration.pianoBlackKeyActiveColor]
 * depending on whether the key is currently pressed.
 *
 * @param config Observable configuration providing key colours
 * @param note   The musical note this key represents
 * @param x      The horizontal position of the key's left edge in keyboard coordinates
 * @see Key
 * @see KeyWhite
 * @see KeyboardView
 */
class KeyBlack(val config: PianoConfiguration, val note: Note, val x: Double) :
    Region(), Key {

    private var key: Rectangle = Rectangle(PIANO_BLACK_KEY_WIDTH, PIANO_BLACK_KEY_HEIGHT).apply {
        fill = config.pianoBlackKeyColor.value
    }

    private val topHighlight = Rectangle(PIANO_BLACK_KEY_WIDTH - 2.0, PIANO_BLACK_KEY_HEIGHT * 0.18).apply {
        fill = Color.WHITE
        opacity = 0.20
        translateX = 1.0
        translateY = 1.0
    }
    private val leftBevel = Rectangle(1.5, PIANO_BLACK_KEY_HEIGHT).apply {
        fill = Color.WHITE
        opacity = 0.10
        translateX = 0.5
    }
    private val rightBevel = Rectangle(1.8, PIANO_BLACK_KEY_HEIGHT).apply {
        fill = Color.BLACK
        opacity = 0.28
        translateX = PIANO_BLACK_KEY_WIDTH - width
    }
    private val bottomShadow = Rectangle(PIANO_BLACK_KEY_WIDTH, 8.0).apply {
        fill = Color.BLACK
        opacity = 0.34
        translateY = PIANO_BLACK_KEY_HEIGHT - height
    }

    private var pressed: Boolean = false
    private var useHighContrastDepth: Boolean = false
    private var pressAnimation: Timeline? = null
    private val pressRotate = Rotate(
        0.0,
        PIANO_BLACK_KEY_WIDTH / 2.0,
        0.0,
        0.0,
        Rotate.X_AXIS
    )

    init {
        children.addAll(key, topHighlight, leftBevel, rightBevel, bottomShadow)
        transforms.add(pressRotate)

        config.pianoBlackKeyColor.addListener { _, _, _ ->
            applyFill()
        }

        config.pianoBlackKeyActiveColor.addListener { _, _, _ ->
            applyFill()
        }
    }

    /**
     * Returns the [Note] associated with this key.
     */
    override fun note(): Note {
        return note
    }

    /**
     * Returns the X position of this key's left edge in keyboard coordinates.
     */
    override fun keyX(): Double {
        return x
    }

    /**
     * Updates the key's visual state and repaints its fill colour.
     *
     * @param pressed True to show the active (pressed) colour, false for the normal colour
     */
    override fun update(pressed: Boolean) {
        this.pressed = pressed
        applyFill()
        animatePress(pressed)
    }

    /**
     * Animates the black key with top-hinged rotation and contrast-aware bevel/shadow response.
     */
    private fun animatePress(pressed: Boolean) {
        pressAnimation?.stop()
        val targetAngle = if (pressed) -12.0 else 0.0

        val topHighlightOpacity = when {
            useHighContrastDepth && pressed -> 0.22
            useHighContrastDepth -> 0.34
            pressed -> 0.10
            else -> 0.20
        }
        val leftBevelOpacity = when {
            useHighContrastDepth && pressed -> 0.18
            useHighContrastDepth -> 0.26
            pressed -> 0.08
            else -> 0.10
        }
        val rightBevelOpacity = when {
            useHighContrastDepth && pressed -> 0.48
            useHighContrastDepth -> 0.38
            pressed -> 0.36
            else -> 0.28
        }
        val bottomShadowOpacity = when {
            useHighContrastDepth && pressed -> 0.56
            useHighContrastDepth -> 0.44
            pressed -> 0.44
            else -> 0.34
        }

        pressAnimation = Timeline(
            KeyFrame(
                if (pressed) Duration.millis(50.0) else Duration.millis(110.0),
                KeyValue(
                    pressRotate.angleProperty(),
                    targetAngle,
                    if (pressed) Interpolator.EASE_OUT else Interpolator.EASE_BOTH
                ),
                KeyValue(
                    topHighlight.opacityProperty(),
                    topHighlightOpacity,
                    if (pressed) Interpolator.EASE_OUT else Interpolator.EASE_BOTH
                ),
                KeyValue(
                    leftBevel.opacityProperty(),
                    leftBevelOpacity,
                    if (pressed) Interpolator.EASE_OUT else Interpolator.EASE_BOTH
                ),
                KeyValue(
                    rightBevel.opacityProperty(),
                    rightBevelOpacity,
                    if (pressed) Interpolator.EASE_OUT else Interpolator.EASE_BOTH
                ),
                KeyValue(
                    bottomShadow.opacityProperty(),
                    bottomShadowOpacity,
                    if (pressed) Interpolator.EASE_OUT else Interpolator.EASE_BOTH
                )
            )
        )
        pressAnimation?.play()
    }

    /**
     * Applies the correct fill colour based on the current pressed state and configuration.
     */
    private fun applyFill() {
        val baseColor = if (pressed) {
            config.pianoBlackKeyActiveColor.value
        } else {
            config.pianoBlackKeyColor.value
        }

        key.fill = baseColor
        updateDepthPalette(baseColor)
    }

    /**
     * Keeps bevel/highlight contrast visible for both bright and very dark custom key colors.
     */
    private fun updateDepthPalette(baseColor: Color) {
        useHighContrastDepth = baseColor.brightness < 0.25

        if (useHighContrastDepth) {
            // Stronger, lighter overlays for near-black themes.
            topHighlight.fill = Color.color(0.92, 0.92, 0.92)
            leftBevel.fill = Color.color(0.80, 0.80, 0.80)
            rightBevel.fill = Color.color(0.62, 0.62, 0.62)
            bottomShadow.fill = Color.color(0.52, 0.52, 0.52)
            return
        }

        topHighlight.fill = baseColor.interpolate(Color.WHITE, 0.55)
        leftBevel.fill = baseColor.interpolate(Color.WHITE, 0.35)
        rightBevel.fill = baseColor.interpolate(Color.BLACK, 0.35)
        bottomShadow.fill = baseColor.interpolate(Color.BLACK, 0.45)
    }
}