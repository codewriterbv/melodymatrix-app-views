package be.codewriter.melodymatrix.view.stage.midi

import be.codewriter.melodymatrix.view.VisualizerStage
import be.codewriter.melodymatrix.view.data.MidiData
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.Scene
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class MidiStage : VisualizerStage() {

    var midiDataList: ObservableList<MidiData> = FXCollections.observableArrayList()

    init {
        val table: TableView<MidiData> = TableView<MidiData>()

        val eventCol: TableColumn<MidiData, String> = TableColumn<MidiData, String>("Event")
        eventCol.cellValueFactory = PropertyValueFactory("event")

        val isNoteOnCol: TableColumn<MidiData, String> = TableColumn<MidiData, String>("On")
        isNoteOnCol.cellValueFactory = PropertyValueFactory("isNoteOn")

        val noteCol: TableColumn<MidiData, String> = TableColumn<MidiData, String>("Note")
        noteCol.cellValueFactory = PropertyValueFactory("note")

        table.items = midiDataList
        table.columns.addAll(eventCol, isNoteOnCol, noteCol)

        title = "MIDI data received from the instrument"
        scene = Scene(table, 610.0, 200.0)

        setOnCloseRequest {
            // Nothing needed here, but must be defined or will cause a problem when closing the window
        }
    }

    override fun onMidiDataReceived(midiData: MidiData) {
        Platform.runLater {
            logger.info(
                "Received note {} {}",
                midiData.note,
                (if (midiData.isNoteOn) "ON" else "OFF")
            )
            midiDataList.addFirst(midiData)
        }
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(MidiStage::class.java.name)
    }
}