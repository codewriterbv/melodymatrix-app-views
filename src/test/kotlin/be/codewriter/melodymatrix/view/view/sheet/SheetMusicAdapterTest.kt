package be.codewriter.melodymatrix.view.view.sheet

import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.event.PlayEvent
import be.codewriter.melodymatrix.view.view.sheet.SheetMusicAdapter.toPitch
import com.sheetmusic4j.core.model.Chord as SmChord
import com.sheetmusic4j.core.model.Note as SmNote
import com.sheetmusic4j.core.model.NoteType
import com.sheetmusic4j.core.model.Rest
import com.sheetmusic4j.core.model.Step
import javafx.util.Duration as FxDuration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Headless adapter tests: no JavaFX runtime is initialized here (only the core sheetmusic4j
 * data model is exercised), so they run reliably under the standard Surefire runner.
 */
class SheetMusicAdapterTest {

    @Test
    fun `toPitch maps middle C to C4 natural`() {
        val pitch = Note.C4.toPitch()
        assertEquals(Step.C, pitch.step())
        assertEquals(4, pitch.octave())
        assertEquals(0, pitch.alter())
    }

    @Test
    fun `toPitch maps sharps to alter=1 in the default key`() {
        val pitch = Note.C4_SHARP.toPitch()
        assertEquals(Step.C, pitch.step())
        assertEquals(4, pitch.octave())
        assertEquals(1, pitch.alter())
    }

    @Test
    fun `toPitch maps A4 to A4`() {
        val pitch = Note.A4.toPitch()
        assertEquals(Step.A, pitch.step())
        assertEquals(4, pitch.octave())
    }

    @Test
    fun `buildReferenceStaff exposes every diatonic C4-B5 and C2-B3 note`() {
        val result = SheetMusicAdapter.buildReferenceStaff()

        val trebleExpected = listOf(
            Note.C4, Note.D4, Note.E4, Note.F4, Note.G4, Note.A4, Note.B4,
            Note.C5, Note.D5, Note.E5, Note.F5, Note.G5, Note.A5, Note.B5
        )
        trebleExpected.forEach { app ->
            assertNotNull(result.notesByApp[app], "Expected mapping for $app")
        }

        val bassExpected = listOf(
            Note.C2, Note.D2, Note.E2, Note.F2, Note.G2, Note.A2, Note.B2,
            Note.C3, Note.D3, Note.E3, Note.F3, Note.G3, Note.A3, Note.B3
        )
        bassExpected.forEach { app ->
            assertNotNull(result.notesByApp[app], "Expected mapping for $app")
        }

        assertEquals(trebleExpected.size, bassExpected.size, "Both staves must have the same length")
    }

    @Test
    fun `buildReferenceStaff does not include sharps in the mapping`() {
        // Sharps are rendered as floating overlay glyphs by the StaffView, so the reference
        // score itself carries only naturals; staffElementForApp resolves any sharp to its
        // natural parent.
        val result = SheetMusicAdapter.buildReferenceStaff()
        listOf(
            Note.C4_SHARP, Note.D4_SHARP, Note.F4_SHARP, Note.G4_SHARP, Note.A4_SHARP,
            Note.C5_SHARP, Note.D5_SHARP, Note.F5_SHARP, Note.G5_SHARP, Note.A5_SHARP,
            Note.C2_SHARP, Note.D2_SHARP, Note.F2_SHARP, Note.G2_SHARP, Note.A2_SHARP,
            Note.C3_SHARP, Note.D3_SHARP, Note.F3_SHARP, Note.G3_SHARP, Note.A3_SHARP
        ).forEach { app ->
            assertNull(result.notesByApp[app], "Sharp $app must not be in the reference-staff mapping")
        }
    }

    @Test
    fun `buildReferenceStaff produces 7 measures with a bass-to-treble handoff and no trailing rests`() {
        val result = SheetMusicAdapter.buildReferenceStaff()
        val measures = result.score.parts()[0].measures()

        // 14 bass + 14 treble = 28 quarters = exactly 7 measures in 4/4.
        assertEquals(7, measures.size)

        // Measures 1..3: bass carries 4 notes each, treble is a single whole rest.
        for (i in 0 until 3) {
            val bassNotes = measures[i].elements().count { it is SmNote && it.staff() == 2 }
            val trebleNotes = measures[i].elements().count { it is SmNote && it.staff() == 1 }
            val trebleRests = measures[i].elements().filterIsInstance<Rest>().filter { it.staff() == 1 }
            assertEquals(4, bassNotes, "Bass-only measure ${i + 1} must carry 4 bass notes")
            assertEquals(0, trebleNotes, "Bass-only measure ${i + 1} must not carry treble notes")
            assertEquals(1, trebleRests.size, "Bass-only measure ${i + 1} treble must be a single whole rest")
            assertEquals(NoteType.WHOLE, trebleRests[0].type())
        }

        // Measure 4: the handoff — bass finishes with 2 notes + 2 quarter rests, treble
        // starts with 2 quarter rests + 2 notes. Both staves keep their 4 quarter slots.
        val handoff = measures[3]
        val handoffBassNotes = handoff.elements().count { it is SmNote && it.staff() == 2 }
        val handoffTrebleNotes = handoff.elements().count { it is SmNote && it.staff() == 1 }
        val handoffBassRests = handoff.elements().filterIsInstance<Rest>().filter { it.staff() == 2 }
        val handoffTrebleRests = handoff.elements().filterIsInstance<Rest>().filter { it.staff() == 1 }
        assertEquals(2, handoffBassNotes, "Handoff measure must carry 2 remaining bass notes")
        assertEquals(2, handoffTrebleNotes, "Handoff measure must carry 2 starting treble notes")
        assertEquals(2, handoffBassRests.size, "Handoff bass must have 2 quarter rests")
        assertEquals(2, handoffTrebleRests.size, "Handoff treble must have 2 quarter rests")

        // Measures 5..7: treble carries 4 notes each, bass is a single whole rest.
        for (i in 4 until 7) {
            val trebleNotes = measures[i].elements().count { it is SmNote && it.staff() == 1 }
            val bassNotes = measures[i].elements().count { it is SmNote && it.staff() == 2 }
            val bassRests = measures[i].elements().filterIsInstance<Rest>().filter { it.staff() == 2 }
            assertEquals(4, trebleNotes, "Treble-only measure ${i + 1} must carry 4 treble notes")
            assertEquals(0, bassNotes, "Treble-only measure ${i + 1} must not carry bass notes")
            assertEquals(1, bassRests.size, "Treble-only measure ${i + 1} bass must be a single whole rest")
            assertEquals(NoteType.WHOLE, bassRests[0].type())
        }

        // No rest sits at the very end of measure 7 (the last treble slot is B5, not a rest).
        val lastMeasure = measures[6]
        val lastTrebleElement = lastMeasure.elements()
            .filter { it is SmNote && it.staff() == 1 || it is Rest && it.staff() == 1 }
            .last()
        assertTrue(lastTrebleElement is SmNote, "Last treble element must be a note, not a trailing rest")
    }

    @Test
    fun `buildReferenceStaff assigns treble notes to staff 1 and bass to staff 2`() {
        val result = SheetMusicAdapter.buildReferenceStaff()

        assertEquals(1, result.notesByApp[Note.C4]!!.staff())
        assertEquals(1, result.notesByApp[Note.B5]!!.staff())
        assertEquals(2, result.notesByApp[Note.C2]!!.staff())
        assertEquals(2, result.notesByApp[Note.B3]!!.staff())
    }

    @Test
    fun `staffElementForApp falls back to the natural parent for sharps`() {
        val result = SheetMusicAdapter.buildReferenceStaff()
        // Sharps are not in the reference score, so a sharp resolves to its natural
        // parent's notehead (which is where the StaffView overlays the ♯ glyph).
        val forCSharp = SheetMusicAdapter.staffElementForApp(Note.C4_SHARP, result.notesByApp)
        assertSame(result.notesByApp[Note.C4], forCSharp)
    }

    @Test
    fun `staffElementForApp returns null when neither the note nor its parent is on the staff`() {
        val result = SheetMusicAdapter.buildReferenceStaff()
        // C7 is above the reference staff and has no parent → null
        assertNull(SheetMusicAdapter.staffElementForApp(Note.C7, result.notesByApp))
    }

    @Test
    fun `buildSnippet with no active notes yields two rests`() {
        val result = SheetMusicAdapter.buildSnippet(emptySet())
        val measure = result.score.parts()[0].measures()[0]
        assertEquals(2, measure.elements().size)
        assertTrue(measure.elements().all { it is Rest })
        assertTrue(result.notesByApp.isEmpty())
    }

    @Test
    fun `buildSnippet places middle C on treble and B3 on bass`() {
        val result = SheetMusicAdapter.buildSnippet(setOf(Note.C4, Note.B3))
        val elements = result.score.parts()[0].measures()[0].elements()

        val trebleNote = elements.filterIsInstance<SmNote>().firstOrNull { it.staff() == 1 }
        val bassNote = elements.filterIsInstance<SmNote>().firstOrNull { it.staff() == 2 }
        assertNotNull(trebleNote)
        assertNotNull(bassNote)
        assertEquals(60, trebleNote!!.pitch().toMidiNumber())
        assertEquals(59, bassNote!!.pitch().toMidiNumber())
    }

    @Test
    fun `buildSnippet groups multiple treble notes into a chord`() {
        val result = SheetMusicAdapter.buildSnippet(setOf(Note.C4, Note.E4, Note.G4))
        val elements = result.score.parts()[0].measures()[0].elements()

        val chord = elements.filterIsInstance<SmChord>().firstOrNull()
        assertNotNull(chord)
        assertEquals(3, chord!!.notes().size)
        // Bass staff should be a rest since all three notes are ≥ C4
        assertTrue(elements.any { it is Rest })
    }

    @Test
    fun `playEventsToScore returns an empty measure for no events`() {
        val result = SheetMusicAdapter.playEventsToScore(emptyList())
        val measures = result.score.parts()[0].measures()
        assertEquals(1, measures.size)
        assertTrue(result.elementByPlayEvent.isEmpty())
    }

    @Test
    fun `playEventsToScore maps each PlayEvent to a MusicElement`() {
        val events = listOf(
            PlayEvent(Note.C4, 0, FxDuration.millis(500.0), 100),
            PlayEvent(Note.E4, 500, FxDuration.millis(500.0), 100),
            PlayEvent(Note.G4, 1000, FxDuration.millis(500.0), 100)
        )
        val result = SheetMusicAdapter.playEventsToScore(events, bpm = 120)
        events.forEach { evt ->
            assertNotNull(result.elementByPlayEvent[evt], "Expected mapping for $evt")
        }
    }

    @Test
    fun `playEventsToScore collapses simultaneous notes on the same staff into a chord`() {
        val events = listOf(
            PlayEvent(Note.C4, 0, FxDuration.millis(500.0), 100),
            PlayEvent(Note.E4, 0, FxDuration.millis(500.0), 100),
            PlayEvent(Note.G4, 0, FxDuration.millis(500.0), 100)
        )
        val result = SheetMusicAdapter.playEventsToScore(events, bpm = 120)
        val elements = result.score.parts()[0].measures()[0].elements()
        val chord = elements.filterIsInstance<SmChord>().firstOrNull()
        assertNotNull(chord)
        assertEquals(3, chord!!.notes().size)

        // All three events point at the same element.
        val target = result.elementByPlayEvent[events.first()]
        events.forEach { evt ->
            assertSame(target, result.elementByPlayEvent[evt])
        }
    }

    @Test
    fun `playEventsToScore splits notes above and below middle C onto the two staves`() {
        val events = listOf(
            PlayEvent(Note.C4, 0, FxDuration.millis(500.0), 100),   // treble
            PlayEvent(Note.B3, 0, FxDuration.millis(500.0), 100)    // bass
        )
        val result = SheetMusicAdapter.playEventsToScore(events, bpm = 120)
        val elements = result.score.parts()[0].measures()[0].elements()
        val staffNotes = elements.filterIsInstance<SmNote>().associateBy { it.staff() }
        assertNotNull(staffNotes[1])
        assertNotNull(staffNotes[2])
        assertEquals(60, staffNotes[1]!!.pitch().toMidiNumber())
        assertEquals(59, staffNotes[2]!!.pitch().toMidiNumber())
    }

    @Test
    fun `playEventsToScore emits enough measures for a multi-measure recording`() {
        // At 120 bpm one measure of 4/4 is 2 seconds; three notes 2 seconds apart span
        // roughly three measures.
        val events = (0..2).map { i ->
            PlayEvent(Note.C4, i * 2000L, FxDuration.millis(500.0), 100)
        }
        val result = SheetMusicAdapter.playEventsToScore(events, bpm = 120)
        val measures = result.score.parts()[0].measures().size
        assertTrue(measures >= 3, "Expected at least 3 measures, got $measures")
    }

    @Test
    fun `playEventsToScore ignores undefined app notes`() {
        val events = listOf(
            PlayEvent(Note.UNDEFINED, 0, FxDuration.millis(500.0), 100)
        )
        val result = SheetMusicAdapter.playEventsToScore(events, bpm = 120)
        assertFalse(result.elementByPlayEvent.containsKey(events[0]))
    }
}
