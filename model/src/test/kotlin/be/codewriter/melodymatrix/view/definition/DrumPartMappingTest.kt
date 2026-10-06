package be.codewriter.melodymatrix.view.definition

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class DrumPartMappingTest {

    @Test
    fun `all configured drum midi notes are unique across parts`() {
        val drumParts = DrumPart.entries

        val duplicateAssignments = drumParts
            .flatMap { part -> part.notes.map { note -> note to part } }
            .groupBy { (note, _) -> note }
            .mapValues { (_, pairs) -> pairs.map { it.second }.distinct() }
            .filterValues { parts -> parts.size > 1 }

        assertTrue(
            duplicateAssignments.isEmpty(),
            "Duplicate drum note assignments found: " +
                    duplicateAssignments.entries.joinToString { (note, parts) -> "$note -> ${parts.joinToString()}" }
        )
    }

    @Test
    fun `all configured drum midi notes are within valid drum range`() {
        val validDrumRange = 22..99  // General MIDI drum range
        val drumParts = DrumPart.entries
        val allNotes = drumParts.flatMap { it.notes }

        val invalidNotes = allNotes.filter { it !in validDrumRange }
        assertTrue(
            invalidNotes.isEmpty(),
            "Invalid drum MIDI notes found outside range $validDrumRange: $invalidNotes"
        )
    }

    @Test
    fun `all expected drum midi values are mapped`() {
        // Based on https://qsrdrums.com/webhelp-responsive/References/r_general_midi_drum_kit.html
        val expectedDrumNotes = setOf(
            35, 36, 37, 38, 39, 40, 41, 42, 43, 44,  // Bass Drum to Hi-Hat Foot
            45, 46, 47, 48, 49, 50, 51, 52, 53,      // Low Tom to Ride Bell
            55, 56, 57, 59,                           // Splash to Ride Cymbal 2
            81, 85                                     // Triangle Open, Castanets
        )

        val drumParts = DrumPart.entries
        val mappedNotes = drumParts.flatMap { it.notes }.toSet()
        val unmappedNotes = expectedDrumNotes - mappedNotes

        assertTrue(
            unmappedNotes.isEmpty(),
            "Expected drum MIDI notes are not mapped: $unmappedNotes"
        )
    }
}