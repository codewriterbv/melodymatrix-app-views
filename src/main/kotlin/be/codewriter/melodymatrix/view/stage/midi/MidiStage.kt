package be.codewriter.melodymatrix.view.stage.midi

import be.codewriter.melodymatrix.view.data.MidiData
import be.codewriter.melodymatrix.view.data.PlayEvent
import be.codewriter.melodymatrix.view.definition.MidiEvent
import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger


class MidiStage : be.codewriter.melodymatrix.view.VisualizerStage() {

    val midiDataList: ObservableList<MidiData> = FXCollections.observableArrayList()
    val midiData0Value: StringProperty = SimpleStringProperty("")
    val midiData0Bits: StringProperty = SimpleStringProperty("")
    val midiDataEventValue: StringProperty = SimpleStringProperty(MidiEvent.UNDEFINED.name)
    val midiDataChannelValue: StringProperty = SimpleStringProperty("")
    val midiData1Value: StringProperty = SimpleStringProperty("")
    val midiData1Bits: StringProperty = SimpleStringProperty("")
    val midiData2Value: StringProperty = SimpleStringProperty("")
    val midiData2Bits: StringProperty = SimpleStringProperty("")
    val noteValue: StringProperty = SimpleStringProperty("")
    val noteValueBits: StringProperty = SimpleStringProperty("")
    val noteVelocityValue: StringProperty = SimpleStringProperty("")
    val noteVelocityValueBits: StringProperty = SimpleStringProperty("")
    val lastInstrument: StringProperty = SimpleStringProperty("")
    val controllerNumber: StringProperty = SimpleStringProperty("")
    val controllerNumberBits: StringProperty = SimpleStringProperty("")
    val controllerValue: StringProperty = SimpleStringProperty("")
    val controllerValueBits: StringProperty = SimpleStringProperty("")
    val pitchBendValue: StringProperty = SimpleStringProperty("")
    val pitchBendValueBits: StringProperty = SimpleStringProperty("")
    val table: TableView<MidiData> = TableView<MidiData>()

    init {
        table.apply {
            items = midiDataList
            columns.addAll(
                TableColumn<MidiData, String>("Event").apply {
                    cellValueFactory = PropertyValueFactory("event")
                    prefWidth = 200.0
                },
                TableColumn<MidiData, String>("Note").apply {
                    cellValueFactory = PropertyValueFactory("note")
                    prefWidth = 150.0
                },
                TableColumn<MidiData, String>("Velocity").apply {
                    cellValueFactory = PropertyValueFactory("velocity")
                    prefWidth = 100.0
                }
            )
            selectionModel.selectedItemProperty().addListener { _, _, newValue ->
                if (newValue != null) {
                    showValues(newValue)
                }
            }
        }

        val midiInfo = GridPane().apply {
            hgap = 5.0
            vgap = 5.0
            padding = Insets(25.0)
            isGridLinesVisible = false
            columnConstraints.addAll(
                ColumnConstraints(200.0),
                ColumnConstraints(100.0),
                ColumnConstraints(150.0)
            )

            add(Button("Clear table").apply {
                setOnMouseClicked { _ ->
                    midiDataList.clear()
                }
            }, 0, 0, 3, 1)

            add(Label("MIDI data").apply {
                style = "-fx-font-size: 20px; -fx-font-weight: bold;"
            }, 0, 2, 3, 1)

            add(Label("Data[0]: Midi Status"), 0, 3)
            add(Label().apply {
                textProperty().bind(midiData0Value)
                style = "-fx-font-family: 'Courier New', Courier, monospace; -fx-font-weight: bold;"
                GridPane.setHalignment(this, HPos.RIGHT)
            }, 1, 3)
            add(Label().apply {
                textProperty().bind(midiData0Bits)
                style = "-fx-font-family: 'Courier New', Courier, monospace; -fx-font-weight: bold;"
                GridPane.setHalignment(this, HPos.RIGHT)
            }, 2, 3)

            add(Label("Event"), 1, 4)
            add(Label().apply {
                textProperty().bind(midiDataEventValue)
                style = "-fx-font-family: 'Courier New', Courier, monospace; -fx-font-weight: bold;"
                GridPane.setHalignment(this, HPos.RIGHT)
            }, 2, 4)

            add(Label("Channel"), 1, 5)
            add(Label().apply {
                textProperty().bind(midiDataChannelValue)
                style = "-fx-font-family: 'Courier New', Courier, monospace; -fx-font-weight: bold;"
                GridPane.setHalignment(this, HPos.RIGHT)
            }, 2, 5)

            add(Label("Data[1]"), 0, 6)
            add(Label("Value").apply {
                textProperty().bind(midiData1Value)
                style = "-fx-font-family: 'Courier New', Courier, monospace; -fx-font-weight: bold;"
                GridPane.setHalignment(this, HPos.RIGHT)
            }, 1, 6)
            add(Label("Bits").apply {
                textProperty().bind(midiData1Bits)
                style = "-fx-font-family: 'Courier New', Courier, monospace; -fx-font-weight: bold;"
                GridPane.setHalignment(this, HPos.RIGHT)
            }, 2, 6)

            add(Label("Data[2]"), 0, 7)
            add(Label("Value").apply {
                textProperty().bind(midiData2Value)
                style = "-fx-font-family: 'Courier New', Courier, monospace; -fx-font-weight: bold;"
                GridPane.setHalignment(this, HPos.RIGHT)
            }, 1, 7)
            add(Label("Bits").apply {
                textProperty().bind(midiData2Bits)
                style = "-fx-font-family: 'Courier New', Courier, monospace; -fx-font-weight: bold;"
                GridPane.setHalignment(this, HPos.RIGHT)
            }, 2, 7)

            add(Separator(), 0, 8, 3, 1)

            add(Label("Data for last events").apply {
                style = "-fx-font-size: 20px; -fx-font-weight: bold;"
            }, 0, 9, 3, 1)

            add(Label("Note On or Off").apply {
                style = "-fx-font-size: 16px; -fx-font-weight: bold;"
            }, 0, 10, 3, 1)

            add(Label("Note"), 0, 11)
            add(Label("Value").apply {
                textProperty().bind(noteValue)
                style = "-fx-font-family: 'Courier New', Courier, monospace; -fx-font-weight: bold;"
                GridPane.setHalignment(this, HPos.RIGHT)
            }, 1, 11)
            add(Label("Value").apply {
                textProperty().bind(noteValueBits)
                style = "-fx-font-family: 'Courier New', Courier, monospace; -fx-font-weight: bold;"
                GridPane.setHalignment(this, HPos.RIGHT)
            }, 2, 11)

            add(Label("Note velocity"), 0, 12)
            add(Label("Value").apply {
                textProperty().bind(noteVelocityValue)
                style = "-fx-font-family: 'Courier New', Courier, monospace; -fx-font-weight: bold;"
                GridPane.setHalignment(this, HPos.RIGHT)
            }, 1, 12)
            add(Label("Value").apply {
                textProperty().bind(noteVelocityValueBits)
                style = "-fx-font-family: 'Courier New', Courier, monospace; -fx-font-weight: bold;"
                GridPane.setHalignment(this, HPos.RIGHT)
            }, 2, 12)

            add(Label("Instrument change").apply {
                style = "-fx-font-size: 16px; -fx-font-weight: bold;"
            }, 0, 13, 3, 1)

            add(Label("Selected instrument"), 0, 14)
            add(Label().apply {
                textProperty().bind(lastInstrument)
                style = "-fx-font-family: 'Courier New', Courier, monospace; -fx-font-weight: bold;"
                GridPane.setHalignment(this, HPos.RIGHT)
            }, 1, 14)

            add(Label("Controller message").apply {
                style = "-fx-font-size: 16px; -fx-font-weight: bold;"
            }, 0, 15, 3, 1)

            add(Label("Controller number"), 0, 16)
            add(Label("Value").apply {
                textProperty().bind(controllerNumber)
                style = "-fx-font-family: 'Courier New', Courier, monospace; -fx-font-weight: bold;"
                GridPane.setHalignment(this, HPos.RIGHT)
            }, 1, 16)
            add(Label("Value").apply {
                textProperty().bind(controllerNumberBits)
                style = "-fx-font-family: 'Courier New', Courier, monospace; -fx-font-weight: bold;"
                GridPane.setHalignment(this, HPos.RIGHT)
            }, 2, 16)

            add(Label("Controller value"), 0, 17)
            add(Label("Value").apply {
                textProperty().bind(controllerValue)
                style = "-fx-font-family: 'Courier New', Courier, monospace; -fx-font-weight: bold;"
                GridPane.setHalignment(this, HPos.RIGHT)
            }, 1, 17)
            add(Label("Value").apply {
                textProperty().bind(controllerValueBits)
                style = "-fx-font-family: 'Courier New', Courier, monospace; -fx-font-weight: bold;"
                GridPane.setHalignment(this, HPos.RIGHT)
            }, 2, 17)

            add(Label("Pitch bend message").apply {
                style = "-fx-font-size: 16px; -fx-font-weight: bold;"
            }, 0, 18, 3, 1)

            add(Label("Pitch bend"), 0, 19)
            add(Label("Value").apply {
                textProperty().bind(pitchBendValue)
                style = "-fx-font-family: 'Courier New', Courier, monospace; -fx-font-weight: bold;"
                GridPane.setHalignment(this, HPos.RIGHT)
            }, 1, 19)
            add(Label("Value").apply {
                textProperty().bind(pitchBendValueBits)
                style = "-fx-font-family: 'Courier New', Courier, monospace; -fx-font-weight: bold;"
                GridPane.setHalignment(this, HPos.RIGHT)
            }, 2, 19)
        }

        title = "MIDI data received from the instrument"
        scene = Scene(HBox().apply {
            spacing = 10.0
            padding = Insets(25.0)
            children.addAll(table, midiInfo)
        }, 1000.0, 800.0)

        setOnCloseRequest {
            // Nothing needed here, but must be defined or will cause a problem when closing the window
        }
    }

    override fun onMidiData(midiData: MidiData) {
        Platform.runLater {
            midiDataList.addFirst(midiData)
            table.selectionModel.selectFirst()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun showValues(midiData: MidiData) {
        Platform.runLater {
            midiData0Value.set(midiData.bytes[0].toString())
            midiData0Bits.set(byteToBitsString(midiData.bytes[0]))
            midiDataEventValue.set(midiData.event.name)
            midiDataChannelValue.set(midiData.channel.toString())
            midiData1Value.set(midiData.bytes[1].toString())
            midiData1Bits.set(byteToBitsString(midiData.bytes[1]))
            midiData2Value.set(midiData.bytes[2].toString())
            midiData2Bits.set(byteToBitsString(midiData.bytes[2]))

            if (midiData.event == MidiEvent.NOTE_ON || midiData.event == MidiEvent.NOTE_OFF) {
                noteValue.set(midiData.note.name)
                noteValueBits.set(byteToBitsString(midiData.bytes[1]))
                noteVelocityValue.set(midiData.velocity.toString())
                noteVelocityValueBits.set(byteToBitsString(midiData.bytes[2]))
            } else if (midiData.event == MidiEvent.SELECT_INSTRUMENT) {
                lastInstrument.set(midiData.instrument.toString())
            } else if (midiData.event == MidiEvent.CONTROLLER) {
                controllerNumber.set(midiData.controllerNumber.toString())
                controllerNumberBits.set(byteToBitsString(midiData.bytes[1]))
                controllerValue.set(midiData.controllerValue.toString())
                controllerValueBits.set(byteToBitsString(midiData.bytes[2]))
            } else if (midiData.event == MidiEvent.PITCH_BEND) {
                pitchBendValue.set(midiData.pitch.toHexString())
                pitchBendValueBits.set(byteToBitsString(midiData.bytes[1]) + " " + byteToBitsString(midiData.bytes[2]))
            }
        }
    }

    override fun onPlayEvent(playEvent: PlayEvent) {
        // Not needed here
    }

    fun byteToBitsString(byte: Byte): String {
        val bits = String
            .format("%8s", Integer.toBinaryString(byte.toInt() and 0xFF))
            .replace(' ', '0')
        return bits.substring(0, 4) + " " + bits.substring(4, 8)
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(MidiStage::class.java.name)
    }
}