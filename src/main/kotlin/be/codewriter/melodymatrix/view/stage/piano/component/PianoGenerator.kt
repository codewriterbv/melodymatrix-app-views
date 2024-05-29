package be.codewriter.melodymatrix.view.stage.piano.component

import be.codewriter.melodymatrix.view.data.Note
import be.codewriter.melodymatrix.view.stage.piano.data.PianoBackgroundImage
import be.codewriter.melodymatrix.view.stage.piano.data.PianoSettingsEffect
import com.almasb.fxgl.app.GameApplication
import com.almasb.fxgl.app.GameSettings
import com.almasb.fxgl.core.math.FXGLMath
import com.almasb.fxgl.core.math.Vec2
import com.almasb.fxgl.dsl.FXGL
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
        vars.put(PianoProperty.PIANO_KEY_COLOR.name, Color.WHITE)
        vars.put(PianoProperty.PIANO_KEY_ACTIVE_COLOR.name, Color.YELLOW)
        vars.put(PianoProperty.PIANO_KEY_NAME_VISIBLE.name, false)
        vars.put(PianoProperty.BACKGROUND_COLOR.name, Color.DARKGRAY)
        vars.put(PianoProperty.BACKGROUND_IMAGE.name, PianoBackgroundImage.NONE)
        vars.put(PianoProperty.BACKGROUND_IMAGE_TRANSPARENCY.name, 1.0)
        vars.put(PianoProperty.LOGO_TRANSPARENCY.name, 1.0)
        vars.put(PianoProperty.LOGO_WIDTH.name, 800.0)
        vars.put(PianoProperty.LOGO_LEFT.name, 0.0)
        vars.put(PianoProperty.LOGO_TOP.name, 0.0)
    }

    enum class PianoProperty {
        PIANO_KEY_COLOR,
        PIANO_KEY_ACTIVE_COLOR,
        PIANO_KEY_NAME_VISIBLE,
        BACKGROUND_COLOR,
        BACKGROUND_IMAGE,
        BACKGROUND_IMAGE_TRANSPARENCY,
        LOGO_TRANSPARENCY,
        LOGO_WIDTH,
        LOGO_LEFT,
        LOGO_TOP
    }

    enum class EntityType {
        BACKGROUND, CENTER, DUKE, CLOUD, BULLET
    }

    override fun initGame() {
        // Bindings can only be created when FXGL has started, so we have a callback here
        pianoConfiguratorBackground.createBindings()
        pianoConfiguratorKey.createBindings()

        getGameScene().setCursor(Cursor.DEFAULT)

        FXGL.getGameWorld().addEntityFactory(this.gameFactory)
        FXGL.spawn(
            "backgroundColor", SpawnData(0.0, 0.0)
                .put("width", FXGL.getAppWidth())
                .put("height", FXGL.getAppHeight())
        )
        FXGL.spawn(
            "backgroundImage", SpawnData(0.0, 0.0)
        )
        FXGL.spawn(
            "logo", SpawnData(0.0, 0.0)
        )

        entityBuilder()
            .view(canvas)
            .with(object : Component() {
                override fun onUpdate(tpf: Double) {
                    g.clearRect(0.0, 0.0, PIANO_WIDTH.toDouble(), PIANO_HEIGHT.toDouble())
                }
            })
            .buildAndAttach()

        var counterNonSharp = 0

        Note.pianoKeys().forEach { note ->
            // logger.info("Adding piano key for note {}", note.name)

            if (note.mainNote.isSharp) {
                /*val x = counterSharp + (counterSharp * widthKeyBlack)
                val y = PIANO_HEIGHT - PIANO_WHITE_KEY_HEIGHT
                val key = PianoKeyBlack(note, x, y, widthKeyBlack, PIANO_BLACK_KEY_HEIGHT)

                keys[note] = key
                addUINode(key, x, y)

                counterSharp++*/
            } else {
                val x = counterNonSharp * PIANO_WHITE_KEY_WIDTH
                val y = PIANO_HEIGHT - PIANO_WHITE_KEY_HEIGHT
                val key = PianoKeyWhite(note, x, y)
                keys[note] = key
                addUINode(key, x, y)
                counterNonSharp++
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

        @Spawns("backgroundColor")
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

        @Spawns("backgroundImage")
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

        @Spawns("logo")
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

    fun playNote(note: Note, noteOn: Boolean, pianoSettingsEffect: PianoSettingsEffect) {
        val keyView = keys[note] ?: return
        keyView.update(noteOn)
        if (pianoSettingsEffect.showEffect) {
            initParticles(keyView.position().x, keyView.position().y, pianoSettingsEffect)
        }
    }

    private fun initParticles(xx: Double, yy: Double, pianoSettingsEffect: PianoSettingsEffect) {
        logger.debug("Initializing particles on {}/{}", xx, yy)
        val emitter = ParticleEmitters.newExplosionEmitter(pianoSettingsEffect.explosionRadius)
        emitter.maxEmissions = Int.MAX_VALUE
        emitter.blendMode = pianoSettingsEffect.blendMode
        emitter.numParticles = pianoSettingsEffect.numParticles
        emitter.emissionRate = 0.86
        emitter.maxEmissions = 1
        emitter.setSize(1.0, pianoSettingsEffect.particleSize)
        emitter.setScaleFunction { _ -> FXGLMath.randomPoint2D().multiply(0.01) }
        emitter.setExpireFunction { _ -> Duration.seconds(FXGLMath.random(0.25, 3.5)) }
        emitter.setAccelerationFunction { Point2D.ZERO }
        emitter.setVelocityFunction { _ -> Point2D.ZERO }
        emitter.setVelocityFunction { _ ->
            FXGLMath.randomPoint2D().multiply(FXGLMath.random(1.0, 45.0)).multiply(0.0003)
        }

        emitter.setColor(pianoSettingsEffect.colorStart)

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
            g.fill = pianoSettingsEffect.colorEnd

            var layer = 1
            while (layer <= 2) {
                g.globalAlpha = 2.0 / layer * p.life
                g.fillOval(
                    (x - layer).toDouble(),
                    (y - layer).toDouble(),
                    (layer * pianoSettingsEffect.particleSize).toDouble(),
                    (layer * pianoSettingsEffect.particleSize).toDouble()
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
        val PIANO_BLACK_KEY_HEIGHT = 80.0
    }
}
