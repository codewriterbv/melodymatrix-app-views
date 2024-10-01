package be.codewriter.melodymatrix.view.definition

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

enum class MidiEvent {
    NOTE_OFF,
    NOTE_ON,
    SELECT_INSTRUMENT,
    CONTROLLER,
    POLYPHONIC_ATERTOUCH,
    PITCH_BEND,
    RESET,
    UNDEFINED;

    companion object {
        private val logger: Logger = LogManager.getLogger(MidiEvent::class.java.name)
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
            } else if (bytes[0].toInt() == 0xFF) {
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