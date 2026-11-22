package be.codewriter.melodymatrix.view.view.mood

import be.codewriter.melodymatrix.view.definition.ChordQuality
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MoodAnalyzerTest {

    @Test
    fun `silence is idle`() {
        val snapshot = MoodAnalyzer().snapshot(0)
        assertTrue(snapshot.idle)
        assertEquals(0.0, snapshot.level)
        assertNull(snapshot.pitchClassPosition)
    }

    @Test
    fun `held loud note gives a high level and a hit fades out after release`() {
        val analyzer = MoodAnalyzer()
        analyzer.noteOn(60, 127, 0)
        assertEquals(1.0, analyzer.snapshot(100).level, 1e-9)
        analyzer.noteOff(60)
        assertTrue(analyzer.snapshot(300).level in 0.1..0.9)
        assertEquals(0.0, analyzer.snapshot(5000).level)
        assertTrue(analyzer.snapshot(5000).idle)
    }

    @Test
    fun `speed counts notes in the window and density the held notes`() {
        val analyzer = MoodAnalyzer()
        listOf(60, 62, 64, 65).forEachIndexed { i, pitch -> analyzer.noteOn(pitch, 80, i * 100L) }
        val snapshot = analyzer.snapshot(500)
        assertEquals(2.0, snapshot.notesPerSecond, 1e-9)
        assertEquals(0.4, snapshot.density, 1e-9)
        assertFalse(snapshot.idle)
    }

    @Test
    fun `low notes give a low tone and high notes a high tone`() {
        val low = MoodAnalyzer().apply { noteOn(24, 100, 0) }.snapshot(10).tone
        val high = MoodAnalyzer().apply { noteOn(100, 100, 0) }.snapshot(10).tone
        assertTrue(low < 0.2 && high > 0.8, "low=$low high=$high")
    }

    @Test
    fun `mood follows the chord quality and returns to neutral when it ends`() {
        val analyzer = MoodAnalyzer()
        analyzer.chord(ChordQuality.MAJOR, true)
        assertEquals(1.0, analyzer.snapshot(0).mood)
        analyzer.chord(ChordQuality.MINOR, true)
        assertTrue(analyzer.snapshot(0).mood < 0.0)
        analyzer.chord(ChordQuality.MINOR, false)
        assertEquals(0.0, analyzer.snapshot(0).mood)
    }

    @Test
    fun `notes a fifth apart sit next to each other on the circle`() {
        assertEquals(1 / 12.0, MoodAnalyzer.circleOfFifths(67), 1e-9)
        assertEquals(0.0, MoodAnalyzer.circleOfFifths(60), 1e-9)
        val analyzer = MoodAnalyzer().apply { noteOn(60, 100, 0) }
        assertNotNull(analyzer.snapshot(10).pitchClassPosition)
    }
}
