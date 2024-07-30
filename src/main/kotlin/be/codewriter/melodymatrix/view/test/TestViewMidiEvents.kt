package be.codewriter.melodymatrix.view.test

import be.codewriter.melodymatrix.view.data.MidiData
import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.definition.Octave
import javafx.beans.value.ObservableValue
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.Slider
import javafx.scene.layout.VBox
import java.util.*

class TestViewMidiEvents(val midiSimulator: MidiSimulator) : VBox() {

    init {
        spacing = 10.0

        children.setAll(
            Label("Midi events selection"),
            createDurationSlider(),
            createButton(
                "Play all notes (repeat)", Note.entries
                    .toList()
                    .sortedWith(compareBy({ it.octave }, { it.mainNote.sortingKey })), true
            ),
            createButton(
                "Play all notes random (repeat)", Note.entries
                    .toList()
                    .shuffled(), true
            ),
            createButton(
                "Play C5 only (once)",
                Collections.singletonList(Note.C5),
                false
            ),
            createButton("Play all from octave 5 (repeat)",
                Note.entries.stream()
                    .filter { n -> n.octave == Octave.OCTAVE_5 }
                    .toList()
                    .sortedBy { it.mainNote.sortingKey },
                true
            ),
            createButton("Stop notes"),
            createButton(
                "Set instrument 5",
                MidiData(byteArrayOf("11000100".toInt(2).toByte(), 0x05, 0x00))
            ),
            createButton(
                "Set instrument 9",
                MidiData(byteArrayOf("11000100".toInt(2).toByte(), 0x09, 0x00))
            ),
            createButton(
                "Send controller message",
                MidiData(byteArrayOf("10110000".toInt(2).toByte(), 0x21, 0x34))
            )
        )
    }

    private fun createDurationSlider(): Slider {
        return Slider().apply {
            min = 250.0
            max = 1000.0
            value = 500.0
            blockIncrement = 5.0
            isShowTickMarks = true
            isShowTickLabels = true
            valueProperty().addListener { _: ObservableValue<out Number>?, _: Number, newValue: Number ->
                midiSimulator.setDelay(newValue.toLong())
            }
        }
    }

    private fun createButton(
        label: String,
        notes: List<Note>,
        repeat: Boolean
    ): Node {
        val view = Button(label).apply {
            minWidth = 200.0
            setOnMouseClicked { _ ->
                midiSimulator.setNotes(notes, repeat)
            }
        }

        return view
    }

    private fun createButton(label: String, midiData: MidiData): Node {
        val view = Button(label).apply {
            minWidth = 200.0
            setOnMouseClicked { _ ->
                midiSimulator.notifyListeners(midiData)
            }
        }

        return view
    }

    private fun createButton(label: String): Node {
        val view = Button(label).apply {
            minWidth = 200.0
            setOnMouseClicked { _ ->
                midiSimulator.stop()
            }
        }

        return view
    }
}
