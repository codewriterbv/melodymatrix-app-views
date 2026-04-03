package be.codewriter.melodymatrix.view.view.piano.keyboard

import be.codewriter.melodymatrix.view.definition.Note

/**
 * Listener for note events triggered by interactive piano keys.
 *
 * Register an implementation on [KeyboardView] to receive callbacks whenever the user
 * physically presses or releases a key with the mouse. The implementor is responsible
 * for routing the note event to the appropriate backend (e.g. a MIDI service or simulator).
 *
 * The interface is intentionally free of any engine or transport dependencies so the
 * viewer module stays fully decoupled from the application/engine layer.
 *
 * @see KeyboardView
 * @see KeyWhite
 * @see KeyBlack
 */
fun interface NoteEventListener {
    /**
     * Called when a key's pressed state changes due to a mouse interaction.
     *
     * @param note  The [Note] associated with the key
     * @param isOn  `true` when the key is pressed (NOTE_ON), `false` when released (NOTE_OFF)
     */
    fun onNote(note: Note, isOn: Boolean)
}

