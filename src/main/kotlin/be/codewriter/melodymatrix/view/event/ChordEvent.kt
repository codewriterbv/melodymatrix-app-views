package be.codewriter.melodymatrix.view.event

import be.codewriter.melodymatrix.view.definition.Chord

/**
 * A high-level event representing a detected chord at a point in time.
 */
data class ChordEvent(
    val chord: Chord,
    val on: Boolean,
    override val timestamp: Long = System.currentTimeMillis()
) : MmxEvent {
    override val type: MmxEventType = MmxEventType.CHORD
}
