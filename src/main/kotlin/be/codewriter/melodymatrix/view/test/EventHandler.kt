package be.codewriter.melodymatrix.view.test

import be.codewriter.melodymatrix.view.data.MidiData
import be.codewriter.melodymatrix.view.data.PlayEvent


interface EventHandler {
    fun onMidiData(midiData: MidiData)

    fun onPlayEvent(playEvent: PlayEvent)
}