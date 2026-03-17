package be.codewriter.melodymatrix.view.stage.chord

import be.codewriter.melodymatrix.view.definition.Note
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ChordDetectorTest {

    @Test
    fun `detect C major triad`() {
        val detected = ChordDetector.detect(setOf(Note.C4, Note.E4, Note.G4))

        assertEquals(ChordQuality.MAJOR, detected?.quality)
        assertEquals("C major", detected?.label)
    }

    @Test
    fun `detect A minor triad`() {
        val detected = ChordDetector.detect(setOf(Note.A3, Note.C4, Note.E4))

        assertEquals(ChordQuality.MINOR, detected?.quality)
        assertEquals("A minor", detected?.label)
    }

    @Test
    fun `detect major inversion through grouped root priority`() {
        val detected = ChordDetector.detect(
            notes = setOf(Note.E4, Note.G4, Note.C5),
            rootPriority = listOf(4, 7, 0)
        )

        assertEquals(ChordQuality.MAJOR, detected?.quality)
        assertEquals("C major", detected?.label)
    }

    @Test
    fun `detect minor inversion through grouped root priority`() {
        val detected = ChordDetector.detect(
            notes = setOf(Note.C4, Note.E4, Note.A4),
            rootPriority = listOf(0, 4, 9)
        )

        assertEquals(ChordQuality.MINOR, detected?.quality)
        assertEquals("A minor", detected?.label)
    }

    @Test
    fun `returns null when no major or minor triad is present`() {
        val detected = ChordDetector.detect(setOf(Note.C4, Note.D4, Note.G4))

        assertNull(detected)
    }
}

