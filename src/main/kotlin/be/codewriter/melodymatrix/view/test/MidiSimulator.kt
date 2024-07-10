package be.codewriter.melodymatrix.view.test

import be.codewriter.melodymatrix.view.data.MidiData
import be.codewriter.melodymatrix.view.definition.Note
import com.almasb.fxgl.dsl.random
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MidiSimulator {

    private val registeredListeners: MutableList<EventHandler> = ArrayList()
    private val notes: MutableList<Note> = mutableListOf()
    private var idx: Int = 0
    private var delay: Long = 500
    private var repeat: Boolean = true

    val scheduler = Executors.newScheduledThreadPool(1)

    fun registerListener(listener: EventHandler) {
        logger.info("Adding listener {}", listener)
        registeredListeners.add(listener)
    }

    fun removeListener(listener: EventHandler) {
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
        stopCurrent()
        idx++
        if (idx >= notes.size) {
            idx = 0
            if (!repeat) {
                return
            }
        }
        notifyListeners(
            MidiData(
                byteArrayOf(
                    "10010000".toInt(2).toByte(),
                    notes[idx].byteValue.toByte(),
                    random(40, 127).toByte()
                )
            )
        )
        scheduler.schedule({ play() }, delay, TimeUnit.MILLISECONDS)
    }

    fun stop() {
        if (notes.isEmpty()) {
            return
        }
        logger.info("Stopping playback of notes")
        stopCurrent()
        notes.clear()
    }

    private fun stopCurrent() {
        if (notes.isEmpty()) {
            return
        }
        notifyListeners(MidiData(byteArrayOf("10000000".toInt(2).toByte(), notes[idx].byteValue.toByte(), 0)))
    }

    fun notifyListeners(midiData: MidiData) {
        for (listener in registeredListeners) {
            listener.onMidiData(midiData)
        }
    }

    fun setNotes(notes: List<Note>, repeat: Boolean) {
        stopCurrent()
        this.notes.clear()
        this.notes.addAll(notes)
        this.idx = 0
        this.repeat = repeat
        play()
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(MidiSimulator::class.java.name)
    }
}