package be.codewriter.melodymatrix.view.test

import javafx.geometry.Insets
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.layout.VBox

/**
 * Side-panel that lists all available visualizer stages as checkboxes.
 *
 * Each checkbox corresponds to one [TestView.StageOption]. Toggling a checkbox
 * calls the provided [onStageToggle] callback, which opens or closes the
 * corresponding visualizer tab in the main dockable area.
 *
 * @param stageOptions   The list of visualizer options to display
 * @param onStageToggle  Callback invoked with (id, isSelected) when a checkbox changes
 * @see TestView
 */
class TestViewStages(
    stageOptions: List<Option>,
    private val onStageToggle: (String, Boolean) -> Unit
) : VBox() {

    private val togglesById: MutableMap<String, CheckBox> = linkedMapOf()

    init {
        spacing = 10.0
        padding = Insets(20.0)

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

    /**
     * Programmatically sets the selected state of the checkbox identified by [id].
     *
     * No-ops if the checkbox is already in the desired state to avoid triggering a
     * recursive listener call.
     *
     * @param id       The unique identifier of the stage option
     * @param selected The desired selection state
     */
    fun setSelected(id: String, selected: Boolean) {
        val toggle = togglesById[id] ?: return
        if (toggle.isSelected != selected) {
            toggle.isSelected = selected
        }
    }

    /**
     * Describes a single visualizer stage option shown in the selector list.
     *
     * @property id    Unique identifier matching the dockable tab ID
     * @property label Human-readable name shown next to the checkbox
     */
    data class Option(
        val id: String,
        val label: String
    )
}
