package be.codewriter.melodymatrix.view.definition

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ChordTriadTest {

    @Test
    fun `from returns triad for pitch class and quality with default extension`() {
        val chord = Chord.from(0, ChordQuality.MAJOR)

        assertEquals(Chord.C_MAJOR, chord)
        assertEquals(ChordExtension.NONE, chord.extension)
        assertEquals(ChordAlteration.NONE, chord.alteration)
    }
}

