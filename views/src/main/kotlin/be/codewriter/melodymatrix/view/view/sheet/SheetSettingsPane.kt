package be.codewriter.melodymatrix.view.view.sheet

import atlantafx.base.theme.Styles
import be.codewriter.melodymatrix.view.component.TickerSlider
import be.codewriter.melodymatrix.view.helper.SettingHelper
import be.codewriter.melodymatrix.view.i18n.I18n
import be.codewriter.melodymatrix.view.view.piano.configurator.BaseConfigurator
import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.ObjectProperty
import javafx.geometry.HorizontalDirection
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ColorPicker
import javafx.scene.control.ToggleButton
import javafx.scene.control.ToggleGroup
import javafx.scene.control.ToolBar
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.paint.Color

/**
 * Toolbar shared by the notation views: a switch between [SheetDisplayMode.FULL_SCORE] and
 * [SheetDisplayMode.NOTE_BY_NOTE], extra toolbar items of the view itself, and settings buttons
 * that open dialogs (like the piano view's) with the colours and sizes the view registers with
 * [section], [color], [slider] and [toggle]. Controls go into a single "Settings" dialog, unless
 * the view splits them over several with [dialog].
 *
 * Every registered property is stored in [settings] under `view.<viewKey>.<name>`, so the
 * choices survive a restart; without [settings] (e.g. in the demo) they only last while the
 * view is open. Labels come from the shared `i18n/view/sheet` bundle.
 *
 * @param viewTitle the view's title, shown in the dialog title
 */
class SheetSettingsPane(
    private val settings: SettingHelper?,
    private val viewKey: String,
    private val viewTitle: () -> String
) : ToolBar() {

    private val bundle = I18n.registerBundle(BUNDLE_BASE_NAME)
    private val toolbarItems = HBox(8.0).apply { alignment = Pos.CENTER_LEFT }
    private val dialogButtons = HBox(8.0).apply { alignment = Pos.CENTER_RIGHT }

    /** The dialog that new sections and controls are added to; created on first use. */
    private var currentDialog: DialogEntries? = null

    init {
        val spacer = Region().apply { HBox.setHgrow(this, Priority.ALWAYS) }
        items.addAll(toolbarItems, spacer, dialogButtons)
    }

    /**
     * Starts a new settings dialog with its own toolbar button, labelled with [titleKey]; the
     * sections and controls registered after this call go into it.
     */
    fun dialog(titleKey: String): SheetSettingsPane {
        currentDialog = newDialog(titleKey) { I18n.get(bundle, titleKey) }
        return this
    }

    /** Adds the display mode switch for [property] to the toolbar and stores the choice. */
    fun displayMode(property: ObjectProperty<SheetDisplayMode>): SheetSettingsPane {
        settings?.bindEnum(property, key("displayMode"), SheetDisplayMode::class.java)
        val group = ToggleGroup()
        val buttons = SheetDisplayMode.entries.mapIndexed { index, mode ->
            ToggleButton().apply {
                textProperty().bind(I18n.binding(bundle, "sheet.mode.${mode.name.lowercase()}"))
                toggleGroup = group
                userData = mode
                isSelected = property.get() == mode
                styleClass.add(
                    when (index) {
                        0 -> Styles.LEFT_PILL
                        SheetDisplayMode.entries.lastIndex -> Styles.RIGHT_PILL
                        else -> Styles.CENTER_PILL
                    }
                )
            }
        }
        group.selectedToggleProperty().addListener { _, old, selected ->
            // A pill group always keeps one choice: clicking the selected button again re-selects it.
            if (selected == null) old?.isSelected = true else property.set(selected.userData as SheetDisplayMode)
        }
        property.addListener { _, _, mode -> buttons.firstOrNull { it.userData == mode }?.isSelected = true }
        toolbarItems.children.add(HBox(*buttons.toTypedArray()))
        return this
    }

    /** Adds [nodes] (controls of the view itself) to the toolbar. */
    fun toolbar(vararg nodes: Node): SheetSettingsPane {
        toolbarItems.children.addAll(*nodes)
        return this
    }

    /** Starts a new section in the settings dialog, titled with [titleKey]. */
    fun section(titleKey: String): SheetSettingsPane {
        entries().add { it.addSection(titleKey) }
        return this
    }

    /** Adds a colour picker for [property], labelled with [labelKey] and stored as [name]. */
    fun color(labelKey: String, property: ObjectProperty<Color>, name: String): SheetSettingsPane {
        settings?.bindColor(property, key(name))
        entries().add { configurator ->
            configurator.addControl(labelKey, ColorPicker(property.get()).apply {
                valueProperty().bindBidirectional(property)
            })
        }
        return this
    }

    /** Adds a slider for [property] between [min] and [max], labelled with [labelKey] and stored as [name]. */
    fun slider(labelKey: String, property: DoubleProperty, min: Double, max: Double, name: String): SheetSettingsPane {
        settings?.bindDouble(property, key(name))
        entries().add { configurator ->
            configurator.addControl(labelKey, TickerSlider().apply {
                this.min = min
                this.max = max
                isSnapToTicks = false
                value = property.get().coerceIn(min, max)
                valueProperty().bindBidirectional(property)
            })
        }
        return this
    }

    /** Adds an on/off switch for [property], labelled with [labelKey] and stored as [name]. */
    fun toggle(labelKey: String, property: BooleanProperty, name: String): SheetSettingsPane {
        settings?.bindBoolean(property, key(name))
        entries().add { configurator -> configurator.addToggle(labelKey, property) }
        return this
    }

    /** Entries of the current dialog; the first control without [dialog] opens the general "Settings" one. */
    private fun entries(): MutableList<(SheetConfigurator) -> Unit> {
        val dialog = currentDialog ?: newDialog("sheet.action.settings", viewTitle).also { currentDialog = it }
        return dialog.entries
    }

    private fun newDialog(buttonKey: String, title: () -> String): DialogEntries {
        val dialog = DialogEntries()
        dialogButtons.children.add(Button().apply {
            textProperty().bind(I18n.binding(bundle, buttonKey))
            setOnAction {
                val configurator = SheetConfigurator(bundle)
                dialog.entries.forEach { it(configurator) }
                configurator.toDialog(title(), scene?.window).showAndWait()
            }
        })
        return dialog
    }

    /** Builds one dialog's content; one entry per registered section or control, in order. */
    private class DialogEntries {
        val entries = mutableListOf<(SheetConfigurator) -> Unit>()
    }

    private fun key(name: String): String = "view.$viewKey.$name"

    /** Dialog content: section titles and labelled controls, laid out like the piano configurators. */
    private class SheetConfigurator(bundle: be.codewriter.melodymatrix.view.i18n.BundleRef) :
        BaseConfigurator(bundle) {

        fun addSection(titleKey: String) {
            contentBox.children.add(sectionTitle(titleKey))
        }

        fun addControl(labelKey: String, control: Node) {
            contentBox.children.add(labeledControl(labelKey, control))
        }

        fun addToggle(labelKey: String, property: BooleanProperty) {
            val toggle = stableToggle(visibleHiddenBinding(property)).apply {
                selectedProperty().bindBidirectional(property)
                labelPosition = HorizontalDirection.RIGHT
            }
            contentBox.children.add(labeledControl(labelKey, toggle))
        }
    }

    companion object {
        const val BUNDLE_BASE_NAME: String = "i18n/view/sheet"

        /** Natural height of the toolbar. */
        const val TOOLBAR_HEIGHT: Double = 44.0
    }
}
