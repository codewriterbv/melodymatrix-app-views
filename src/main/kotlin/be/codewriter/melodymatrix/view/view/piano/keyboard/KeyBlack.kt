package be.codewriter.melodymatrix.view.view.piano.keyboard

import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.view.piano.data.PianoConfiguration
import be.codewriter.melodymatrix.view.view.piano.keyboard.KeyboardView.Companion.PIANO_BLACK_KEY_HEIGHT
import be.codewriter.melodymatrix.view.view.piano.keyboard.KeyboardView.Companion.PIANO_BLACK_KEY_WIDTH
import javafx.scene.layout.Region
import javafx.scene.shape.Rectangle

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
    }

    /**
     * Applies the correct fill colour based on the current pressed state and configuration.
     */
    private fun applyFill() {
        key.fill = if (pressed) {
            config.pianoBlackKeyActiveColor.value
        } else {
            config.pianoBlackKeyColor.value
        }
    }
}