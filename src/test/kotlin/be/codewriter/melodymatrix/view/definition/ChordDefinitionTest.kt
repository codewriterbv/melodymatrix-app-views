package be.codewriter.melodymatrix.view.definition

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ChordDefinitionTest {

    @Test
    fun `from returns triad for pitch class and quality with default extension`() {
        val chord = Chord.from(0, ChordQuality.MAJOR)

        assertEquals(Chord.C_MAJOR, chord)
        assertEquals(ChordExtension.NONE, chord.extension)
        assertEquals(ChordAlteration.NONE, chord.alteration)
    }

    @Test
    fun `from returns dominant seventh with no alteration when extension is minor seventh`() {
        val chord = Chord.from(0, ChordQuality.DOMINANT, ChordExtension.MINOR_SEVENTH)

        assertEquals(Chord.C_DOMINANT_SEVENTH, chord)
        assertEquals(ChordAlteration.NONE, chord.alteration)
    }

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

