package be.codewriter.melodymatrix.view.data

import be.codewriter.melodymatrix.view.event.MmxEvent

/**
 * Interface for components that handle MelodyMatrix events.
 *
 * Classes implementing this interface receive and process MelodyMatrix events
 * such as MIDI data, play events, and chord detection events.
 *
 * @see MmxEvent
 */
interface MmxEventHandler {
    /**
     * Handles an incoming MelodyMatrix event.
     *
     * @param event The event to process
     */
    fun onEvent(event: MmxEvent)
}