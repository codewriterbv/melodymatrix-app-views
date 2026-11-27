package be.codewriter.melodymatrix.view.component

import atlantafx.base.theme.Styles
import javafx.beans.property.ObjectProperty
import javafx.beans.value.ObservableStringValue
import javafx.scene.control.ToggleButton
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.HBox

/**
 * A row of joined "pill" buttons that switches [property] between [options], for example `Notes | Chords | Auto`.
 *
 * One option is always selected: clicking the selected button again keeps it selected. The buttons follow the
 * property when it changes elsewhere (settings restore). Labels are live bindings so they follow the language.
 *
 * @param labelOf live label of an option
 */
class PillSwitch<T : Any>(
    options: List<T>,
    property: ObjectProperty<T>,
    labelOf: (T) -> ObservableStringValue,
    controlHeight: Double = MmxToolbar.CONTROL_HEIGHT
) : HBox() {

    init {
        val group = ToggleGroup()
        val buttons = options.mapIndexed { index, option ->
            ToggleButton().apply {
                textProperty().bind(labelOf(option))
                toggleGroup = group
                userData = option
                isSelected = property.get() == option
                minHeight = controlHeight
                prefHeight = controlHeight
                maxHeight = controlHeight
                styleClass.add(
                    when (index) {
                        0 -> Styles.LEFT_PILL
                        options.lastIndex -> Styles.RIGHT_PILL
                        else -> Styles.CENTER_PILL
                    }
                )
            }
        }
        group.selectedToggleProperty().addListener { _, old, selected ->
            if (selected == null) {
                old?.isSelected = true
            } else {
                @Suppress("UNCHECKED_CAST")
                property.set(selected.userData as T)
            }
        }
        property.addListener { _, _, value -> buttons.firstOrNull { it.userData == value }?.isSelected = true }
        children.addAll(buttons)
    }
}
