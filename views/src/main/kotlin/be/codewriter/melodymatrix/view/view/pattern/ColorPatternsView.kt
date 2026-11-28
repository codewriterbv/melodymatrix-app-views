package be.codewriter.melodymatrix.view.view.pattern

import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.event.MidiDataEvent
import be.codewriter.melodymatrix.view.event.MmxEvent
import be.codewriter.melodymatrix.view.event.MmxEventType
import be.codewriter.melodymatrix.view.event.PlaybackPositionEvent
import be.codewriter.melodymatrix.view.helper.SettingHelper
import be.codewriter.melodymatrix.view.i18n.I18n
import be.codewriter.melodymatrix.view.view.MmxView
import be.codewriter.melodymatrix.view.view.MmxViewMetadata
import be.codewriter.melodymatrix.view.view.mood.MoodAnalyzer
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
import javafx.scene.Node
import javafx.scene.canvas.Canvas
import javafx.scene.SnapshotParameters
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ScrollPane
import javafx.scene.control.Slider
import javafx.scene.control.ToggleButton
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.RadialGradient
import javafx.scene.paint.Stop
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

/**
 * Colour patterns that are drawn from the notes themselves: every piano key has an energy that jumps
 * when the key is played and fades afterwards (see [PitchEnergy]), and four patterns turn those 88
 * energies into shapes.
 *
 * - **Pillars**: rounded, glowing columns, mirrored around the centre line, low notes left, high notes right
 * - **Starburst**: tapered rays around a glowing core, mirrored left and right, slowly turning
 * - **Ribbon**: one smooth, mirrored wave whose colour flows along the pitch
 * - **Bloom**: one ring per octave with a dot for every note, placed by its place on the circle of fifths
 * - **Curtains**: soft, swaying curtains of light that hang from the top and reach lower with every note
 * - **Waterfall**: the notes of the last seconds, scrolling upwards as glowing cells
 * - **Dots**: a grid of dots that lights up like an old-school equaliser
 * - **Petals**: twelve petals, one per note name on the circle of fifths, that open with the notes
 *
 * A palette decides how pitch becomes colour. Where [MoodColorView] reacts to the character of the music
 * as a whole, this view shows which notes sound.
 *
 * @param settings Optional storage to persist the configuration.
 */
class ColorPatternsView(
    settings: SettingHelper? = null
) : MmxView() {

    override val fitToViewport: Boolean = true

    enum class Pattern(val key: String) {
        PILLARS("pillars"),
        STARBURST("starburst"),
        RIBBON("ribbon"),
        BLOOM("bloom"),
        CURTAINS("curtains"),
        WATERFALL("waterfall"),
        DOTS("dots"),
        PETALS("petals")
    }

    /** How a position on the keyboard becomes a hue (0..360). */
    enum class Palette(val key: String) {
        PITCH_CLASS("pitch"),
        RAINBOW("rainbow"),
        FIRE("fire"),
        OCEAN("ocean"),
        AURORA("aurora");

        /** [position] is 0..1 from the lowest to the highest key, [pitchClass] 0..1 on the circle of fifths. */
        fun hue(position: Double, pitchClass: Double): Double = when (this) {
            PITCH_CLASS -> pitchClass * 360.0
            RAINBOW -> position * 300.0
            FIRE -> -12.0 + position * 72.0
            OCEAN -> 165.0 + position * 105.0
            AURORA -> 125.0 + position * 185.0
        }
    }

    private val pattern: ObjectProperty<Pattern> = SimpleObjectProperty(Pattern.PILLARS)
    private val palette: ObjectProperty<Palette> = SimpleObjectProperty(Palette.RAINBOW)
    private val bars: IntegerProperty = SimpleIntegerProperty(DEFAULT_BARS)
    private val spread: DoubleProperty = SimpleDoubleProperty(DEFAULT_SPREAD)
    private val release: DoubleProperty = SimpleDoubleProperty(DEFAULT_RELEASE)
    private val glow: DoubleProperty = SimpleDoubleProperty(DEFAULT_GLOW)
    private val rotation: DoubleProperty = SimpleDoubleProperty(DEFAULT_ROTATION)
    private val shimmer: BooleanProperty = SimpleBooleanProperty(true)

    private val energy = PitchEnergy()
    private val canvas = Canvas()

    // Only touched by the animation timer
    private val levels = DoubleArray(PitchEnergy.KEYS)
    private val columns = DoubleArray(PitchEnergy.KEYS)
    private val scratch = DoubleArray(PitchEnergy.KEYS)
    private var phase = 0.0
    private var turn = 0.0
    private var lastNanos = 0L

    private val history = ArrayDeque<DoubleArray>()
    private var historyClock = 0.0

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
            style = "-fx-background-color: $BACKGROUND_CSS;"
            StackPane.setAlignment(overlay, Pos.TOP_RIGHT)
        }

        rootNode = root
        setupSurface(root, 1000.0, 700.0, canvasPane, 1000, 700) { timer.stop() }

        // Only draw while the view can be seen (selected tab, window showing and not minimised)
        whileOnScreen(
            show = {
                lastNanos = 0L
                timer.start()
            },
            hide = { timer.stop() }
        )
    }

    override fun onEvent(event: MmxEvent) {
        when (event.type) {
            MmxEventType.MIDI -> {
                val midi = event as? MidiDataEvent ?: return
                if (midi.isDrum) return
                when (midi.event) {
                    MidiEvent.NOTE_ON -> energy.noteOn(midi.note.byteValue, midi.velocity)
                    MidiEvent.NOTE_OFF -> energy.noteOff(midi.note.byteValue)
                    else -> {}
                }
            }

            MmxEventType.PLAYBACK_POSITION -> {
                val position = event as? PlaybackPositionEvent ?: return
                if (position.songPositionMs == 0L && position.speed > 0.0) energy.reset()
            }

            else -> {}
        }
    }

    private fun frame(dt: Double) {
        energy.step(dt, release.get())
        energy.copyTo(levels)
        phase += dt

        if (shimmer.get()) {
            // A faint wave runs over the keys while nothing sounds, so the view is never an empty screen
            for (key in levels.indices) {
                levels[key] = max(levels[key], SHIMMER_BASE + SHIMMER_AMPLITUDE * sin(key * 0.32 - phase * 1.6))
            }
        }

        val count = bars.get().coerceIn(MIN_BARS, PitchEnergy.KEYS)
        PitchEnergy.pool(levels, count, columns)
        PitchEnergy.spread(columns, count, spread.get(), scratch)

        var total = 0.0
        for (i in 0 until count) total += columns[i]
        val average = total / count
        trackHistory(dt, count)
        turn += dt * rotation.get() * (TURN_BASE + average * TURN_ENERGY)

        draw(canvas.graphicsContext2D, count, average)
    }

    /** Remembers the columns a few times per second for the waterfall. */
    private fun trackHistory(dt: Double, count: Int) {
        historyClock += dt
        if (historyClock < WATERFALL_INTERVAL) return
        historyClock = 0.0
        history.addFirst(columns.copyOf(count))
        while (history.size > WATERFALL_ROWS) history.removeLast()
    }

    // --- colour ---------------------------------------------------------------------------------

    /** Colour for [position] (0..1 over the keyboard) at the given [level]: louder is brighter and a little whiter. */
    private fun colorAt(position: Double, level: Double, alpha: Double = 1.0): Color {
        val pitch = PitchEnergy.LOWEST_PITCH + (position * (PitchEnergy.KEYS - 1)).toInt()
        return colorOf(position, MoodAnalyzer.circleOfFifths(pitch), level, alpha)
    }

    private fun colorOf(position: Double, pitchClass: Double, level: Double, alpha: Double = 1.0): Color {
        val hue = palette.get().hue(position, pitchClass) + level * HUE_LIFT
        val saturation = 0.92 - 0.35 * level.pow(3)
        val brightness = 0.5 + 0.5 * level
        return Color.hsb(((hue % 360.0) + 360.0) % 360.0, saturation.coerceIn(0.0, 1.0), brightness.coerceIn(0.0, 1.0), alpha.coerceIn(0.0, 1.0))
    }

    // --- drawing --------------------------------------------------------------------------------

    private fun draw(gc: GraphicsContext, count: Int, average: Double) {
        val w = canvas.width
        val h = canvas.height
        if (w < 1 || h < 1) return

        gc.fill = Color.web(BACKGROUND_CSS)
        gc.fillRect(0.0, 0.0, w, h)
        // Plain alpha blending on purpose: on a JavaFX canvas every shape drawn with another blend mode (SCREEN, ADD)
        // is 30 to 150 times slower, which made the dots and the waterfall run at 3 fps.

        when (pattern.get()) {
            Pattern.PILLARS -> drawPillars(gc, w, h, count)
            Pattern.STARBURST -> drawStarburst(gc, w, h, count, average)
            Pattern.RIBBON -> drawRibbon(gc, w, h, count)
            Pattern.BLOOM -> drawBloom(gc, w, h)
            Pattern.CURTAINS -> drawCurtains(gc, w, h, count)
            Pattern.WATERFALL -> drawWaterfall(gc, w, h, count)
            Pattern.DOTS -> drawDots(gc, w, h, count)
            Pattern.PETALS -> drawPetals(gc, w, h)
        }
    }

    private fun drawPillars(gc: GraphicsContext, w: Double, h: Double, count: Int) {
        val slot = w * 0.92 / count
        val width = max(3.0, slot * 0.56)
        val left = w * 0.04
        val centre = h / 2
        for (i in 0 until count) {
            val level = columns[i]
            val height = width + level * h * 0.8
            val x = left + slot * (i + 0.5)
            val color = colorAt((i + 0.5) / count, level)
            if (glow.get() > 0.0 && level > GLOW_MIN) {
                gc.fill = colorAt((i + 0.5) / count, level, GLOW_ALPHA * glow.get() * (0.25 + level))
                val pad = width * 0.9
                gc.fillRoundRect(x - width / 2 - pad, centre - height / 2 - pad, width + 2 * pad, height + 2 * pad, width + 2 * pad, width + 2 * pad)
            }
            gc.fill = color
            gc.fillRoundRect(x - width / 2, centre - height / 2, width, height, width, width)
        }
    }

    private fun drawStarburst(gc: GraphicsContext, w: Double, h: Double, count: Int, average: Double) {
        val cx = w / 2
        val cy = h / 2
        val size = min(w, h)
        val inner = size * 0.1
        val reach = size * 0.4

        if (glow.get() > 0.0) {
            val radius = inner * (2.2 + 2.5 * average)
            gc.fill = RadialGradient(
                0.0, 0.0, cx, cy, radius, false, CycleMethod.NO_CYCLE,
                Stop(0.0, colorAt(0.5 + 0.3 * sin(phase * 0.4), average, (0.2 + average) * glow.get() * 0.8)),
                Stop(1.0, Color.TRANSPARENT)
            )
            gc.fillOval(cx - radius, cy - radius, radius * 2, radius * 2)
        }

        // Every column is drawn twice, mirrored left and right, so the burst stays symmetric while it turns.
        // With many columns only some are drawn: beyond MAX_RAYS the rays are too thin to tell apart anyway.
        val rays = min(count, MAX_RAYS)
        val half = PI * inner / rays * 0.38
        for (mirror in 0..1) {
            for (i in 0 until rays) {
                val level = columns[min(count - 1, (i * count + count / 2) / rays)]
                val angle = turn - PI / 2 + (if (mirror == 0) 1 else -1) * (i + 0.5) / rays * PI
                val length = size * 0.012 + level * reach
                val color = colorAt((i + 0.5) / rays, level)
                if (glow.get() > 0.0 && level > GLOW_MIN) {
                    gc.fill = colorAt((i + 0.5) / rays, level, GLOW_ALPHA * glow.get() * (0.3 + level))
                    ray(gc, cx, cy, angle, inner, length * 1.04, half * 2.6)
                }
                gc.fill = color
                ray(gc, cx, cy, angle, inner, length, half)
            }
        }
    }

    /** A thin triangle that starts at radius [inner] with a base of [half] to both sides and ends in a point. */
    private fun ray(gc: GraphicsContext, cx: Double, cy: Double, angle: Double, inner: Double, length: Double, half: Double) {
        val dx = cos(angle)
        val dy = sin(angle)
        val bx = cx + dx * inner
        val by = cy + dy * inner
        gc.fillPolygon(
            doubleArrayOf(bx - dy * half, cx + dx * (inner + length), bx + dy * half),
            doubleArrayOf(by + dx * half, cy + dy * (inner + length), by - dx * half),
            3
        )
    }

    private fun drawRibbon(gc: GraphicsContext, w: Double, h: Double, count: Int) {
        val steps = (w / RIBBON_STEP).toInt().coerceAtLeast(8)
        val xs = DoubleArray(steps * 2 + 2)
        val top = DoubleArray(steps + 1)
        val centre = h / 2

        fun sample(position: Double): Double {
            val f = position * (count - 1)
            val i0 = floor(f).toInt().coerceIn(0, count - 1)
            val i1 = min(i0 + 1, count - 1)
            val t = (f - i0).let { it * it * (3 - 2 * it) }
            return columns[i0] * (1 - t) + columns[i1] * t
        }
        for (s in 0..steps) top[s] = sample(s.toDouble() / steps)

        val stops = (0..RIBBON_STOPS).map {
            val p = it.toDouble() / RIBBON_STOPS
            Stop(p, colorAt(p, sample(p)))
        }
        val ys = DoubleArray(steps * 2 + 2)
        for ((layer, scale) in listOf(1.0 to 0.35, 0.55 to 0.95)) {
            for (s in 0..steps) {
                val x = w * s / steps
                val offset = h * 0.004 + top[s] * scale * h * 0.42
                xs[s] = x
                ys[s] = centre - offset
                xs[steps * 2 + 1 - s] = x
                ys[steps * 2 + 1 - s] = centre + offset
            }
            gc.fill = LinearGradient(0.0, 0.0, w, 0.0, false, CycleMethod.NO_CYCLE, stops.map {
                Stop(it.offset, Color(it.color.red, it.color.green, it.color.blue, layer))
            })
            gc.fillPolygon(xs, ys, steps * 2 + 2)
            if (glow.get() > 0.0 && scale == 1.0) {
                gc.lineWidth = 1.5 + 6.0 * glow.get()
                gc.stroke = LinearGradient(0.0, 0.0, w, 0.0, false, CycleMethod.NO_CYCLE, stops.map {
                    Stop(it.offset, Color(it.color.red, it.color.green, it.color.blue, 0.25 * glow.get()))
                })
                gc.strokePolyline(xs.copyOfRange(0, steps + 1), ys.copyOfRange(0, steps + 1), steps + 1)
            }
        }
    }

    private fun drawBloom(gc: GraphicsContext, w: Double, h: Double) {
        val cx = w / 2
        val cy = h / 2
        val size = min(w, h)
        val inner = size * 0.1
        val outer = size * 0.46
        val rings = (PitchEnergy.KEYS + 11) / 12
        val gap = (outer - inner) / rings

        for (key in 0 until PitchEnergy.KEYS) {
            val pitch = PitchEnergy.LOWEST_PITCH + key
            val ring = key / 12
            val level = levels[key]
            // Notes with the same name sit on the same spoke; each octave twists a little further
            val angle = turn * 0.4 - PI / 2 + MoodAnalyzer.circleOfFifths(pitch) * 2 * PI + ring * 0.1
            val radius = inner + gap * (ring + 0.5)
            val x = cx + cos(angle) * radius
            val y = cy + sin(angle) * radius
            val position = key / (PitchEnergy.KEYS - 1.0)

            if (level > BLOOM_VISIBLE) {
                val dot = gap * (0.1 + 0.6 * level)
                if (glow.get() > 0.0) {
                    // Two flat, translucent discs instead of a gradient: far cheaper and looks the same at this size
                    val halo = dot * (1.8 + 1.6 * glow.get())
                    gc.fill = colorAt(position, level, 0.16 * glow.get())
                    gc.fillOval(x - halo, y - halo, halo * 2, halo * 2)
                    val mid = dot * (1.2 + 0.5 * glow.get())
                    gc.fill = colorAt(position, level, 0.3 * glow.get())
                    gc.fillOval(x - mid, y - mid, mid * 2, mid * 2)
                }
                gc.fill = colorAt(position, level)
                gc.fillOval(x - dot, y - dot, dot * 2, dot * 2)
            } else {
                gc.fill = colorAt(position, 0.0, BLOOM_DIM_ALPHA)
                val dot = max(1.5, gap * 0.07)
                gc.fillOval(x - dot, y - dot, dot * 2, dot * 2)
            }
        }
    }

    private fun drawCurtains(gc: GraphicsContext, w: Double, h: Double, count: Int) {
        val slot = w / count
        for (i in 0 until count) {
            val level = columns[i]
            val position = (i + 0.5) / count
            val sway = sin(phase * 0.8 + i * 0.35) * slot * (0.3 + 1.6 * level)
            val x = slot * (i + 0.5) + sway
            val length = h * (0.12 + 0.85 * level)
            val half = slot * (0.7 + 0.5 * glow.get())
            gc.fill = LinearGradient(
                0.0, 0.0, 0.0, length, false, CycleMethod.NO_CYCLE,
                Stop(0.0, colorAt(position, level, 0.35 + 0.55 * level)),
                Stop(0.55, colorAt(position, level * 0.8, 0.25 * (0.4 + level))),
                Stop(1.0, Color.TRANSPARENT)
            )
            gc.fillRect(x - half, 0.0, half * 2, length)
        }
    }

    private fun drawWaterfall(gc: GraphicsContext, w: Double, h: Double, count: Int) {
        // Never more than WATERFALL_MAX_COLUMNS cells per row: bigger cells look better and cost a lot less
        val cells = min(count, WATERFALL_MAX_COLUMNS)
        val slot = w * 0.96 / cells
        val rowHeight = h / WATERFALL_ROWS
        val cell = min(slot, rowHeight) * 0.82
        val left = w * 0.02
        history.forEachIndexed { row, values ->
            val age = 1.0 - row.toDouble() / WATERFALL_ROWS
            val y = h - (row + 0.5) * rowHeight
            for (i in 0 until cells) {
                val level = values[min(values.size - 1, i * values.size / cells)]
                if (level < WATERFALL_MIN) continue
                val x = left + slot * (i + 0.5)
                gc.fill = colorAt((i + 0.5) / cells, level, age * (0.2 + 0.8 * level))
                val edge = cell * (0.5 + 0.5 * level)
                gc.fillRoundRect(x - edge / 2, y - edge / 2, edge, edge, edge * 0.6, edge * 0.6)
            }
        }
    }

    private var matrixImage: Image? = null
    private var matrixWidth = 0.0
    private var matrixHeight = 0.0
    private var matrixColumns = 0

    private fun drawDots(gc: GraphicsContext, w: Double, h: Double, count: Int) {
        val slot = w * 0.94 / count
        val rows = ((h * 0.86) / slot).toInt().coerceIn(6, MAX_DOT_ROWS)
        val step = h * 0.86 / rows
        val radius = min(slot, step) * 0.36
        val left = w * 0.03
        val bottom = h * 0.93

        // The unlit grid never changes, so it is drawn once into an image and reused until the size changes
        if (matrixImage == null || matrixWidth != w || matrixHeight != h || matrixColumns != count) {
            val grid = Canvas(w, h)
            grid.graphicsContext2D.fill = Color(0.7, 0.8, 1.0, DOT_DIM_ALPHA)
            for (i in 0 until count) for (r in 0 until rows) {
                val size = radius * 0.55
                grid.graphicsContext2D.fillOval(left + slot * (i + 0.5) - size, bottom - (r + 0.5) * step - size, size * 2, size * 2)
            }
            matrixImage = grid.snapshot(SnapshotParameters().apply { fill = Color.TRANSPARENT }, null)
            matrixWidth = w
            matrixHeight = h
            matrixColumns = count
        }
        gc.drawImage(matrixImage, 0.0, 0.0)

        for (i in 0 until count) {
            val lit = columns[i] * rows
            val litDots = kotlin.math.ceil(lit).toInt()
            if (litDots <= 0) continue
            val x = left + slot * (i + 0.5)
            val position = (i + 0.5) / count
            if (glow.get() > 0.0 && columns[i] > GLOW_MIN) {
                // One soft column of light instead of a glow disc behind every dot
                gc.fill = colorAt(position, 0.5, GLOW_ALPHA * glow.get() * (0.3 + columns[i]))
                gc.fillRoundRect(x - slot * 0.6, bottom - lit * step - radius, slot * 1.2, lit * step + radius * 2, slot * 1.2, slot * 1.2)
            }
            for (r in 0 until litDots) {
                val on = (lit - r).coerceIn(0.0, 1.0)
                // Higher dots in a lit column are hotter, like the top of an equaliser
                gc.fill = colorAt(position, 0.35 + 0.65 * r / rows, on)
                val size = radius * (0.55 + 0.45 * on)
                gc.fillOval(x - size, bottom - (r + 0.5) * step - size, size * 2, size * 2)
            }
        }
    }

    private fun drawPetals(gc: GraphicsContext, w: Double, h: Double) {
        val cx = w / 2
        val cy = h / 2
        val size = min(w, h)
        // The loudest key of every note name, in circle-of-fifths order
        val names = DoubleArray(PETALS)
        for (key in levels.indices) {
            val slot = (MoodAnalyzer.circleOfFifths(PitchEnergy.LOWEST_PITCH + key) * PETALS).toInt().coerceIn(0, PETALS - 1)
            names[slot] = max(names[slot], levels[key])
        }
        val average = names.average()

        if (glow.get() > 0.0) {
            val radius = size * (0.12 + 0.2 * average)
            gc.fill = RadialGradient(
                0.0, 0.0, cx, cy, radius, false, CycleMethod.NO_CYCLE,
                Stop(0.0, colorAt(0.5 + 0.4 * sin(phase * 0.3), average, (0.3 + average) * glow.get())),
                Stop(1.0, Color.TRANSPARENT)
            )
            gc.fillOval(cx - radius, cy - radius, radius * 2, radius * 2)
        }

        // Back layer: turned half a petal, fed by the neighbours; front layer: the petals themselves
        for (layer in 0..1) {
            for (slot in 0 until PETALS) {
                val level = if (layer == 0) (names[slot] + names[(slot + 1) % PETALS]) / 2 * 0.8 else names[slot]
                val angle = turn * 0.5 - PI / 2 + (slot + layer * 0.5) * 2 * PI / PETALS
                val length = size * (0.06 + 0.4 * level) * (if (layer == 0) 1.1 else 0.85)
                val width = length * (0.28 + 0.12 * level)
                val color = colorOf(slot.toDouble() / PETALS, slot.toDouble() / PETALS, level, if (layer == 0) 0.55 else 0.95)
                petal(gc, cx, cy, angle, size * 0.04, length, width, color)
            }
        }
    }

    /** A teardrop that starts at radius [start] and points outwards. */
    private fun petal(gc: GraphicsContext, cx: Double, cy: Double, angle: Double, start: Double, length: Double, width: Double, color: Color) {
        val dx = cos(angle)
        val dy = sin(angle)
        val bx = cx + dx * start
        val by = cy + dy * start
        val tx = cx + dx * (start + length)
        val ty = cy + dy * (start + length)
        val mx = bx + dx * length * 0.35
        val my = by + dy * length * 0.35
        gc.fill = color
        gc.beginPath()
        gc.moveTo(bx, by)
        gc.bezierCurveTo(mx - dy * width, my + dx * width, tx - dy * width * 0.2, ty + dx * width * 0.2, tx, ty)
        gc.bezierCurveTo(tx + dy * width * 0.2, ty - dx * width * 0.2, mx + dy * width, my - dx * width, bx, by)
        gc.closePath()
        gc.fill()
    }

    // --- settings panel -------------------------------------------------------------------------

    /** The same panel as in the view, for the video recorder (`panel|patterns`). */
    override fun demoSettingsPanel(name: String): Node? = if (name == "patterns") settingsPanel() else null

    private fun settingsPanel(): ScrollPane {
        val grid = GridPane().apply {
            hgap = 8.0
            vgap = 6.0
        }
        var row = 0
        fun setting(labelKey: String, control: Node) {
            grid.add(Label().apply {
                textProperty().bind(binding(labelKey))
                style = "-fx-text-fill: #f2f7ff;"
            }, 0, row)
            grid.add(control, 1, row++)
        }

        setting("pattern", enumCombo(pattern, Pattern.entries) { "pattern.${it.key}" })
        setting("palette", enumCombo(palette, Palette.entries) { "palette.${it.key}" })
        setting("bars", slider(bars, MIN_BARS.toDouble(), PitchEnergy.KEYS.toDouble()))
        setting("spread", slider(spread, 0.0, 1.0))
        setting("release", slider(release, 0.1, 3.0))
        setting("glow", slider(glow, 0.0, 1.0))
        setting("rotation", slider(rotation, 0.0, 3.0))
        setting("idle", CheckBox().apply { selectedProperty().bindBidirectional(shimmer) })

        grid.add(Button().apply {
            textProperty().bind(binding("reset"))
            setOnAction { resetDefaults() }
        }, 0, row, 2, 1)

        return ScrollPane(grid).apply {
            isFitToWidth = true
            prefWidth = PANEL_WIDTH
            maxWidth = PANEL_WIDTH
            maxHeight = Double.MAX_VALUE
            padding = Insets(4.0)
            style = "-fx-background: rgba(10,16,26,0.82); -fx-background-color: rgba(10,16,26,0.82); -fx-background-radius: 12;"
            grid.padding = Insets(8.0)
            VBox.setVgrow(this, Priority.ALWAYS)
        }
    }

    private fun slider(property: DoubleProperty, min: Double, max: Double): Node =
        sliderRow(Slider(min, max, property.get()).also { property.bindBidirectional(it.valueProperty()) }, "%.2f")

    private fun slider(property: IntegerProperty, min: Double, max: Double): Node {
        val slider = Slider(min, max, property.get().toDouble()).apply {
            isSnapToTicks = true
            majorTickUnit = 1.0
            minorTickCount = 0
            blockIncrement = 1.0
        }
        slider.valueProperty().addListener { _, _, v -> property.set(v.toInt()) }
        property.addListener { _, _, v -> if (slider.value.toInt() != v.toInt()) slider.value = v.toDouble() }
        return sliderRow(slider, "%.0f")
    }

    private fun sliderRow(slider: Slider, format: String): Node {
        slider.prefWidth = SLIDER_WIDTH
        val value = Label().apply {
            textProperty().bind(slider.valueProperty().asString(format))
            style = "-fx-text-fill: #cfe0f5; -fx-font-size: 11px;"
            minWidth = 34.0
        }
        return HBox(4.0, slider, value).apply { alignment = Pos.CENTER_LEFT }
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
        pattern.set(Pattern.PILLARS)
        palette.set(Palette.RAINBOW)
        bars.set(DEFAULT_BARS)
        spread.set(DEFAULT_SPREAD)
        release.set(DEFAULT_RELEASE)
        glow.set(DEFAULT_GLOW)
        rotation.set(DEFAULT_ROTATION)
        shimmer.set(true)
    }

    private fun bindSettings(settings: SettingHelper) {
        settings.bindEnum(pattern, "$SETTING_PREFIX.pattern", Pattern::class.java)
        settings.bindEnum(palette, "$SETTING_PREFIX.palette", Palette::class.java)
        settings.bindInt(bars, "$SETTING_PREFIX.bars")
        settings.bindDouble(spread, "$SETTING_PREFIX.spread")
        settings.bindDouble(release, "$SETTING_PREFIX.release")
        settings.bindDouble(glow, "$SETTING_PREFIX.glow")
        settings.bindDouble(rotation, "$SETTING_PREFIX.rotation")
        settings.bindBoolean(shimmer, "$SETTING_PREFIX.idle")
    }

    private fun binding(key: String): StringBinding = I18n.binding(bundle, "$PREFIX.$key")

    companion object : MmxViewMetadata {
        override val bundleBaseName = "i18n/view/pattern"
        override val bundleKeyPrefix = "pattern."
        override fun getViewImagePath(): String = "/view/led.png"
        override fun getViewIcon(): Ikon = FontAwesomeSolid.CHART_BAR

        private const val PREFIX = "pattern"
        private const val SETTING_PREFIX = "view.pattern"
        private const val BACKGROUND_CSS = "#05070b"
        private const val PANEL_WIDTH = 330.0
        private const val SLIDER_WIDTH = 110.0
        private const val COMBO_WIDTH = 140.0
        private const val MAX_FRAME_SECONDS = 0.1
        private const val MIN_BARS = 8
        private const val DEFAULT_BARS = 44
        private const val DEFAULT_SPREAD = 0.5
        private const val DEFAULT_RELEASE = 0.6
        private const val DEFAULT_GLOW = 0.6
        private const val DEFAULT_ROTATION = 1.0
        private const val GLOW_ALPHA = 0.22
        private const val HUE_LIFT = 24.0
        private const val SHIMMER_BASE = 0.05
        private const val SHIMMER_AMPLITUDE = 0.045
        private const val TURN_BASE = 0.08
        private const val TURN_ENERGY = 0.5
        private const val RIBBON_STEP = 10.0
        private const val RIBBON_STOPS = 10
        private const val BLOOM_VISIBLE = 0.06
        private const val BLOOM_DIM_ALPHA = 0.14
        private const val DOT_DIM_ALPHA = 0.08
        private const val MAX_DOT_ROWS = 28
        private const val WATERFALL_ROWS = 28
        private const val WATERFALL_INTERVAL = 0.09
        private const val WATERFALL_MIN = 0.1 // above the idle shimmer, so silence costs nothing
        private const val WATERFALL_MAX_COLUMNS = 44
        private const val GLOW_MIN = 0.1
        private const val PETALS = 12
        private const val MAX_RAYS = 64

        // Same bundle as the metadata lookups (title/description) so the live bindings are shared.
        private val bundle by lazy { I18n.registerBundle(bundleBaseName, bundleClassLoader) }
    }
}
