package be.codewriter.melodymatrix.view.view.ledstrip.pixelblaze

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.CRC32

/**
 * Helper class for communicating with a Pixelblaze Output Expander over a serial port.
 *
 * Builds and transmits the binary framing protocol expected by the Pixelblaze Output Expander,
 * including WS2812 colour data packets and draw-all commands. Each packet is CRC32-protected.
 *
 * @property address The system serial port path (e.g. "/dev/ttyUSB0" or "COM3")
 * @see ExpanderDataWriteAdapter
 * @see LedStripStage
 */
class PixelblazeOutputExpanderHelper(val address: String) {
    /** Command byte for WS2812 LED data. */
    val CH_WS2812_DATA: Byte = 1

    /** Command byte that triggers all channels to render their buffered data. */
    val CH_DRAW_ALL: Byte = 2

    private var adapter: ExpanderDataWriteAdapter? = null

    init {
        logger.info("Initializing serial")
        adapter = ExpanderDataWriteAdapter(address)
    }

    /**
     * Sends an all-off (black) command to the specified channel.
     *
     * @param channel      The output channel index (0–7)
     * @param numberOfLeds The number of LEDs on the channel to turn off
     */
    fun sendAllOff(channel: Int, numberOfLeds: Int) {
        logger.info("All off on channel {} with {}", channel, numberOfLeds)
        sendColors(channel, 3, 1, 0, 2, 0, ByteArray(numberOfLeds * 3), false)
    }

    /**
     * Sends RGB colour data to the specified channel using 3 bytes per pixel (R, G, B).
     *
     * @param channel     The output channel index (0–7)
     * @param rgbPerPixel Byte array of RGB triplets; length must be a multiple of 3
     * @param debug       When true, logs verbose hex output (may slow execution)
     */
    fun sendColors(channel: Int, rgbPerPixel: ByteArray, debug: Boolean) {
        logger.debug("Sending colors to {}, size {}", channel, (rgbPerPixel.size / 3))
        sendColors(channel, 3, rgbPerPixel, debug)
    }

    /**
     * Sends colour data with a configurable number of bytes per pixel.
     *
     * @param channel       The output channel index (0–7)
     * @param bytesPerPixel Number of bytes per pixel (3 or 4)
     * @param rgbPerPixel   Byte array of colour data; length must be a multiple of [bytesPerPixel]
     * @param debug         When true, logs verbose hex output
     */
    fun sendColors(channel: Int, bytesPerPixel: Int, rgbPerPixel: ByteArray?, debug: Boolean) {
        sendColors(channel, bytesPerPixel, 1, 0, 2, 0, rgbPerPixel, debug)
    }

    /**
     * Sends colour data with full control over bytes-per-pixel and channel index ordering.
     *
     * Builds the Pixelblaze Output Expander packet header, appends the pixel data, computes
     * a CRC32 checksum, and sends everything followed by a draw-all command.
     *
     * @param channel       The output channel index (0–7)
     * @param bytesPerPixel Number of bytes per pixel (must be 3 or 4)
     * @param rIndex        Index of the red component within each pixel (0–3)
     * @param gIndex        Index of the green component within each pixel (0–3)
     * @param bIndex        Index of the blue component within each pixel (0–3)
     * @param wIndex        Index of the white component within each pixel (0–3); ignored for RGB
     * @param rgbPerPixel   Byte array of colour data; must not be null
     * @param debug         When true, logs verbose hex output
     */
    fun sendColors(
        channel: Int, bytesPerPixel: Int, rIndex: Int, gIndex: Int, bIndex: Int, wIndex: Int,
        rgbPerPixel: ByteArray?, debug: Boolean
    ) {
        if (debug) {
            logger.info("Sending colors on channel {}", channel)
        }

        if (bytesPerPixel != 3 && bytesPerPixel != 4) {
            logger.info("bytesPerPixel not within expected range")
            return
        }
        if (rIndex > 3 || gIndex > 3 || bIndex > 3 || wIndex > 3) {
            logger.info("one or more indexes not within expected range")
            return
        }
        if (rgbPerPixel == null) {
            logger.info("rgbPerPixel can not be null")
            return
        }

        val pixels = rgbPerPixel.size / bytesPerPixel
        val crc = CRC32()
        crc.reset()
        val headerBuffer: ByteBuffer = initHeaderBuffer(10, channel.toByte(), CH_WS2812_DATA)
        headerBuffer.put(bytesPerPixel.toByte())
        headerBuffer.put((rIndex or (gIndex shl 2) or (bIndex shl 4) or (wIndex shl 6)).toByte())
        headerBuffer.putShort(pixels.toShort())
        val header: ByteArray = headerBuffer.array()

        if (debug) {
            // Output the RGB byte array for testing
            // This slows down the execution of the application!
            for (i in rgbPerPixel.indices) {
                System.out.printf("%02x ", rgbPerPixel[i])
                if (i % 12 == 11) {
                    print("\n")
                } else if (i % 4 == 3) {
                    print("\t")
                }
            }
            print("\n")
        }

        crc.update(header)
        adapter!!.write(header)

        crc.update(rgbPerPixel)
        adapter!!.write(rgbPerPixel)

        writeCrc(crc)

        sendDrawAll()
    }

    /**
     * Closes the underlying serial port connection.
     */
    fun closePort() {
        adapter!!.closePort()
    }

    /**
     * Sends a draw-all command so the expander renders all buffered channel data simultaneously.
     */
    private fun sendDrawAll() {
        val crc = CRC32()
        crc.reset()
        val buffer: ByteBuffer = initHeaderBuffer(6, 0xff.toByte(), CH_DRAW_ALL)
        val bytes: ByteArray = buffer.array()
        crc.update(bytes)
        adapter!!.write(bytes)
        writeCrc(crc)
    }

    /**
     * Writes the 4-byte little-endian CRC32 checksum to the serial port.
     *
     * @param crc The CRC32 accumulator whose current value will be written
     */
    private fun writeCrc(crc: CRC32) {
        val crcBytes = ByteArray(4)
        packInt(crcBytes, 0, crc.value.toInt())
        adapter!!.write(crcBytes)
    }

    /**
     * Packs a 32-bit integer into four consecutive bytes in little-endian order.
     *
     * @param outgoing The target byte array
     * @param index    The start index within [outgoing]
     * @param val      The integer value to pack
     */
    private fun packInt(outgoing: ByteArray, index: Int, `val`: Int) {
        var index = index
        var `val` = `val`
        outgoing[index++] = (`val` and 0xFF).toByte()
        `val` = `val` shr 8
        outgoing[index++] = (`val` and 0xFF).toByte()
        `val` = `val` shr 8
        outgoing[index++] = (`val` and 0xFF).toByte()
        `val` = `val` shr 8
        outgoing[index] = (`val` and 0xFF).toByte()
    }

    /**
     * Allocates and initialises a packet header buffer with the Pixelblaze magic bytes, channel, and command.
     *
     * @param size    Total buffer size in bytes
     * @param channel The channel byte to write into the header
     * @param command The command byte to write into the header
     * @return A [ByteBuffer] with the header already written, positioned after the header fields
     */
    private fun initHeaderBuffer(size: Int, channel: Byte, command: Byte): ByteBuffer {
        val buffer: ByteBuffer = ByteBuffer.allocate(size)
        buffer.order(ByteOrder.LITTLE_ENDIAN)

        buffer.put('U'.code.toByte())
        buffer.put('P'.code.toByte())
        buffer.put('X'.code.toByte())
        buffer.put('L'.code.toByte())
        buffer.put(channel)
        buffer.put(command)
        return buffer
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(PixelblazeOutputExpanderHelper::class.java.name)
    }
}