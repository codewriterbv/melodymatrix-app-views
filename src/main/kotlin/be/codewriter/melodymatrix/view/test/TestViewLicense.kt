package be.codewriter.melodymatrix.view.test

import be.codewriter.melodymatrix.view.data.LicenseStatus
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

    init {
        spacing = 10.0
        padding = Insets(20.0)

        children.setAll(
            Label("License Status"),
            Label().apply {
                textProperty().bind(
                    Bindings.`when`(licenseStatus.isValid)
                        .then("Is valid")
                        .otherwise("Is not valid")
                )
            },
            createButton(
                "Toggle license validity",
                licenseStatus
            )
        )
    }

    /**
     * Creates a toggle button bound to the given license status.
     *
     * @param label         The button label text
     * @param licenseStatus The license status to toggle on click
     * @return A [Button] node wired to flip the [LicenseStatus.isValid] property
     */
    private fun createButton(label: String, licenseStatus: LicenseStatus): Node {
        val view = Button(label).apply {
            minWidth = 200.0
            setOnMouseClicked { _ ->
                licenseStatus.isValid.set(!licenseStatus.isValid.get())
            }
        }

        return view
    }
}
