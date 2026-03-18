package be.codewriter.melodymatrix.view.event

interface MmxEvent {
    val timestamp: Long
    val type: MmxEventType
}
