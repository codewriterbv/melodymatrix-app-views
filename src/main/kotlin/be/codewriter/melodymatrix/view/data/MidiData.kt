package be.codewriter.melodymatrix.view.data

import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.definition.Note
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * MIDI data is the "raw" data sent between music instruments and the computer. It contains notes that are being
 * played (or stopped), but also control info like changing instruments, channel info, etc.
 *
 * To understand the MIDI data format, read:
 * https://www.cs.cmu.edu/~music/cmsip/readings/MIDI%20tutorial%20for%20programmers.html
 * https://www.songstuff.com/recording/article/midi-message-format/
 *
 * Convert the bytes to usable values, MIDI uses three bytes
 * * Status byte : 1001 CCCC - CCCC is the channel
 *    * 1001 = NOTE ON
 *    * 1000 = NOTE OFF
 *    * 1100 = SELECT INSTRUMENT
 * * Data byte 1 : 0PPP PPPP - PPP PPPP is the pitch value from 0 to 127
 * * Data byte 2 : 0VVV VVVV - VVV VVVV is the velocity value from 0 to 127
 *    * Velocity is 0 also means NOTE OFF
 */

open class MidiData(val bytes: ByteArray) {
    private val logger: Logger = LogManager.getLogger(MidiData::class.java.name)

    var event: MidiEvent = MidiEvent.UNDEFINED
    var note: Note = Note.UNDEFINED
    var velocity: Int = 0
    var channel: Int = 0
    var instrument: Int = 0
    var isDrum: Boolean = false
    var controllerNumber: Int = 0
    var controllerValue: Int = 0
    var pitch: Int = 0

    init {
        if (bytes.size < 3) {
            throw Exception("MIDI data expects three bytes")
        }

        this.event = MidiEvent.fromData(bytes)

        when (this.event) {
            MidiEvent.NOTE_ON, MidiEvent.NOTE_OFF -> {
                this.channel = (bytes[0].toInt() and 0x0f)
                this.note = Note.from(bytes[1])
                this.velocity = bytes[2].toInt()
                if ((bytes[0].toInt() and 0x0f) == 0x09) {
                    isDrum = true
                }
            }

            MidiEvent.SELECT_INSTRUMENT -> {
                this.channel = (bytes[0].toInt() and 0x0f)
                this.instrument = bytes[1].toInt()
            }

            MidiEvent.CONTROLLER -> {
                this.channel = (bytes[0].toInt() and 0x0f)
                this.controllerNumber = bytes[1].toInt()
                this.controllerValue = bytes[2].toInt()
            }

            MidiEvent.PITCH_BEND -> {
                this.channel = (bytes[0].toInt() and 0x0f)
                // Data byte 1 : 0LLL LLLL = LSB
                // Data byte 2 : 0MMM MMMM = MSB
                this.pitch =
                    ((bytes[2].toInt() and "01111111".toInt(2)) shl 7) + (bytes[1].toInt() and "01111111".toInt(2))
            }

            else -> {
                // No data to be set
            }
        }
    }
}
