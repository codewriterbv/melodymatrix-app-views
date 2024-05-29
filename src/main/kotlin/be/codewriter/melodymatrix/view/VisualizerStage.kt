package be.codewriter.melodymatrix.view

import be.codewriter.melodymatrix.view.test.MidiDataReceivedListener
import javafx.stage.Stage

/**
 * Base class for all visualizer stages.
 */
abstract class VisualizerStage : Stage(), MidiDataReceivedListener