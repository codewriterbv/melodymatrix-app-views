package be.codewriter.melodymatrix.view.test

import be.codewriter.melodymatrix.view.data.Note
import be.codewriter.melodymatrix.view.data.Octave
import javafx.beans.value.ObservableValue
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Slider
import javafx.scene.layout.VBox

class TestViewMusicSelection(val midiSimulator: MidiSimulator) : VBox() {

    init {
        spacing = 10.0

        children.setAll(
            createDurationSlider(),
            createButton("All Notes", Note.entries
                .toList()
                .sortedWith(compareBy({ it.octave }, { it.mainNote.sortingKey }))
            ),
            createButton("Octave 5", Note.entries.stream()
                .filter { n -> n.octave == Octave.OCTAVE_5 }
                .toList()
                .sortedBy { it.mainNote.sortingKey })
        )
    }

    private fun createDurationSlider(): Slider {
        return Slider().apply {
            min = 150.0
            max = 2000.0
            value = 500.0
            blockIncrement = 5.0
            isShowTickMarks = true
            isShowTickLabels = true
            valueProperty().addListener { _: ObservableValue<out Number>?, _: Number, newValue: Number ->
                midiSimulator.setDelay(newValue.toLong())
            }
        }
    }

    private fun createButton(label: String, notes: List<Note>): Node {
        val view = Button(label).apply {
            minWidth = 200.0
            setOnMouseClicked { _ ->
                midiSimulator.setNotes(notes)
            }
        }

        return view
    }
}
