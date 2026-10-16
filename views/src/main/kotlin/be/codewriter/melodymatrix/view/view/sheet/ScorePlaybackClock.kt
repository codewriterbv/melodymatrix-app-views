package be.codewriter.melodymatrix.view.view.sheet

import be.codewriter.melodymatrix.view.event.PlaybackPositionEvent

/**
 * Works out where in a score (in quarter notes) the playback is at any wall-clock moment.
 *
 * For a loaded score with [PlaybackPositionEvent]s the song position is used, so the result
 * stays in sync when the user jumps to another part of the recording, pauses or changes the
 * tempo. Hosts that send no position events (and live captures) get the position from the
 * timing of the PLAY events or of the first captured note instead.
 *
 * Plain state, no JavaFX: call it from one thread (the FX thread in the views).
 */
class ScorePlaybackClock {

    /** True once a PLAY event of the current session has been seen. */
    var playbackSeen: Boolean = false
        private set

    /** Where the running playback is, `null` when the host sends no position events or after a reset. */
    var position: PlaybackPositionEvent? = null
        private set

    /** True while a score has been loaded with [loadScore]. */
    var hasScore: Boolean = false
        private set

    /** Wall-clock ms of the first note of the current session. */
    private var timelineStartMs: Long = 0L

    /** Latest end (wall-clock ms) of the PLAY events of the current session. */
    private var timelineEndMs: Long = 0L

    /** Milliseconds per quarter note when following the PLAY timeline. */
    var msPerQuarter: Double = 60_000.0 / SheetMusicAdapter.DEFAULT_BPM

    /** Song position (ms) of the first beat of the loaded score. */
    private var songOffsetMs: Long = 0L

    /** Milliseconds per quarter of the loaded score at the recording's own tempo. */
    private var songMsPerQuarter: Double = 60_000.0 / SheetMusicAdapter.DEFAULT_BPM

    /** Onset (quarters) of the first and end of the last sounding note of the loaded score. */
    private var firstOnset: Double = 0.0
    private var lastEnd: Double = 0.0

    /** True when there is something to follow: a PLAY timeline or a playback position. */
    val following: Boolean
        get() = playbackSeen || position != null

    /**
     * A score was loaded: [firstOnset] and [lastEnd] (quarters) bound its sounding notes,
     * [bpm] is its tempo when known and [songOffsetMs] the song position of its first beat.
     */
    fun loadScore(firstOnset: Double, lastEnd: Double, bpm: Int?, songOffsetMs: Long) {
        hasScore = true
        this.firstOnset = firstOnset
        this.lastEnd = lastEnd
        bpm?.let { msPerQuarter = 60_000.0 / it.coerceAtLeast(1) }
        songMsPerQuarter = 60_000.0 / (bpm ?: SheetMusicAdapter.DEFAULT_BPM).coerceAtLeast(1)
        this.songOffsetMs = songOffsetMs
        if (playbackSeen) updateTempo()
    }

    /** Forget the loaded score (the timeline and position stay). */
    fun clearScore() {
        hasScore = false
    }

    /**
     * A PLAY event of [durationMs] at wall-clock [startMs]. The first one of a session
     * anchors the timeline.
     *
     * @return true when this was the first PLAY event of the session
     */
    fun onPlay(startMs: Long, durationMs: Long): Boolean {
        val first = !playbackSeen
        if (first) {
            playbackSeen = true
            timelineStartMs = startMs
        } else if (startMs < timelineStartMs) {
            timelineStartMs = startMs
        }
        timelineEndMs = maxOf(timelineEndMs, startMs + durationMs)
        if (hasScore) updateTempo()
        return first
    }

    /** Live capture: the session starts with the note played at wall-clock [startMs]. */
    fun startLive(startMs: Long) {
        timelineStartMs = startMs
    }

    /** Playback (re)started, paused or moved: from now on follow [event]. */
    fun onPosition(event: PlaybackPositionEvent) {
        position = event
    }

    /** Playback stopped: forget the session (a loaded score stays). */
    fun reset() {
        playbackSeen = false
        timelineStartMs = 0L
        timelineEndMs = 0L
        position = null
    }

    /** Position in the score, in quarter notes, at wall-clock time [nowMs]; never negative. */
    fun quartersAt(nowMs: Long): Double {
        val current = position
        if (hasScore && current != null) {
            return scoreQuarters(current, nowMs, songOffsetMs, songMsPerQuarter).coerceAtLeast(0.0)
        }
        val elapsedMs = nowMs - timelineStartMs
        if (elapsedMs <= 0) return 0.0
        return (if (hasScore) firstOnset else 0.0) + elapsedMs / msPerQuarter
    }

    /** Derives ms per quarter from the PLAY timeline, so a changed playback tempo is respected. */
    private fun updateTempo() {
        val quarters = lastEnd - firstOnset
        val spanMs = timelineEndMs - timelineStartMs
        if (quarters > 0 && spanMs > 0) {
            msPerQuarter = spanMs / quarters
        }
    }

    companion object {
        /**
         * Position (quarters into the score) at wall-clock time [nowMs] of a playback at
         * [position], for a score whose first beat is at [songOffsetMs] in the recording and
         * whose quarter lasts [songMsPerQuarter] at the recording's own tempo.
         */
        fun scoreQuarters(
            position: PlaybackPositionEvent,
            nowMs: Long,
            songOffsetMs: Long,
            songMsPerQuarter: Double
        ): Double = (position.songPositionAt(nowMs) - songOffsetMs) / songMsPerQuarter
    }
}
