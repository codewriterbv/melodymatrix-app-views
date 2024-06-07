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

    var midiData: ObservableList<MidiData> = FXCollections.observableArrayList()

    init {
        val table: TableView<MidiData> = TableView<MidiData>()

        val firstNameCol: TableColumn<MidiData, String> = TableColumn<MidiData, String>("First Name")
        firstNameCol.cellValueFactory = PropertyValueFactory<MidiData, String>("firstName")

        val lastNameCol: TableColumn<MidiData, String> = TableColumn<MidiData, String>("Last Name")
        lastNameCol.cellValueFactory = PropertyValueFactory<MidiData, String>("lastName")

        table.items = midiData
        table.columns.addAll(firstNameCol, lastNameCol)

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

        }
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(MidiStage::class.java.name)
    }
}