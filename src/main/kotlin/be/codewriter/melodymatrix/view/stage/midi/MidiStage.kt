package be.codewriter.melodymatrix.view.stage.midi

import be.codewriter.melodymatrix.view.data.MidiData
import be.codewriter.melodymatrix.view.data.PlayEvent
import be.codewriter.melodymatrix.view.definition.MidiEvent
import javafx.application.Platform
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class MidiStage : be.codewriter.melodymatrix.view.VisualizerStage() {

    val midiDataList: ObservableList<MidiData> =
        FXCollections.observableArrayList()
    val lastInstrument: StringProperty = SimpleStringProperty("")
    val midiData0Value: StringProperty = SimpleStringProperty("")
    val midiData0Bits: StringProperty = SimpleStringProperty("")
    val midiDataType: ObjectProperty<MidiEvent> = SimpleObjectProperty(MidiEvent.UNDEFINED)
    val midiDataEvent: StringProperty = SimpleStringProperty("")
    val midiData1Value: StringProperty = SimpleStringProperty("")
    val midiData1Bits: StringProperty = SimpleStringProperty("")
    val midiData2Value: StringProperty = SimpleStringProperty("")
    val midiData2Bits: StringProperty = SimpleStringProperty("")
    val controllerNumber: StringProperty = SimpleStringProperty("")
    val controllerNumberBits: StringProperty = SimpleStringProperty("")
    val controllerValue: StringProperty = SimpleStringProperty("")
    val controllerValueBits: StringProperty = SimpleStringProperty("")

    init {
        val table: TableView<MidiData> =
            TableView<MidiData>().apply {
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
            }

        val midiInfo = GridPane().apply {
            hgap = 5.0
            vgap = 5.0
            padding = Insets(25.0)

            add(Button("Clear table").apply {
                setOnMouseClicked { _ ->
                    midiDataList.clear()
                }
            }, 0, 0, 3, 1)

            add(Label("Last MIDI data").apply {
                style = "-fx-font-size: 20px; -fx-font-weight: bold;"
            }, 0, 2, 3, 1)

            add(Label("Midi Status"), 0, 3)
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
            /*add(Label().apply {
                textProperty().bind(midiDataEvent.name)
                style = "-fx-font-family: 'Courier New', Courier, monospace; -fx-font-weight: bold;"
                GridPane.setHalignment(this, HPos.RIGHT)
            }, 2, 4)*/

            add(Label("Channel"), 1, 5)
            /*add(Label().apply {
                textProperty().bind(midiDataChannel)
                style = "-fx-font-family: 'Courier New', Courier, monospace; -fx-font-weight: bold;"
                GridPane.setHalignment(this, HPos.RIGHT)
            }, 2, 5)*/


            add(Label("Midi Data 1"), 0, 7)
            add(Label("Value").apply {
                textProperty().bind(midiData1Value)
                style = "-fx-font-family: 'Courier New', Courier, monospace; -fx-font-weight: bold;"
                GridPane.setHalignment(this, HPos.RIGHT)
            }, 1, 7)
            add(Label("Bits").apply {
                textProperty().bind(midiData1Bits)
                style = "-fx-font-family: 'Courier New', Courier, monospace; -fx-font-weight: bold;"
                GridPane.setHalignment(this, HPos.RIGHT)
            }, 2, 7)

            add(Label("Midi Data 2"), 0, 8)
            add(Label("Value").apply {
                textProperty().bind(midiData2Value)
                style = "-fx-font-family: 'Courier New', Courier, monospace; -fx-font-weight: bold;"
                GridPane.setHalignment(this, HPos.RIGHT)
            }, 1, 8)
            add(Label("Bits").apply {
                textProperty().bind(midiData2Bits)
                style = "-fx-font-family: 'Courier New', Courier, monospace; -fx-font-weight: bold;"
                GridPane.setHalignment(this, HPos.RIGHT)
            }, 2, 8)

            add(Label("Instrument change").apply {
                style = "-fx-font-size: 20px; -fx-font-weight: bold;"
            }, 0, 9, 3, 1)

            add(Label("Last selected instrument"), 0, 10)
            add(Label().apply {
                textProperty().bind(lastInstrument)
                style = "-fx-font-family: 'Courier New', Courier, monospace; -fx-font-weight: bold;"
                GridPane.setHalignment(this, HPos.RIGHT)
            }, 1, 10)

            add(Label("Last controller message").apply {
                style = "-fx-font-size: 20px; -fx-font-weight: bold;"
            }, 0, 11, 3, 1)

            add(Label("Controller number"), 0, 12)
            add(Label("Value").apply {
                textProperty().bind(controllerNumber)
                style = "-fx-font-family: 'Courier New', Courier, monospace; -fx-font-weight: bold;"
                GridPane.setHalignment(this, HPos.RIGHT)
            }, 1, 12)
            add(Label("Value").apply {
                textProperty().bind(controllerNumberBits)
                style = "-fx-font-family: 'Courier New', Courier, monospace; -fx-font-weight: bold;"
                GridPane.setHalignment(this, HPos.RIGHT)
            }, 2, 12)

            add(Label("Controller value"), 0, 13)
            add(Label("Value").apply {
                textProperty().bind(controllerValue)
                style = "-fx-font-family: 'Courier New', Courier, monospace; -fx-font-weight: bold;"
                GridPane.setHalignment(this, HPos.RIGHT)
            }, 1, 13)
            add(Label("Value").apply {
                textProperty().bind(controllerValueBits)
                style = "-fx-font-family: 'Courier New', Courier, monospace; -fx-font-weight: bold;"
                GridPane.setHalignment(this, HPos.RIGHT)
            }, 2, 13)
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

            midiData0Value.set(midiData.bytes[0].toString())
            midiData0Bits.set(byteToBitsString(midiData.bytes[0]))
            //midiDataEvent.set(midiData.event)
            //midiDataChannel.set(midiData.channel)
            midiData1Value.set(midiData.bytes[1].toString())
            midiData1Bits.set(byteToBitsString(midiData.bytes[1]))
            midiData2Value.set(midiData.bytes[2].toString())
            midiData2Bits.set(byteToBitsString(midiData.bytes[2]))

            if (midiData.event == MidiEvent.SELECT_INSTRUMENT) {
                lastInstrument.set(midiData.instrument.toString())
            } else if (midiData.event == MidiEvent.CONTROLLER) {
                controllerNumber.set(midiData.controllerNumber.toString())
                controllerNumberBits.set(byteToBitsString(midiData.bytes[1]))
                controllerValue.set(midiData.controllerValue.toString())
                controllerValueBits.set(byteToBitsString(midiData.bytes[2]))
            }
        }
    }

    override fun onPlayEvent(playEvent: PlayEvent) {
        // Not needed here
    }

    fun byteToBitsString(byte: Byte): String {
        return String.format("%8s", Integer.toBinaryString(byte.toInt() and 0xFF)).replace(' ', '0')
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(MidiStage::class.java.name)
    }
}