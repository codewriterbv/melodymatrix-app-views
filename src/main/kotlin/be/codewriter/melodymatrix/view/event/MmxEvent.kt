package be.codewriter.melodymatrix.view.event

/**
 * Base interface for MelodyMatrix events.
 *
 * All events in the MelodyMatrix system must implement this interface.
 * It provides common event metadata such as timestamp and event type.
 *
 * @see MmxEventType
 */
interface MmxEvent {
    /** The timestamp when the event was created, in milliseconds since epoch */
    val timestamp: Long

    /** The type of event this represents */
    val type: MmxEventType
}
