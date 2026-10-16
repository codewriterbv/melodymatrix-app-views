package be.codewriter.melodymatrix.view.event

/**
 * Signals where a recording playback is: at wall-clock time [wallClockMs] the playback is at
 * [songPositionMs] and from then on advances [speed] times as fast as real time. Sent each
 * time playback (re)starts — on start, after a seek and when a loop begins — right before its
 * [PlayEvent]s.
 *
 * Views that follow the playback with a cursor compute the song position at any moment with
 * [songPositionAt], so they stay in sync when the user jumps to another part or changes the
 * tempo. During the preroll [wallClockMs] lies in the future and the position stays put.
 *
 * @property songPositionMs Position in the recording, in milliseconds from its first event,
 *                          at the recording's own tempo (not scaled by [speed])
 * @property wallClockMs    [System.currentTimeMillis] at which the playback is at [songPositionMs]
 * @property speed          Playback speed multiplier (1.0 = original tempo)
 * @property durationMs     Length of the recording, in the same clock as [songPositionMs]
 *
 * @see MmxEventType.PLAYBACK_POSITION
 */
data class PlaybackPositionEvent(
    val songPositionMs: Long,
    val wallClockMs: Long,
    val speed: Double,
    val durationMs: Long,
    override val timestamp: Long = System.currentTimeMillis()
) : MmxEvent {
    override val type: MmxEventType = MmxEventType.PLAYBACK_POSITION

    /** Song position (ms) at wall-clock time [nowMs], never past [durationMs]. */
    fun songPositionAt(nowMs: Long): Double {
        if (nowMs <= wallClockMs) return songPositionMs.toDouble()
        return (songPositionMs + (nowMs - wallClockMs) * speed).coerceAtMost(durationMs.toDouble())
    }
}
