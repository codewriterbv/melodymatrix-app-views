package be.codewriter.melodymatrix.view.view.piano.data

import be.codewriter.melodymatrix.view.helper.SettingHelper
import be.codewriter.melodymatrix.view.helper.SettingStorage
import javafx.scene.paint.Color
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PianoConfigurationTest {

    @Test
    fun `restoreSettings migrates legacy falling-rising block toggles`() {
        val settings = InMemorySettingHelper(
            mutableMapOf(
                "view.piano.test.fallingBlocksEnabled" to "true",
                "view.piano.test.risingBlocksEnabled" to "false"
            )
        )
        val config = PianoConfiguration(viewName = "test", settings = settings)

        config.restoreSettings()

        assertTrue(config.noteBlocksEnabled.value)
    }

    @Test
    fun `restoreSettings prefers unified noteBlocksEnabled over legacy keys`() {
        val settings = InMemorySettingHelper(
            mutableMapOf(
                "view.piano.test.noteBlocksEnabled" to "false",
                "view.piano.test.fallingBlocksEnabled" to "true",
                "view.piano.test.risingBlocksEnabled" to "true"
            )
        )
        val config = PianoConfiguration(viewName = "test", settings = settings)

        config.restoreSettings()

        assertFalse(config.noteBlocksEnabled.value)
        config.noteBlocksEnabled.set(true)
        assertEquals("true", settings.values["view.piano.test.noteBlocksEnabled"])
    }

    private class InMemorySettingHelper(
        val values: MutableMap<String, String> = mutableMapOf()
    ) : SettingHelper {

        private val listeners = mutableListOf<SettingStorage.ChangeListener>()

        override fun get(key: String): String = values[key] ?: ""

        override fun put(key: String, value: String) {
            values[key] = value
            listeners.forEach { it.onChanged(key, value) }
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

        override fun addChangeListener(listener: SettingStorage.ChangeListener) {
            listeners += listener
        }

        override fun removeChangeListener(listener: SettingStorage.ChangeListener) {
            listeners -= listener
        }

        override fun getColor(key: String, defaultValue: Color): Color {
            val raw = values[key] ?: return defaultValue
            return runCatching { Color.web(raw) }.getOrDefault(defaultValue)
        }

        override fun putColor(key: String, value: Color) {
            put(key, value.toString())
        }
    }
}

