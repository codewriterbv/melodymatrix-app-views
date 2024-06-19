package be.codewriter.melodymatrix.view.stage.piano.component

import be.codewriter.melodymatrix.view.definition.Note
import javafx.geometry.Point2D

interface PianoKey {
    fun note(): Note

    fun position(): Point2D

    fun update(isPressed: Boolean)
}