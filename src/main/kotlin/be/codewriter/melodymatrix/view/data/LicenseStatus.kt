package be.codewriter.melodymatrix.view.data

import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty

data class LicenseStatus(val licenseValidated: BooleanProperty = SimpleBooleanProperty(false))
