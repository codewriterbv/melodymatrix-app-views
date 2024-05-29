package be.codewriter.melodymatrix.view.data

data class MidiData(val note: Note, val isNoteOn: Boolean, val velocity: Int) {
    val NOTE_OFF = 0x80
    val NOTE_ON = 0x90

    constructor(bytes: ByteArray) : this(
        Note.from(bytes[1]),
        (bytes.size == 3) && (bytes[0].toInt() and 0x90) == 0x90 && (bytes[2] > 0),
        bytes[2].toInt()
    )
}
