package be.codewriter.melodymatrix.view.helper

import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.ObjectProperty
import javafx.scene.paint.Color
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.prefs.Preferences

class RegistryHelper {

    companion object {
        private val logger: Logger = LogManager.getLogger(RegistryHelper::class.java.name)
        private val prefs: Preferences = Preferences.userNodeForPackage(RegistryHelper::class.java)

        fun get(key: String): String {
            return prefs[key, ""]
        }

        fun put(key: String, value: String) {
            if (get(key) != value) {
                logger.info("Storing in {}: {}", key, value)
                prefs.put(key, value)
            }
        }

        fun getBoolean(key: String, defaultValue: Boolean): Boolean {
            return get(key).toBooleanStrictOrNull() ?: defaultValue
        }

        fun putBoolean(key: String, value: Boolean) = put(key, value.toString())

        fun getInt(key: String, defaultValue: Int): Int {
            return get(key).toIntOrNull() ?: defaultValue
        }

        fun putInt(key: String, value: Int) = put(key, value.toString())

        fun getDouble(key: String, defaultValue: Double): Double {
            return get(key).toDoubleOrNull() ?: defaultValue
        }

        fun putDouble(key: String, value: Double) = put(key, value.toString())

        fun getColor(key: String, defaultValue: Color): Color {
            return runCatching { Color.web(get(key)) }.getOrDefault(defaultValue)
        }

        fun putColor(key: String, value: Color) = put(key, value.toString())

        fun <E : Enum<E>> getEnum(key: String, enumType: Class<E>, defaultValue: E): E {
            val raw = get(key)
            return enumType.enumConstants?.firstOrNull { it.name == raw } ?: defaultValue
        }

        fun <E : Enum<E>> putEnum(key: String, value: E) = put(key, value.name)

        fun bindBoolean(property: BooleanProperty, key: String) {
            property.set(getBoolean(key, property.get()))
            property.addListener { _, _, newValue -> putBoolean(key, newValue) }
        }

        fun bindInt(property: IntegerProperty, key: String) {
            property.set(getInt(key, property.get()))
            property.addListener { _, _, newValue -> putInt(key, newValue.toInt()) }
        }

        fun bindDouble(property: DoubleProperty, key: String) {
            property.set(getDouble(key, property.get()))
            property.addListener { _, _, newValue -> putDouble(key, newValue.toDouble()) }
        }

        fun bindColor(property: ObjectProperty<Color>, key: String) {
            property.set(getColor(key, property.get()))
            property.addListener { _, _, newValue -> putColor(key, newValue) }
        }

        fun <E : Enum<E>> bindEnum(property: ObjectProperty<E>, key: String, enumType: Class<E>) {
            property.set(getEnum(key, enumType, property.get()))
            property.addListener { _, _, newValue -> putEnum(key, newValue) }
        }
    }
}