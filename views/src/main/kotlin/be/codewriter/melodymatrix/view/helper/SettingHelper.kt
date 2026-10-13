package be.codewriter.melodymatrix.view.helper

import be.codewriter.melodymatrix.view.data.SettingStorage
import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.Property
import javafx.application.Platform
import javafx.scene.paint.Color
import java.lang.ref.WeakReference

/**
 * JavaFX-enriched settings interface.
 * Extends [SettingStorage] (no JavaFX) with Color helpers and property-binding
 * convenience methods — all provided as default implementations so any
 * [SettingStorage] can be adapted without additional code.
 */
interface SettingHelper : SettingStorage {

    private fun runOnFxThread(action: () -> Unit) {
        if (Platform.isFxApplicationThread()) action() else Platform.runLater(action)
    }

    /**
     * Pushes external changes of [key] into [property]. The storage outlives the views, so the
     * listener only holds the property weakly and unregisters itself once the property (and
     * with it the view that owns it) has been garbage collected.
     */
    private fun <T> listenWeakly(property: Property<T>, key: String, parse: (String) -> T?) {
        val propertyRef = WeakReference(property)
        addChangeListener(object : SettingStorage.ChangeListener {
            override fun onChanged(changedKey: String, value: String) {
                val target = propertyRef.get() ?: return removeChangeListener(this)
                if (changedKey != key) return
                val parsed = parse(value) ?: return
                runOnFxThread {
                    if (target.value != parsed) target.value = parsed
                }
            }
        })
    }

    fun getColor(key: String, defaultValue: Color): Color =
        runCatching { Color.web(get(key)) }.getOrDefault(defaultValue)

    fun putColor(key: String, value: Color) = put(key, value.toString())

    fun bindBoolean(property: BooleanProperty, key: String) {
        property.set(getBoolean(key, property.get()))
        property.addListener { _, _, newValue -> putBoolean(key, newValue) }
        listenWeakly(property, key) { value -> value.toBooleanStrictOrNull() }
    }

    fun bindInt(property: IntegerProperty, key: String) {
        property.set(getInt(key, property.get()))
        property.addListener { _, _, newValue -> putInt(key, newValue.toInt()) }
        listenWeakly(property, key) { value -> value.toIntOrNull() }
    }

    fun bindDouble(property: DoubleProperty, key: String) {
        property.set(getDouble(key, property.get()))
        property.addListener { _, _, newValue -> putDouble(key, newValue.toDouble()) }
        listenWeakly(property, key) { value -> value.toDoubleOrNull() }
    }

    fun bindColor(property: ObjectProperty<Color>, key: String) {
        property.set(getColor(key, property.get()))
        property.addListener { _, _, newValue -> putColor(key, newValue) }
        listenWeakly(property, key) { value -> runCatching { Color.web(value) }.getOrNull() }
    }

    fun <E : Enum<E>> bindEnum(property: ObjectProperty<E>, key: String, enumType: Class<E>) {
        property.set(getEnum(key, enumType, property.get()))
        property.addListener { _, _, newValue -> putEnum(key, newValue) }
        listenWeakly(property, key) { value -> enumType.enumConstants?.firstOrNull { it.name == value } }
    }
}
