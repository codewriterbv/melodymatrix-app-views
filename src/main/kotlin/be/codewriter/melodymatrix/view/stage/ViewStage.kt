package be.codewriter.melodymatrix.view.stage

import be.codewriter.melodymatrix.view.data.MmxEventHandler
import javafx.stage.Stage

/**
 * Base class for all MelodyMatrix view stages.
 *
 * Extends JavaFX [Stage] and implements [MmxEventHandler], so each visualizer
 * is both a window and a listener for MelodyMatrix events (MIDI, play, chord, etc.).
 * Subclasses must implement [onEvent] to react to incoming events.
 *
 * Metadata methods let each stage provide a display title, short description,
 * and an optional image path for selectors/previews.
 *
 * @see be.codewriter.melodymatrix.view.data.MmxEventHandler
 */

abstract class ViewStage : Stage(), MmxEventHandler

interface ViewStageMetadata {
    fun getViewTitle(): String

    fun getViewDescription(): String

    fun getViewImagePath(): String
}

