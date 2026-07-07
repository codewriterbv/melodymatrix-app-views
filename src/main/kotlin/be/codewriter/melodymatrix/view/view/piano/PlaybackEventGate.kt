package be.codewriter.melodymatrix.view.view.piano

/**
 * Tracks whether playback reference events are currently active.
 *
 * Rising note blocks are reserved for live input, so they are suppressed while playback runs.
 */
class PlaybackEventGate {
    @Volatile
    private var playbackActive: Boolean = false

    /** Returns true when this PLAY event transitions the gate into active playback mode. */
    fun onPlayEvent(): Boolean {
        val enteringPlayback = !playbackActive
        playbackActive = true
        return enteringPlayback
    }

    fun onPlaybackStop() {
        playbackActive = false
    }

    fun shouldRenderRisingBlocks(): Boolean = !playbackActive
}

