package be.codewriter.melodymatrix.view.event

import be.codewriter.melodymatrix.view.definition.Chord

/**
 * A high-level event representing a detected chord at a point in time.
 *
 * ChordEvent is generated when a chord is detected from the underlying MIDI data.
 * It indicates both the chord that was detected and whether the chord is currently
 * active (on) or has been released (off).
 *
 * @property chord The detected chord
 * @property on True if the chord is currently sounding, false if released
 * @property timestamp The time when the chord event was generated
 *
 * @see MmxEvent
 * @see Chord
 */
data class ChordEvent(
    val chord: Chord,
    val on: Boolean,
    override val timestamp: Long = System.currentTimeMillis()
) : MmxEvent {
    override val type: MmxEventType = MmxEventType.CHORD
}
