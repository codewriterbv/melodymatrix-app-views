package be.codewriter.melodymatrix.view.view.ledstrip.serial

import com.fazecast.jSerialComm.SerialPort
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/** [SerialPorts] implementation based on jSerialComm. */
object JSerialCommPorts : SerialPorts {
    private val logger: Logger = LogManager.getLogger(JSerialCommPorts::class.java)

    override fun available(): List<SerialPortInfo> = try {
        SerialPort.getCommPorts()?.map { SerialPortInfo(it.systemPortName, it.descriptivePortName) } ?: emptyList()
    } catch (e: Exception) {
        logger.error("Could not get serial ports: {}", e.message)
        emptyList()
    }

    override fun open(systemName: String): SerialLink = JSerialCommLink(systemName)

    /**
     * Serial connection at 2 Mbit/s (the Pixelblaze Output Expander speed). Opens on creation and
     * re-opens automatically when a [write] finds the port closed or in error.
     */
    private class JSerialCommLink(private val portPath: String) : SerialLink {
        private var port: SerialPort? = null

        init {
            openPort()
        }

        private fun openPort() {
            port?.let {
                logger.info("Closing {}", portPath)
                it.closePort()
            }
            port = try {
                SerialPort.getCommPort(portPath).apply {
                    baudRate = 2000000
                    setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0)
                    openPort(0, 8192, 8192)
                }.also { logger.info("Opening {}", portPath) }
            } catch (e: Exception) {
                logger.error("Could not open serial port {}: {}", portPath, e.message)
                null
            }
        }

        override fun write(data: ByteArray) {
            val current = port
            if (current == null || !current.isOpen || current.lastErrorCode != 0) {
                logger.warn("Port {} was open: {}, last error: {}", portPath, current?.isOpen, current?.lastErrorCode)
                openPort()
            }
            port?.writeBytes(data, data.size)
        }

        override fun close() {
            port?.let {
                logger.info("Closing {}", portPath)
                it.closePort()
            }
            port = null
        }
    }
}
