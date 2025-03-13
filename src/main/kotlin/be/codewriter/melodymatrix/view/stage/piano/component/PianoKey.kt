package be.codewriter.melodymatrix.view.stage.piano.component

import javafx.geometry.Point2D


interface PianoKey {
    fun note(): be.codewriter.melodymatrix.view.definition.Note

    fun position(): Point2D

    fun update(isPressed: Boolean)
}