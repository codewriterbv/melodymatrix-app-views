package be.codewriter.melodymatrix.view.test

import be.codewriter.melodymatrix.view.data.LicenseStatus
import javafx.geometry.Insets
import javafx.scene.layout.HBox
import javafx.stage.Stage

class TestView(stage: Stage) : HBox() {

    init {
        val midiSimulator = MidiSimulator()
        val licenseStatus = LicenseStatus()
        this.children.addAll(
            TestViewMidiEvents(midiSimulator),
            TestViewStages(stage, midiSimulator, licenseStatus),
            TestViewLicense(licenseStatus)
        )
        spacing = 25.0
        padding = Insets(25.0)
    }
}