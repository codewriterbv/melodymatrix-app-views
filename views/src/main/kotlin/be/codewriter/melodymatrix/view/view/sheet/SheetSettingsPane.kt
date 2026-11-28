package be.codewriter.melodymatrix.view.view.sheet

import be.codewriter.melodymatrix.view.component.MmxToolbar
import be.codewriter.melodymatrix.view.component.PillSwitch
import be.codewriter.melodymatrix.view.component.TickerSlider
import be.codewriter.melodymatrix.view.component.toolbarHeight
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
import javafx.scene.control.ComboBox
import javafx.scene.control.Spinner
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
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
) : HBox(8.0) {

    private val bundle = I18n.registerBundle(BUNDLE_BASE_NAME)
    private val toolbarItems = HBox(8.0).apply { alignment = Pos.CENTER_LEFT }
    private val dialogButtons = HBox(8.0).apply { alignment = Pos.CENTER_LEFT }

    /** The dialog that new sections and controls are added to; created on first use. */
    private var currentDialog: DialogEntries? = null

    init {
        // The standard top bar of every view (see MmxToolbar): left aligned, the settings buttons after the view's own controls
        alignment = Pos.CENTER_LEFT
        padding = MmxToolbar.BAR_PADDING
        styleClass.add(MmxToolbar.STYLE_CLASS)
        children.addAll(toolbarItems, dialogButtons)
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
        toolbarItems.children.add(
            PillSwitch(
                options = SheetDisplayMode.entries,
                property = property,
                labelOf = { I18n.binding(bundle, "sheet.mode.${it.name.lowercase()}") },
            )
        )
        return this
    }

    /** Adds [nodes] (controls of the view itself) to the toolbar. */
    fun toolbar(vararg nodes: Node): SheetSettingsPane {
        nodes.forEach(::standardHeight)
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

    /** The dialogs by button key, so the video recorder can show their content beside the view. */
    private val dialogsByKey = mutableMapOf<String, DialogEntries>()

    /** The content of the settings dialog `sheet.action.<name>` (`colors`, `camera`, `settings`...) as a node, or null. */
    fun demoPanel(name: String): Node? {
        val dialog = dialogsByKey["sheet.action.$name"] ?: return null
        return SheetConfigurator(bundle).also { configurator -> dialog.entries.forEach { it(configurator) } }
    }

    private fun newDialog(buttonKey: String, title: () -> String): DialogEntries {
        val dialog = DialogEntries()
        dialogsByKey[buttonKey] = dialog
        dialogButtons.children.add(Button().apply {
            toolbarHeight()
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

    /** Gives the buttons, drop-downs and spinners in [node] the height of the other toolbar controls. */
    private fun standardHeight(node: Node) {
        if (node is Button || node is ComboBox<*> || node is Spinner<*>) (node as Region).toolbarHeight()
        if (node is Pane) node.children.forEach(::standardHeight)
    }

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

        /** Natural height of the toolbar with the padding of the view frame around it (see [MmxToolbar.frame]). */
        const val TOOLBAR_HEIGHT: Double = 68.0
    }
}
