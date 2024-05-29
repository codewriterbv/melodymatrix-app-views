package be.codewriter.melodymatrix.view.data

data class MidiData(val bytes: ByteArray) {
    val NOTE_OFF = 0x80
    val NOTE_ON = 0x90

    fun isNoteOn(): Boolean {
        return bytes.size == 3
                && isBitOne(bytes[0], NOTE_ON)
                && bytes[2] > 0
    }

    fun isNoteOff(): Boolean {
        return !isNoteOn()
    }

    fun note(): Note {
        if (isNoteOn() || isNoteOff()) {
            return Note.from(bytes[1])
        }
        return Note.UNDEFINED
    }

    private fun isBitOne(value: Byte, mask: Int): Boolean {
        //println((value.toInt() and 0xff).toString(2))
        //println(mask.toString(2))
        //println((value.toInt() and mask) == mask)
        return (value.toInt() and mask) == mask
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MidiData

        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }
}
