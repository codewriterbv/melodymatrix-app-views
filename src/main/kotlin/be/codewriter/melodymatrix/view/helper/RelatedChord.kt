package be.codewriter.melodymatrix.view.helper

import be.codewriter.melodymatrix.view.definition.Chord
import be.codewriter.melodymatrix.view.definition.RelationshipType

/**
 * A single chord that is harmonically related to a given chord.
 *
 * @property chord       The related [Chord] to display.
 * @property relationship The type of harmonic relationship.
 * @property angleDeg    Placement angle in **clockwise degrees from 12 o'clock** (top = 0°).
 */
data class RelatedChord(
    val chord: Chord,
    val relationship: RelationshipType,
    val angleDeg: Double
)