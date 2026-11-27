package be.codewriter.melodymatrix.view.view.guitar

import be.codewriter.melodymatrix.view.component.MmxToolbar
import be.codewriter.melodymatrix.view.component.PillSwitch
import be.codewriter.melodymatrix.view.component.ToggleButton
import be.codewriter.melodymatrix.view.definition.Chord
import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.event.ChordEvent
import be.codewriter.melodymatrix.view.event.MidiDataEvent
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.MmxEventType
import be.codewriter.melodymatrix.view.helper.SettingHelper
import be.codewriter.melodymatrix.view.i18n.I18n
import be.codewriter.melodymatrix.view.view.MmxView
import be.codewriter.melodymatrix.view.view.MmxViewMetadata
import javafx.application.Platform
import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid

/** What the fretboard of [GuitarView] shows. */
enum class GuitarMode {
    /** Where the notes you play can be played. */
    NOTES,

    /** The fingering of the chord you play. */
    CHORDS,

    /** The fingering when a chord is detected, otherwise where the notes can be played. */
    AUTO
}

/**
 * Guitar fretboard that shows where the notes you play can be played and how to finger the chords you play.
 *
 * The `Notes | Chords | Auto` switch in the toolbar decides what is shown; [GuitarMode.AUTO] (the default) shows a chord
 * as soon as one is detected and falls back to the notes otherwise. Replaces the former Guitar Note and Guitar Chord
 * views: their two settings keep their keys, so nobody's choices are lost.
 *
 * @see GuitarVisualizer
 */
class GuitarView(
    settings: SettingHelper? = null
) : MmxView() {

    companion object : MmxViewMetadata {
        private const val REGISTRY_MODE = "view.guitar.mode"
        // Keys of the former Guitar Note and Guitar Chord views, kept so existing settings still apply
        private const val REGISTRY_SHOW_ALL_POSITIONS = "view.guitarNote.showAllPositions"
        private const val REGISTRY_KEEP_LAST_CHORD = "view.guitarChord.keepLastChord"

        override val bundleBaseName = "i18n/view/guitar"
        override fun getViewImagePath(): String = "/view/guitar-chord.png"
        override fun getViewIcon(): Ikon = FontAwesomeSolid.GUITAR
    }

    // Declared before the init block: the toolbar reads it while it is built
    private val bundle = I18n.registerBundle(bundleBaseName)

    private val visualizer = GuitarVisualizer(GuitarVisualizer.Mode.AUTO)
    private val modeProperty: ObjectProperty<GuitarMode> = SimpleObjectProperty(GuitarMode.AUTO)
    private val showAllPositionsProperty: BooleanProperty = SimpleBooleanProperty(false)
    private val keepLastChordProperty: BooleanProperty = SimpleBooleanProperty(true)

    // State, only touched on the FX thread
    private val activeNotes = linkedSetOf<Note>()
    private var currentChord: Chord? = null
    private var lastChord: Chord? = null

    init {
        settings?.bindEnum(modeProperty, REGISTRY_MODE, GuitarMode::class.java)
        settings?.bindBoolean(showAllPositionsProperty, REGISTRY_SHOW_ALL_POSITIONS)
        settings?.bindBoolean(keepLastChordProperty, REGISTRY_KEEP_LAST_CHORD)

        setupSurface(
            rootNode = MmxToolbar.frame(buildToolbar(), visualizer.rootNode),
            naturalWidth = 880.0,
            naturalHeight = 400.0,
            captureNode = visualizer.rootNode
        )

        modeProperty.addListener { _, _, _ -> refresh() }
        showAllPositionsProperty.addListener { _, _, _ -> refresh() }
        keepLastChordProperty.addListener { _, _, _ -> refresh() }
        refresh()
    }

    override fun onEvent(event: MmxEvent) {
        when (event.type) {
            MmxEventType.MIDI -> {
                val midiEvent = event as? MidiDataEvent ?: return
                if (midiEvent.isDrum || midiEvent.note == Note.UNDEFINED) return
                Platform.runLater {
                    when (midiEvent.event) {
                        MidiEvent.NOTE_ON -> {
                            activeNotes.remove(midiEvent.note)
                            activeNotes.add(midiEvent.note)
                        }

                        MidiEvent.NOTE_OFF -> activeNotes.remove(midiEvent.note)
                        else -> return@runLater // other MIDI messages do not change the notes
                    }
                    refresh()
                }
            }

            MmxEventType.CHORD -> {
                val chordEvent = event as? ChordEvent ?: return
                Platform.runLater {
                    if (chordEvent.on && chordEvent.chord != Chord.UNDEFINED) {
                        currentChord = chordEvent.chord
                        lastChord = chordEvent.chord
                    } else {
                        currentChord = null
                    }
                    refresh()
                }
            }

            else -> Unit
        }
    }

    /** Shows what the current mode wants to show for the current notes and chord. */
    private fun refresh() {
        visualizer.mode = when (modeProperty.get()) {
            GuitarMode.NOTES -> GuitarVisualizer.Mode.NOTE
            GuitarMode.CHORDS -> GuitarVisualizer.Mode.CHORD
            else -> GuitarVisualizer.Mode.AUTO
        }
        visualizer.showAllPositions = showAllPositionsProperty.get()
        val chordToShow = currentChord ?: lastChord.takeIf { keepLastChordProperty.get() }
        when (modeProperty.get()) {
            GuitarMode.NOTES -> showNoteOrClear()
            GuitarMode.CHORDS -> chordToShow?.let(visualizer::showChord) ?: visualizer.clear()
            else -> when {
                currentChord != null -> visualizer.showChord(currentChord!!)
                activeNotes.isNotEmpty() -> visualizer.showNote(activeNotes.last())
                chordToShow != null -> visualizer.showChord(chordToShow)
                else -> visualizer.clear()
            }
        }
    }

    private fun showNoteOrClear() {
        val note = activeNotes.lastOrNull()
        if (note != null) visualizer.showNote(note) else visualizer.clear()
    }

    private fun buildToolbar() = MmxToolbar.create(
        PillSwitch(
            options = GuitarMode.entries,
            property = modeProperty,
            labelOf = { I18n.binding(bundle, "mode.${it.name.lowercase()}") }
        ),
        ToggleButton(
            titleBinding = I18n.binding(bundle, "toolbar.show_all_positions"),
            property = showAllPositionsProperty
        ).apply { disableProperty().bind(modeProperty.isEqualTo(GuitarMode.CHORDS)) },
        ToggleButton(
            titleBinding = I18n.binding(bundle, "toolbar.keep_last_chord"),
            property = keepLastChordProperty
        ).apply { disableProperty().bind(modeProperty.isEqualTo(GuitarMode.NOTES)) }
    )
}
