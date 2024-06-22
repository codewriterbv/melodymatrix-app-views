package be.codewriter.melodymatrix.view.stage.piano.component

import be.codewriter.melodymatrix.view.data.MidiData
import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.stage.piano.data.PianoBackgroundImage
import com.almasb.fxgl.app.GameApplication
import com.almasb.fxgl.app.GameSettings
import com.almasb.fxgl.core.math.FXGLMath
import com.almasb.fxgl.core.math.Vec2
import com.almasb.fxgl.dsl.FXGL
import com.almasb.fxgl.dsl.FXGL.Companion.getbp
import com.almasb.fxgl.dsl.FXGL.Companion.getdp
import com.almasb.fxgl.dsl.FXGL.Companion.getop
import com.almasb.fxgl.dsl.addUINode
import com.almasb.fxgl.dsl.components.ExpireCleanComponent
import com.almasb.fxgl.dsl.entityBuilder
import com.almasb.fxgl.dsl.getGameScene
import com.almasb.fxgl.entity.Entity
import com.almasb.fxgl.entity.EntityFactory
import com.almasb.fxgl.entity.SpawnData
import com.almasb.fxgl.entity.Spawns
import com.almasb.fxgl.entity.component.Component
import com.almasb.fxgl.entity.components.IrremovableComponent
import com.almasb.fxgl.particle.ParticleComponent
import com.almasb.fxgl.particle.ParticleEmitters
import javafx.beans.property.ObjectProperty
import javafx.geometry.Point2D
import javafx.scene.Cursor
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.effect.BlendMode
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.util.Duration
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.collections.set

class PianoGenerator(
    val pianoConfiguratorBackground: PianoConfiguratorBackground,
    val pianoConfiguratorEffect: PianoConfiguratorEffect,
    val pianoConfiguratorKey: PianoConfiguratorKey
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
        vars.put(PianoProperty.PIANO_WHITE_KEY_COLOR.name, Color.WHITE)
        vars.put(PianoProperty.PIANO_WHITE_KEY_ACTIVE_COLOR.name, Color.ORANGE)
        vars.put(PianoProperty.PIANO_WHITE_KEY_NAME_VISIBLE.name, false)
        vars.put(PianoProperty.PIANO_BLACK_KEY_COLOR.name, Color.BLACK)
        vars.put(PianoProperty.PIANO_BLACK_KEY_ACTIVE_COLOR.name, Color.CYAN)

        vars.put(PianoProperty.BACKGROUND_COLOR.name, Color.DARKGRAY)
        vars.put(PianoProperty.BACKGROUND_IMAGE.name, PianoBackgroundImage.NONE)
        vars.put(PianoProperty.BACKGROUND_IMAGE_TRANSPARENCY.name, 1.0)

        vars.put(PianoProperty.LOGO_TRANSPARENCY.name, 1.0)
        vars.put(PianoProperty.LOGO_WIDTH.name, 800.0)
        vars.put(PianoProperty.LOGO_LEFT.name, 0.0)
        vars.put(PianoProperty.LOGO_TOP.name, 0.0)

        vars.put(PianoProperty.EXPLOSION_ENABLED.name, true)
        vars.put(PianoProperty.EXPLOSION_COLOR_END.name, Color.YELLOW)
        vars.put(PianoProperty.EXPLOSION_COLOR_START.name, Color.RED)
        vars.put(PianoProperty.EXPLOSION_RADIUS.name, 100.0)
        vars.put(PianoProperty.EXPLOSION_BLENDMODE.name, BlendMode.HARD_LIGHT)
        vars.put(PianoProperty.EXPLOSION_NUMBER_OF_PARTICLES.name, 5.0)
        vars.put(PianoProperty.EXPLOSION_PARTICLE_SIZE.name, 2.0)
    }

    enum class PianoProperty {
        BACKGROUND_COLOR,
        BACKGROUND_IMAGE,
        BACKGROUND_IMAGE_TRANSPARENCY,
        EXPLOSION_ENABLED,
        EXPLOSION_COLOR_END,
        EXPLOSION_COLOR_START,
        EXPLOSION_RADIUS,
        EXPLOSION_BLENDMODE,
        EXPLOSION_NUMBER_OF_PARTICLES,
        EXPLOSION_PARTICLE_SIZE,
        LOGO_WIDTH,
        LOGO_LEFT,
        LOGO_TOP,
        PIANO_WHITE_KEY_COLOR,
        PIANO_WHITE_KEY_ACTIVE_COLOR,
        PIANO_WHITE_KEY_NAME_VISIBLE,
        PIANO_BLACK_KEY_COLOR,
        PIANO_BLACK_KEY_ACTIVE_COLOR,
        LOGO_TRANSPARENCY
    }

    enum class EntityType {
        BACKGROUND_COLOR, BACKGROUND_IMAGE, LOGO
    }

    override fun initGame() {
        // Bindings can only be created when FXGL has started, so we have a callback here
        pianoConfiguratorBackground.createBindings()
        pianoConfiguratorEffect.createBindings()
        pianoConfiguratorKey.createBindings()

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
            .with(object : Component() {
                override fun onUpdate(tpf: Double) {
                    g.clearRect(0.0, 0.0, PIANO_WIDTH.toDouble(), PIANO_HEIGHT.toDouble())
                }
            })
            .buildAndAttach()

        var counterWhiteKeys = 0
        var previousWhiteKeyX = 0.0

        Note.pianoKeys().forEach { note ->
            // logger.info("Adding piano key for note {}", note.name)
            if (note.mainNote.isSharp) {
                val x = previousWhiteKeyX + PIANO_WHITE_KEY_WIDTH - (PIANO_BLACK_KEY_WIDTH / 2)
                val y = PIANO_HEIGHT - PIANO_WHITE_KEY_HEIGHT
                val key = PianoKeyBlack(note, x, y)
                keys[note] = key
                addUINode(key, x, y)
            } else {
                val x = counterWhiteKeys * PIANO_WHITE_KEY_WIDTH
                val y = PIANO_HEIGHT - PIANO_WHITE_KEY_HEIGHT
                val key = PianoKeyWhite(note, x, y)
                keys[note] = key
                addUINode(key, x, y)
                counterWhiteKeys++
                previousWhiteKeyX = x
            }
        }

        entityBuilder()
            .at(-50.0, PIANO_HEIGHT - 170.0)
            .zIndex(1000)
            .buildAndAttach()
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
                data.get<Int>("height").toDouble(),
                Color.YELLOWGREEN
            )
            rectangle.fillProperty().bindBidirectional(getop(PianoProperty.BACKGROUND_COLOR.name))
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
                opacityProperty().bind(getdp(PianoProperty.BACKGROUND_IMAGE_TRANSPARENCY.name))
            }
            (getop<ObjectProperty<*>>(PianoProperty.BACKGROUND_IMAGE.name) as ObjectProperty<PianoBackgroundImage>).addListener { _, _, newValue ->
                if (newValue == PianoBackgroundImage.NONE) {
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
                opacityProperty().bind(getdp(PianoProperty.LOGO_TRANSPARENCY.name))
                fitWidthProperty().bind(getdp(PianoProperty.LOGO_WIDTH.name))
                xProperty().bind(getdp(PianoProperty.LOGO_LEFT.name))
                yProperty().bind(getdp(PianoProperty.LOGO_TOP.name))
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

    fun playNote(midiData: MidiData) {
        val keyView = keys[midiData.note] ?: return
        keyView.update(midiData.event == MidiEvent.NOTE_ON)
        if (getbp(PianoProperty.EXPLOSION_ENABLED.name).value) {
            initParticles(keyView.position().x, keyView.position().y)
        }
    }

    private fun initParticles(xx: Double, yy: Double) {
        logger.debug("Initializing particles on {}/{}", xx, yy)
        val emitter = ParticleEmitters.newExplosionEmitter(getdp(PianoProperty.EXPLOSION_RADIUS.name).intValue())
        emitter.maxEmissions = Int.MAX_VALUE
        emitter.blendMode = getop<BlendMode>(PianoProperty.EXPLOSION_BLENDMODE.name).value
        emitter.numParticles = getdp(PianoProperty.EXPLOSION_NUMBER_OF_PARTICLES.name).intValue()
        emitter.emissionRate = 0.86
        emitter.maxEmissions = 1
        emitter.setSize(1.0, getdp(PianoProperty.EXPLOSION_PARTICLE_SIZE.name).value)
        emitter.setScaleFunction { _ -> FXGLMath.randomPoint2D().multiply(0.01) }
        emitter.setExpireFunction { _ -> Duration.seconds(FXGLMath.random(0.25, 3.5)) }
        emitter.setAccelerationFunction { Point2D.ZERO }
        emitter.setVelocityFunction { _ -> Point2D.ZERO }
        emitter.setVelocityFunction { _ ->
            FXGLMath.randomPoint2D().multiply(FXGLMath.random(1.0, 45.0)).multiply(0.0003)
        }

        emitter.setColor(getop<Color>(PianoProperty.EXPLOSION_COLOR_START.name).value)

        emitter.isAllowParticleRotation = true

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

            val vx = p.velocity.x * 0.8f + v.x * 0.2f
            val vy = p.velocity.y * 0.8f + v.y * 0.2f

            p.velocity.x = vx * 0.8f + 0.2f
            p.velocity.y = Math.abs(vy) * -1.1f

            // Lighting
            g.fill = getop<Color>(PianoProperty.EXPLOSION_COLOR_END.name).value

            var layer = 1
            while (layer <= 2) {
                g.globalAlpha = 2.0 / layer * p.life
                g.fillOval(
                    (x - layer).toDouble(),
                    (y - layer).toDouble(),
                    (layer * getdp(PianoProperty.EXPLOSION_PARTICLE_SIZE.name).value),
                    (layer * getdp(PianoProperty.EXPLOSION_PARTICLE_SIZE.name).value)
                )
                layer += 1
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
        val keys: MutableMap<Note, PianoKey> = mutableMapOf()
        val PIANO_WIDTH = 800
        val PIANO_HEIGHT = 600
        val PIANO_WHITE_KEY_WIDTH = 13.56
        val PIANO_WHITE_KEY_HEIGHT = 120.0
        val PIANO_BLACK_KEY_WIDTH = 5.0
        val PIANO_BLACK_KEY_HEIGHT = 80.0
    }
}
