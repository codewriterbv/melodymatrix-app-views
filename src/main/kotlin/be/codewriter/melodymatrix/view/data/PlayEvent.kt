package be.codewriter.melodymatrix.view.data

import be.codewriter.melodymatrix.view.definition.Note
import javafx.util.Duration

/**
 * A play event is based on MIDI data, and only contains notes-info. It is used as the "timeline" of a recording to
 * define which note is played, with a start time relative to the beginning of the recording, and a duration of which
 * the note is played.
 */
data class PlayEvent(
    val note: Note,
    val startTime: Long,
    val duration: Duration,
    val velocity: Int
)
