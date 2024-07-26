package be.codewriter.melodymatrix.view.test

interface EventHandler {
    fun onMidiData(midiData: be.codewriter.melodymatrix.view.data.MidiData)

    fun onPlayEvent(playEvent: be.codewriter.melodymatrix.view.data.PlayEvent)
}