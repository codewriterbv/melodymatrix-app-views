package be.codewriter.melodymatrix.view.data

/**
 * To understand the MIDI data format, read:
 *
 * https://www.cs.cmu.edu/~music/cmsip/readings/MIDI%20tutorial%20for%20programmers.html
 * https://www.songstuff.com/recording/article/midi-message-format/</li>
 */
open class MidiData {
    var event: MidiEvent = MidiEvent.UNDEFINED
    var note: Note = Note.UNDEFINED
    var isNoteOn: Boolean = false
    var velocity: Int = 0
    var channel: Int = 0
    var instrument: Int = 0
    var isDrum: Boolean = false
    var controllerNumber: Int = 0
    var controllerValue: Int = 0
    var pitch: Int = 0

    constructor(note: Note, isNoteOn: Boolean, velocity: Int) {
        this.note = note
        this.isNoteOn = isNoteOn
        this.velocity = velocity
    }

    /**
     * Convert the bytes to usable values, MIDI uses three bytes
     * * Status byte : 1001 CCCC - CCCC is the channel
     *    * 1001 = NOTE ON
     *    * 1000 = NOTE OFF
     *    * 1100 = SELECT INSTRUMENT
     * * Data byte 1 : 0PPP PPPP - PPP PPPP is the pitch value from 0 to 127
     * * Data byte 2 : 0VVV VVVV - VVV VVVV is the velocity value from 0 to 127
     *    * Velocity is 0 also means NOTE OFF
     */
    constructor(bytes: ByteArray) {
        // Handle notes data
        if ((bytes[0].toInt() and 0xf0) == "10010000".toInt(2) || (bytes[0].toInt() and 0xf0) == "10000000".toInt(2)) {
            this.event = MidiEvent.NOTE
            this.channel = (bytes[0].toInt() and 0x0f)
            this.note = Note.from(bytes[1])
            this.velocity = bytes[2].toInt()

            if ((bytes[0].toInt() and 0xf0) == "10010000".toInt(2) && this.velocity > 0) {
                this.isNoteOn = true
            }

            if ((bytes[0].toInt() and 0x0f) == 0x09) {
                isDrum = true
            }

            return
        }

        // Selecting instrument
        if ((bytes[0].toInt() and 0xf0) == "11000000".toInt(2)) {
            this.event = MidiEvent.SELECT_INSTRUMENT
            this.channel = (bytes[0].toInt() and 0x0f)
            this.instrument = bytes[1].toInt()
            return
        }

        // Controller
        if ((bytes[0].toInt() and 0xf0) == "10110000".toInt(2)) {
            this.event = MidiEvent.CONTROLLER
            this.channel = (bytes[0].toInt() and 0x0f)
            this.controllerNumber = bytes[1].toInt()
            this.controllerValue = bytes[2].toInt()
            return
        }

        // Pitch bend
        if ((bytes[0].toInt() and 0xf0) == "11100000".toInt(2)) {
            this.event = MidiEvent.PITCH_BEND
            this.channel = (bytes[0].toInt() and 0x0f)
            this.pitch = 0 // TODO
            return
        }
    }
}
