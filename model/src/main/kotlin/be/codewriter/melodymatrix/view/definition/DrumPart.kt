package be.codewriter.melodymatrix.view.definition

/**
 * Midi values for drum parts based on:
 * https://qsrdrums.com/webhelp-responsive/References/r_general_midi_drum_kit.html
 */
enum class DrumPart(
    val title: String,
    val isCymbal: Boolean = false,
    val primaryNote: Int,
    val alternativeNotes: List<Int>
) {
    BELL("Bell", false, 53, listOf(56, 85)),
    HI_HAT_OPEN("Hi-hat (Open)", true, 46, listOf(26)),
    HI_HAT_CLOSED("Hi-hat (Closed)", true, 42, listOf(44, 22)),
    CRASH("Crash", true, 49, listOf(52, 55, 57)),
    RIDE("Ride", true, 51, listOf(59, 81)),
    SNARE("Snare", false, 38, listOf(37, 40, 39)),
    KICK("Kick", false, 36, listOf(35)),
    LOW_FLOOR_TOM("Low floor tom", false, 41, listOf()),
    HIGH_FLOOR_TOM("High floor tom", false, 43, listOf()),
    LOW_TOM("Low tom", false, 45, listOf(47)),
    HIGH_TOM("High tom", false, 50, listOf(48, 60));

    val notes: List<Int>
        get() = listOf(primaryNote) + alternativeNotes

    infix fun byNote(note: Note): DrumPart {
        return entries.firstOrNull { it.notes.contains(note.byteValue) }
            ?: throw IllegalArgumentException("No drum part found for note ${note.byteValue}")
    }

    fun note(): Note {
        return Note.from(notes.first())
    }

    companion object {
        fun byNote(note: Note): DrumPart {
            return entries.firstOrNull { it.notes.contains(note.byteValue) }
                ?: throw IllegalArgumentException("No drum part found for note ${note.byteValue}")
        }

        fun byValue(value: Int): DrumPart {
            return entries.firstOrNull { it.notes.contains(value) }
                ?: throw IllegalArgumentException("No drum part found for note value $value")
        }
    }
}