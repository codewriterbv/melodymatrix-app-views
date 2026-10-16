package be.codewriter.melodymatrix.view.event

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PlaybackPositionEventTest {

    private val position = PlaybackPositionEvent(songPositionMs = 5_000, wallClockMs = 1_000, speed = 1.0, durationMs = 10_000)

    @Test
    fun `position stays put until the wall-clock anchor, e g during the preroll`() {
        assertEquals(5_000.0, position.songPositionAt(0))
        assertEquals(5_000.0, position.songPositionAt(1_000))
    }

    @Test
    fun `position advances with the speed and stops at the end`() {
        assertEquals(5_500.0, position.songPositionAt(1_500))
        assertEquals(6_000.0, position.copy(speed = 2.0).songPositionAt(1_500))
        assertEquals(5_250.0, position.copy(speed = 0.5).songPositionAt(1_500))
        assertEquals(10_000.0, position.songPositionAt(99_000))
    }
}
