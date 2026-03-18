package be.codewriter.melodymatrix.view.definition

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * Enumeration of MIDI event types parsed from raw MIDI status bytes.
 *
 * Represents the different types of MIDI messages that can be received from a MIDI device.
 * The type is determined by inspecting the upper nibble of the first status byte.
 *
 * @see MidiDataEvent
 */
enum class MidiEvent {
    /** Note off event (key released) */
    NOTE_OFF,

    /** Note on event (key pressed with velocity > 0) */
    NOTE_ON,

    /** Program change event (select instrument) */
    SELECT_INSTRUMENT,

    /** Control change event (modulation, volume, etc.) */
    CONTROLLER,

    /** Polyphonic aftertouch event */
    POLYPHONIC_ATERTOUCH,

    /** Pitch bend event */
    PITCH_BEND,

    /** System reset event */
    RESET,

    /** Undefined or unrecognised MIDI event */
    UNDEFINED;

    companion object {
        private val logger: Logger = LogManager.getLogger(MidiEvent::class.java.name)

        /**
         * Determines the MIDI event type from raw MIDI bytes.
         *
         * Inspects the upper nibble of the status byte (first byte) to determine the event type.
         * A NOTE_ON event with velocity 0 is treated as NOTE_OFF.
         *
         * @param bytes The raw MIDI byte array (must contain at least 3 bytes)
         * @return The corresponding MidiEvent type, or UNDEFINED if unrecognised
         */
        fun fromData(bytes: ByteArray): MidiEvent {
            if ((bytes[0].toInt() and 0xf0) == "10010000".toInt(2) || (bytes[0].toInt() and 0xf0) == "10000000".toInt(2)) {
                if ((bytes[0].toInt() and 0xf0) == "10010000".toInt(2) && bytes[2].toInt() > 0) {
                    return NOTE_ON
                } else {
                    return NOTE_OFF
                }
            } else if ((bytes[0].toInt() and 0xf0) == "10100000".toInt(2)) {
                return POLYPHONIC_ATERTOUCH
            } else if ((bytes[0].toInt() and 0xf0) == "11000000".toInt(2)) {
                return SELECT_INSTRUMENT
            } else if ((bytes[0].toInt() and 0xf0) == "10110000".toInt(2)) {
                return CONTROLLER
            } else if ((bytes[0].toInt() and 0xf0) == "11100000".toInt(2)) {
                return PITCH_BEND
            } else if ((bytes[0].toInt() and 0xf0) == "11110000".toInt(2)) {
                return RESET
            }
            logger.warn(
                "Don't know how to convert Midi data with status {}, {}",
                (bytes[0].toInt() and 0xf0),
                (bytes[0].toInt() and 0xf0).toString(2)
            )
            return UNDEFINED
        }
    }
}