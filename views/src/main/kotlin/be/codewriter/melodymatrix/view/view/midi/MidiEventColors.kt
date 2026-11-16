package be.codewriter.melodymatrix.view.view.midi

import javafx.scene.paint.Color

/**
 * Colours per MIDI message type, shared by [MidiView] and [MidiActivityView] so both
 * show the same event in the same colour.
 */
internal object MidiEventColors {
    const val NOTE_ON = "#22c55e"
    const val NOTE_OFF = "#ef4444"
    const val CONTROLLER = "#0ea5e9"
    const val INSTRUMENT = "#a855f7"
    const val PITCH_BEND = "#f59e0b"
    const val OTHER = "#94a3b8"

    /** CSS `rgba(...)` for a `#rrggbb` colour with the given [alpha]. */
    fun rgba(hex: String, alpha: Double): String {
        val c = Color.web(hex)
        return "rgba(${(c.red * 255).toInt()}, ${(c.green * 255).toInt()}, ${(c.blue * 255).toInt()}, $alpha)"
    }
}
