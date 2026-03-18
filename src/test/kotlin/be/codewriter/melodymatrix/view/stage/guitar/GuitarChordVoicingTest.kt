package be.codewriter.melodymatrix.view.stage.guitar

import be.codewriter.melodymatrix.view.definition.Chord
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class GuitarChordVoicingTest {

    @Test
    fun `uses CSV voicing for C major`() {
        val voicing = GuitarChordVoicing.forChord(Chord.C_MAJOR)

        assertContentEquals(intArrayOf(0, 3, 2, 0, 1, 0), voicing.fretsByString)
    }

    @Test
    fun `uses CSV voicing for half diminished chords`() {
        val voicing = GuitarChordVoicing.forChord(Chord.B_HALF_DIMINISHED_SEVENTH)

        assertContentEquals(intArrayOf(-1, 2, 0, 3, 4, 1), voicing.fretsByString)
    }

    @Test
    fun `uses CSV voicing for dominant sharp fifth chords`() {
        val voicing = GuitarChordVoicing.forChord(Chord.C_DOMINANT_SEVENTH_SHARP_FIFTH)

        assertContentEquals(intArrayOf(-1, 1, 2, 1, 1, -1), voicing.fretsByString)
    }
}

