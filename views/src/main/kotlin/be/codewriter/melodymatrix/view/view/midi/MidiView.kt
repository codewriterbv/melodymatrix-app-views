package be.codewriter.melodymatrix.view.view.midi

import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.event.MidiDataEvent
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.MmxEventType
import be.codewriter.melodymatrix.view.i18n.I18n
import be.codewriter.melodymatrix.view.view.MmxView
import be.codewriter.melodymatrix.view.view.MmxViewMetadata
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.binding.StringBinding
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * Visualizer stage that displays raw MIDI data in a detailed inspector table.
 *
 * Maintains a live log of all received [MidiDataEvent] objects in a [TableView].
 * Selecting a row shows a detailed breakdown of the three MIDI bytes — including
 * decimal values, binary representations, and parsed fields such as note, velocity,
 * instrument, controller and pitch-bend information.
 *
 * @see MmxView
 * @see MidiDataEvent
 */
class MidiView : MmxView() {

    override val fitToViewport: Boolean = true

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
    val eventCountValue: StringProperty = SimpleStringProperty("0")
    val table: TableView<MidiDataEvent> = TableView<MidiDataEvent>()

    init {
        midiDataEventList.addListener(ListChangeListener<MidiDataEvent> {
            eventCountValue.set(midiDataEventList.size.toString())
        })

        table.apply {
            items = midiDataEventList
            prefWidth = 460.0
            columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS
            placeholder = Label().apply {
                textProperty().bind(binding("activity.waiting"))
                style = "-fx-text-fill: #9fb1c8; -fx-font-size: 13px; -fx-font-style: italic;"
            }
            style = "-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;"
            columns.addAll(
                TableColumn<MidiDataEvent, String>().apply {
                    textProperty().bind(binding("inspector.event"))
                    cellValueFactory = PropertyValueFactory("event")
                    style = "-fx-alignment: CENTER-LEFT; -fx-font-weight: bold;"
                },
                TableColumn<MidiDataEvent, String>().apply {
                    textProperty().bind(binding("inspector.note"))
                    cellValueFactory = PropertyValueFactory("note")
                    style = "-fx-alignment: CENTER-LEFT;"
                },
                TableColumn<MidiDataEvent, String>().apply {
                    textProperty().bind(binding("inspector.velocity"))
                    cellValueFactory = PropertyValueFactory("velocity")
                    style = "-fx-alignment: CENTER-RIGHT;"
                }
            )
            setRowFactory {
                object : TableRow<MidiDataEvent>() {
                    override fun updateItem(item: MidiDataEvent?, empty: Boolean) {
                        super.updateItem(item, empty)
                        style = when {
                            empty -> "-fx-background-color: transparent;"
                            isSelected -> "-fx-background-color: #2b4d73; -fx-text-fill: #f8fafc; -fx-background-insets: 1;"
                            item != null -> rowStyleFor(item)
                            else -> "-fx-background-color: transparent;"
                        }
                    }
                }
            }
            selectionModel.selectedItemProperty().addListener { _, _, newValue ->
                if (newValue != null) {
                    showValues(newValue)
                }
            }
        }

        val clearEventsButton = Button().apply {
            textProperty().bind(binding("activity.clear"))
            style =
                "-fx-background-color: #2f6fb1; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 9; -fx-padding: 8 14 8 14;"
            setOnAction {
                midiDataEventList.clear()
                clearDecodedValues()
            }
        }

        val eventCounterLabel = Label().apply {
            textProperty().bind(
                Bindings.createStringBinding(
                    { I18n.get(bundle, "inspector.events", eventCountValue.get()) },
                    eventCountValue,
                    bundle.bundle
                )
            )
            style =
                "-fx-background-color: rgba(118,195,255,0.18); -fx-text-fill: #dbeeff; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 5 10 5 10;"
        }

        // Same header as the MIDI activity view: title on the left, actions on the right.
        val header = HBox(
            12.0,
            Label().apply {
                textProperty().bind(titleBinding())
                style = "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f2f7ff;"
            },
            Region().also { HBox.setHgrow(it, Priority.ALWAYS) },
            eventCounterLabel,
            clearEventsButton
        ).apply { alignment = Pos.CENTER_LEFT }

        val tablePanel = VBox(10.0).apply {
            padding = Insets(16.0)
            style =
                "-fx-background-color: #101a28; -fx-background-radius: 14; -fx-border-color: #223246; -fx-border-radius: 14; -fx-border-width: 1;"
            children.addAll(
                Label().apply {
                    textProperty().bind(binding("inspector.subtitle"))
                    style = "-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #b5d4ff;"
                },
                table
            )
        }
        VBox.setVgrow(table, Priority.ALWAYS)

        val midiInfo = GridPane().apply {
            hgap = 10.0
            vgap = 10.0
            padding = Insets(16.0)
            style =
                "-fx-background-color: #101a28; -fx-background-radius: 14; -fx-border-color: #223246; -fx-border-radius: 14; -fx-border-width: 1;"
            isGridLinesVisible = false
            columnConstraints.addAll(
                ColumnConstraints(200.0),
                ColumnConstraints(100.0),
                ColumnConstraints(150.0)
            )

            add(sectionTitleLabel(binding("inspector.section.midi_data")), 0, 0, 3, 1)

            add(infoLabel(binding("inspector.data0")), 0, 1)
            add(valueLabel(midiData0Value), 1, 1)
            add(valueLabel(midiData0Bits), 2, 1)

            add(infoLabel(binding("inspector.event")), 1, 2)
            add(valueLabel(midiDataEventValue), 2, 2)

            add(infoLabel(binding("inspector.channel")), 1, 3)
            add(valueLabel(midiDataChannelValue), 2, 3)

            add(infoLabel(binding("inspector.data1")), 0, 4)
            add(valueLabel(midiData1Value), 1, 4)
            add(valueLabel(midiData1Bits), 2, 4)

            add(infoLabel(binding("inspector.data2")), 0, 5)
            add(valueLabel(midiData2Value), 1, 5)
            add(valueLabel(midiData2Bits), 2, 5)

            add(sectionDivider(), 0, 6, 3, 1)

            add(sectionTitleLabel(binding("inspector.section.decoded")), 0, 7, 3, 1)

            add(sectionSubtitleLabel(binding("inspector.section.note")), 0, 8, 3, 1)

            add(infoLabel(binding("inspector.note")), 0, 9)
            add(valueLabel(noteValue), 1, 9)
            add(valueLabel(noteValueBits), 2, 9)

            add(infoLabel(binding("inspector.note_velocity")), 0, 10)
            add(valueLabel(noteVelocityValue), 1, 10)
            add(valueLabel(noteVelocityValueBits), 2, 10)

            add(sectionSubtitleLabel(binding("inspector.section.instrument")), 0, 11, 3, 1)

            add(infoLabel(binding("inspector.selected_instrument")), 0, 12)
            add(valueLabel(lastInstrument), 1, 12)

            add(sectionSubtitleLabel(binding("inspector.section.controller")), 0, 13, 3, 1)

            add(infoLabel(binding("inspector.controller_number")), 0, 14)
            add(valueLabel(controllerNumber), 1, 14)
            add(valueLabel(controllerNumberBits), 2, 14)

            add(infoLabel(binding("inspector.controller_value")), 0, 15)
            add(valueLabel(controllerValue), 1, 15)
            add(valueLabel(controllerValueBits), 2, 15)

            add(sectionSubtitleLabel(binding("inspector.section.pitch_bend")), 0, 16, 3, 1)

            add(infoLabel(binding("activity.tile.pitch_bend")), 0, 17)
            add(valueLabel(pitchBendValue), 1, 17)
            add(valueLabel(pitchBendValueBits), 2, 17)
        }

        val body = HBox(12.0, tablePanel, midiInfo)
        HBox.setHgrow(tablePanel, Priority.ALWAYS)
        HBox.setHgrow(midiInfo, Priority.NEVER)
        VBox.setVgrow(body, Priority.ALWAYS)

        val root = VBox(16.0, header, body).apply {
            padding = Insets(20.0)
            style = "-fx-background-color: linear-gradient(to bottom, #091019, #0f1a27);"
        }

        setupSurface(root, 1000.0, 800.0, root)
    }

    private fun rowStyleFor(item: MidiDataEvent): String {
        return when (item.event) {
            MidiEvent.NOTE_ON -> rowStyle(MidiEventColors.NOTE_ON, "#ecfff3")
            MidiEvent.NOTE_OFF -> rowStyle(MidiEventColors.NOTE_OFF, "#fff0f0")
            MidiEvent.SELECT_INSTRUMENT -> rowStyle(MidiEventColors.INSTRUMENT, "#f8eeff")
            MidiEvent.CONTROLLER -> rowStyle(MidiEventColors.CONTROLLER, "#ebf9ff")
            MidiEvent.PITCH_BEND -> rowStyle(MidiEventColors.PITCH_BEND, "#fff6e8")
            else -> if (table.items.indexOf(item) % 2 == 0) {
                "-fx-background-color: rgba(255,255,255,0.03); -fx-text-fill: #e5ecf4;"
            } else {
                "-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: #e5ecf4;"
            }
        }
    }

    private fun rowStyle(color: String, textColor: String): String =
        "-fx-background-color: ${MidiEventColors.rgba(color, 0.34)}; -fx-text-fill: $textColor;"

    private fun binding(key: String): StringBinding = I18n.binding(bundle, key)

    private fun sectionTitleLabel(text: StringBinding): Label {
        return Label().apply {
            textProperty().bind(text)
            style = "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #e6edf7;"
        }
    }

    private fun sectionSubtitleLabel(text: StringBinding): Label {
        return Label().apply {
            textProperty().bind(text)
            style = "-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #b5d4ff;"
        }
    }

    private fun sectionDivider(): Separator {
        return Separator().apply {
            style = "-fx-opacity: 0.5;"
        }
    }

    private fun infoLabel(text: StringBinding): Label {
        return Label().apply {
            textProperty().bind(text)
            style = "-fx-text-fill: #9fb1c8; -fx-font-size: 13px; -fx-font-weight: 600;"
        }
    }

    private fun valueLabel(property: StringProperty): Label {
        return Label().apply {
            textProperty().bind(property)
            style =
                "-fx-font-family: 'JetBrains Mono', 'Courier New', Courier, monospace; -fx-font-weight: bold; -fx-text-fill: #f8fbff; -fx-background-color: rgba(255,255,255,0.04); -fx-background-radius: 6; -fx-padding: 3 6 3 6;"
            GridPane.setHalignment(this, HPos.RIGHT)
        }
    }

    /**
     * Handles incoming MelodyMatrix events.
     *
     * Prepends MIDI events to the event list and selects the newest row in the table.
     * PLAY and CHORD events are ignored.
     *
     * @param event The MelodyMatrix event to process
     */
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

            MmxEventType.AUDIO_SPECTRUM -> {
                // Not needed here
            }

            MmxEventType.PLAYBACK_STOP -> {
                // Not needed here
            }

            MmxEventType.SCORE_LOADED -> {
                // Not needed here
            }

            MmxEventType.PLAYBACK_POSITION -> {
                // Not needed here
            }
        }
    }

    /**
     * Populates the detail panel with the field values of the selected MIDI event.
     *
     * Updates all bound [StringProperty] fields so the GridPane labels refresh automatically.
     * The method is annotated with [ExperimentalStdlibApi] because it relies on experimental
     * Kotlin standard-library APIs for hex formatting.
     *
     * @param midiDataEvent The MIDI event whose values should be displayed
     */
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
            clearDecodedValues()

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

    private fun clearDecodedValues() {
        noteValue.set("")
        noteValueBits.set("")
        noteVelocityValue.set("")
        noteVelocityValueBits.set("")
        lastInstrument.set("")
        controllerNumber.set("")
        controllerNumberBits.set("")
        controllerValue.set("")
        controllerValueBits.set("")
        pitchBendValue.set("")
        pitchBendValueBits.set("")
    }

    /**
     * Converts a byte to a formatted 8-bit binary string with a space between the nibbles.
     *
     * For example, `0x90.toByte()` becomes `"1001 0000"`.
     *
     * @param byte The byte to format
     * @return An 8-character binary string with a space in the middle (e.g., "1001 0000")
     */
    fun byteToBitsString(byte: Byte): String {
        val bits = String
            .format("%8s", Integer.toBinaryString(byte.toInt() and 0xFF))
            .replace(' ', '0')
        return bits.substring(0, 4) + " " + bits.substring(4, 8)
    }

    companion object : MmxViewMetadata {
        override val bundleBaseName = "i18n/view/midi"
        override fun getViewImagePath(): String = "/view/midi.png"
        private val logger: Logger = LogManager.getLogger(MidiView::class.java.name)
        private val bundle by lazy { I18n.registerBundle(bundleBaseName, bundleClassLoader) }
    }
}
