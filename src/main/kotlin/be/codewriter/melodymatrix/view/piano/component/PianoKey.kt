package be.codewriter.melodymatrix.view.piano.component

import be.codewriter.melodymatrix.app.data.Note
import javafx.geometry.Point2D

interface PianoKey {

    fun note(): Note

    fun position(): Point2D

    fun update(isPressed: Boolean)
}