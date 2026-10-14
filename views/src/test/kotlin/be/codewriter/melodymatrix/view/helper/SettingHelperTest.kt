package be.codewriter.melodymatrix.view.helper

import be.codewriter.melodymatrix.view.data.SettingStorage
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import org.junit.jupiter.api.Test
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.test.assertEquals

class SettingHelperTest {

    private class InMemoryStorage : SettingHelper {
        val values = mutableMapOf<String, String>()
        val listeners = CopyOnWriteArrayList<SettingStorage.ChangeListener>()

        override fun get(key: String) = values[key] ?: ""
        override fun put(key: String, value: String) {
            values[key] = value
        }

        override fun getBoolean(key: String, defaultValue: Boolean) = values[key]?.toBooleanStrictOrNull() ?: defaultValue
        override fun putBoolean(key: String, value: Boolean) = put(key, value.toString())
        override fun getInt(key: String, defaultValue: Int) = values[key]?.toIntOrNull() ?: defaultValue
        override fun putInt(key: String, value: Int) = put(key, value.toString())
        override fun getDouble(key: String, defaultValue: Double) = values[key]?.toDoubleOrNull() ?: defaultValue
        override fun putDouble(key: String, value: Double) = put(key, value.toString())
        override fun <E : Enum<E>> getEnum(key: String, enumType: Class<E>, defaultValue: E) =
            enumType.enumConstants.firstOrNull { it.name == values[key] } ?: defaultValue

        override fun <E : Enum<E>> putEnum(key: String, value: E) = put(key, value.name)
        override fun addChangeListener(listener: SettingStorage.ChangeListener) {
            listeners.add(listener)
        }

        override fun removeChangeListener(listener: SettingStorage.ChangeListener) {
            listeners.remove(listener)
        }

        override fun getSettingsFile() = File("unused")
    }

    @Test
    fun `listener of a collected property is removed without a setting change`() {
        val storage = InMemoryStorage()
        bindAndForget(storage)
        assertEquals(2, storage.listeners.size)

        val deadline = System.currentTimeMillis() + 10_000
        while (storage.listeners.isNotEmpty() && System.currentTimeMillis() < deadline) {
            System.gc()
            Thread.sleep(20)
        }
        assertEquals(0, storage.listeners.size, "Listeners of collected properties must be unregistered")
    }

    @Test
    fun `listener of a live property stays registered`() {
        val storage = InMemoryStorage()
        val property = SimpleBooleanProperty(false)
        storage.bindBoolean(property, "flag")

        repeat(5) {
            System.gc()
            Thread.sleep(20)
        }
        assertEquals(1, storage.listeners.size)
        assertEquals(false, property.get())
    }

    /** Separate function so no local variable keeps the properties reachable. */
    private fun bindAndForget(storage: InMemoryStorage) {
        storage.bindBoolean(SimpleBooleanProperty(true), "flag")
        storage.bindDouble(SimpleDoubleProperty(1.0), "size")
    }
}
