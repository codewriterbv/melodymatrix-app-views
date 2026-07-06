package be.codewriter.melodymatrix.view.test

import be.codewriter.melodymatrix.view.data.LicenseStatus
import be.codewriter.melodymatrix.view.i18n.I18n
import javafx.beans.binding.Bindings
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.VBox

/**
 * Test panel that displays and allows toggling of the [LicenseStatus].
 *
 * Shows whether the current license is valid and provides a button to toggle
 * the validity flag, which is useful for testing license-gated UI features.
 *
 * @param licenseStatus The observable license status to display and toggle
 * @see TestView
 */
class TestViewLicense(licenseStatus: LicenseStatus) : VBox() {

    private val commonBundle = I18n.registerBundle("i18n/common")

    init {
        spacing = 10.0
        padding = Insets(20.0)

        children.setAll(
            Label().apply { textProperty().bind(I18n.binding(commonBundle, "testview.license_status")) },
            Label().apply {
                textProperty().bind(
                    Bindings.`when`(licenseStatus.isValid)
                        .then(I18n.binding(commonBundle, "testview.license.is_valid"))
                        .otherwise(I18n.binding(commonBundle, "testview.license.is_not_valid"))
                )
            },
            createToggleButton(licenseStatus)
        )
    }

    /**
     * Creates a toggle button bound to the given license status; label follows the active language.
     */
    private fun createToggleButton(licenseStatus: LicenseStatus): Node {
        return Button().apply {
            textProperty().bind(I18n.binding(commonBundle, "testview.license.toggle"))
            minWidth = 200.0
            setOnMouseClicked {
                licenseStatus.isValid.set(!licenseStatus.isValid.get())
            }
        }
    }
}
