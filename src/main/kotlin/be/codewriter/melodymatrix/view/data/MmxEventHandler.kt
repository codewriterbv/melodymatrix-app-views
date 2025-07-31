package be.codewriter.melodymatrix.view.data

interface MmxEventHandler {
    fun onMidiData(midiData: MidiData)

    fun onPlayEvent(playEvent: PlayEvent)
}