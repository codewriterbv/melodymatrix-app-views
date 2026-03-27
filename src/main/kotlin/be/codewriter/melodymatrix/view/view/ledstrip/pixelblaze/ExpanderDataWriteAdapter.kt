package be.codewriter.melodymatrix.view.view.ledstrip.pixelblaze

import com.fazecast.jSerialComm.SerialPort

/**
 * Low-level serial port adapter for writing binary data to a Pixelblaze Output Expander.
 *
 * Opens the specified serial port at 2 Mbit/s on construction and automatically re-opens it
 * if an error is detected during a [write] call.
 *
 * @property portPath The system path of the serial port (e.g. "/dev/ttyUSB0" or "COM3")
 */
class ExpanderDataWriteAdapter(val portPath: String?) {
    private var port: SerialPort? = null

    init {
        openPort()
    }

    /**
     * Opens (or re-opens) the serial port at the configured [portPath].
     *
     * If a port is already open it will be closed first. Any exception during open is caught
     * and printed to stderr; [port] remains null in that case.
     */
    private fun openPort() {
        if (port != null) {
            println("Closing $portPath")
            port!!.closePort()
        }
        try {
            port = null //set to null in case getCommPort throws, port will remain null.
            port = SerialPort.getCommPort(this.portPath).apply {
                baudRate = 2000000
                setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0)
                openPort(0, 8192, 8192)
            }
            println("Opening $portPath")
        } catch (e: Exception) {
            System.err.println("Could not open serial port " + e.message)
        }
    }

    /**
     * Closes the serial port if it is currently open.
     */
    fun closePort() {
        if (port != null) {
            println("Closing $portPath")
            port!!.closePort()
        }
    }

    /**
     * Writes raw bytes to the serial port, re-opening it first if the port is closed or in error.
     *
     * @param data The byte array to transmit
     */
    fun write(data: ByteArray) {
        val lastErrorCode = if (port != null) port!!.lastErrorCode else 0
        val isOpen = port != null && port!!.isOpen
        if (port == null || !isOpen || lastErrorCode != 0) {
            println("Port was open:$isOpen, last error:$lastErrorCode")
            openPort()
        }
        port!!.writeBytes(data, data.size)
    }
}