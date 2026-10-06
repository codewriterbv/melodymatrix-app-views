package be.codewriter.melodymatrix.view.view.ledstrip

import be.codewriter.melodymatrix.view.view.ledstrip.pixelblaze.PixelblazeOutputExpanderHelper
import be.codewriter.melodymatrix.view.view.ledstrip.serial.SerialLink
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.CRC32
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PixelblazeOutputExpanderHelperTest {

    private class RecordingLink : SerialLink {
        val writes = mutableListOf<ByteArray>()
        var closed = false
        override fun write(data: ByteArray) {
            writes += data.copyOf()
        }

        override fun close() {
            closed = true
        }
    }

    @Test
    fun `colour frame has header, pixels and crc, followed by a draw-all frame`() {
        val link = RecordingLink()
        val helper = PixelblazeOutputExpanderHelper("test", link)
        val rgb = byteArrayOf(10, 20, 30, 40, 50, 60) // two pixels

        helper.sendColors(2, rgb, false)

        // header, pixel data, crc, then draw-all header and its crc
        assertEquals(5, link.writes.size)
        val header = link.writes[0]
        assertContentEquals("UPXL".toByteArray(), header.copyOfRange(0, 4))
        assertEquals(2, header[4].toInt(), "channel")
        assertEquals(1, header[5].toInt(), "WS2812 data command")
        assertEquals(3, header[6].toInt(), "bytes per pixel")
        assertEquals(2, ByteBuffer.wrap(header, 8, 2).order(ByteOrder.LITTLE_ENDIAN).short.toInt(), "pixel count")
        assertContentEquals(rgb, link.writes[1])

        val crc = CRC32().apply { update(header); update(rgb) }.value.toInt()
        assertEquals(crc, ByteBuffer.wrap(link.writes[2]).order(ByteOrder.LITTLE_ENDIAN).int)

        val drawAll = link.writes[3]
        assertEquals(2, drawAll[5].toInt(), "draw-all command")
        assertTrue(drawAll[4] == 0xff.toByte(), "draw-all goes to all channels")
    }

    @Test
    fun `closing the helper closes the serial link`() {
        val link = RecordingLink()
        PixelblazeOutputExpanderHelper("test", link).closePort()
        assertTrue(link.closed)
    }
}
