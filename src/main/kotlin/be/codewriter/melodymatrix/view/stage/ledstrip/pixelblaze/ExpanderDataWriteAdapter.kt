package be.codewriter.melodymatrix.view.stage.ledstrip.pixelblaze

import com.fazecast.jSerialComm.SerialPort

class ExpanderDataWriteAdapter(val portPath: String?) {
    private var port: SerialPort? = null

    init {
        openPort()
    }

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

    fun closePort() {
        if (port != null) {
            println("Closing $portPath")
            port!!.closePort()
        }
    }

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