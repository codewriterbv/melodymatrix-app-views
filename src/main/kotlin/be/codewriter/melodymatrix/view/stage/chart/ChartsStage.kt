package be.codewriter.melodymatrix.view.stage.chart

import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.event.MidiDataEvent
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.MmxEventType
import be.codewriter.melodymatrix.view.stage.ViewStage
import be.codewriter.melodymatrix.view.stage.ViewStageMetadata
import be.codewriter.melodymatrix.view.stage.chart.ChartsStage.Companion.chartHolders
import be.codewriter.melodymatrix.view.stage.chart.component.*
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox


/**
 * Visualizer stage that displays multiple musical chart visualizations.
 *
 * Shows a row of chart selection buttons at the top of the window. Clicking a button
 * expands the corresponding chart into the central area. All charts are kept in sync
 * with MIDI note-on events received via [onEvent].
 *
 * Available chart types: bar chart, concentric chart, Cox-comb chart, ridge-line chart,
 * and radar chart.
 *
 * @see ViewStage
 * @see ChartVisualizer
 * @see ChartHolder
 */
class ChartsStage : ViewStage() {

    init {
        title = VIEW_TITLE

        var buttons = HBox().apply {
            spacing = 10.0
            children.addAll(
                createChartSelection("Bar chart", BarChartPlayedNotes()),
                createChartSelection("Concentric chart", ConcentricPlayedNotes()),
                createChartSelection("Cox Comb chart", CoxCombPlayedNotes()),
                createChartSelection("Ridge Line chart", RidgeLineChartPlayedNotes()),
                createChartSelection("Radar chart", RadarChartPlayedNotes())
            )
        }

        borderPane = BorderPane().apply {
            top = buttons
        }

        scene = Scene(borderPane, 900.0, 700.0)

        setOnCloseRequest {
            // Nothing needed here, but must be defined or will cause a problem when closing the window
        }
    }

    /**
     * Creates a [ChartHolder] for the given label and chart, and registers it
     * in the [chartHolders] list so it can receive note events.
     *
     * @param label The button label shown for this chart
     * @param chart The chart visualizer instance
     * @return A [BorderPane]-based [ChartHolder] wrapping the chart
     */
    fun createChartSelection(
        label: String,
        chart: ChartVisualizer
    ): BorderPane {
        var chartHolder = ChartHolder(label, chart)
        chartHolders.add(chartHolder)
        return chartHolder
    }

    /**
     * A selectable chart panel combining a title button with a [ChartVisualizer].
     *
     * Clicking the button expands this chart into the central area of [ChartsStage]
     * and resets all other holders to their compact preview size.
     *
     * @property label The display label used for the selection button
     * @property chart The chart visualizer displayed in this holder
     */
    class ChartHolder(
        val label: String,
        val chart: ChartVisualizer
    ) : BorderPane() {
        init {
            prefWidth = 200.0
            prefHeight = 230.0
            top = Button(label).apply {
                prefWidth = 200.0
                setOnAction {
                    borderPane.center = (chart as Node)
                    for (chartHolder in chartHolders) {
                        if (chartHolder.label != label) {
                            chartHolder.resetView()
                        }
                    }
                }
            }
            resetView()
        }

        /**
         * Resets this chart holder to its compact preview size (200×200 px).
         *
         * Called when another chart is expanded to the full central area.
         */
        fun resetView() {
            center = (chart as Node).apply {
                prefWidth = 200.0
                prefHeight = 200.0
            }
        }
    }

    /**
     * Handles incoming MelodyMatrix events.
     *
     * Forwards NOTE_ON MIDI events to all registered chart holders.
     * PLAY and CHORD events are ignored.
     *
     * @param event The MelodyMatrix event to process
     */
    override fun onEvent(event: MmxEvent) {
        when (event.type) {
            MmxEventType.MIDI -> {
                val midiDataEvent = event as? MidiDataEvent ?: return
                if (midiDataEvent.event != MidiEvent.NOTE_ON) {
                    return
                }
                val note = midiDataEvent.note
                chartHolders.stream().forEach { v -> v.chart.onNote(note) }
            }

            MmxEventType.PLAY -> {
                // Not needed here
            }

            MmxEventType.CHORD -> {
                // Not needed here
            }
        }
    }

    companion object : ViewStageMetadata {
        const val VIEW_TITLE = "See your music in charts..."
        const val VIEW_DESCRIPTION = "Visualizes played notes using multiple chart types."
        val VIEW_IMAGE_PATH: String? = null
        override fun getViewTitle(): String = VIEW_TITLE
        override fun getViewDescription(): String = VIEW_DESCRIPTION
        override fun getViewImagePath(): String? = VIEW_IMAGE_PATH
        var chartHolders: MutableList<ChartHolder> = mutableListOf()
        lateinit var borderPane: BorderPane
    }
}