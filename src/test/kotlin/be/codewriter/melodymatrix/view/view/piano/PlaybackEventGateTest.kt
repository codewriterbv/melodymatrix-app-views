package be.codewriter.melodymatrix.view.view.piano

import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlaybackEventGateTest {

    @Test
    fun `suppresses rising blocks while playback is active`() {
        val gate = PlaybackEventGate()

        assertTrue(gate.shouldRenderRisingBlocks())
        assertTrue(gate.onPlayEvent())
        assertFalse(gate.shouldRenderRisingBlocks())

        // Additional PLAY events stay in playback mode and are not new transitions.
        assertFalse(gate.onPlayEvent())
        assertFalse(gate.shouldRenderRisingBlocks())
    }

    @Test
    fun `re-enables rising blocks after playback stop`() {
        val gate = PlaybackEventGate()

        gate.onPlayEvent()
        gate.onPlaybackStop()

        assertTrue(gate.shouldRenderRisingBlocks())
        assertTrue(gate.onPlayEvent())
    }
}

