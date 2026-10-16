package be.codewriter.melodymatrix.view.view.learnstrip

import be.codewriter.melodymatrix.view.event.PlaybackPositionEvent
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LearnStripViewTest {

    @Test
    fun `cursor follows the song position after a seek`() {
        // 120 bpm: 500 ms per quarter. Playback jumped to 8 s, i.e. quarter 16 (bar 5 in 4/4).
        val afterSeek = PlaybackPositionEvent(songPositionMs = 8_000, wallClockMs = 10_000, speed = 1.0, durationMs = 60_000)
        assertEquals(16.0, LearnStripView.scoreQuarters(afterSeek, 10_000, 0, 500.0))
        assertEquals(17.0, LearnStripView.scoreQuarters(afterSeek, 10_500, 0, 500.0))
    }

    @Test
    fun `slower playback moves the cursor slower through the score`() {
        val halfSpeed = PlaybackPositionEvent(songPositionMs = 0, wallClockMs = 0, speed = 0.5, durationMs = 60_000)
        assertEquals(1.0, LearnStripView.scoreQuarters(halfSpeed, 1_000, 0, 500.0))
    }

    @Test
    fun `a score derived from the notes starts at the first note`() {
        // First note 2 s into the recording: at song position 3 s the cursor is 1 s into the score.
        val position = PlaybackPositionEvent(songPositionMs = 3_000, wallClockMs = 0, speed = 1.0, durationMs = 60_000)
        assertEquals(2.0, LearnStripView.scoreQuarters(position, 0, 2_000, 500.0))
    }
}
