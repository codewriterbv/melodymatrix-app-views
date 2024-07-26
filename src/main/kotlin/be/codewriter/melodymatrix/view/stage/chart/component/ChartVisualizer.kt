package be.codewriter.melodymatrix.view.stage.chart.component

interface ChartVisualizer {

    fun onNote(note: be.codewriter.melodymatrix.view.definition.Note)

    fun reset()
}