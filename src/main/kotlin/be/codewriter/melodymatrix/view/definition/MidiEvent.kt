package be.codewriter.melodymatrix.view.definition

enum class MidiEvent {
    NOTE_OFF,
    NOTE_ON,
    SELECT_INSTRUMENT,
    CONTROLLER,
    POLYPHONIC_ATERTOUCH,
    PITCH_BEND,
    UNDEFINED
}