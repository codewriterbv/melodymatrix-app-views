package be.codewriter.melodymatrix.view.component

import atlantafx.base.controls.ToggleSwitch
import javafx.beans.property.BooleanProperty
import javafx.beans.value.ObservableStringValue
import javafx.event.ActionEvent
import javafx.scene.control.Button
import javafx.scene.control.ContentDisplay
import javafx.scene.input.MouseEvent

/**
 * A reusable toggle button combining a text label with a [ToggleSwitch] graphic.
 *
 * The button's text serves as the label, while the toggle switch appears to the right.
 * Clicking either the button or switch toggles the underlying [BooleanProperty].
 * Event handlers consume mouse/action events so the switch and button don't conflict.
 *
 * Two label modes are supported:
 *  - **static** — pass a plain [String] via the primary constructor when the label
 *    never changes (test/dev surfaces);
 *  - **live** — pass an [ObservableStringValue] via the secondary constructor when
 *    the label must follow the active i18n locale.
 *
 * @param titleBinding Live [ObservableStringValue] for the button label.
 * @param property The [BooleanProperty] to bind to and toggle.
 * @param controlHeight Height in pixels; defaults to 40.0.
 * @param onButtonAction Optional callback invoked when the button (not the switch) is pressed.
 * @param toggleOnButtonAction When true, the underlying property flips on button press
 *        (in addition to any [onButtonAction]).
 */
class ToggleButton private constructor(
    property: BooleanProperty,
    controlHeight: Double,
    onButtonAction: (() -> Unit)?,
    toggleOnButtonAction: Boolean
) : Button() {

    /** Static-label constructor: label text never changes after construction. */
    constructor(
        title: String,
        property: BooleanProperty,
        controlHeight: Double = 40.0,
        onButtonAction: (() -> Unit)? = null,
        toggleOnButtonAction: Boolean = true
    ) : this(property, controlHeight, onButtonAction, toggleOnButtonAction) {
        text = title
    }

    /** Live-label constructor: label follows [titleBinding]. */
    constructor(
        titleBinding: ObservableStringValue,
        property: BooleanProperty,
        controlHeight: Double = 40.0,
        onButtonAction: (() -> Unit)? = null,
        toggleOnButtonAction: Boolean = true
    ) : this(property, controlHeight, onButtonAction, toggleOnButtonAction) {
        textProperty().bind(titleBinding)
    }

    init {
        val toggle = ToggleSwitch().apply {
            selectedProperty().bindBidirectional(property)
            addEventHandler(MouseEvent.MOUSE_PRESSED) { it.consume() }
            addEventHandler(MouseEvent.MOUSE_RELEASED) { it.consume() }
            addEventHandler(MouseEvent.MOUSE_CLICKED) { it.consume() }
            addEventHandler(ActionEvent.ACTION) { it.consume() }
        }

        graphic = toggle
        contentDisplay = ContentDisplay.RIGHT
        graphicTextGap = 10.0
        minHeight = controlHeight
        prefHeight = controlHeight
        maxHeight = controlHeight
        setOnAction {
            if (toggleOnButtonAction) {
                property.set(!property.get())
            }
            onButtonAction?.invoke()
        }
    }
}
