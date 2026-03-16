package be.codewriter.melodymatrix.view.test

import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.layout.VBox

class TestViewStages(
    stageOptions: List<Option>,
    private val onStageToggle: (String, Boolean) -> Unit
) : VBox() {

    private val togglesById: MutableMap<String, CheckBox> = linkedMapOf()

    init {
        spacing = 10.0

        val toggles = stageOptions.map { option ->
            CheckBox(option.label).apply {
                minWidth = 220.0
                selectedProperty().addListener { _, _, isSelected ->
                    onStageToggle(option.id, isSelected)
                }
                togglesById[option.id] = this
            }
        }

        children.setAll(
            Label("Select visualizer tabs for the main view"),
            *toggles.toTypedArray()
        )
    }

    fun setSelected(id: String, selected: Boolean) {
        val toggle = togglesById[id] ?: return
        if (toggle.isSelected != selected) {
            toggle.isSelected = selected
        }
    }

    data class Option(
        val id: String,
        val label: String
    )
}
