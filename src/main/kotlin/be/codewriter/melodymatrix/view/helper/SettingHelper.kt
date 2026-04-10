package be.codewriter.melodymatrix.view.helper

import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.ObjectProperty
import javafx.scene.paint.Color

/**
 * JavaFX-enriched settings interface.
 * Extends [SettingStorage] (no JavaFX) with Color helpers and property-binding
 * convenience methods — all provided as default implementations so any
 * [SettingStorage] can be adapted without additional code.
 */
interface SettingHelper : SettingStorage {

    fun getColor(key: String, defaultValue: Color): Color =
        runCatching { Color.web(get(key)) }.getOrDefault(defaultValue)

    fun putColor(key: String, value: Color) = put(key, value.toString())

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
