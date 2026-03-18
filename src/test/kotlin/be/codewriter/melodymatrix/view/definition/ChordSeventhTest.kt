package be.codewriter.melodymatrix.view.definition

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ChordSeventhTest {

    @Test
    fun `from returns dominant seventh with no alteration when extension is minor seventh`() {
        val chord = Chord.from(0, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH)

        assertEquals(Chord.C_DOMINANT_SEVENTH, chord)
        assertEquals(ChordAlteration.NONE, chord.alteration)
    }

    @Test
    fun `from returns undefined when quality extension combination does not exist`() {
        val chord = Chord.from(
            pitchClass = 9,
            quality = ChordQuality.HALF_DIMINISHED,
            extension = ChordExtension.DIMINISHED_SEVENTH
        )

        assertEquals(Chord.UNDEFINED, chord)
    }
}

