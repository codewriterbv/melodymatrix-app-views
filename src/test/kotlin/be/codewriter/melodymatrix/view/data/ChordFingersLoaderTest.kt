package be.codewriter.melodymatrix.view.data

import be.codewriter.melodymatrix.view.definition.Chord
import be.codewriter.melodymatrix.view.helper.ChordFingersLoader
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class ChordFingersLoaderTest {

    @Test
    fun `returns expected first voicing for C major`() {
        val voicings = ChordFingersLoader.voicingsFor(Chord.C_MAJOR)

        assertContentEquals(intArrayOf(0, 3, 2, 0, 1, 0), voicings.first())
    }

    @Test
    fun `parses flat roots and muted strings from csv lines`() {
        val voicings = ChordFingersLoader.voicingsFor(
            Chord.A_SHARP_MAJOR,
            sequenceOf(
                "CHORD_ROOT;CHORD_TYPE;CHORD_STRUCTURE;FINGER_POSITIONS;NOTE_NAMES",
                "Bb;maj;\"1;3;5\";x,1,3,3,3,x;Bb,D,F"
            )
        )

        assertContentEquals(intArrayOf(-1, 1, 3, 3, 3, -1), voicings.single())
    }
}
