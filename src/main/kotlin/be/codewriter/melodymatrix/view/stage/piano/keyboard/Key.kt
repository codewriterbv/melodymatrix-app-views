package be.codewriter.melodymatrix.view.stage.piano.keyboard

import be.codewriter.melodymatrix.view.definition.Note

/**
 * Represents a single key on the piano keyboard display.
 *
 * Implemented by [KeyWhite] and [KeyBlack] to provide both white and black key behaviour.
 *
 * @see KeyboardView
 * @see KeyWhite
 * @see KeyBlack
 */
interface Key {
    /**
     * Returns the musical note this key represents.
     *
     * @return The [Note] associated with this key
     */
    fun note(): Note

    /**
     * Returns the horizontal centre position of this key in scene coordinates.
     *
     * Used to determine where to spawn visual effects when the key is pressed.
     *
     * @return The X coordinate of the key's centre
     */
    fun keyX(): Double

    /**
     * Updates the visual state of the key to reflect whether it is currently pressed.
     *
     * @param pressed True to show the key as pressed, false to restore the normal appearance
     */
    fun update(pressed: Boolean)
}