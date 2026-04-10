package be.codewriter.melodymatrix.view.helper

import java.io.File

/**
 * Pure storage contract for application settings — no JavaFX dependency.
 * Engine-level services (e.g. SettingService) implement this interface so that
 * Viewers code can consume them without the Engine touching JavaFX types.
 */
interface SettingStorage {
    fun get(key: String): String
    fun put(key: String, value: String)

    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun putBoolean(key: String, value: Boolean)

    fun getInt(key: String, defaultValue: Int): Int
    fun putInt(key: String, value: Int)

    fun getDouble(key: String, defaultValue: Double): Double
    fun putDouble(key: String, value: Double)

    fun <E : Enum<E>> getEnum(key: String, enumType: Class<E>, defaultValue: E): E
    fun <E : Enum<E>> putEnum(key: String, value: E)

    fun getSettingsFile(): File
}

