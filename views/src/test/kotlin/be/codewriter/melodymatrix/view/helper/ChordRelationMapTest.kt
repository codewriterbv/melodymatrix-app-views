package be.codewriter.melodymatrix.view.helper

import be.codewriter.melodymatrix.view.definition.Chord
import be.codewriter.melodymatrix.view.definition.RelationshipType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChordRelationMapTest {

    @Test
    fun `undefined chord returns no relations`() {
        val relations = ChordRelationMap.getRelatedChords(Chord.UNDEFINED)
        assertTrue(relations.isEmpty())
    }

    @Test
    fun `major chord returns expected related chords and angles`() {
        val relations = ChordRelationMap.getRelatedChords(Chord.C_MAJOR)

        assertEquals(
            listOf(
                RelationExpectation(Chord.G_MAJOR, RelationshipType.DOMINANT, 0.0),
                RelationExpectation(Chord.F_MAJOR, RelationshipType.SUBDOMINANT, 60.0),
                RelationExpectation(Chord.A_MINOR, RelationshipType.RELATIVE_MINOR, 120.0),
                RelationExpectation(Chord.C_MINOR, RelationshipType.PARALLEL, 180.0),
                RelationExpectation(Chord.D_MINOR, RelationshipType.SUPERTONIC, 240.0),
                RelationExpectation(Chord.B_DIMINISHED, RelationshipType.LEADING_TONE, 300.0)
            ),
            toExpectations(relations)
        )

        assertTrue(relations.none { it.chord == Chord.UNDEFINED })
    }

    @Test
    fun `dominant quality is handled like major`() {
        val majorRelations = toExpectations(ChordRelationMap.getRelatedChords(Chord.C_MAJOR))
        val dominantRelations = toExpectations(ChordRelationMap.getRelatedChords(Chord.C_DOMINANT_SEVENTH))

        assertEquals(majorRelations, dominantRelations)
    }

    @Test
    fun `minor chord returns expected related chords and angles`() {
        val relations = ChordRelationMap.getRelatedChords(Chord.A_MINOR)

        assertEquals(
            listOf(
                RelationExpectation(Chord.C_MAJOR, RelationshipType.RELATIVE_MAJOR, 0.0),
                RelationExpectation(Chord.E_MAJOR, RelationshipType.MAJOR_DOMINANT, 60.0),
                RelationExpectation(Chord.D_MINOR, RelationshipType.MINOR_SUBDOMINANT, 120.0),
                RelationExpectation(Chord.A_MAJOR, RelationshipType.PARALLEL, 180.0),
                RelationExpectation(Chord.B_DIMINISHED, RelationshipType.SUPERTONIC_DIM, 240.0),
                RelationExpectation(Chord.G_MAJOR, RelationshipType.FLAT_SEVENTH, 300.0)
            ),
            toExpectations(relations)
        )

        assertTrue(relations.none { it.chord == Chord.UNDEFINED })
    }

    @Test
    fun `diminished and half-diminished return same reduced relation set`() {
        val diminished = toExpectations(ChordRelationMap.getRelatedChords(Chord.C_DIMINISHED_SEVENTH))
        val halfDiminished = toExpectations(ChordRelationMap.getRelatedChords(Chord.C_HALF_DIMINISHED_SEVENTH))

        val expected = listOf(
            RelationExpectation(Chord.C_SHARP_MAJOR, RelationshipType.DOMINANT, 0.0),
            RelationExpectation(Chord.C_SHARP_MINOR, RelationshipType.DOMINANT, 60.0),
            RelationExpectation(Chord.C_MINOR, RelationshipType.PARALLEL, 180.0)
        )

        assertEquals(expected, diminished)
        assertEquals(expected, halfDiminished)
    }

    private fun toExpectations(relations: List<RelatedChord>): List<RelationExpectation> =
        relations.map { RelationExpectation(it.chord, it.relationship, it.angleDeg) }

    private data class RelationExpectation(
        val chord: Chord,
        val relationship: RelationshipType,
        val angleDeg: Double
    )
}

