package be.codewriter.melodymatrix.view.view.piano.data

import be.codewriter.melodymatrix.view.helper.SettingStorage
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PianoConfigurationTest {

    @Test
    fun `resolveNoteBlocksEnabled migrates legacy falling-rising block toggles`() {
        val settings = InMemorySettingStorage(
            mutableMapOf(
                "view.piano.test.fallingBlocksEnabled" to "true",
                "view.piano.test.risingBlocksEnabled" to "false"
            )
        )

        assertTrue(PianoConfiguration.resolveNoteBlocksEnabled(settings, "test"))
    }

    @Test
    fun `resolveNoteBlocksEnabled prefers unified noteBlocksEnabled over legacy keys`() {
        val settings = InMemorySettingStorage(
            mutableMapOf(
                "view.piano.test.noteBlocksEnabled" to "false",
                "view.piano.test.fallingBlocksEnabled" to "true",
                "view.piano.test.risingBlocksEnabled" to "true"
            )
        )

        assertFalse(PianoConfiguration.resolveNoteBlocksEnabled(settings, "test"))
    }

    private class InMemorySettingStorage(
        val values: MutableMap<String, String> = mutableMapOf()
    ) : SettingStorage {

        override fun get(key: String): String = values[key] ?: ""

        override fun put(key: String, value: String) {
            values[key] = value
        }

        override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
            values[key]?.toBooleanStrictOrNull() ?: defaultValue

        override fun putBoolean(key: String, value: Boolean) = put(key, value.toString())

        override fun getInt(key: String, defaultValue: Int): Int = values[key]?.toIntOrNull() ?: defaultValue

        override fun putInt(key: String, value: Int) = put(key, value.toString())

        override fun getDouble(key: String, defaultValue: Double): Double = values[key]?.toDoubleOrNull() ?: defaultValue

        override fun putDouble(key: String, value: Double) = put(key, value.toString())

        override fun <E : Enum<E>> getEnum(key: String, enumType: Class<E>, defaultValue: E): E =
            enumType.enumConstants.firstOrNull { it.name == values[key] } ?: defaultValue

        override fun <E : Enum<E>> putEnum(key: String, value: E) = put(key, value.name)

        override fun getSettingsFile(): File = File("/tmp/test-settings.properties")
    }
}

