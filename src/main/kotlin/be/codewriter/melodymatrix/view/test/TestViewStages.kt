package be.codewriter.melodymatrix.view.test

import be.codewriter.melodymatrix.view.i18n.I18n
import be.codewriter.melodymatrix.view.i18n.SupportedLocale
import javafx.beans.binding.StringBinding
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.Tooltip
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.util.Callback
import javafx.util.StringConverter

/**
 * Side-panel that lists all available visualizer stages as checkboxes.
 *
 * Each checkbox corresponds to one [Option]; its label is a live
 * [StringBinding] so the text follows the active language. Toggling a
 * checkbox calls the provided [onStageToggle] callback, which opens or
 * closes the corresponding visualizer tab in the main dockable area.
 *
 * The panel also embeds a language selector combo (bound to
 * [I18n.currentLocale]) so testers can exercise every viewer in every
 * supported language without leaving the app.
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
    private val commonBundle = I18n.registerBundle("i18n/common")

    init {
        spacing = 10.0
        padding = Insets(20.0)

        val toggles = stageOptions.map { option ->
            CheckBox().apply {
                textProperty().bind(option.labelBinding)
                minWidth = 220.0
                selectedProperty().addListener { _, _, isSelected ->
                    onStageToggle(option.id, isSelected)
                }
                togglesById[option.id] = this
            }
        }

        children.setAll(
            buildLanguageRow(),
            Label().apply {
                textProperty().bind(I18n.binding(commonBundle, "testview.stage_selector_hint"))
            },
            *toggles.toTypedArray()
        )
    }

    private fun buildLanguageRow(): HBox {
        val languageLabel = Label().apply {
            textProperty().bind(I18n.binding(commonBundle, "common.language.label"))
        }
        val combo = ComboBox(FXCollections.observableArrayList(SupportedLocale.entries)).apply {
            value = I18n.currentLocale.get()
            val localeConverter = object : StringConverter<SupportedLocale>() {
                override fun toString(locale: SupportedLocale?): String = locale?.labelWithFlag ?: ""
                override fun fromString(string: String?): SupportedLocale? =
                    items.firstOrNull { it.labelWithFlag == string }
            }
            converter = localeConverter
            cellFactory = Callback {
                object : ListCell<SupportedLocale>() {
                    override fun updateItem(item: SupportedLocale?, empty: Boolean) {
                        super.updateItem(item, empty)
                        text = if (empty || item == null) null else item.labelWithFlag
                    }
                }
            }
            buttonCell = object : ListCell<SupportedLocale>() {
                override fun updateItem(item: SupportedLocale?, empty: Boolean) {
                    super.updateItem(item, empty)
                    text = if (empty || item == null) null else item.labelWithFlag
                }
            }
            setOnAction {
                value?.let { selected ->
                    if (selected != I18n.currentLocale.get()) {
                        I18n.currentLocale.set(selected)
                    }
                }
            }
            I18n.currentLocale.addListener { _, _, newLocale ->
                if (value != newLocale) value = newLocale
            }
            tooltip = Tooltip().apply {
                textProperty().bind(I18n.binding(commonBundle, "common.language.tooltip"))
            }
        }
        return HBox(8.0, languageLabel, combo).apply {
            alignment = Pos.CENTER_LEFT
        }
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
     * @property id            Unique identifier matching the dockable tab ID
     * @property labelBinding  Live [StringBinding] providing the name shown next to the checkbox
     */
    data class Option(
        val id: String,
        val labelBinding: StringBinding
    )
}
