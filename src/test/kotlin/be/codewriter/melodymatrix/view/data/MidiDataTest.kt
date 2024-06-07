package be.codewriter.melodymatrix.view.data

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MidiDataTest {

    @Test
    fun `test midi not is on or off`() {
        val midiDataOn = MidiData(byteArrayOf("10010000".toInt(2).toByte(), 50, 48))
        assertTrue(midiDataOn.isNoteOn, "First test value should return note on")

        val midiDataOnAndOther = MidiData(byteArrayOf(0x91.toByte(), 0x00, 0x01))
        assertTrue(midiDataOnAndOther.isNoteOn, "First test value with extra data should return note on")
        val midiDataOff = MidiData(byteArrayOf(0x80.toByte(), 0x00, 0x00))
        assertFalse(midiDataOff.isNoteOn, "Second test value should return not note on")
        val midiDataOnNoVelocity = MidiData(byteArrayOf(0x91.toByte(), 0x00, 0x00))
        assertFalse(midiDataOnNoVelocity.isNoteOn, "Second test value with no velocity should return not note on")
    }

    @Test
    fun shouldReturnInstrumentChange() {
        val data = byteArrayOf("11000100".toInt(2).toByte(), 0x05, 0x00)
        val midi = MidiData(data)

        assertFalse(midi.isDrum, "Should return drum as false")
        assertEquals(4, midi.channel, "Should return correct channel")
        assertEquals(5, midi.instrument, "Should return correct instrument")
    }

    @Test
    fun shouldReturnDrumNoteOn() {
        val data = byteArrayOf(0x99.toByte(), 0x23, 0x40)
        val midi = MidiData(data)

        assertTrue(midi.isDrum, "Should return drum as true")
        assertTrue(midi.isNoteOn, "Should return note on true")
    }

    @Test
    fun shouldReturnDrumNoteOff() {
        val data = byteArrayOf(0x89.toByte(), 0x23, 0x00)
        val midi = MidiData(data)

        assertTrue(midi.isDrum, "Should return drum as true")
        assertFalse(midi.isNoteOn, "Should return note on as false")
    }
}