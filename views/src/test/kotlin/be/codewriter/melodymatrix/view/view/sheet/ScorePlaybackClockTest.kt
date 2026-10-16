package be.codewriter.melodymatrix.view.view.sheet

import be.codewriter.melodymatrix.view.event.PlaybackPositionEvent
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScorePlaybackClockTest {

    @Test
    fun `cursor follows the song position after a seek`() {
        // 120 bpm: 500 ms per quarter. Playback jumped to 8 s, i.e. quarter 16 (bar 5 in 4/4).
        val afterSeek = PlaybackPositionEvent(songPositionMs = 8_000, wallClockMs = 10_000, speed = 1.0, durationMs = 60_000)
        assertEquals(16.0, ScorePlaybackClock.scoreQuarters(afterSeek, 10_000, 0, 500.0))
        assertEquals(17.0, ScorePlaybackClock.scoreQuarters(afterSeek, 10_500, 0, 500.0))
    }

    @Test
    fun `slower playback moves the cursor slower through the score`() {
        val halfSpeed = PlaybackPositionEvent(songPositionMs = 0, wallClockMs = 0, speed = 0.5, durationMs = 60_000)
        assertEquals(1.0, ScorePlaybackClock.scoreQuarters(halfSpeed, 1_000, 0, 500.0))
    }

    @Test
    fun `a score derived from the notes starts at the first note`() {
        // First note 2 s into the recording: at song position 3 s the cursor is 1 s into the score.
        val position = PlaybackPositionEvent(songPositionMs = 3_000, wallClockMs = 0, speed = 1.0, durationMs = 60_000)
        assertEquals(2.0, ScorePlaybackClock.scoreQuarters(position, 0, 2_000, 500.0))
    }

    @Test
    fun `a loaded score follows the position, also while paused`() {
        val clock = ScorePlaybackClock()
        clock.loadScore(firstOnset = 0.0, lastEnd = 32.0, bpm = 120, songOffsetMs = 0)
        assertFalse(clock.following)
        clock.onPosition(PlaybackPositionEvent(songPositionMs = 4_000, wallClockMs = 1_000, speed = 0.0, durationMs = 60_000))
        assertTrue(clock.following)
        assertEquals(8.0, clock.quartersAt(1_000))
        assertEquals(8.0, clock.quartersAt(5_000), "a paused playback does not move")
    }

    @Test
    fun `without positions the PLAY timeline drives the cursor`() {
        val clock = ScorePlaybackClock()
        assertTrue(clock.onPlay(startMs = 10_000, durationMs = 500))
        assertFalse(clock.onPlay(startMs = 10_500, durationMs = 500))
        assertEquals(0.0, clock.quartersAt(9_000), "nothing before the first note")
        // Default 100 bpm: 600 ms per quarter.
        assertEquals(1.0, clock.quartersAt(10_600), 1e-9)
        clock.reset()
        assertFalse(clock.following)
    }

    @Test
    fun `the PLAY timeline sets the tempo of a loaded score`() {
        val clock = ScorePlaybackClock()
        clock.loadScore(firstOnset = 0.0, lastEnd = 4.0, bpm = null, songOffsetMs = 0)
        clock.onPlay(startMs = 0, durationMs = 1_000)
        clock.onPlay(startMs = 1_000, durationMs = 1_000)
        // Four quarters played in 2 s: 500 ms per quarter.
        assertEquals(2.0, clock.quartersAt(1_000), 1e-9)
    }
}
