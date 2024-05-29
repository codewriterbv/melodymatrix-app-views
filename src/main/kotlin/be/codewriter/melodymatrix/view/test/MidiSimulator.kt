package be.codewriter.melodymatrix.view.test

import be.codewriter.melodymatrix.view.data.MidiData
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class MidiSimulator {

    private val registeredListeners: MutableList<MidiDataReceivedListener> = ArrayList()

    fun registerListener(listener: MidiDataReceivedListener) {
        logger.info("Adding listener {}", listener)
        registeredListeners.add(listener)
    }

    fun removeListener(listener: MidiDataReceivedListener) {
        logger.info("Removing listener {}", listener)
        registeredListeners.remove(listener)
    }

    fun play(midiData: MidiData) {
        notifyListeners(midiData)
    }

    private fun notifyListeners(midiData: MidiData) {
        for (listener in registeredListeners) {
            listener.onMidiDataReceived(midiData)
        }
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(MidiSimulator::class.java.name)

    }
}