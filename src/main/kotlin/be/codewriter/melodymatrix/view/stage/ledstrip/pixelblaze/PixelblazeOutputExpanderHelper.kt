package be.codewriter.melodymatrix.view.stage.ledstrip.pixelblaze

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.CRC32

class PixelblazeOutputExpanderHelper(val address: String) {
    val CH_WS2812_DATA: Byte = 1
    val CH_DRAW_ALL: Byte = 2

    private var adapter: be.codewriter.melodymatrix.view.stage.ledstrip.pixelblaze.ExpanderDataWriteAdapter? = null

    init {
        logger.info("Initializing serial")
        adapter = be.codewriter.melodymatrix.view.stage.ledstrip.pixelblaze.ExpanderDataWriteAdapter(address)
    }

    fun sendAllOff(channel: Int, numberOfLeds: Int) {
        logger.info("All off on channel {} with {}", channel, numberOfLeds)
        sendColors(channel, 3, 1, 0, 2, 0, ByteArray(numberOfLeds * 3), false)
    }

    fun sendColors(channel: Int, rgbPerPixel: ByteArray, debug: Boolean) {
        logger.debug("Sending colors to {}, size {}", channel, (rgbPerPixel.size / 3))
        sendColors(channel, 3, rgbPerPixel, debug)
    }

    fun sendColors(channel: Int, bytesPerPixel: Int, rgbPerPixel: ByteArray?, debug: Boolean) {
        sendColors(channel, bytesPerPixel, 1, 0, 2, 0, rgbPerPixel, debug)
    }

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

    fun closePort() {
        adapter!!.closePort()
    }

    private fun sendDrawAll() {
        val crc = CRC32()
        crc.reset()
        val buffer: ByteBuffer = initHeaderBuffer(6, 0xff.toByte(), CH_DRAW_ALL)
        val bytes: ByteArray = buffer.array()
        crc.update(bytes)
        adapter!!.write(bytes)
        writeCrc(crc)
    }

    private fun writeCrc(crc: CRC32) {
        val crcBytes = ByteArray(4)
        packInt(crcBytes, 0, crc.value.toInt())
        adapter!!.write(crcBytes)
    }

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