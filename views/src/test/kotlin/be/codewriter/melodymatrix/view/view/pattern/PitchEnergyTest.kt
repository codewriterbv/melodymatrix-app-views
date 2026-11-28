package be.codewriter.melodymatrix.view.view.pattern

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PitchEnergyTest {

    private fun levels(energy: PitchEnergy) = DoubleArray(PitchEnergy.KEYS).also { energy.copyTo(it) }

    @Test
    fun `note sets its own key to the velocity`() {
        val energy = PitchEnergy()
        energy.noteOn(21, 127)
        energy.noteOn(108, 64)
        val values = levels(energy)
        assertEquals(1.0, values[0], 1e-9)
        assertEquals(64 / 127.0, values[87], 1e-9)
        assertEquals(0.0, values[40])
    }

    @Test
    fun `notes outside the piano are ignored`() {
        val energy = PitchEnergy()
        energy.noteOn(10, 100)
        energy.noteOn(120, 100)
        assertTrue(energy.isSilent())
    }

    @Test
    fun `released key fades out, held key stays`() {
        val energy = PitchEnergy()
        energy.noteOn(60, 127)
        energy.noteOn(64, 127)
        energy.noteOff(64)
        repeat(100) { energy.step(0.1, 0.4) }
        val values = levels(energy)
        assertEquals(0.0, values[64 - PitchEnergy.LOWEST_PITCH])
        assertTrue(values[60 - PitchEnergy.LOWEST_PITCH] >= 0.35)
        assertFalse(energy.isSilent())
        energy.noteOff(60)
        repeat(100) { energy.step(0.1, 0.4) }
        assertTrue(energy.isSilent())
    }

    @Test
    fun `pooling keeps the loudest key per column`() {
        val levels = DoubleArray(PitchEnergy.KEYS)
        levels[0] = 0.3
        levels[1] = 0.8
        levels[87] = 0.5
        val out = DoubleArray(44)
        PitchEnergy.pool(levels, 44, out)
        assertEquals(0.8, out[0], 1e-9)
        assertEquals(0.5, out[43], 1e-9)
        assertEquals(0.0, out[20])
    }

    @Test
    fun `spread shares energy with neighbours and never lowers a value`() {
        val values = doubleArrayOf(0.0, 0.0, 1.0, 0.0, 0.0)
        PitchEnergy.spread(values, 5, 1.0, DoubleArray(5))
        assertEquals(1.0, values[2], 1e-9)
        assertTrue(values[1] > 0.0 && values[1] == values[3])
        assertTrue(values[0] > 0.0 && values[0] < values[1])
    }

    @Test
    fun `spread of zero changes nothing`() {
        val values = doubleArrayOf(0.0, 1.0, 0.0)
        PitchEnergy.spread(values, 3, 0.0, DoubleArray(3))
        assertEquals(listOf(0.0, 1.0, 0.0), values.toList())
    }
}
