package be.codewriter.melodymatrix.view.view.midi

import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.event.MidiDataEvent
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.MmxEventType
import be.codewriter.melodymatrix.view.event.PlaybackPositionEvent
import be.codewriter.melodymatrix.view.component.MmxToolbar
import be.codewriter.melodymatrix.view.component.toolbarHeight
import be.codewriter.melodymatrix.view.i18n.I18n
import be.codewriter.melodymatrix.view.view.MmxView
import be.codewriter.melodymatrix.view.view.MmxViewMetadata
import javafx.animation.FadeTransition
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.binding.StringBinding
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.control.SplitPane
import javafx.scene.control.Tooltip
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle
import javafx.util.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Visualizer stage that shows the latest MIDI activity in a visual, colour-coded dashboard.
 *
 * Where [MidiView] is an inspector for raw MIDI bytes, this view summarises what is happening:
 * - one tile per message family (notes, controllers, instrument changes, pitch bend, other)
 *   that counts events and flashes on activity,
 * - a feed with the most recent events,
 * - the notes that are currently held down,
 * - the latest value of each controller,
 * - a pitch bend meter.
 *
 * @see MmxView
 * @see MidiDataEvent
 */
class MidiActivityView : MmxView() {

    override val fitToViewport: Boolean = true

    private val tiles = ActivityKind.entries.associateWith { ActivityTile(it) }

    private val feed = VBox(8.0)
    private val activeNotes = FlowPane(6.0, 6.0)
    private val activeNoteChips = mutableMapOf<Pair<Int, Note>, Label>()
    private val controllers = VBox(8.0)
    private val controllerRows = LinkedHashMap<Int, ControllerRow>()
    private val pitchBendText = SimpleStringProperty("0")
    private val pitchThumb = Circle(PITCH_THUMB_RADIUS, Color.web(ActivityKind.PITCH_BEND.color))

    init {
        activeNotes.apply { fixHeight(ACTIVE_NOTES_HEIGHT) }
        controllers.apply { fixHeight(CONTROLLERS_HEIGHT) }
        feed.minWidth = 0.0
        feed.minHeight = 0.0

        val clearButton = Button().apply {
            textProperty().bind(binding("clear"))
            toolbarHeight()
            setOnAction { clear() }
        }

        // The standard top bar of every view; the tab already shows the title
        val header = MmxToolbar.create(clearButton)

        val tileRow = HBox(12.0).apply {
            tiles.values.forEach {
                HBox.setHgrow(it.node, Priority.ALWAYS)
                it.node.minWidth = 0.0
                it.node.prefWidth = TILE_WIDTH
                it.node.maxWidth = Double.MAX_VALUE
                it.node.setPrefHeight(TILE_HEIGHT)
                it.node.setMinHeight(TILE_HEIGHT)
                it.node.setMaxHeight(TILE_HEIGHT)
                children.add(it.node)
            }
        }

        val placeholder = Label().apply {
            textProperty().bind(binding("waiting"))
            style = "-fx-text-fill: #9fb1c8; -fx-font-size: 13px; -fx-font-style: italic;"
            visibleProperty().bind(Bindings.isEmpty(feed.children))
        }
        val feedContent = StackPane(feed, placeholder).apply {
            alignment = Pos.TOP_LEFT
            // Cards are added at the top and the oldest ones fall off the bottom, so clip the overflow.
            val clip = Rectangle()
            clip.widthProperty().bind(widthProperty())
            clip.heightProperty().bind(heightProperty())
            setClip(clip)
        }
        feedContent.minHeight = 0.0
        VBox.setVgrow(feedContent, Priority.ALWAYS)
        val feedPanel = panel(binding("panel.recent"), feedContent)
        feedPanel.minHeight = 0.0
        VBox.setVgrow(feedPanel, Priority.ALWAYS)

        val sidePanel = VBox(
            12.0,
            panel(binding("panel.active_notes"), withEmptyHint(activeNotes)),
            panel(binding("panel.controllers"), withEmptyHint(controllers)),
            panel(binding("panel.pitch_bend"), pitchBendMeter())
        ).apply { minWidth = SIDE_PANEL_MIN_WIDTH }

        feedPanel.minWidth = FEED_MIN_WIDTH
        // The user can drag the divider to give the controller names or the event feed more room.
        val body = SplitPane(feedPanel, sidePanel).apply {
            setDividerPositions(DIVIDER_POSITION)
            style = "-fx-background-color: transparent; -fx-padding: 0;"
            skinProperty().addListener { _, _, _ ->
                Platform.runLater {
                    lookupAll(".split-pane-divider").forEach {
                        it.style = "-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 3; -fx-padding: 0 3 0 3;"
                    }
                }
            }
        }
        body.minHeight = 0.0
        VBox.setVgrow(body, Priority.ALWAYS)

        val root = VBox(16.0, header, tileRow, body).apply {
            padding = Insets(MmxToolbar.FRAME_PADDING)
            minHeight = 0.0
            style = "-fx-background-color: linear-gradient(to bottom, #091019, #0f1a27);"
        }

        setupSurface(root, 1000.0, 800.0, root)
    }

    /**
     * Handles incoming MelodyMatrix events: [MidiDataEvent]s are shown, and the view is cleared
     * when a recording starts playing from its beginning (start or loop), so every playback
     * begins with an empty view.
     *
     * @param event The MelodyMatrix event to process
     */
    override fun onEvent(event: MmxEvent) {
        when (event.type) {
            MmxEventType.MIDI -> {
                val midiEvent = event as? MidiDataEvent ?: return
                Platform.runLater { show(midiEvent) }
            }

            MmxEventType.PLAYBACK_POSITION -> {
                // Also sent on pause (speed 0) and seek; only a start at position 0 means a fresh playback.
                val position = event as? PlaybackPositionEvent ?: return
                if (position.songPositionMs == 0L && position.speed > 0.0) {
                    Platform.runLater { clear() }
                }
            }

            else -> {
                // Not needed here
            }
        }
    }

    private fun show(event: MidiDataEvent) {
        val kind = ActivityKind.of(event.event)
        val channel = (event.channel + 1).toString()
        val headline: String
        val detail: String
        var velocity: Int? = null

        when (event.event) {
            MidiEvent.NOTE_ON -> {
                headline = noteName(event.note)
                detail = I18n.get(bundle, "$PREFIX.feed.note_on", channel, event.velocity.toString())
                velocity = event.velocity
                holdNote(event.channel, event.note)
            }

            MidiEvent.NOTE_OFF -> {
                headline = noteName(event.note)
                detail = I18n.get(bundle, "$PREFIX.feed.note_off", channel)
                releaseNote(event.channel, event.note)
            }

            MidiEvent.CONTROLLER -> {
                val name = controllerName(event.controllerNumber)
                headline = "CC ${event.controllerNumber}"
                detail = listOfNotNull(
                    name,
                    I18n.get(bundle, "$PREFIX.feed.controller", event.controllerValue.toString(), channel)
                ).joinToString(" · ")
                updateController(event.controllerNumber, event.controllerValue)
            }

            MidiEvent.SELECT_INSTRUMENT -> {
                headline = "PC ${event.instrument}"
                detail = I18n.get(bundle, "$PREFIX.feed.instrument", channel)
            }

            MidiEvent.PITCH_BEND -> {
                val offset = event.pitch - PITCH_CENTER
                headline = if (offset > 0) "+$offset" else offset.toString()
                detail = I18n.get(bundle, "$PREFIX.feed.pitch_bend", channel)
                updatePitchBend(event.pitch, headline)
            }

            else -> {
                headline = event.event.name
                detail = I18n.get(bundle, "$PREFIX.feed.other")
            }
        }

        tiles.getValue(kind).record(headline)

        val time = TIME_FORMAT.format(Instant.ofEpochMilli(event.timestamp))
        val color = if (event.event == MidiEvent.NOTE_OFF) MidiEventColors.NOTE_OFF else kind.color
        feed.children.add(0, feedCard(color, headline, detail, time, velocity))
        if (feed.children.size > MAX_FEED_ITEMS) {
            feed.children.removeLast()
        }
    }

    private fun holdNote(channel: Int, note: Note) {
        activeNoteChips.getOrPut(channel to note) {
            chip(noteName(note)).also { activeNotes.children.add(it) }
        }
    }

    private fun releaseNote(channel: Int, note: Note) {
        activeNoteChips.remove(channel to note)?.let { activeNotes.children.remove(it) }
    }

    private fun updateController(number: Int, value: Int) {
        val row = controllerRows.getOrPut(number) {
            if (controllerRows.size >= MAX_CONTROLLERS) {
                val oldest = controllerRows.keys.first()
                controllers.children.remove(controllerRows.remove(oldest)?.node)
            }
            ControllerRow(number, controllerName(number)).also { controllers.children.add(it.node) }
        }
        row.set(value)
    }

    private fun updatePitchBend(pitch: Int, text: String) {
        pitchThumb.centerX = PITCH_THUMB_RADIUS + pitch / PITCH_MAX * (PITCH_TRACK_WIDTH - 2 * PITCH_THUMB_RADIUS)
        pitchBendText.set(text)
    }

    private fun clear() {
        feed.children.clear()
        activeNotes.children.clear()
        activeNoteChips.clear()
        controllers.children.clear()
        controllerRows.clear()
        updatePitchBend(PITCH_CENTER, "0")
        tiles.values.forEach { it.reset() }
    }

    // --- building blocks ---

    private fun panel(title: StringBinding, vararg content: Node): VBox {
        return VBox(
            10.0,
            Label().apply {
                textProperty().bind(title)
                style = "-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #b5d4ff;"
            },
            *content
        ).apply {
            padding = Insets(16.0)
            style =
                "-fx-background-color: #101a28; -fx-background-radius: 14; -fx-border-color: #223246; -fx-border-radius: 14; -fx-border-width: 1;"
        }
    }

    /**
     * Wraps [container] with a hint that is only visible while it has no children.
     * The hint overlays the container, so showing or hiding it never changes the layout.
     */
    private fun withEmptyHint(container: Pane): StackPane {
        val hint = Label().apply {
            textProperty().bind(binding("panel.none"))
            style = "-fx-text-fill: #9fb1c8; -fx-font-size: 12px; -fx-font-style: italic;"
            visibleProperty().bind(Bindings.isEmpty(container.children))
        }
        return StackPane(container, hint).apply {
            alignment = Pos.TOP_LEFT
            fixHeight(container.prefHeight)
            // Never let overflowing children push into the panel below.
            val clip = Rectangle()
            clip.widthProperty().bind(widthProperty())
            clip.heightProperty().bind(heightProperty())
            setClip(clip)
        }
    }

    private fun chip(text: String): Label {
        val color = ActivityKind.NOTE.color
        return Label(text).apply {
            style =
                "-fx-font-family: 'JetBrains Mono', 'Courier New', Courier, monospace; -fx-font-weight: bold; -fx-text-fill: #ecfff3; " +
                        "-fx-background-color: ${rgba(color, 0.35)}; -fx-border-color: $color; -fx-border-radius: 999; " +
                        "-fx-background-radius: 999; -fx-padding: 3 10 3 10;"
        }
    }

    private fun pitchBendMeter(): VBox {
        val track = Rectangle(PITCH_TRACK_WIDTH, 6.0, Color.web("#223246")).apply {
            arcWidth = 6.0
            arcHeight = 6.0
            y = PITCH_THUMB_RADIUS - 3.0
        }
        val centerMark = Rectangle(2.0, 14.0, Color.web("#9fb1c8")).apply {
            x = PITCH_TRACK_WIDTH / 2 - 1.0
            y = PITCH_THUMB_RADIUS - 7.0
        }
        pitchThumb.centerY = PITCH_THUMB_RADIUS
        pitchThumb.centerX = PITCH_TRACK_WIDTH / 2

        val meter = Pane(track, centerMark, pitchThumb).apply {
            setPrefSize(PITCH_TRACK_WIDTH, 2 * PITCH_THUMB_RADIUS)
            setMinSize(PITCH_TRACK_WIDTH, 2 * PITCH_THUMB_RADIUS)
        }
        val value = Label().apply {
            textProperty().bind(pitchBendText)
            prefWidth = PITCH_TRACK_WIDTH
            style = MONO_VALUE_STYLE
        }
        return VBox(8.0, meter, value).apply { alignment = Pos.CENTER_LEFT }
    }

    private fun feedCard(color: String, headline: String, detail: String, time: String, velocity: Int?): HBox {
        val accent = Region().apply {
            prefWidth = 5.0
            minWidth = 5.0
            style = "-fx-background-color: ${color}; -fx-background-radius: 3;"
        }
        val text = VBox(
            3.0,
            Label(headline).apply { style = "$MONO_VALUE_STYLE -fx-font-size: 18px;" },
            Label(detail).apply { style = "-fx-text-fill: #9fb1c8; -fx-font-size: 12px;" }
        )
        // Always reserve the bar slot so note cards and other cards have the same height.
        text.children.add(Region().apply {
            fixHeight(4.0)
            prefWidth = 8.0 + (velocity ?: 0) / 127.0 * VELOCITY_BAR_WIDTH
            maxWidth = prefWidth
            minWidth = prefWidth
            if (velocity != null) {
                style = "-fx-background-color: ${color}; -fx-background-radius: 2;"
            }
        })
        val timeLabel = Label(time).apply {
            prefWidth = 64.0
            minWidth = 64.0
            alignment = Pos.CENTER_RIGHT
            style = "-fx-text-fill: #6f819a; -fx-font-size: 12px; -fx-font-family: 'JetBrains Mono', 'Courier New', Courier, monospace;"
        }
        val spacer = Region().also { HBox.setHgrow(it, Priority.ALWAYS) }

        return HBox(12.0, accent, text, spacer, timeLabel).apply {
            alignment = Pos.CENTER_LEFT
            padding = Insets(8.0, 12.0, 8.0, 8.0)
            style = "-fx-background-color: ${rgba(color, 0.16)}; -fx-background-radius: 10;"
            fixHeight(CARD_HEIGHT)
            opacity = 0.0
            FadeTransition(Duration.millis(180.0), this).apply {
                toValue = 1.0
                play()
            }
        }
    }

    private fun Region.fixHeight(height: Double) {
        minHeight = height
        prefHeight = height
        maxHeight = height
    }

    /** Standard MIDI 1.0 name of controller [number], or `null` when it has no common meaning. */
    private fun controllerName(number: Int): String? {
        if (number in GENERAL_PURPOSE_CONTROLLERS) {
            return I18n.get(bundle, "$PREFIX.cc.general_purpose", (number - GENERAL_PURPOSE_CONTROLLERS.first + 1).toString())
        }
        return CONTROLLER_KEYS[number]?.let { I18n.get(bundle, "$PREFIX.cc.$it") }
    }

    private fun noteName(note: Note): String = note.name.replace("_SHARP", "♯")

    private fun binding(key: String): StringBinding = I18n.binding(bundle, "$PREFIX.$key")

    /** One summary tile per [ActivityKind]: title, event count, last value and a flash overlay. */
    private class ActivityTile(private val kind: ActivityKind) {
        private val count = SimpleIntegerProperty(0)
        private val last = SimpleStringProperty(EMPTY_VALUE)
        private val flash = Region().apply {
            isMouseTransparent = true
            opacity = 0.0
            style = "-fx-background-color: ${kind.color}; -fx-background-radius: 14;"
        }
        private val fade = FadeTransition(Duration.millis(500.0), flash).apply {
            fromValue = 0.55
            toValue = 0.0
        }

        val node = StackPane(
            VBox(
                4.0,
                Label().apply {
                    textProperty().bind(I18n.binding(bundle, "$PREFIX.${kind.titleKey}"))
                    style = "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: ${kind.color};"
                },
                Label().apply {
                    textProperty().bind(count.asString())
                    minWidth = 0.0
                    style = "-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #f8fbff;"
                },
                Label().apply {
                    textProperty().bind(last)
                    minWidth = 0.0
                    style = "$MONO_VALUE_STYLE -fx-text-fill: #9fb1c8;"
                }
            ).apply { padding = Insets(14.0) },
            flash
        ).apply {
            style =
                "-fx-background-color: rgba(255,255,255,0.04); -fx-background-radius: 14; -fx-border-color: ${rgba(kind.color, 0.5)}; -fx-border-radius: 14;"
        }

        fun record(value: String) {
            count.set(count.get() + 1)
            last.set(value)
            fade.playFromStart()
        }

        fun reset() {
            fade.stop()
            flash.opacity = 0.0
            count.set(0)
            last.set(EMPTY_VALUE)
        }
    }

    /** Controller number with a bar and the latest value. */
    private class ControllerRow(number: Int, name: String?) {
        private val bar = ProgressBar(0.0).apply {
            prefWidth = 90.0
            minWidth = 90.0
            style = "-fx-accent: ${ActivityKind.CONTROLLER.color};"
        }
        private val value = Label("0").apply {
            prefWidth = 36.0
            minWidth = 36.0
            alignment = Pos.CENTER_RIGHT
            style = MONO_VALUE_STYLE
        }

        val node = HBox(
            10.0,
            Label(if (name == null) "CC $number" else "CC $number · $name").apply {
                minWidth = 120.0
                maxWidth = Double.MAX_VALUE
                HBox.setHgrow(this, Priority.ALWAYS)
                if (name != null) tooltip = Tooltip(text)
                style = "-fx-text-fill: #9fb1c8; -fx-font-size: 13px; -fx-font-weight: 600;"
            },
            bar,
            value
        ).apply {
            alignment = Pos.CENTER_LEFT
        }

        fun set(newValue: Int) {
            bar.progress = newValue / 127.0
            value.text = newValue.toString()
        }
    }

    private enum class ActivityKind(val color: String, val titleKey: String) {
        NOTE(MidiEventColors.NOTE_ON, "tile.notes"),
        CONTROLLER(MidiEventColors.CONTROLLER, "tile.controllers"),
        INSTRUMENT(MidiEventColors.INSTRUMENT, "tile.instruments"),
        PITCH_BEND(MidiEventColors.PITCH_BEND, "tile.pitch_bend"),
        OTHER(MidiEventColors.OTHER, "tile.other");

        companion object {
            fun of(event: MidiEvent): ActivityKind = when (event) {
                MidiEvent.NOTE_ON, MidiEvent.NOTE_OFF -> NOTE
                MidiEvent.CONTROLLER -> CONTROLLER
                MidiEvent.SELECT_INSTRUMENT -> INSTRUMENT
                MidiEvent.PITCH_BEND -> PITCH_BEND
                else -> OTHER
            }
        }
    }

    companion object : MmxViewMetadata {
        override val bundleBaseName = "i18n/view/midi"
        override val bundleKeyPrefix = "activity."
        override fun getViewImagePath(): String = "/view/midi.png"
        override fun getViewIcon(): Ikon = FontAwesomeSolid.HEARTBEAT

        private const val PREFIX = "activity"
        private const val EMPTY_VALUE = "–"
        private const val MAX_FEED_ITEMS = 12
        private const val MAX_CONTROLLERS = 5
        private const val SIDE_PANEL_MIN_WIDTH = 340.0
        private const val FEED_MIN_WIDTH = 300.0
        private const val DIVIDER_POSITION = 0.5
        private const val TILE_WIDTH = 150.0
        private const val TILE_HEIGHT = 100.0
        private const val CARD_HEIGHT = 62.0
        private const val ACTIVE_NOTES_HEIGHT = 66.0
        private const val CONTROLLERS_HEIGHT = MAX_CONTROLLERS * 24.0 + (MAX_CONTROLLERS - 1) * 8.0
        private const val VELOCITY_BAR_WIDTH = 140.0
        private const val PITCH_CENTER = 8192
        private const val PITCH_MAX = 16383.0
        private const val PITCH_TRACK_WIDTH = 280.0
        private const val PITCH_THUMB_RADIUS = 9.0
        private const val MONO_VALUE_STYLE =
            "-fx-font-family: 'JetBrains Mono', 'Courier New', Courier, monospace; -fx-font-weight: bold; -fx-text-fill: #f8fbff;"

        private val GENERAL_PURPOSE_CONTROLLERS = 16..19

        /** Controller number to the suffix of its `activity.cc.*` bundle key. */
        private val CONTROLLER_KEYS = mapOf(
            0 to "bank_select", 1 to "modulation", 2 to "breath", 6 to "data_entry", 7 to "volume", 10 to "pan",
            11 to "expression", 64 to "sustain", 65 to "portamento", 66 to "sostenuto", 67 to "soft",
            71 to "resonance", 72 to "release", 73 to "attack", 74 to "brightness", 88 to "hires_velocity",
            91 to "reverb", 93 to "chorus", 98 to "nrpn", 99 to "nrpn", 100 to "rpn", 101 to "rpn",
            120 to "all_sound_off", 121 to "reset_controllers", 123 to "all_notes_off"
        )

        private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())

        // Same bundle as the metadata lookups (title/description) so the live bindings are shared.
        private val bundle by lazy { I18n.registerBundle(bundleBaseName, bundleClassLoader) }

        private fun rgba(hex: String, alpha: Double): String = MidiEventColors.rgba(hex, alpha)
    }
}
