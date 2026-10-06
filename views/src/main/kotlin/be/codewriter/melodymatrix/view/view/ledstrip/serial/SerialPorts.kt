package be.codewriter.melodymatrix.view.view.ledstrip.serial

/** A serial port found on this machine. */
data class SerialPortInfo(val systemName: String, val description: String) {
    override fun toString(): String = description.ifBlank { systemName }
}

/** An open connection to a serial port. */
interface SerialLink {
    fun write(data: ByteArray)
    fun close()
}

/**
 * Access to the machine's serial ports. The LED strip view only talks to this interface, so the
 * serial library stays in one place ([JSerialCommPorts]) and tests can use a fake.
 */
interface SerialPorts {
    fun available(): List<SerialPortInfo>
    fun open(systemName: String): SerialLink
}
