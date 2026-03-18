package be.codewriter.melodymatrix.view.test

import be.codewriter.melodymatrix.view.data.MmxEventHandler
import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.event.MidiDataEvent
import be.codewriter.melodymatrix.view.event.MmxEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * Simulates a MIDI input device for testing purposes.
 *
 * Plays back a configurable sequence of notes at a fixed interval, generating
 * NOTE_ON and NOTE_OFF [MidiDataEvent]s that are forwarded to all registered
 * [MmxEventHandler] listeners. Used by [TestView] to drive visualizers without
 * a real MIDI device.
 *
 * @see MmxEventHandler
 * @see TestView
 */
class MidiSimulator {

    private val registeredListeners: MutableList<MmxEventHandler> = ArrayList()
    private val notes: MutableList<Note> = mutableListOf()
    private var idx: Int = 0
    private var delay: Long = 500
    private var repeat: Boolean = true

    /** Scheduler used to fire the next note event after the current [delay]. */
    val scheduler = Executors.newScheduledThreadPool(1)

    /**
     * Registers a listener to receive simulated MIDI events.
     *
     * @param listener The event handler to add
     */
    fun registerListener(listener: MmxEventHandler) {
        logger.info("Adding listener {}", listener)
        registeredListeners.add(listener)
    }

    /**
     * Removes a previously registered listener.
     *
     * @param listener The event handler to remove
     */
    fun removeListener(listener: MmxEventHandler) {
        logger.info("Removing listener {}", listener)
        registeredListeners.remove(listener)
    }

    /**
     * Changes the playback delay between notes and restarts playback with the new interval.
     *
     * @param delay Delay in milliseconds between consecutive notes
     */
    fun setDelay(delay: Long) {
        stopCurrent()
        this.delay = delay
        logger.info("Delay changed to {}", delay)
        play()
    }

    /**
     * Advances to the next note in the sequence and schedules the following one.
     *
     * If the sequence has been exhausted and [repeat] is false, playback stops.
     */
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
            MidiDataEvent(
                byteArrayOf(
                    "10010000".toInt(2).toByte(),
                    notes[idx].byteValue.toByte(),
                    Random.nextInt(40, 127).toByte()
                )
            )
        )
        scheduler.schedule({ play() }, delay, TimeUnit.MILLISECONDS)
    }

    /**
     * Stops playback and clears the note sequence.
     */
    fun stop() {
        if (notes.isEmpty()) {
            return
        }
        logger.info("Stopping playback of notes")
        stopCurrent()
        notes.clear()
    }

    /**
     * Sends a NOTE_OFF event for the current note to silence it before advancing.
     */
    private fun stopCurrent() {
        if (notes.isEmpty()) {
            return
        }
        notifyListeners(
            MidiDataEvent(
                byteArrayOf(
                    "10000000".toInt(2).toByte(),
                    notes[idx].byteValue.toByte(),
                    0
                )
            )
        )
    }

    /**
     * Dispatches the given event to all registered listeners.
     *
     * @param mmxEvent The event to broadcast
     */
    fun notifyListeners(mmxEvent: MmxEvent) {
        for (listener in registeredListeners) {
            listener.onEvent(mmxEvent)
        }
    }

    /**
     * Sets the note sequence to play and starts playback.
     *
     * @param notes  The list of notes to play in order
     * @param repeat Whether to loop the sequence indefinitely
     */
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