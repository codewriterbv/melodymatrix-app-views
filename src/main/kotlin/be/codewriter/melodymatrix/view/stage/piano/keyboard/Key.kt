package be.codewriter.melodymatrix.view.stage.piano.keyboard

import be.codewriter.melodymatrix.view.definition.Note


interface Key {
    fun note(): Note

    fun keyX(): Double

    fun update(pressed: Boolean)
}