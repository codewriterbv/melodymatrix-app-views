package be.codewriter.melodymatrix.view.data

enum class MidiEvent {
    NOTE_OFF,
    NOTE_ON,
    SELECT_INSTRUMENT,
    CONTROLLER,
    PITCH_BEND,
    UNDEFINED
}