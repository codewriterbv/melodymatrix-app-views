package be.codewriter.melodymatrix.view.stage.piano.keyboard

import be.codewriter.melodymatrix.view.definition.Note
import javafx.geometry.Point2D


interface Key {
    fun note(): Note

    fun position(): Point2D

    fun update(pressed: Boolean)
}