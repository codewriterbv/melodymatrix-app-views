package be.codewriter.melodymatrix.view.event

import be.codewriter.melodymatrix.view.definition.Note
import javafx.util.Duration

/**
 * Represents a play event based on MIDI note data.
 *
 * A play event contains information about a note that is played during playback.
 * It is used as the "timeline" of a recording to define which note is played,
 * with a start time relative to the beginning of the recording, and a duration
 * for which the note is played.
 *
 * @property note The musical note being played
 * @property startTime The time when the note starts playing, relative to recording start (in milliseconds)
 * @property duration The duration for which the note is played
 * @property velocity The MIDI velocity value (0-127) indicating note strength
 *
 * @see MmxEvent
 * @see Note
 */

data class PlayEvent(
    val note: Note,
    val startTime: Long,
    val duration: Duration,
    val velocity: Int
) : MmxEvent {
    override val timestamp: Long
        get() = startTime
    override val type: MmxEventType = MmxEventType.PLAY
}
