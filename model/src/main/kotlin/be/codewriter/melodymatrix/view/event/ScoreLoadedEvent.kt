package be.codewriter.melodymatrix.view.event

import com.sheetmusic4j.core.model.Score

/**
 * Signals that a full [Score] has been loaded (for example after opening a MIDI, `.mmxr`
 * or MusicXML file) and is ready to be rendered by notation-oriented viewers such as
 * [be.codewriter.melodymatrix.view.view.sheetmusic.SheetMusicView].
 *
 * @property score The sheetmusic4j score to display
 * @property recordingName Optional display name of the source recording, used for logs/UX
 * @property bpm Optional source bpm associated with the score (falls back to viewer default when null)
 * @see MmxEventType.SCORE_LOADED
 */
class ScoreLoadedEvent(
    val score: Score,
    val recordingName: String? = null,
    val bpm: Int? = null,
    override val timestamp: Long = System.currentTimeMillis()
) : MmxEvent {
    override val type: MmxEventType = MmxEventType.SCORE_LOADED
}
