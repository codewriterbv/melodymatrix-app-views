package be.codewriter.melodymatrix.view.component

import be.codewriter.melodymatrix.view.i18n.BundleRef
import be.codewriter.melodymatrix.view.i18n.I18n
import be.codewriter.melodymatrix.view.view.piano.configurator.BaseConfigurator
import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.ObjectProperty
import javafx.geometry.HorizontalDirection
import javafx.scene.Node
import javafx.scene.control.ColorPicker
import javafx.scene.control.ComboBox
import javafx.scene.control.ListCell
import javafx.scene.layout.Region
import javafx.scene.paint.Color

/**
 * The content of a settings dialog, laid out like the piano configurators: section titles and labelled controls
 * under each other. Every view builds its dialogs from this one class (see [MmxToolbar.settingsButton]), so all
 * settings look and behave the same. All labels are i18n keys of [bundle].
 *
 * The form edits the given properties directly, so changes show live in the view behind the dialog.
 */
class MmxSettingsForm(bundle: BundleRef) : BaseConfigurator(bundle) {

    /** A bold title that starts a group of controls. */
    fun section(titleKey: String): MmxSettingsForm = apply { contentBox.children.add(sectionTitle(titleKey)) }

    /** Any control under a label. */
    fun control(labelKey: String, control: Node): MmxSettingsForm =
        apply { contentBox.children.add(labeledControl(labelKey, control)) }

    /** An on/off switch. */
    fun toggle(labelKey: String, property: BooleanProperty): MmxSettingsForm = apply {
        val toggle = stableToggle(visibleHiddenBinding(property)).apply {
            selectedProperty().bindBidirectional(property)
            labelPosition = HorizontalDirection.RIGHT
        }
        contentBox.children.add(labeledControl(labelKey, toggle))
    }

    /** A slider between [min] and [max] with tick labels. */
    fun slider(labelKey: String, property: DoubleProperty, min: Double, max: Double): MmxSettingsForm = apply {
        contentBox.children.add(labeledControl(labelKey, TickerSlider().apply {
            this.min = min
            this.max = max
            isSnapToTicks = false
            value = property.get().coerceIn(min, max)
            valueProperty().bindBidirectional(property)
        }))
    }

    /** A slider for whole numbers between [min] and [max]. */
    fun integerSlider(labelKey: String, property: IntegerProperty, min: Int, max: Int): MmxSettingsForm = apply {
        val slider = TickerSlider().apply {
            this.min = min.toDouble()
            this.max = max.toDouble()
            isSnapToTicks = false
            value = property.get().coerceIn(min, max).toDouble()
        }
        slider.valueProperty().addListener { _, _, v -> property.set(v.toInt()) }
        property.addListener { _, _, v -> if (slider.value.toInt() != v.toInt()) slider.value = v.toDouble() }
        contentBox.children.add(labeledControl(labelKey, slider))
    }

    /** A colour picker. */
    fun color(labelKey: String, property: ObjectProperty<Color>): MmxSettingsForm = apply {
        contentBox.children.add(labeledControl(labelKey, ColorPicker(property.get()).apply {
            valueProperty().bindBidirectional(property)
        }))
    }

    /** A drop-down with [items]; [itemKey] gives the i18n key of the label of an item. */
    fun <T> choice(
        labelKey: String,
        property: ObjectProperty<T>,
        items: List<T>,
        itemKey: (T) -> String
    ): MmxSettingsForm = apply {
        val combo = ComboBox<T>().apply {
            this.items.setAll(items)
            valueProperty().bindBidirectional(property)
            val cell = {
                object : ListCell<T>() {
                    override fun updateItem(item: T?, empty: Boolean) {
                        super.updateItem(item, empty)
                        textProperty().unbind()
                        if (empty || item == null) text = null
                        else textProperty().bind(I18n.binding(labelBundle, itemKey(item)))
                    }
                }
            }
            setCellFactory { cell() }
            buttonCell = cell()
            maxWidth = Double.MAX_VALUE
        }
        contentBox.children.add(labeledControl(labelKey, combo))
    }

    /** Adds every control of [other] below this form's own, to show several dialogs as one panel. */
    fun append(other: Region): MmxSettingsForm = apply { contentBox.children.add(other) }
}
