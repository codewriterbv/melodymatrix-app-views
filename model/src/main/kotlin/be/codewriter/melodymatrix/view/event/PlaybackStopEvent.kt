package be.codewriter.melodymatrix.view.event

/**
 * Signals that recording playback has been stopped (either by user action or completion).
 *
 * Views that maintain playback-derived state \u2014 e.g. `PianoWithEffectsView`'s scheduled
 * falling note blocks \u2014 should discard that state on receipt to avoid stale visuals.
 *
 * @property timestamp Wall-clock milliseconds when the stop was signalled
 *
 * @see MmxEvent
 * @see MmxEventType.PLAYBACK_STOP
 */
data class PlaybackStopEvent(
    override val timestamp: Long = System.currentTimeMillis()
) : MmxEvent {
    override val type: MmxEventType = MmxEventType.PLAYBACK_STOP
}
