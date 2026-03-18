package be.codewriter.melodymatrix.view

import be.codewriter.melodymatrix.view.data.MmxEventHandler
import javafx.stage.Stage

/**
 * Base class for all MelodyMatrix visualizer stages.
 *
 * Extends JavaFX [Stage] and implements [MmxEventHandler], so each visualizer
 * is both a window and a listener for MelodyMatrix events (MIDI, play, chord, etc.).
 * Subclasses must implement [MmxEventHandler.onEvent] to react to incoming events.
 *
 * @see MmxEventHandler
 */

abstract class VisualizerStage : Stage(), MmxEventHandler