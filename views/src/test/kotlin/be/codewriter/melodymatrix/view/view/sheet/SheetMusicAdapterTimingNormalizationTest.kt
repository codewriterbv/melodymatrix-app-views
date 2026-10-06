package be.codewriter.melodymatrix.view.view.sheet

import java.time.Duration
import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.event.PlayEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SheetMusicAdapterTimingNormalizationTest {

    @Test
    fun `nanosecond and millisecond onsets yield same measure count`() {
        val msEvents = listOf(
            PlayEvent(Note.C4, 0L, Duration.ofMillis(500), 90),
            PlayEvent(Note.E4, 1_000L, Duration.ofMillis(500), 90),
            PlayEvent(Note.G4, 2_000L, Duration.ofMillis(500), 90),
            PlayEvent(Note.C5, 3_000L, Duration.ofMillis(500), 90)
        )

        val nsBase = 1_719_055_826_618_416_000L
        val nsEvents = listOf(
            PlayEvent(Note.C4, nsBase, Duration.ofMillis(500), 90),
            PlayEvent(Note.E4, nsBase + 1_000_000_000L, Duration.ofMillis(500), 90),
            PlayEvent(Note.G4, nsBase + 2_000_000_000L, Duration.ofMillis(500), 90),
            PlayEvent(Note.C5, nsBase + 3_000_000_000L, Duration.ofMillis(500), 90)
        )

        val msScore = SheetMusicAdapter.playEventsToScore(msEvents, bpm = 120)
        val nsScore = SheetMusicAdapter.playEventsToScore(nsEvents, bpm = 120)

        val msMeasures = msScore.score.parts().first().measures().size
        val nsMeasures = nsScore.score.parts().first().measures().size
        assertEquals(msMeasures, nsMeasures)
    }
}

