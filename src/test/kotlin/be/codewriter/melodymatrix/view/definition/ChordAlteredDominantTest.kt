package be.codewriter.melodymatrix.view.definition

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ChordAlteredDominantTest {

    @Test
    fun `from returns altered dominant seventh b5`() {
        val chord = Chord.from(
            pitchClass = 7,
            quality = ChordQuality.DOMINANT,
            extension = ChordExtension.MINOR_SEVENTH,
            alteration = ChordAlteration.FLAT_FIFTH
        )

        assertEquals(Chord.G_DOMINANT_SEVENTH_FLAT_FIFTH, chord)
    }

    @Test
    fun `from returns altered dominant seventh sharp 5`() {
        val chord = Chord.from(
            pitchClass = 10,
            quality = ChordQuality.DOMINANT,
            extension = ChordExtension.MINOR_SEVENTH,
            alteration = ChordAlteration.SHARP_FIFTH
        )

        assertEquals(Chord.A_SHARP_DOMINANT_SEVENTH_SHARP_FIFTH, chord)
    }
}

