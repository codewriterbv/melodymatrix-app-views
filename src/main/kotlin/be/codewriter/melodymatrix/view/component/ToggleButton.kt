package be.codewriter.melodymatrix.view.component

import atlantafx.base.controls.ToggleSwitch
import javafx.beans.property.BooleanProperty
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
 * @param title Display text for the button label
 * @param property The [BooleanProperty] to bind to and toggle
 * @param controlHeight Height in pixels; defaults to 40.0
 */
class ToggleButton(
    title: String,
    property: BooleanProperty,
    controlHeight: Double = 40.0
) : Button(title) {
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
            property.set(!property.get())
        }
    }
}

