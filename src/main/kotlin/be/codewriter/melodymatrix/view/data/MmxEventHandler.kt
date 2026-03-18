package be.codewriter.melodymatrix.view.data

import be.codewriter.melodymatrix.view.event.MmxEvent

interface MmxEventHandler {
    fun onEvent(event: MmxEvent)
}