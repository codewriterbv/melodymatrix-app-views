package be.codewriter.melodymatrix.view.stage.midi

import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.event.MidiDataEvent
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.MmxEventType
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

    val midiDataEventList: ObservableList<MidiDataEvent> = FXCollections.observableArrayList()
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
    val table: TableView<MidiDataEvent> = TableView<MidiDataEvent>()

    init {
        table.apply {
            items = midiDataEventList
            columns.addAll(
                TableColumn<MidiDataEvent, String>("Event").apply {
                    cellValueFactory = PropertyValueFactory("event")
                    prefWidth = 200.0
                },
                TableColumn<MidiDataEvent, String>("Note").apply {
                    cellValueFactory = PropertyValueFactory("note")
                    prefWidth = 150.0
                },
                TableColumn<MidiDataEvent, String>("Velocity").apply {
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
                    midiDataEventList.clear()
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

    override fun onEvent(event: MmxEvent) {
        when (event.type) {
            MmxEventType.MIDI -> {
                val midiDataEvent = event as? MidiDataEvent ?: return
                Platform.runLater {
                    midiDataEventList.addFirst(midiDataEvent)
                    table.selectionModel.selectFirst()
                }
            }

            MmxEventType.PLAY -> {
                // Not needed here
            }

            MmxEventType.CHORD -> {
                // Not needed here
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun showValues(midiDataEvent: MidiDataEvent) {
        Platform.runLater {
            midiData0Value.set(midiDataEvent.bytes[0].toString())
            midiData0Bits.set(byteToBitsString(midiDataEvent.bytes[0]))
            midiDataEventValue.set(midiDataEvent.event.name)
            midiDataChannelValue.set(midiDataEvent.channel.toString())
            midiData1Value.set(midiDataEvent.bytes[1].toString())
            midiData1Bits.set(byteToBitsString(midiDataEvent.bytes[1]))
            midiData2Value.set(midiDataEvent.bytes[2].toString())
            midiData2Bits.set(byteToBitsString(midiDataEvent.bytes[2]))

            if (midiDataEvent.event == MidiEvent.NOTE_ON || midiDataEvent.event == MidiEvent.NOTE_OFF) {
                noteValue.set(midiDataEvent.note.name)
                noteValueBits.set(byteToBitsString(midiDataEvent.bytes[1]))
                noteVelocityValue.set(midiDataEvent.velocity.toString())
                noteVelocityValueBits.set(byteToBitsString(midiDataEvent.bytes[2]))
            } else if (midiDataEvent.event == MidiEvent.SELECT_INSTRUMENT) {
                lastInstrument.set(midiDataEvent.instrument.toString())
            } else if (midiDataEvent.event == MidiEvent.CONTROLLER) {
                controllerNumber.set(midiDataEvent.controllerNumber.toString())
                controllerNumberBits.set(byteToBitsString(midiDataEvent.bytes[1]))
                controllerValue.set(midiDataEvent.controllerValue.toString())
                controllerValueBits.set(byteToBitsString(midiDataEvent.bytes[2]))
            } else if (midiDataEvent.event == MidiEvent.PITCH_BEND) {
                pitchBendValue.set(midiDataEvent.pitch.toString())
                pitchBendValueBits.set(byteToBitsString(midiDataEvent.bytes[1]) + " " + byteToBitsString(midiDataEvent.bytes[2]))
            }
        }
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