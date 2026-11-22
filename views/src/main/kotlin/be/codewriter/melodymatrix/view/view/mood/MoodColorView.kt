package be.codewriter.melodymatrix.view.view.mood

import be.codewriter.melodymatrix.view.event.ChordEvent
import be.codewriter.melodymatrix.view.event.MidiDataEvent
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.MmxEventType
import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.event.PlaybackPositionEvent
import be.codewriter.melodymatrix.view.helper.SettingHelper
import be.codewriter.melodymatrix.view.i18n.I18n
import be.codewriter.melodymatrix.view.view.MmxView
import be.codewriter.melodymatrix.view.view.MmxViewMetadata
import javafx.animation.AnimationTimer
import javafx.beans.binding.StringBinding
import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ScrollPane
import javafx.scene.control.Slider
import javafx.scene.control.ToggleButton
import javafx.scene.layout.GridPane
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.RadialGradient
import javafx.scene.paint.Stop
import javafx.scene.effect.BlendMode
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.min
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.sin

/**
 * Abstract colour animation driven by the character of the music instead of by individual notes.
 *
 * A [MoodAnalyzer] condenses the incoming MIDI into level, speed, tone, density, mood and the key
 * the music is in. Five visual channels (hue, saturation, brightness, motion and size) can each be
 * driven by any of those sources, within a configurable minimum and maximum, and one of four
 * patterns draws the result. The settings panel (top right) edits all of it live; when a
 * [SettingHelper] is supplied the choices are remembered.
 *
 * @param settings Optional storage to persist the configuration.
 *
 * @see MoodAnalyzer
 */
class MoodColorView(
    settings: SettingHelper? = null
) : MmxView() {

    override val fitToViewport: Boolean = true

    /** What a visual channel follows. */
    enum class Source(val key: String) {
        PITCH_CLASS("pitch_class"),
        TONE("tone"),
        LEVEL("level"),
        SPEED("speed"),
        MOOD("mood"),
        DENSITY("density"),
        FIXED("fixed")
    }

    enum class Pattern(val key: String) {
        BLOBS("blobs"),
        WAVES("waves"),
        RINGS("rings"),
        GRADIENT("gradient")
    }

    /** One visual property: which [Source] drives it and between which values. */
    private class Channel(
        val name: String,
        source: Source,
        min: Double,
        max: Double,
        val range: ClosedFloatingPointRange<Double>
    ) {
        val source: ObjectProperty<Source> = SimpleObjectProperty(source)
        val min: DoubleProperty = SimpleDoubleProperty(min)
        val max: DoubleProperty = SimpleDoubleProperty(max)

        fun resolve(value: Double): Double = if (source.get() == Source.FIXED) min.get()
        else min.get() + (max.get() - min.get()) * value.coerceIn(0.0, 1.0)

        fun reset(defaults: Channel) {
            source.set(defaults.source.get())
            min.set(defaults.min.get())
            max.set(defaults.max.get())
        }
    }

    private val hue = Channel("hue", Source.PITCH_CLASS, 0.0, 360.0, 0.0..360.0)
    private val saturation = Channel("saturation", Source.MOOD, 0.45, 1.0, 0.0..1.0)
    private val brightness = Channel("brightness", Source.LEVEL, 0.25, 1.0, 0.0..1.0)
    private val motion = Channel("motion", Source.SPEED, 0.15, 1.5, 0.0..3.0)
    private val size = Channel("size", Source.DENSITY, 0.2, 1.0, 0.0..1.0)
    private val channels = listOf(hue, saturation, brightness, motion, size)

    private val pattern: ObjectProperty<Pattern> = SimpleObjectProperty(Pattern.BLOBS)
    private val layers: IntegerProperty = SimpleIntegerProperty(6)
    private val variety: DoubleProperty = SimpleDoubleProperty(40.0)
    private val reaction: DoubleProperty = SimpleDoubleProperty(0.6)
    private val trails: DoubleProperty = SimpleDoubleProperty(0.5)
    private val idleGlow: BooleanProperty = SimpleBooleanProperty(true)
    private val maxNotesPerSecond: DoubleProperty = SimpleDoubleProperty(8.0)

    private val analyzer = MoodAnalyzer()
    private val canvas = Canvas()

    // Smoothed values, only touched by the animation timer
    private var level = 0.0
    private var speed = 0.0
    private var tone = 0.0
    private var density = 0.0
    private var mood = 0.0
    private var keyX = 1.0
    private var keyY = 0.0
    private var phase = 0.0

    /** Short punch (0..1) that every played note adds and that dies away within a fraction of a second. */
    private var pulse = 0.0

    /** A note that was played; turned into a [Ripple] by the animation timer. */
    private class NoteHit(val pitch: Int, val velocity: Int)

    /** An expanding splash created by one note: placed by its pitch, coloured by its key, sized by its velocity. */
    private class Ripple(val x: Double, val y: Double, val hueShift: Double, val strength: Double, var age: Double = 0.0)

    private val noteHits = ConcurrentLinkedQueue<NoteHit>()
    private val ripples = ArrayList<Ripple>()
    private var lastNanos = 0L

    private val timer = object : AnimationTimer() {
        override fun handle(now: Long) {
            val dt = if (lastNanos == 0L) 0.0 else ((now - lastNanos) / 1e9).coerceAtMost(MAX_FRAME_SECONDS)
            lastNanos = now
            frame(dt)
        }
    }

    init {
        if (settings != null) bindSettings(settings)

        val canvasPane = object : Pane(canvas) {
            override fun layoutChildren() {
                canvas.width = width
                canvas.height = height
                super.layoutChildren()
            }
        }

        val panelToggle = ToggleButton().apply {
            textProperty().bind(binding("settings"))
            isSelected = true
            style = "-fx-background-color: rgba(10,16,26,0.7); -fx-text-fill: white; -fx-background-radius: 9;"
        }
        val panel = settingsPanel()
        panel.visibleProperty().bind(panelToggle.selectedProperty())
        panel.managedProperty().bind(panel.visibleProperty())

        val overlay = VBox(8.0, panelToggle, panel).apply {
            alignment = Pos.TOP_RIGHT
            padding = Insets(12.0)
            isPickOnBounds = false
            maxWidth = PANEL_WIDTH + 24.0
        }
        val root = StackPane(canvasPane, overlay).apply {
            style = "-fx-background-color: #05070b;"
            StackPane.setAlignment(overlay, Pos.TOP_RIGHT)
        }

        rootNode = root
        setupSurface(root, 1000.0, 700.0, canvasPane, 1000, 700) { timer.stop() }

        root.sceneProperty().addListener { _, _, scene ->
            if (scene != null) {
                lastNanos = 0L
                timer.start()
            } else timer.stop()
        }
    }

    override fun onEvent(event: MmxEvent) {
        val now = System.currentTimeMillis()
        when (event.type) {
            MmxEventType.MIDI -> {
                val midi = event as? MidiDataEvent ?: return
                when (midi.event) {
                    MidiEvent.NOTE_ON -> if (!midi.isDrum) {
                        analyzer.noteOn(midi.note.byteValue, midi.velocity, now)
                        if (midi.velocity > 0) noteHits.add(NoteHit(midi.note.byteValue, midi.velocity))
                    }
                    MidiEvent.NOTE_OFF -> analyzer.noteOff(midi.note.byteValue)
                    else -> {}
                }
            }

            MmxEventType.CHORD -> {
                val chord = event as? ChordEvent ?: return
                analyzer.chord(chord.chord.quality, chord.on)
            }

            MmxEventType.PLAYBACK_POSITION -> {
                val position = event as? PlaybackPositionEvent ?: return
                if (position.songPositionMs == 0L && position.speed > 0.0) analyzer.reset()
            }

            else -> {}
        }
    }

    private fun frame(dt: Double) {
        val raw = analyzer.snapshot(System.currentTimeMillis())

        val slow = reaction.get().coerceAtLeast(MIN_REACTION)
        val fall = 1.0 - exp(-dt / slow)
        val rise = 1.0 - exp(-dt / (slow * ATTACK_FACTOR))

        var targetLevel = raw.level
        if (raw.idle && idleGlow.get()) targetLevel = IDLE_LEVEL + IDLE_BREATH * sin(phase * 0.8)
        level += (targetLevel - level) * (if (targetLevel > level) rise else fall)
        speed += ((raw.notesPerSecond / maxNotesPerSecond.get()).coerceIn(0.0, 1.0) - speed) * fall
        tone += (raw.tone - tone) * fall
        density += (raw.density - density) * fall
        mood += (raw.mood - mood) * fall
        raw.pitchClassPosition?.let {
            // Smooth the position as a vector so it moves along the circle instead of jumping across the wrap
            keyX += (cos(it * 2 * PI) - keyX) * fall
            keyY += (sin(it * 2 * PI) - keyY) * fall
        }

        val value = mapOf(
            Source.PITCH_CLASS to ((atan2(keyY, keyX) / (2 * PI) + 1.0) % 1.0),
            Source.TONE to tone,
            Source.LEVEL to level,
            Source.SPEED to speed,
            Source.MOOD to (mood + 1.0) / 2.0,
            Source.DENSITY to density,
            Source.FIXED to 0.0
        )
        fun of(channel: Channel) = channel.resolve(value.getValue(channel.source.get()))

        collectHits(dt)
        phase += dt * (BASE_MOTION + of(motion) * (1.0 + PULSE_MOTION * pulse))
        draw(
            canvas.graphicsContext2D,
            baseHue = of(hue),
            sat = of(saturation).coerceIn(0.0, 1.0),
            bright = of(brightness).coerceIn(0.0, 1.0),
            spread = of(size).coerceIn(0.0, 1.0)
        )
    }

    private fun collectHits(dt: Double) {
        while (true) {
            val hit = noteHits.poll() ?: break
            val strength = hit.velocity / 127.0
            pulse = maxOf(pulse, strength)
            val tonePosition = ((hit.pitch - 21) / 87.0).coerceIn(0.0, 1.0)
            ripples.add(
                Ripple(
                    x = 0.1 + 0.8 * tonePosition,
                    y = 0.25 + 0.5 * ((hit.pitch * 7 % 12) / 12.0),
                    hueShift = MoodAnalyzer.circleOfFifths(hit.pitch) * RIPPLE_HUE_RANGE,
                    strength = strength
                )
            )
            if (ripples.size > MAX_RIPPLES) ripples.removeAt(0)
        }
        pulse *= exp(-dt / PULSE_DECAY_SECONDS)
        ripples.forEach { it.age += dt }
        ripples.removeAll { it.age > RIPPLE_LIFE_SECONDS }
    }

    private fun draw(gc: GraphicsContext, baseHue: Double, sat: Double, bright: Double, spread: Double) {
        val w = canvas.width
        val h = canvas.height
        if (w < 1 || h < 1) return

        gc.globalBlendMode = BlendMode.SRC_OVER
        gc.fill = Color.hsb(baseHue, sat * 0.6, BACKGROUND_BRIGHTNESS * (0.4 + bright), 1.0 - trails.get() * MAX_TRAIL)
        gc.fillRect(0.0, 0.0, w, h)

        val count = layers.get().coerceAtLeast(1)
        fun layerColor(i: Int, alpha: Double) =
            Color.hsb(((baseHue + i * variety.get() + 360.0) % 360.0), sat, bright, alpha.coerceIn(0.0, 1.0))

        when (pattern.get()) {
            Pattern.BLOBS -> {
                gc.globalBlendMode = BlendMode.SCREEN
                val base = min(w, h) * (0.15 + 0.5 * spread) * (1.0 + BLOB_PULSE * pulse)
                for (i in 0 until count) {
                    val t = phase + i * GOLDEN_ANGLE
                    val cx = w * (0.5 + 0.4 * sin(t * 0.7 + i))
                    val cy = h * (0.5 + 0.4 * cos(t * 0.9 + i * 1.3))
                    val radius = base * (0.7 + 0.3 * sin(t * 1.3))
                    gc.fill = RadialGradient(
                        0.0, 0.0, cx, cy, radius, false, CycleMethod.NO_CYCLE,
                        Stop(0.0, layerColor(i, 0.85 * bright + 0.1)), Stop(1.0, layerColor(i, 0.0))
                    )
                    gc.fillOval(cx - radius, cy - radius, radius * 2, radius * 2)
                }
                ripples.forEach { ripple ->
                    val life = ripple.age / RIPPLE_LIFE_SECONDS
                    val radius = min(w, h) * (0.1 + 0.5 * life) * (0.5 + ripple.strength)
                    val cx = w * ripple.x
                    val cy = h * ripple.y
                    val color = Color.hsb((baseHue + ripple.hueShift) % 360.0, sat, 1.0, (1.0 - life) * 0.8)
                    gc.fill = RadialGradient(
                        0.0, 0.0, cx, cy, radius, false, CycleMethod.NO_CYCLE,
                        Stop(0.0, color), Stop(1.0, Color.TRANSPARENT)
                    )
                    gc.fillOval(cx - radius, cy - radius, radius * 2, radius * 2)
                }
            }

            Pattern.WAVES -> {
                gc.globalBlendMode = BlendMode.SCREEN
                val step = WAVE_STEP
                val points = (w / step).toInt() + 3
                val xs = DoubleArray(points + 2)
                val ys = DoubleArray(points + 2)
                for (i in 0 until count) {
                    val centre = h * (i + 0.5) / count
                    val amplitude = h / count * (0.4 + 2.0 * spread) * (1.0 + WAVE_PULSE * pulse)
                    val t = phase * (1.0 + i * 0.25) + i
                    for (p in 0 until points) {
                        val x = p * step
                        xs[p] = x
                        ys[p] = centre + amplitude * sin(x / w * 2 * PI * (1.5 + i * 0.3) + t)
                    }
                    xs[points] = w + step
                    ys[points] = h
                    xs[points + 1] = 0.0
                    ys[points + 1] = h
                    gc.fill = LinearGradient(
                        0.0, centre - amplitude, 0.0, h, false, CycleMethod.NO_CYCLE,
                        Stop(0.0, layerColor(i, 0.6 * bright + 0.1)), Stop(1.0, layerColor(i, 0.0))
                    )
                    gc.fillPolygon(xs, ys, points + 2)
                }
            }

            Pattern.RINGS -> {
                gc.globalBlendMode = BlendMode.SCREEN
                val maxRadius = hypot(w, h) / 2
                for (i in 0 until count) {
                    val fraction = (phase * 0.2 + i.toDouble() / count) % 1.0
                    val radius = fraction * maxRadius
                    gc.stroke = layerColor(i, (1.0 - fraction) * (0.3 + 0.7 * bright))
                    gc.lineWidth = (3.0 + spread * 50.0 * (0.3 + fraction)) * (1.0 + pulse)
                    gc.strokeOval(w / 2 - radius, h / 2 - radius, radius * 2, radius * 2)
                }
                ripples.forEach { ripple ->
                    val life = ripple.age / RIPPLE_LIFE_SECONDS
                    val cx = w * ripple.x
                    val cy = h * ripple.y
                    for (ring in 0 until RIPPLE_RINGS) {
                        val radius = maxRadius * 0.6 * (life - ring * 0.08).coerceAtLeast(0.0) * (0.5 + ripple.strength)
                        if (radius <= 0.0) continue
                        gc.stroke = Color.hsb((baseHue + ripple.hueShift) % 360.0, sat, 1.0, (1.0 - life) * (1.0 - ring * 0.3))
                        gc.lineWidth = (2.0 + spread * 18.0) * (1.0 - life * 0.5)
                        gc.strokeOval(cx - radius, cy - radius, radius * 2, radius * 2)
                    }
                }
            }

            Pattern.GRADIENT -> {
                val angle = phase * 0.15 + pulse * 0.6
                val stops = (0 until count.coerceAtLeast(2)).map {
                    Stop(it / (count.coerceAtLeast(2) - 1.0), layerColor(it, 0.5 + 0.5 * bright))
                }
                val reach = 0.25 + 0.75 * (1.0 - spread * 0.7)
                gc.fill = LinearGradient(
                    0.5 - cos(angle) * reach, 0.5 - sin(angle) * reach,
                    0.5 + cos(angle) * reach, 0.5 + sin(angle) * reach,
                    true, CycleMethod.REFLECT, stops
                )
                gc.fillRect(0.0, 0.0, w, h)
            }
        }
        gc.globalBlendMode = BlendMode.SRC_OVER
    }

    private fun settingsPanel(): ScrollPane {
        val grid = GridPane().apply {
            hgap = 8.0
            vgap = 6.0
        }
        var row = 0
        fun section(key: String) {
            grid.add(Label().apply {
                textProperty().bind(binding(key))
                style = "-fx-text-fill: #9fb1c8; -fx-font-weight: bold; -fx-padding: 8 0 0 0;"
            }, 0, row++, 4, 1)
        }

        section("section.mapping")
        grid.add(headerLabel("header.source"), 1, row)
        grid.add(headerLabel("header.min"), 2, row)
        grid.add(headerLabel("header.max"), 3, row++)
        channels.forEach { channel ->
            grid.add(textLabel(binding("channel.${channel.name}")), 0, row)
            grid.add(enumCombo(channel.source, Source.entries, { "source.${it.key}" }), 1, row)
            grid.add(slider(channel.min, channel.range.start, channel.range.endInclusive), 2, row)
            grid.add(slider(channel.max, channel.range.start, channel.range.endInclusive), 3, row++)
        }

        section("section.look")
        fun setting(labelKey: String, control: javafx.scene.Node) {
            grid.add(textLabel(binding(labelKey)), 0, row)
            grid.add(control, 1, row++, 3, 1)
        }
        setting("pattern", enumCombo(pattern, Pattern.entries, { "pattern.${it.key}" }))
        setting("layers", slider(layers, 1.0, 12.0, integer = true))
        setting("variety", slider(variety, 0.0, 120.0))
        setting("reaction", slider(reaction, MIN_REACTION, 3.0))
        setting("trails", slider(trails, 0.0, 1.0))
        setting("rate", slider(maxNotesPerSecond, 1.0, 20.0))
        setting("idle", CheckBox().apply { selectedProperty().bindBidirectional(idleGlow) })

        grid.add(Button().apply {
            textProperty().bind(binding("reset"))
            setOnAction { resetDefaults() }
        }, 0, row, 4, 1)

        return ScrollPane(grid).apply {
            isFitToWidth = true
            prefWidth = PANEL_WIDTH
            maxWidth = PANEL_WIDTH
            maxHeight = Double.MAX_VALUE
            padding = Insets(4.0)
            style = "-fx-background: rgba(10,16,26,0.82); -fx-background-color: rgba(10,16,26,0.82); -fx-background-radius: 12;"
            grid.padding = Insets(8.0)
            VBox.setVgrow(this, javafx.scene.layout.Priority.ALWAYS)
        }
    }

    private fun textLabel(text: StringBinding) = Label().apply {
        textProperty().bind(text)
        style = "-fx-text-fill: #f2f7ff;"
    }

    private fun headerLabel(key: String) = Label().apply {
        textProperty().bind(binding(key))
        style = "-fx-text-fill: #9fb1c8; -fx-font-size: 11px;"
    }

    private fun slider(property: DoubleProperty, min: Double, max: Double): javafx.scene.Node =
        sliderRow(Slider(min, max, property.get()).also { property.bindBidirectional(it.valueProperty()) }, "%.2f")

    private fun slider(property: IntegerProperty, min: Double, max: Double, integer: Boolean): javafx.scene.Node {
        val slider = Slider(min, max, property.get().toDouble()).apply {
            isSnapToTicks = integer
            majorTickUnit = 1.0
            minorTickCount = 0
            blockIncrement = 1.0
        }
        slider.valueProperty().addListener { _, _, v -> property.set(v.toInt()) }
        property.addListener { _, _, v -> if (slider.value.toInt() != v.toInt()) slider.value = v.toDouble() }
        return sliderRow(slider, "%.0f")
    }

    private fun sliderRow(slider: Slider, format: String): javafx.scene.Node {
        slider.prefWidth = SLIDER_WIDTH
        val value = Label().apply {
            textProperty().bind(slider.valueProperty().asString(format))
            style = "-fx-text-fill: #cfe0f5; -fx-font-size: 11px;"
            minWidth = 34.0
        }
        return javafx.scene.layout.HBox(4.0, slider, value).apply { alignment = Pos.CENTER_LEFT }
    }

    private fun <T : Enum<T>> enumCombo(
        property: ObjectProperty<T>,
        items: List<T>,
        keyOf: (T) -> String
    ): ComboBox<T> = ComboBox<T>().apply {
        this.items.setAll(items)
        valueProperty().bindBidirectional(property)
        val cellFactory = {
            object : ListCell<T>() {
                override fun updateItem(item: T?, empty: Boolean) {
                    super.updateItem(item, empty)
                    textProperty().unbind()
                    if (empty || item == null) text = null else textProperty().bind(binding(keyOf(item)))
                }
            }
        }
        setCellFactory { cellFactory() }
        buttonCell = cellFactory()
        prefWidth = COMBO_WIDTH
        maxWidth = Double.MAX_VALUE
    }

    private fun resetDefaults() {
        val defaults = listOf(
            Channel("hue", Source.PITCH_CLASS, 0.0, 360.0, hue.range),
            Channel("saturation", Source.MOOD, 0.45, 1.0, saturation.range),
            Channel("brightness", Source.LEVEL, 0.25, 1.0, brightness.range),
            Channel("motion", Source.SPEED, 0.15, 1.5, motion.range),
            Channel("size", Source.DENSITY, 0.2, 1.0, size.range)
        )
        channels.zip(defaults).forEach { (channel, default) -> channel.reset(default) }
        pattern.set(Pattern.BLOBS)
        layers.set(6)
        variety.set(40.0)
        reaction.set(0.6)
        trails.set(0.5)
        idleGlow.set(true)
        maxNotesPerSecond.set(8.0)
    }

    private fun bindSettings(settings: SettingHelper) {
        channels.forEach { channel ->
            val key = "$SETTING_PREFIX.${channel.name}"
            settings.bindEnum(channel.source, "$key.source", Source::class.java)
            settings.bindDouble(channel.min, "$key.min")
            settings.bindDouble(channel.max, "$key.max")
        }
        settings.bindEnum(pattern, "$SETTING_PREFIX.pattern", Pattern::class.java)
        settings.bindInt(layers, "$SETTING_PREFIX.layers")
        settings.bindDouble(variety, "$SETTING_PREFIX.variety")
        settings.bindDouble(reaction, "$SETTING_PREFIX.reaction")
        settings.bindDouble(trails, "$SETTING_PREFIX.trails")
        settings.bindBoolean(idleGlow, "$SETTING_PREFIX.idle")
        settings.bindDouble(maxNotesPerSecond, "$SETTING_PREFIX.rate")
    }

    private fun binding(key: String): StringBinding = I18n.binding(bundle, "$PREFIX.$key")

    companion object : MmxViewMetadata {
        override val bundleBaseName = "i18n/view/mood"
        override val bundleKeyPrefix = "mood."
        override fun getViewImagePath(): String = "/view/led.png"
        override fun getViewIcon(): Ikon = FontAwesomeSolid.PALETTE

        private const val PREFIX = "mood"
        private const val SETTING_PREFIX = "view.mood"
        private const val PANEL_WIDTH = 400.0
        private const val SLIDER_WIDTH = 90.0
        private const val COMBO_WIDTH = 120.0
        private const val MAX_FRAME_SECONDS = 0.1
        private const val MIN_REACTION = 0.05
        private const val ATTACK_FACTOR = 0.25
        private const val IDLE_LEVEL = 0.15
        private const val IDLE_BREATH = 0.06
        private const val BACKGROUND_BRIGHTNESS = 0.12
        private const val MAX_TRAIL = 0.93
        private const val GOLDEN_ANGLE = 2.399963
        private const val WAVE_STEP = 8.0
        private const val BASE_MOTION = 0.3
        private const val PULSE_MOTION = 3.0
        private const val PULSE_DECAY_SECONDS = 0.35
        private const val BLOB_PULSE = 0.6
        private const val WAVE_PULSE = 1.0
        private const val RIPPLE_LIFE_SECONDS = 2.5
        private const val RIPPLE_RINGS = 3
        private const val RIPPLE_HUE_RANGE = 120.0
        private const val MAX_RIPPLES = 40

        // Same bundle as the metadata lookups (title/description) so the live bindings are shared.
        private val bundle by lazy { I18n.registerBundle(bundleBaseName, bundleClassLoader) }
    }
}
