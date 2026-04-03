package be.codewriter.melodymatrix.view.view.piano.keyboard

import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.event.NoteEventListener
import be.codewriter.melodymatrix.view.view.piano.data.PianoConfiguration
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
 * @param dims   The calculated pixel dimensions for this keyboard's keys
 * @see Key
 * @see KeyWhite
 * @see KeyboardView
 */
class KeyBlack(
    val config: PianoConfiguration,
    val note: Note,
    val x: Double,
    val dims: KeyDimensions
) : Region(), Key {

    /**
     * Optional listener that is notified when this key is pressed or released via mouse interaction.
     * Set by [KeyboardView]; the viewer module never references engine types directly.
     */
    var noteEventListener: NoteEventListener? = null

    private var key: Rectangle = Rectangle(dims.blackKeyWidth, dims.blackKeyHeight).apply {
        fill = config.pianoBlackKeyColor.value
    }

    private val topHighlight = Rectangle(dims.blackKeyWidth - 2.0, dims.blackKeyHeight * 0.18).apply {
        fill = Color.WHITE
        opacity = 0.20
        translateX = 1.0
        translateY = 1.0
    }
    private val topRim = Rectangle(dims.blackKeyWidth - 1.0, 1.2).apply {
        fill = Color.color(0.95, 0.95, 0.95)
        opacity = 0.30
        translateX = 0.5
        translateY = 0.25
    }
    private val leftBevel = Rectangle(1.5, dims.blackKeyHeight).apply {
        fill = Color.WHITE
        opacity = 0.10
        translateX = 0.5
    }
    private val rightBevel = Rectangle(1.8, dims.blackKeyHeight).apply {
        fill = Color.BLACK
        opacity = 0.28
        translateX = dims.blackKeyWidth - width
    }
    private val bottomShadow = Rectangle(dims.blackKeyWidth, 8.0).apply {
        fill = Color.BLACK
        opacity = 0.34
        translateY = dims.blackKeyHeight - height
    }

    private var pressed: Boolean = false
    private var useHighContrastDepth: Boolean = false
    private var pressAnimation: Timeline? = null
    private val pressRotate = Rotate(
        0.0,
        dims.blackKeyWidth / 2.0,
        0.0,
        0.0,
        Rotate.X_AXIS
    )

    init {
        children.addAll(key, topHighlight, topRim, leftBevel, rightBevel, bottomShadow)
        transforms.add(pressRotate)

        config.pianoBlackKeyColor.addListener { _, _, _ ->
            applyFill()
        }

        config.pianoBlackKeyActiveColor.addListener { _, _, _ ->
            applyFill()
        }

        // Re-apply current visual intensities immediately when depth slider changes.
        config.pianoBlackKeyDepth.addListener { _, _, _ -> animatePress(this.pressed) }

        // Mouse interaction: give immediate visual feedback and fire the note listener.
        setOnMousePressed {
            update(true)
            noteEventListener?.onNote(note, true)
        }
        setOnMouseReleased {
            update(false)
            noteEventListener?.onNote(note, false)
        }
        // Send NOTE_OFF if the mouse leaves the key while the button is still held,
        // so notes never get stuck.
        setOnMouseExited { event ->
            if (event.isPrimaryButtonDown) {
                update(false)
                noteEventListener?.onNote(note, false)
            }
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
        val depth = config.pianoBlackKeyDepth.value.coerceIn(0.0, 2.0)
        val targetAngle = if (pressed) -12.0 * depth else 0.0

        val topRimOpacity = when {
            useHighContrastDepth && pressed -> 0.40
            useHighContrastDepth -> 0.52
            pressed -> 0.18
            else -> 0.30
        } * depth
        val topHighlightOpacity = when {
            useHighContrastDepth && pressed -> 0.22
            useHighContrastDepth -> 0.34
            pressed -> 0.10
            else -> 0.20
        } * depth
        val leftBevelOpacity = when {
            useHighContrastDepth && pressed -> 0.18
            useHighContrastDepth -> 0.26
            pressed -> 0.08
            else -> 0.10
        } * depth
        val rightBevelOpacity = when {
            useHighContrastDepth && pressed -> 0.48
            useHighContrastDepth -> 0.38
            pressed -> 0.36
            else -> 0.28
        } * depth
        val bottomShadowOpacity = when {
            useHighContrastDepth && pressed -> 0.56
            useHighContrastDepth -> 0.44
            pressed -> 0.44
            else -> 0.34
        } * depth

        pressAnimation = Timeline(
            KeyFrame(
                if (pressed) Duration.millis(50.0) else Duration.millis(110.0),
                KeyValue(
                    pressRotate.angleProperty(),
                    targetAngle,
                    if (pressed) Interpolator.EASE_OUT else Interpolator.EASE_BOTH
                ),
                KeyValue(
                    topRim.opacityProperty(),
                    topRimOpacity,
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
            topRim.fill = Color.color(0.98, 0.98, 0.98)
            leftBevel.fill = Color.color(0.80, 0.80, 0.80)
            rightBevel.fill = Color.color(0.62, 0.62, 0.62)
            bottomShadow.fill = Color.color(0.52, 0.52, 0.52)
            return
        }

        topHighlight.fill = baseColor.interpolate(Color.WHITE, 0.55)
        topRim.fill = baseColor.interpolate(Color.WHITE, 0.75)
        leftBevel.fill = baseColor.interpolate(Color.WHITE, 0.35)
        rightBevel.fill = baseColor.interpolate(Color.BLACK, 0.35)
        bottomShadow.fill = baseColor.interpolate(Color.BLACK, 0.45)
    }
}