package be.codewriter.melodymatrix.view.stage.piano.component

import com.almasb.fxgl.app.GameApplication
import com.almasb.fxgl.app.GameSettings
import com.almasb.fxgl.core.math.FXGLMath
import com.almasb.fxgl.core.math.Vec2
import com.almasb.fxgl.dsl.*
import com.almasb.fxgl.dsl.FXGL.Companion.getbp
import com.almasb.fxgl.dsl.FXGL.Companion.getdp
import com.almasb.fxgl.dsl.FXGL.Companion.getop
import com.almasb.fxgl.dsl.components.AccumulatedUpdateComponent
import com.almasb.fxgl.dsl.components.ExpireCleanComponent
import com.almasb.fxgl.entity.Entity
import com.almasb.fxgl.entity.EntityFactory
import com.almasb.fxgl.entity.SpawnData
import com.almasb.fxgl.entity.Spawns
import com.almasb.fxgl.entity.components.IrremovableComponent
import com.almasb.fxgl.particle.ParticleComponent
import com.almasb.fxgl.particle.ParticleEmitters
import javafx.geometry.Point2D
import javafx.scene.Cursor
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.effect.BlendMode
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.util.Duration
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.collections.set

class PianoGenerator(
    val configuratorBackground: be.codewriter.melodymatrix.view.stage.piano.component.ConfiguratorBackground,
    val configuratorEffect: be.codewriter.melodymatrix.view.stage.piano.component.ConfiguratorEffect,
    val configuratorKey: be.codewriter.melodymatrix.view.stage.piano.component.ConfiguratorKey
) : GameApplication() {

    var gameFactory: GameFactory = GameFactory()

    init {
        canvas = Canvas(PIANO_WIDTH.toDouble(), PIANO_HEIGHT.toDouble())
        g = canvas.getGraphicsContext2D()
    }

    override fun initSettings(settings: GameSettings) {
        settings.width = PIANO_WIDTH
        settings.height = PIANO_HEIGHT
        settings.ticksPerSecond = 60
    }

    override fun initGameVars(vars: MutableMap<String, Any>) {
        be.codewriter.melodymatrix.view.stage.piano.component.DefaultValues.setDefaults(vars)
    }

    enum class EntityType {
        BACKGROUND_COLOR, BACKGROUND_IMAGE, LOGO
    }

    override fun initGame() {
        // Bindings can only be created when FXGL has started, so we have a callback here
        configuratorBackground.createBindings()
        configuratorEffect.createBindings()
        configuratorKey.createBindings()

        getGameScene().setCursor(Cursor.DEFAULT)

        FXGL.getGameWorld().addEntityFactory(this.gameFactory)
        FXGL.spawn(
            EntityType.BACKGROUND_COLOR.name, SpawnData(0.0, 0.0)
                .put("width", FXGL.getAppWidth())
                .put("height", FXGL.getAppHeight())
        )
        FXGL.spawn(
            EntityType.BACKGROUND_IMAGE.name, SpawnData(0.0, 0.0)
        )
        FXGL.spawn(
            EntityType.LOGO.name, SpawnData(0.0, 0.0)
        )
        entityBuilder()
            .view(canvas)
            .with(object : AccumulatedUpdateComponent(3) {
                // Accumulated number can be adjusted to make more or less performant
                // TODO make this value bindable from settings
                override fun onAccumulatedUpdate(tpfSum: Double) {
                    g.clearRect(0.0, 0.0, PIANO_WIDTH.toDouble(), PIANO_HEIGHT.toDouble())
                }
            })
            .buildAndAttach()

        var counterWhiteKeys = 0
        var previousWhiteKeyX = 0.0

        be.codewriter.melodymatrix.view.definition.Note.pianoKeys().forEach { note ->
            if (note.mainNote.isSharp) {
                val x = previousWhiteKeyX + PIANO_WHITE_KEY_WIDTH - (PIANO_BLACK_KEY_WIDTH / 2)
                val y = PIANO_HEIGHT - PIANO_WHITE_KEY_HEIGHT
                val key = be.codewriter.melodymatrix.view.stage.piano.component.PianoKeyBlack(note, x, y)
                keys[note] = key
                addUINode(key, x, y)
            } else {
                val x = counterWhiteKeys * PIANO_WHITE_KEY_WIDTH
                val y = PIANO_HEIGHT - PIANO_WHITE_KEY_HEIGHT
                val key = be.codewriter.melodymatrix.view.stage.piano.component.PianoKeyWhite(note, x, y)
                keys[note] = key
                addUINode(key, x, y)
                counterWhiteKeys++
                previousWhiteKeyX = x
            }
        }
    }

    class GameFactory : EntityFactory {
        /**
         * Types of objects we are going to use in our game.
         */
        enum class EntityType {
            BACKGROUND_COLOR, BACKGROUND_IMAGE, LOGO
        }

        @Spawns("BACKGROUND_COLOR")
        fun spawnBackgroundColor(data: SpawnData): Entity {
            var rectangle = Rectangle(
                data.get<Int>("width").toDouble(),
                data.get<Int>("height").toDouble()
            )
            rectangle.fillProperty()
                .bindBidirectional(getop(be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.BACKGROUND_COLOR.name))
            return FXGL.entityBuilder(data)
                .type(EntityType.BACKGROUND_COLOR)
                .view(rectangle)
                .with(IrremovableComponent())
                .zIndex(-100)
                .build()
        }

        @Spawns("BACKGROUND_IMAGE")
        fun spawnBackgroundImage(data: SpawnData): Entity {
            var imageView = ImageView().apply {
                isVisible = false
                opacityProperty().bind(getdp(be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.BACKGROUND_IMAGE_TRANSPARENCY.name))
            }
            (getop<be.codewriter.melodymatrix.view.stage.piano.data.PianoBackgroundImage>(be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.BACKGROUND_IMAGE.name)).addListener { _, _, newValue ->
                if (newValue == be.codewriter.melodymatrix.view.stage.piano.data.PianoBackgroundImage.NONE) {
                    imageView.isVisible = false
                } else {
                    imageView.image = Image(newValue.file)
                    imageView.isVisible = true
                }
            }
            return FXGL.entityBuilder(data)
                .type(EntityType.BACKGROUND_IMAGE)
                .view(imageView)
                .with(IrremovableComponent())
                .zIndex(-99)
                .build()
        }

        @Spawns("LOGO")
        fun spawnLogo(data: SpawnData): Entity {
            var imageView = ImageView().apply {
                image = Image("logo/heavy-melodymatrix.png")
                isPreserveRatio = true
                opacityProperty().bind(getdp(be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.LOGO_TRANSPARENCY.name))
                fitWidthProperty().bind(getdp(be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.LOGO_WIDTH.name))
                visibleProperty().bind(getbp(be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.LOGO_VISIBLE.name))
                xProperty().bind(getdp(be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.LOGO_LEFT.name))
                yProperty().bind(getdp(be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.LOGO_TOP.name))
            }
            return FXGL.entityBuilder(data)
                .type(EntityType.BACKGROUND_IMAGE)
                .view(imageView)
                .with(IrremovableComponent())
                .zIndex(-98)
                .build()
        }
    }

    private var t = 4.0

    override fun onUpdate(tpf: Double) {
        t += tpf

        if (t > 12) {
            t = 4.0
        }
    }

    override fun initInput() {
        onKeyDown(KeyCode.F) {
            initFallingBlock(200.0)
        }
    }

    fun playNote(midiData: be.codewriter.melodymatrix.view.data.MidiData) {
        val keyView = keys[midiData.note] ?: return
        keyView.update(midiData.event == be.codewriter.melodymatrix.view.definition.MidiEvent.NOTE_ON)
        if (getb(be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.EXPLOSION_ENABLED.name) && midiData.event == be.codewriter.melodymatrix.view.definition.MidiEvent.NOTE_ON) {
            initParticles(keyView.position().x, keyView.position().y, midiData.velocity)
        }
        if (midiData.event == be.codewriter.melodymatrix.view.definition.MidiEvent.NOTE_ON) {
            //initFallingBlock(keyView.position().x)
        }
    }

    private fun initFallingBlock(x: Double) {
        val speed = 50.0
        val height = 50.0 // Needs to be calculated based on the duration of the note
        val block = entityBuilder()
            .at(x, 0.0 - height)
            .viewWithBBox(Rectangle(PIANO_WHITE_KEY_WIDTH, height, Color.RED))
            //.with(ProjectileComponent(Point2D(0.0, 1.0), speed).allowRotation(false))
            //.with(OffscreenCleanComponent())
            .buildAndAttach()

        animationBuilder()
            .duration(Duration.seconds(3.0))
            .onFinished {
                // called when the animation is finished
                // how to remove block from screen
                // block.removeFromWorld()
            }
            .translate(block)
            .to(Point2D(x, PIANO_HEIGHT - PIANO_WHITE_KEY_HEIGHT))
            .buildAndPlay()
    }

    private fun initParticles(xx: Double, yy: Double, keyVelocity: Int) {
        val emitter =
            ParticleEmitters.newExplosionEmitter(getdp(be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.EXPLOSION_RADIUS.name).intValue())
        emitter.maxEmissions = Int.MAX_VALUE
        emitter.blendMode = BlendMode.SRC_OVER
        emitter.numParticles =
            getdp(be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.EXPLOSION_NUMBER_OF_PARTICLES.name).intValue()
        // How quickly they are spawned 1 = every frame 0,5 = every second frame, make it bindable
        emitter.emissionRate = 0.86
        emitter.maxEmissions = 1
        emitter.setSize(
            1.0,
            getd(be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.EXPLOSION_PARTICLE_SIZE.name)
        )
        emitter.setScaleFunction { _ -> FXGLMath.randomPoint2D().multiply(0.01) }
        emitter.setExpireFunction { _ -> Duration.seconds(FXGLMath.random(0.25, 3.5)) } // make bindable for end time
        emitter.setAccelerationFunction { Point2D.ZERO }
        emitter.setVelocityFunction { _ ->
            FXGLMath.randomPoint2D().multiply(FXGLMath.random(1.0, 45.0)).multiply(0.0003)
        }
        emitter.setColor(geto<Color>(be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.EXPLOSION_COLOR_START.name))

        val particleSize =
            2 * getd(be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.EXPLOSION_PARTICLE_SIZE.name)

        // The animation of each particle of the explosion
        emitter.setControl { p ->
            val x = p.position.x
            val y = p.position.y

            val noiseValue = FXGLMath.noise2D(x * 0.008 * t, y * 0.002 * t)
            var angle = FXGLMath.toDegrees((noiseValue + 1) * Math.PI * 1.5)

            angle %= 360.0

            val v = Vec2.fromAngle(angle)
                .normalizeLocal()
                .mulLocal(FXGLMath.random(1.0, 25.0))
                .mulLocal(0.5)

            // Move the particles (a bit) left-right
            val vx = p.velocity.x * 0.4f + v.x * 0.2f
            p.velocity.x = vx * 0.8f + 0.2f // How wide does the particle move

            // Move the particles up, depending on how hard the key is pressed
            // Maps MIDI velocity between 1 and 127 (7 bits) to a range to define the upwards speed
            val vy = p.velocity.y * 0.8f + v.y * 0.2f
            val vyMult = FXGLMath.map(keyVelocity.toDouble(), 1.0, 127.0, 0.5, 1.5) * -1
            p.velocity.y = Math.abs(vy) * vyMult.toFloat()

            // Lighting
            g.fill =
                getop<Color>(be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.EXPLOSION_COLOR_END.name).value

            // Draw the particle using JavaFX canvas
            g.fillOval(
                (x - 2).toDouble(),
                (y - 2).toDouble(),
                particleSize,
                particleSize
            )

            // Add a tail
            if (getd(be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.EXPLOSION_TAIL_NUMBER_OF_ARTICLES.name).toInt() > 0) {
                for (i in 1..getd(be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.EXPLOSION_TAIL_NUMBER_OF_ARTICLES.name).toInt()) {
                    g.fillOval(
                        (x - 2).toDouble() + (particleSize / 2) - 1 + FXGLMath.random(-2.0, 2.0),
                        (y - 2).toDouble() + ((particleSize / 5.0) * (i * 2)),
                        particleSize / 5.0,
                        particleSize / 5.0
                    )
                }
            }
        }

        val comp = ParticleComponent(emitter)

        entityBuilder()
            .at(xx + 25 / 2.0, yy)
            .zIndex(100)
            .with(ExpireCleanComponent(Duration.seconds(2.0)))
            .with(comp)
            .buildAndAttach()
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(PianoGenerator::class.java.name)
        private lateinit var canvas: Canvas
        private lateinit var g: GraphicsContext
        val keys: MutableMap<be.codewriter.melodymatrix.view.definition.Note, be.codewriter.melodymatrix.view.stage.piano.component.PianoKey> =
            mutableMapOf()
        val PIANO_WIDTH = 800
        val PIANO_HEIGHT = 600
        val PIANO_WHITE_KEY_WIDTH = 13.56
        val PIANO_WHITE_KEY_HEIGHT = 120.0
        val PIANO_BLACK_KEY_WIDTH = 5.0
        val PIANO_BLACK_KEY_HEIGHT = 80.0
    }
}
