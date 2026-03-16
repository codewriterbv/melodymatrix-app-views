package be.codewriter.melodymatrix.view.test

import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.layout.VBox

class TestViewStages(
    stageLabels: List<String>,
    private val onStageToggle: (String, Boolean) -> Unit
) : VBox() {

    private val togglesByLabel: MutableMap<String, CheckBox> = linkedMapOf()

    init {
        spacing = 10.0

        val toggles = stageLabels.map { label ->
            CheckBox(label).apply {
                minWidth = 220.0
                selectedProperty().addListener { _, _, isSelected ->
                    onStageToggle(label, isSelected)
                }
                togglesByLabel[label] = this
            }
        }

        children.setAll(
            Label("Select visualizer tabs for the main view"),
            *toggles.toTypedArray()
        )
    }

    fun setSelected(label: String, selected: Boolean) {
        val toggle = togglesByLabel[label] ?: return
        if (toggle.isSelected != selected) {
            toggle.isSelected = selected
        }
    }
}
