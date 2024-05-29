package be.codewriter.melodymatrix.view.test

import be.codewriter.melodymatrix.view.data.MidiData
import be.codewriter.melodymatrix.view.data.Note
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MidiSimulator {

    private val registeredListeners: MutableList<MidiDataReceivedListener> = ArrayList()
    private val notes: MutableList<Note> = mutableListOf()
    private var idx: Int = 0
    private var delay: Long = 500

    val scheduler = Executors.newScheduledThreadPool(1)

    fun registerListener(listener: MidiDataReceivedListener) {
        logger.info("Adding listener {}", listener)
        registeredListeners.add(listener)
    }

    fun removeListener(listener: MidiDataReceivedListener) {
        logger.info("Removing listener {}", listener)
        registeredListeners.remove(listener)
    }

    fun setDelay(delay: Long) {
        stopCurrent()
        this.delay = delay
        logger.info("Delay changed to {}", delay)
        play()
    }

    private fun play() {
        if (notes.isEmpty()) {
            return
        }
        logger.info("Playing {} of {} notes", idx, notes.size)
        stopCurrent()
        idx++
        if (idx >= notes.size) {
            idx = 0
        }
        notifyListeners(MidiData(notes[idx], true, 255))
        scheduler.schedule({ play() }, delay, TimeUnit.MILLISECONDS)
    }

    private fun stopCurrent() {
        if (notes.isEmpty()) {
            return
        }
        notifyListeners(MidiData(notes[idx], false, 0))
    }

    private fun notifyListeners(midiData: MidiData) {
        logger.info("Sending {}", midiData)
        for (listener in registeredListeners) {
            listener.onMidiDataReceived(midiData)
        }
    }

    fun setNotes(notes: List<Note>) {
        logger.info("Setting {} notes", notes.size)
        stopCurrent()
        this.notes.clear()
        this.notes.addAll(notes)
        this.idx = 0
        play()
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(MidiSimulator::class.java.name)
    }
}