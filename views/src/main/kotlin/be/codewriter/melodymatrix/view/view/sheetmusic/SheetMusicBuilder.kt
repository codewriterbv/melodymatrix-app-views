package be.codewriter.melodymatrix.view.view.sheetmusic

import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.event.PlayEvent
import be.codewriter.melodymatrix.view.view.sheet.SheetMusicAdapter
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
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.round

/**
 * Builds a sheetmusic4j [Score] from a rolling list of user-played [PlayEvent]s.
 *
 * Used by [SheetMusicView] to render the "score as you play" experience: notes are
 * quantized to a sixteenth-note grid using the supplied bpm, grouped by onset into
 * chords, and packed into measures according to the current time signature.
 */
internal object SheetMusicBuilder {

    private const val DIVISIONS: Int = SheetMusicAdapter.DIVISIONS
    private const val TREBLE_MIN_MIDI: Int = SheetMusicAdapter.TREBLE_MIN_MIDI

    /** Grid step for quantization, expressed in quarter notes. */
    private const val QUANTUM_QUARTERS: Double = 0.25

    /**
     * Build a live score.
     *
     * @param playEvents captured NOTE_ON→NOTE_OFF pairs so far
     * @param bpm        tempo used for ms→quarters conversion
     * @param beats      time signature numerator
     * @param beatType   time signature denominator (1, 2, 4, 8, 16)
     * @param fifths     key signature (-7 = 7 flats … +7 = 7 sharps)
     */
    fun buildLiveScore(
        playEvents: List<PlayEvent>,
        bpm: Int,
        beats: Int,
        beatType: Int,
        fifths: Int
    ): Score {
        val safeBpm = if (bpm > 0) bpm else 100
        val safeBeats = if (beats > 0) beats else 4
        val safeBeatType = if (beatType > 0) beatType else 4
        val timeSignature = TimeSignature(safeBeats, safeBeatType)
        val keySignature = KeySignature(fifths.coerceIn(-7, 7))
        val measureLengthQuarters = timeSignature.measureLengthInQuarters()
        val msPerQuarter = 60_000.0 / safeBpm

        // Quantize to the sixteenth-note grid, keeping the earliest event as t=0.
        val startMs = playEvents.minOfOrNull { it.startTime } ?: 0L
        val slots = playEvents
            .asSequence()
            .filter { it.note != Note.UNDEFINED }
            .groupBy { evt ->
                val onsetMs = evt.startTime - startMs
                val onsetQ = quantize(onsetMs / msPerQuarter, QUANTUM_QUARTERS)
                onsetQ to staffFor(evt.note.byteValue)
            }
            .map { (key, evts) ->
                val (onsetQ, staff) = key
                val duration = evts.maxOf { evt ->
                    quantize(evt.duration.toMillis() / msPerQuarter, QUANTUM_QUARTERS)
                        .coerceAtLeast(QUANTUM_QUARTERS)
                }
                Slot(onsetQ, staff, duration, evts.map { it.note })
            }
            .sortedWith(compareBy({ it.onsetQuarters }, { it.staff }))
            .toList()

        val musicalDuration = slots.maxOfOrNull { it.onsetQuarters + it.durationQuarters } ?: 0.0
        val measureCount = max(1, ceil(musicalDuration / measureLengthQuarters).toInt())

        val trebleByMeasure = Array(measureCount) { mutableListOf<Slot>() }
        val bassByMeasure = Array(measureCount) { mutableListOf<Slot>() }
        for (slot in slots) {
            val idx = (slot.onsetQuarters / measureLengthQuarters).toInt().coerceIn(0, measureCount - 1)
            when (slot.staff) {
                1 -> trebleByMeasure[idx].add(slot)
                2 -> bassByMeasure[idx].add(slot)
            }
        }

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
                        .keySignature(keySignature)
                        .timeSignature(timeSignature)
                        .build()
                )
            }
            fillStaff(
                measureBuilder = builder,
                measureIndex = m,
                measureLengthQuarters = measureLengthQuarters,
                slots = trebleByMeasure[m],
                staff = 1,
                keySignature = keySignature
            )
            fillStaff(
                measureBuilder = builder,
                measureIndex = m,
                measureLengthQuarters = measureLengthQuarters,
                slots = bassByMeasure[m],
                staff = 2,
                keySignature = keySignature
            )
            measures.add(builder.build())
        }

        val part = Part.builder("P1").name("Piano").measures(measures).build()
        return Score.builder().addPart(part).build()
    }

    private fun fillStaff(
        measureBuilder: Measure.Builder,
        measureIndex: Int,
        measureLengthQuarters: Double,
        slots: List<Slot>,
        staff: Int,
        keySignature: KeySignature
    ) {
        val measureStart = measureIndex * measureLengthQuarters
        val measureEnd = measureStart + measureLengthQuarters

        if (slots.isEmpty()) {
            addRest(measureBuilder, measureLengthQuarters, staff)
            return
        }

        val ordered = slots.sortedBy { it.onsetQuarters }
        var cursor = measureStart

        for (slot in ordered) {
            val start = slot.onsetQuarters.coerceAtLeast(measureStart)
            if (start > cursor) {
                addRest(measureBuilder, start - cursor, staff)
            }
            val end = (slot.onsetQuarters + slot.durationQuarters).coerceAtMost(measureEnd)
            val duration = (end - start).coerceAtLeast(QUANTUM_QUARTERS)
            measureBuilder.addElement(buildElement(slot, duration, staff, keySignature))
            cursor = start + duration
        }

        if (cursor < measureEnd) {
            addRest(measureBuilder, measureEnd - cursor, staff)
        }
    }

    private fun buildElement(
        slot: Slot,
        durationQuarters: Double,
        staff: Int,
        keySignature: KeySignature
    ): MusicElement {
        val distinct = slot.notes.distinctBy { it.byteValue }
        val duration = Duration.ofQuarters(durationQuarters, DIVISIONS)
        val type = NoteType.fromQuarterValue(durationQuarters)
        val smNotes = distinct.map { note ->
            SmNote.builder()
                .pitch(Pitch.fromMidiNumber(note.byteValue, keySignature))
                .duration(duration)
                .type(type)
                .staff(staff)
                .build()
        }
        return if (smNotes.size == 1) smNotes[0] else Chord(smNotes)
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

    private fun staffFor(midi: Int): Int = if (midi >= TREBLE_MIN_MIDI) 1 else 2

    private fun quantize(value: Double, quantum: Double): Double {
        if (quantum <= 0) return value
        return round(value / quantum) * quantum
    }

    private data class Slot(
        val onsetQuarters: Double,
        val staff: Int,
        val durationQuarters: Double,
        val notes: List<Note>
    )
}
