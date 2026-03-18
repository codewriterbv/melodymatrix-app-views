package be.codewriter.melodymatrix.view.stage.chart.component

import be.codewriter.melodymatrix.view.definition.Note

/**
 * Interface for chart components that visualize played notes.
 *
 * Implementations render a specific chart style (bar, radar, concentric, etc.)
 * and are notified of each NOTE_ON event so they can update their visualization.
 *
 * @see ChartsStage
 * @see ChartBase
 */
interface ChartVisualizer {

    /**
     * Called when a note is played (NOTE_ON event received).
     *
     * @param note The note that was played
     */
    fun onNote(note: Note)

    /**
     * Resets the chart to its initial empty state, clearing all accumulated note data.
     */
    fun reset()
}