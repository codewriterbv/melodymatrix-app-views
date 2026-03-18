package be.codewriter.melodymatrix.view.event

/**
 * Enumeration of event types in the MelodyMatrix system.
 *
 * @see MmxEvent
 */
enum class MmxEventType {
    /** MIDI data event type for individual note events */
    MIDI,

    /** Play/playback event type */
    PLAY,

    /** Chord detection event type */
    CHORD
}