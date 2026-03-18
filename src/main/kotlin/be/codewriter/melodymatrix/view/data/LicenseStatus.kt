package be.codewriter.melodymatrix.view.data

import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty

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
)
