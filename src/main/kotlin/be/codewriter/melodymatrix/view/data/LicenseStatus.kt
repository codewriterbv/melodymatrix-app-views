package be.codewriter.melodymatrix.view.data

import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty

data class LicenseStatus(
    val isValid: BooleanProperty = SimpleBooleanProperty(false),
    val name: StringProperty = SimpleStringProperty(""),
    val email: StringProperty = SimpleStringProperty(""),
    val validTill: StringProperty = SimpleStringProperty(""),
    val key: StringProperty = SimpleStringProperty("")
)
