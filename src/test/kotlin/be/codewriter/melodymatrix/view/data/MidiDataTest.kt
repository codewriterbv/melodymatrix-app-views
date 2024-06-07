package be.codewriter.melodymatrix.view.data

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MidiDataTest {

    @Test
    fun `test midi not is on or off`() {
        val midi = MidiData(byteArrayOf("10010000".toInt(2).toByte(), 50, 48))
        assertEquals(MidiEvent.NOTE, midi.event, "Should return controller")
        assertTrue(midi.isNoteOn, "First test value should return note on")

        val midiDataOnAndOther = MidiData(byteArrayOf(0x91.toByte(), 0x00, 0x01))
        assertTrue(midiDataOnAndOther.isNoteOn, "First test value with extra data should return note on")
        val midiDataOff = MidiData(byteArrayOf(0x80.toByte(), 0x00, 0x00))
        assertFalse(midiDataOff.isNoteOn, "Second test value should return not note on")
        val midiDataOnNoVelocity = MidiData(byteArrayOf(0x91.toByte(), 0x00, 0x00))
        assertFalse(midiDataOnNoVelocity.isNoteOn, "Second test value with no velocity should return not note on")
    }

    @Test
    fun `test instrument change`() {
        val data = byteArrayOf("11000100".toInt(2).toByte(), 0x05, 0x00)
        val midi = MidiData(data)

        assertEquals(MidiEvent.SELECT_INSTRUMENT, midi.event, "Should return controller")
        assertFalse(midi.isDrum, "Should return drum as false")
        assertEquals(4, midi.channel, "Should return correct channel")
        assertEquals(5, midi.instrument, "Should return correct instrument")
    }

    @Test
    fun `test if drum note is recognized and note is on`() {
        val data = byteArrayOf(0x99.toByte(), 0x23, 0x40)
        val midi = MidiData(data)

        assertEquals(MidiEvent.NOTE, midi.event, "Should return controller")
        assertTrue(midi.isDrum, "Should return drum as true")
        assertTrue(midi.isNoteOn, "Should return note on true")
    }

    @Test
    fun `test if drum note is recognized and note is off`() {
        val data = byteArrayOf(0x89.toByte(), 0x23, 0x00)
        val midi = MidiData(data)

        assertEquals(MidiEvent.NOTE, midi.event, "Should return controller")
        assertTrue(midi.isDrum, "Should return drum as true")
        assertFalse(midi.isNoteOn, "Should return note on as false")
    }

    @Test
    fun `test if controller data is correct`() {
        val data = byteArrayOf("10110010".toInt(2).toByte(), 64, 99)
        val midi = MidiData(data)

        assertEquals(MidiEvent.CONTROLLER, midi.event, "Should return controller")
        assertFalse(midi.isDrum, "Should return drum as false")
        assertEquals(2, midi.channel, "Should return correct channel")
        assertEquals(64, midi.controllerNumber, "Should return correct controller number")
        assertEquals(99, midi.controllerValue, "Should return correct controller value")
    }

    @Test
    fun `test if pitch bend data is correct`() {
        val data = byteArrayOf("11100010".toInt(2).toByte(), 0x00, 0x60)
        val midi = MidiData(data)

        assertEquals(MidiEvent.PITCH_BEND, midi.event, "Should return controller")
        assertFalse(midi.isDrum, "Should return drum as false")
        // TODO assertEquals(0x3000, midi.pitch, "Should return correct pitch value")
    }
}