package be.codewriter.melodymatrix.view.data

import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.beans.binding.Bindings
import javafx.beans.binding.StringBinding

/**
 * Represents the license status of the application.
 *
 * This data class holds license information including validity status,
 * licensee details, and the license key. All properties are JavaFX observable
 * properties to support UI binding.
 *
 * @property isValid Boolean property indicating whether the license is valid
 * @property name The name of the licensee
 * @property email The email address of the licensee
 * @property validTill The expiration date of the license
 * @property key The license key
 */
data class LicenseStatus(
    val isValid: BooleanProperty = SimpleBooleanProperty(false),
    val name: StringProperty = SimpleStringProperty(""),
    val email: StringProperty = SimpleStringProperty(""),
    val validTill: StringProperty = SimpleStringProperty(""),
    val key: StringProperty = SimpleStringProperty("")
) {
    /**
     * Display-friendly version of validTill.
     * Shows "Forever!" for perpetual licenses (empty or null validTill),
     * otherwise shows the actual expiration date.
     */
    fun getValidTillDisplay(): StringBinding {
        return Bindings.createStringBinding(
            {
                val date = validTill.value
                if (date.isNullOrBlank()) "Forever!" else date
            },
            validTill
        )
    }
}
