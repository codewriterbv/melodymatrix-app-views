package be.codewriter.melodymatrix.view.test

import be.codewriter.melodymatrix.view.data.MidiData

interface MidiDataReceivedListener {
    fun onMidiDataReceived(midiData: MidiData)

    fun onPlayEvents(playEvents: List<PlayEvent>)
}