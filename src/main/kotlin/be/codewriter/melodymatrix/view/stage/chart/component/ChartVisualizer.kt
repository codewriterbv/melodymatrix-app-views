package be.codewriter.melodymatrix.view.stage.chart.component

import be.codewriter.melodymatrix.view.data.Note

interface ChartVisualizer {

    fun onNote(note: Note)

    fun reset()
}