package be.codewriter.melodymatrix.view.test

import javafx.beans.binding.Bindings
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.VBox

class TestViewLicense(licenseStatus: be.codewriter.melodymatrix.view.data.LicenseStatus) : VBox() {

    init {
        spacing = 10.0

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

    private fun createButton(label: String, licenseStatus: be.codewriter.melodymatrix.view.data.LicenseStatus): Node {
        val view = Button(label).apply {
            minWidth = 200.0
            setOnMouseClicked { _ ->
                licenseStatus.isValid.set(!licenseStatus.isValid.get())
            }
        }

        return view
    }
}
