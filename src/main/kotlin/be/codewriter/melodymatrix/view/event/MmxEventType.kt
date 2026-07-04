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
    CHORD,

    /** Live audio spectrum event type carrying FFT bin magnitudes */
    AUDIO_SPECTRUM,

    /**
     * Signals that the current recording playback has been stopped/cancelled.
     * Views should discard any pre-scheduled playback state (e.g. falling note blocks).
     */
    PLAYBACK_STOP
    }
