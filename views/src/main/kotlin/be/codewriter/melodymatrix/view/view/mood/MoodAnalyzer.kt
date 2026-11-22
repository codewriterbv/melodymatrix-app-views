package be.codewriter.melodymatrix.view.view.mood

import be.codewriter.melodymatrix.view.definition.ChordQuality
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Turns a stream of notes and chords into a few musical characteristics, without any JavaFX or timing
 * of its own: the caller passes the current time, so the results are reproducible in tests.
 *
 * - **level**: how loud it is right now (held notes, or the fading hits just played), 0..1
 * - **speed**: notes per second over the last [SPEED_WINDOW_MS]
 * - **tone**: average pitch of the recent notes, 0 (lowest piano key) .. 1 (highest)
 * - **density**: how many notes are held at the same time, 0..1
 * - **mood**: -1 (dark: minor, diminished) .. +1 (bright: major, augmented), taken from the last chord
 * - **pitch class position**: where the recent notes sit on the circle of fifths, so related keys get related colours
 *
 * All methods are synchronized: MIDI events and the animation timer run on different threads.
 */
class MoodAnalyzer {

    private class Hit(val timeMs: Long, val pitch: Int, val velocity: Int)

    /** The characteristics at one moment. */
    data class Snapshot(
        val level: Double,
        val notesPerSecond: Double,
        val tone: Double,
        val density: Double,
        val mood: Double,
        /** 0..1 on the circle of fifths, or `null` when no note was played recently. */
        val pitchClassPosition: Double?,
        /** `true` when nothing is held and nothing was played within [IDLE_AFTER_MS]. */
        val idle: Boolean
    )

    private val hits = ArrayDeque<Hit>()
    private val held = HashMap<Int, Int>()
    private var mood = 0.0

    @Synchronized
    fun noteOn(pitch: Int, velocity: Int, nowMs: Long) {
        if (velocity <= 0) return noteOff(pitch)
        hits.addLast(Hit(nowMs, pitch, velocity))
        held[pitch] = velocity
        trim(nowMs)
    }

    @Synchronized
    fun noteOff(pitch: Int) {
        held.remove(pitch)
    }

    @Synchronized
    fun chord(quality: ChordQuality, on: Boolean) {
        mood = if (on) moodOf(quality) else 0.0
    }

    @Synchronized
    fun reset() {
        hits.clear()
        held.clear()
        mood = 0.0
    }

    @Synchronized
    fun snapshot(nowMs: Long): Snapshot {
        trim(nowMs)

        val recentLevel = hits.lastOrNull { nowMs - it.timeMs <= LEVEL_FADE_MS }?.let { hit ->
            hit.velocity / MAX_VELOCITY * (1.0 - (nowMs - hit.timeMs).toDouble() / LEVEL_FADE_MS)
        } ?: 0.0
        val heldLevel = (held.values.maxOrNull() ?: 0) / MAX_VELOCITY
        val speed = hits.count { nowMs - it.timeMs <= SPEED_WINDOW_MS } * 1000.0 / SPEED_WINDOW_MS

        var toneSum = 0.0
        var weightSum = 0.0
        var pcX = 0.0
        var pcY = 0.0
        for (hit in hits) {
            val weight = hit.velocity / MAX_VELOCITY * (1.0 - (nowMs - hit.timeMs).toDouble() / TONE_WINDOW_MS)
            toneSum += weight * hit.pitch
            weightSum += weight
            val angle = Math.TAU * circleOfFifths(hit.pitch)
            pcX += weight * cos(angle)
            pcY += weight * sin(angle)
        }
        held.forEach { (pitch, velocity) ->
            val weight = velocity / MAX_VELOCITY
            toneSum += weight * pitch
            weightSum += weight
            val angle = Math.TAU * circleOfFifths(pitch)
            pcX += weight * cos(angle)
            pcY += weight * sin(angle)
        }

        val tone = if (weightSum > 0.0) ((toneSum / weightSum - LOWEST_PITCH) / PITCH_RANGE).coerceIn(0.0, 1.0) else 0.0
        val pitchClass = if (hypot(pcX, pcY) > 1e-6) {
            (atan2(pcY, pcX) / Math.TAU + 1.0) % 1.0
        } else null

        return Snapshot(
            level = max(recentLevel, heldLevel).coerceIn(0.0, 1.0),
            notesPerSecond = speed,
            tone = tone,
            density = min(held.size / FULL_DENSITY_NOTES, 1.0),
            mood = mood,
            pitchClassPosition = pitchClass,
            idle = held.isEmpty() && hits.none { nowMs - it.timeMs <= IDLE_AFTER_MS }
        )
    }

    private fun trim(nowMs: Long) {
        while (hits.isNotEmpty() && nowMs - hits.first().timeMs > TONE_WINDOW_MS) {
            hits.removeFirst()
        }
    }

    companion object {
        const val SPEED_WINDOW_MS = 2000L
        const val TONE_WINDOW_MS = 4000L
        const val IDLE_AFTER_MS = 3000L
        private const val LEVEL_FADE_MS = 600L
        private const val MAX_VELOCITY = 127.0
        private const val LOWEST_PITCH = 21.0
        private const val PITCH_RANGE = 87.0
        private const val FULL_DENSITY_NOTES = 10.0

        /** Position of the pitch class of [pitch] on the circle of fifths (C = 0, G = 1/12, D = 2/12, ...). */
        fun circleOfFifths(pitch: Int): Double = ((pitch % 12) * 7 % 12) / 12.0

        fun moodOf(quality: ChordQuality): Double = when (quality) {
            ChordQuality.MAJOR -> 1.0
            ChordQuality.AUGMENTED -> 0.6
            ChordQuality.DOMINANT -> 0.4
            ChordQuality.SUSPENDED_SECOND, ChordQuality.SUSPENDED_FOURTH -> 0.2
            ChordQuality.TRITONE -> -0.4
            ChordQuality.MINOR -> -0.8
            ChordQuality.HALF_DIMINISHED -> -0.9
            ChordQuality.DIMINISHED -> -1.0
        }
    }
}
