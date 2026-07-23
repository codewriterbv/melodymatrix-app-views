package be.codewriter.melodymatrix.view.view.sheet

import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.event.PlayEvent
import com.sheetmusic4j.core.model.Attributes
import com.sheetmusic4j.core.model.Chord
import com.sheetmusic4j.core.model.Clef
import com.sheetmusic4j.core.model.Duration
import com.sheetmusic4j.core.model.KeySignature
import com.sheetmusic4j.core.model.Measure
import com.sheetmusic4j.core.model.MusicElement
import com.sheetmusic4j.core.model.NoteType
import com.sheetmusic4j.core.model.Part
import com.sheetmusic4j.core.model.Pitch
import com.sheetmusic4j.core.model.Rest
import com.sheetmusic4j.core.model.Score
import com.sheetmusic4j.core.model.TimeSignature
import com.sheetmusic4j.core.model.Note as SmNote

/**
 * Adapter that translates MelodyMatrix domain types (app [Note], [PlayEvent], recordings)
 * into sheetmusic4j model instances.
 *
 * Kept as pure Kotlin (no JavaFX) so it can be unit-tested headlessly.
 */
object SheetMusicAdapter {

    /** MusicXML divisions per quarter note used by every score this adapter builds. */
    const val DIVISIONS: Int = 480

    /** MIDI number for middle C (C4), used as the treble/bass split for the grand staff. */
    const val TREBLE_MIN_MIDI: Int = 60

    private val WHOLE: Duration = Duration(4 * DIVISIONS, DIVISIONS)
    private val QUARTER: Duration = Duration(DIVISIONS, DIVISIONS)

    /** How many reference-staff notes are packed into a single 4/4 measure. */
    private const val NOTES_PER_REFERENCE_MEASURE: Int = 4

    /**
     * Convert an app [Note] to a sheetmusic4j [Pitch]. When [key] is supplied the chosen
     * enharmonic spelling follows the key signature (flats in flat keys); otherwise
     * defaults to sharps for black keys.
     */
    fun Note.toPitch(key: KeySignature? = null): Pitch =
        if (key == null) Pitch.fromMidiNumber(byteValue) else Pitch.fromMidiNumber(byteValue, key)

    /** Which staff of a grand staff (1 = treble, 2 = bass) a given MIDI number belongs to. */
    private fun staffFor(midi: Int): Int = if (midi >= TREBLE_MIN_MIDI) 1 else 2

    /**
     * Result of building a reference-staff snippet: the [Score] to hand to a [SheetView][com.sheetmusic4j.fxviewer.SheetView],
     * plus a map from every app [Note] shown to the sheetmusic4j [SmNote] instance that
     * carries it (usable as an identity-keyed highlight map key).
     */
    data class SnippetResult(
        val score: Score,
        val notesByApp: Map<Note, SmNote>
    )

    /**
     * Result of converting a recording into a strip-view score: the [Score] plus the map
     * `PlayEvent → MusicElement` that the learn view uses to highlight the exact element
     * corresponding to each timed playback event.
     */
    data class RecordingScore(
        val score: Score,
        val elementByPlayEvent: Map<PlayEvent, MusicElement>,
        val bpm: Int
    )

    /**
     * Fixed two-octave grand-staff reference score used by the new `StaffView`.
     *
     * The reference is laid out as **one continuous ascending line** from low to high
     * with no trailing pauses:
     *  - the bass (fa/F) clef plays C2..B3 (14 diatonic naturals),
     *  - then, without any "padding" rests, the treble (sol/G) clef takes over and plays
     *    C4..B5 (14 diatonic naturals).
     *
     * 14 + 14 = 28 quarter notes fit into exactly `28 / [NOTES_PER_REFERENCE_MEASURE] = 7`
     * measures. Fully-silent staves are collapsed to a single whole rest per measure
     * (measures 1..3 and 5..7); the transition measure 4 keeps 4 quarter positions on
     * each staff so bass can finish (`A3, B3`) on beats 1..2 while treble starts
     * (`C4, D4`) on beats 3..4 within the same bar.
     *
     * Sharps of the app model are **not** part of the reference score; the [StaffView]
     * draws a floating ♯ glyph next to the natural parent's notehead when a sharp is
     * pressed. See [staffElementForApp] for the parent-fallback resolution.
     */
    fun buildReferenceStaff(): SnippetResult {
        val bassScale = listOf(
            Note.C2, Note.D2, Note.E2, Note.F2, Note.G2, Note.A2, Note.B2,
            Note.C3, Note.D3, Note.E3, Note.F3, Note.G3, Note.A3, Note.B3
        )
        val trebleScale = listOf(
            Note.C4, Note.D4, Note.E4, Note.F4, Note.G4, Note.A4, Note.B4,
            Note.C5, Note.D5, Note.E5, Note.F5, Note.G5, Note.A5, Note.B5
        )
        val mapping = mutableMapOf<Note, SmNote>()

        val combinedSlots = bassScale.size + trebleScale.size
        val totalMeasures =
            (combinedSlots + NOTES_PER_REFERENCE_MEASURE - 1) / NOTES_PER_REFERENCE_MEASURE

        val measures = ArrayList<Measure>(totalMeasures)
        for (m in 0 until totalMeasures) {
            val trebleElements = ArrayList<MusicElement>(NOTES_PER_REFERENCE_MEASURE)
            val bassElements = ArrayList<MusicElement>(NOTES_PER_REFERENCE_MEASURE)

            for (slot in 0 until NOTES_PER_REFERENCE_MEASURE) {
                val combinedIndex = m * NOTES_PER_REFERENCE_MEASURE + slot
                when {
                    combinedIndex < bassScale.size -> {
                        // Bass hand still playing at this quarter slot.
                        bassElements.add(scaleStep(bassScale, combinedIndex, staff = 2, mapping))
                        trebleElements.add(quarterRest(staff = 1))
                    }

                    combinedIndex < combinedSlots -> {
                        // Treble hand plays here; bass rests.
                        val trebleIndex = combinedIndex - bassScale.size
                        trebleElements.add(scaleStep(trebleScale, trebleIndex, staff = 1, mapping))
                        bassElements.add(quarterRest(staff = 2))
                    }

                    else -> {
                        // Beyond the end of both scales (only hit if the scales don't
                        // combine to a whole number of measures — currently a no-op).
                        trebleElements.add(quarterRest(staff = 1))
                        bassElements.add(quarterRest(staff = 2))
                    }
                }
            }

            // Collapse a full measure of rests on one staff into a single whole rest so
            // the notation reads as a proper silent bar rather than four quarter rests.
            val trebleFinal =
                if (trebleElements.all { it is Rest }) listOf(wholeRest(staff = 1)) else trebleElements
            val bassFinal =
                if (bassElements.all { it is Rest }) listOf(wholeRest(staff = 2)) else bassElements

            val builder = Measure.builder(m + 1)
            if (m == 0) {
                builder.attributes(
                    Attributes.builder()
                        .divisions(DIVISIONS)
                        .staves(2)
                        .addClef(Clef.treble())
                        .addClef(Clef.bass())
                        .keySignature(KeySignature.cMajor())
                        .timeSignature(TimeSignature.fourFour())
                        .build()
                )
            }
            trebleFinal.forEach(builder::addElement)
            bassFinal.forEach(builder::addElement)
            measures.add(builder.build())
        }

        val part = Part.builder("P1").name("Piano").measures(measures).build()
        val score = Score.builder().addPart(part).build()
        return SnippetResult(score, mapping)
    }

    private fun scaleStep(
        scale: List<Note>,
        index: Int,
        staff: Int,
        mapping: MutableMap<Note, SmNote>
    ): MusicElement =
        if (index < scale.size) {
            val app = scale[index]
            val smNote = SmNote.builder()
                .pitch(app.toPitch())
                .duration(QUARTER)
                .type(NoteType.QUARTER)
                .staff(staff)
                .build()
            mapping[app] = smNote
            smNote
        } else {
            quarterRest(staff)
        }

    private fun quarterRest(staff: Int): Rest =
        Rest.builder().duration(QUARTER).type(NoteType.QUARTER).staff(staff).build()

    private fun wholeRest(staff: Int): Rest =
        Rest.builder().duration(WHOLE).type(NoteType.WHOLE).staff(staff).build()

    /**
     * Given a highlight target from an [AppNote][Note] MIDI event and the mapping produced
     * by [buildReferenceStaff], return the [SmNote] whose notehead visually represents this
     * pitch on the reference staff. Sharps fall back to their natural parent so the
     * accidental is applied through recolouring (matching the original view's "sharps share
     * the parent's position" behaviour).
     */
    fun staffElementForApp(app: Note, mapping: Map<Note, SmNote>): SmNote? {
        mapping[app]?.let { return it }
        val parent = app.parentNote ?: return null
        return mapping[parent]
    }

    /**
     * Snippet score for the ChordView: exactly one measure, grand staff, containing the
     * active notes as chords of whole notes (treble = MIDI >= 60, bass = MIDI < 60). Empty
     * staves get a whole rest. Sharps are spelled as sharps (Pitch#fromMidiNumber default).
     */
    fun buildSnippet(activeNotes: Set<Note>): SnippetResult {
        val (treble, bass) = activeNotes
            .asSequence()
            .filter { it != Note.UNDEFINED }
            .sortedBy { it.byteValue }
            .partition { it.byteValue >= TREBLE_MIN_MIDI }

        val mapping = mutableMapOf<Note, SmNote>()
        val trebleEl = chordOrRest(treble, staff = 1, mapping)
        val bassEl = chordOrRest(bass, staff = 2, mapping)

        val measure = Measure.builder(1)
            .attributes(
                Attributes.builder()
                    .divisions(DIVISIONS)
                    .staves(2)
                    .addClef(Clef.treble())
                    .addClef(Clef.bass())
                    .keySignature(KeySignature.cMajor())
                    .timeSignature(TimeSignature.fourFour())
                    .build()
            )
            .addElement(trebleEl)
            .addElement(bassEl)
            .build()

        val part = Part.builder("P1").name("Piano").addMeasure(measure).build()
        val score = Score.builder().addPart(part).build()
        return SnippetResult(score, mapping)
    }

    private fun chordOrRest(
        notes: List<Note>,
        staff: Int,
        mapping: MutableMap<Note, SmNote>
    ): MusicElement {
        if (notes.isEmpty()) {
            return Rest.builder().duration(WHOLE).type(NoteType.WHOLE).staff(staff).build()
        }
        val smNotes = notes.map { app ->
            SmNote.builder()
                .pitch(app.toPitch())
                .duration(WHOLE)
                .type(NoteType.WHOLE)
                .staff(staff)
                .build()
                .also { mapping[app] = it }
        }
        return if (smNotes.size == 1) smNotes[0] else Chord(smNotes)
    }

    /**
     * Convert a sequence of [PlayEvent]s into a sheetmusic4j [Score] laid out in 4/4,
     * grand staff. Onset and duration are quantized to the nearest sixteenth using [bpm].
     *
     * Returns:
     *  - the built [Score] (suitable for `StripSheetView.setScore`)
     *  - an identity map `PlayEvent → MusicElement` so the caller can drive per-event
     *    highlights during playback (either the [SmNote] instance or the enclosing [Chord])
     *  - the effective bpm actually used for quantization (echoes [bpm])
     *
     * This is a pragmatic first pass: notes overlapping in time on the same MIDI onset
     * are collapsed into a chord; other overlaps become simultaneous entries in the
     * measure without a formal voice split. Good enough for the "clean" studio recordings
     * shipped as learn lessons; free-play rendering is future work.
     */
    fun playEventsToScore(playEvents: List<PlayEvent>, bpm: Int = DEFAULT_BPM): RecordingScore {
        if (playEvents.isEmpty()) {
            val score = Score.builder()
                .addPart(
                    Part.builder("P1")
                        .name("Piano")
                        .addMeasure(
                            Measure.builder(1)
                                .attributes(
                                    Attributes.builder()
                                        .divisions(DIVISIONS)
                                        .staves(2)
                                        .addClef(Clef.treble())
                                        .addClef(Clef.bass())
                                        .keySignature(KeySignature.cMajor())
                                        .timeSignature(TimeSignature.fourFour())
                                        .build()
                                )
                                .addElement(Rest.builder().duration(WHOLE).type(NoteType.WHOLE).staff(1).build())
                                .addElement(Rest.builder().duration(WHOLE).type(NoteType.WHOLE).staff(2).build())
                                .build()
                        )
                        .build()
                )
                .build()
            return RecordingScore(score, emptyMap(), bpm)
        }

        val msPerQuarter = 60_000.0 / bpm
        val quantumQuarters = 0.25 // quantize to the nearest sixteenth note
        val startMs = playEvents.minOf { it.startTime }

        val slots = playEvents
            .filter { it.note != Note.UNDEFINED }
            .groupBy { evt ->
                val onsetMs = evt.startTime - startMs
                val onsetQ = quantize(onsetMs / msPerQuarter, quantumQuarters)
                onsetQ to staffFor(evt.note.byteValue)
            }
            .map { (key, evts) ->
                val (onsetQ, staff) = key
                val durQ = evts.maxOf { evt ->
                    quantize(evt.duration.toMillis() / msPerQuarter, quantumQuarters).coerceAtLeast(quantumQuarters)
                }
                Slot(onsetQ, staff, durQ, evts)
            }
            .sortedWith(compareBy({ it.onsetQuarters }, { it.staff }))

        val totalQuarters = slots.maxOfOrNull { it.onsetQuarters + it.durationQuarters } ?: 4.0
        val measureCount = maxOf(1, kotlin.math.ceil(totalQuarters / 4.0).toInt())

        val buckets = Array(measureCount) { MeasureBucket() }
        for (slot in slots) {
            val measureIdx = (slot.onsetQuarters / 4.0).toInt().coerceIn(0, measureCount - 1)
            when (slot.staff) {
                1 -> buckets[measureIdx].trebleSlots.add(slot)
                2 -> buckets[measureIdx].bassSlots.add(slot)
            }
        }

        val elementByPlayEvent = mutableMapOf<PlayEvent, MusicElement>()
        val measures = ArrayList<Measure>(measureCount)
        for (m in 0 until measureCount) {
            val builder = Measure.builder(m + 1)
            if (m == 0) {
                builder.attributes(
                    Attributes.builder()
                        .divisions(DIVISIONS)
                        .staves(2)
                        .addClef(Clef.treble())
                        .addClef(Clef.bass())
                        .keySignature(KeySignature.cMajor())
                        .timeSignature(TimeSignature.fourFour())
                        .build()
                )
            }
            appendStaffElements(builder, m, buckets[m].trebleSlots, staff = 1, elementByPlayEvent)
            appendStaffElements(builder, m, buckets[m].bassSlots, staff = 2, elementByPlayEvent)
            measures.add(builder.build())
        }

        val part = Part.builder("P1").name("Piano").measures(measures).build()
        val score = Score.builder().addPart(part).build()
        return RecordingScore(score, elementByPlayEvent, bpm)
    }

    private fun appendStaffElements(
        measureBuilder: Measure.Builder,
        measureIndex: Int,
        slots: List<Slot>,
        staff: Int,
        elementByPlayEvent: MutableMap<PlayEvent, MusicElement>
    ) {
        val measureStart = measureIndex * 4.0
        val measureEnd = measureStart + 4.0
        val ordered = slots.sortedBy { it.onsetQuarters }

        var cursor = measureStart
        for (slot in ordered) {
            val start = slot.onsetQuarters.coerceAtLeast(measureStart)
            if (start > cursor) {
                addRest(measureBuilder, start - cursor, staff)
            }
            val end = (slot.onsetQuarters + slot.durationQuarters).coerceAtMost(measureEnd)
            val duration = (end - start).coerceAtLeast(0.25)
            val element = buildSlotElement(slot, duration, staff)
            for (evt in slot.events) {
                elementByPlayEvent[evt] = element
            }
            measureBuilder.addElement(element)
            cursor = start + duration
        }
        if (cursor < measureEnd) {
            addRest(measureBuilder, measureEnd - cursor, staff)
        }
    }

    private fun buildSlotElement(slot: Slot, durationQuarters: Double, staff: Int): MusicElement {
        val notes = slot.events.distinctBy { it.note.byteValue }.map { evt ->
            SmNote.builder()
                .pitch(Pitch.fromMidiNumber(evt.note.byteValue))
                .duration(Duration.ofQuarters(durationQuarters, DIVISIONS))
                .type(NoteType.fromQuarterValue(durationQuarters))
                .staff(staff)
                .build()
        }
        return if (notes.size == 1) notes[0] else Chord(notes)
    }

    private fun addRest(builder: Measure.Builder, quarters: Double, staff: Int) {
        if (quarters <= 0) return
        builder.addElement(
            Rest.builder()
                .duration(Duration.ofQuarters(quarters, DIVISIONS))
                .type(NoteType.fromQuarterValue(quarters))
                .staff(staff)
                .build()
        )
    }

    private data class Slot(
        val onsetQuarters: Double,
        val staff: Int,
        val durationQuarters: Double,
        val events: List<PlayEvent>
    )

    private data class MeasureBucket(
        val trebleSlots: MutableList<Slot> = mutableListOf(),
        val bassSlots: MutableList<Slot> = mutableListOf()
    )

    private fun quantize(value: Double, quantum: Double): Double {
        if (quantum <= 0) return value
        return kotlin.math.round(value / quantum) * quantum
    }

    /** Default bpm used when a recording has no [Recording.sourceBpm]. */
    const val DEFAULT_BPM: Int = 100
}
