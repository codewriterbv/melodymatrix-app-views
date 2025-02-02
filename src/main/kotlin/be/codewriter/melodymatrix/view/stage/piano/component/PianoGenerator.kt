package be.codewriter.melodymatrix.view.stage.piano.component

import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.stage.piano.data.PianoBackgroundImage
import com.almasb.fxgl.animation.Interpolators
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
import com.almasb.fxgl.dsl.components.ProjectileComponent
import com.almasb.fxgl.entity.Entity
import com.almasb.fxgl.entity.EntityFactory
import com.almasb.fxgl.entity.SpawnData
import com.almasb.fxgl.entity.Spawns
import com.almasb.fxgl.entity.components.IrremovableComponent
import com.almasb.fxgl.particle.ParticleComponent
import com.almasb.fxgl.particle.ParticleEmitter
import com.almasb.fxgl.particle.ParticleEmitters
import com.almasb.fxgl.physics.PhysicsComponent
import javafx.geometry.Point2D
import javafx.scene.Cursor
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.effect.BlendMode
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle
import javafx.util.Duration
import java.util.function.Function
import java.util.function.Supplier
import kotlin.collections.set
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class PianoGenerator(
    val configuratorBackground: ConfiguratorBackground,
    val configuratorEffect: ConfiguratorEffect,
    val configuratorKey: ConfiguratorKey
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
        DefaultValues.setDefaults(vars)
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

        Note.pianoKeys().forEach { note ->
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
                .bindBidirectional(getop(PianoProperty.BACKGROUND_COLOR.name))
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
            (getop<PianoBackgroundImage>(PianoProperty.BACKGROUND_IMAGE.name)).addListener { _, _, newValue ->
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
                visibleProperty().bind(getbp(PianoProperty.LOGO_VISIBLE.name))
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

    override fun initInput() {
        onKeyDown(KeyCode.F) {
            initFallingBlock(200.0)
        }
    }

    fun playNote(midiData: be.codewriter.melodymatrix.view.data.MidiData) {
        val keyView = keys[midiData.note] ?: return
        keyView.update(midiData.event == MidiEvent.NOTE_ON)
        if (getb(PianoProperty.EXPLOSION_ENABLED.name) && midiData.event == MidiEvent.NOTE_ON) {
            initParticles(keyView.position().x, keyView.position().y, midiData.velocity)
        }
        if (midiData.event == MidiEvent.NOTE_ON) {
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

    private fun initParticles(keyPosX: Double, keyPosY: Double, keyVelocity: Int) {
//        if (getb(PianoProperty.EXPLOSION_TYPE.name)){
//            spawnFireworksStarterAnim(Point2D(keyPosX, keyPosY))
//        }else{
//            spawnSplashAnim(Point2D(keyPosX, keyPosY))
//        }
        spawnSplashAnim(Point2D(keyPosX, keyPosY))
    }

    private fun spawnFireworksStarterAnim(pos: Point2D) {
        val emitter = ParticleEmitters.newFireEmitter().apply {
            numParticles = 1
            emissionRate = 0.5
            setSize(0.2, 1.0)
            setExpireFunction { _ -> Duration.seconds(random(0.5, 2.0)) }
            blendMode = BlendMode.SRC_OVER
            setColor(Color.YELLOW)
            isAllowParticleRotation = true
        }

        val e = entityBuilder()
            .at(pos)
            .view(Circle(1.0, 1.0, 1.0, Color.WHITE))
            .with(ProjectileComponent(Point2D(random(-0.8, 0.8), -1.0), 50.0).allowRotation(true))
            .with(ParticleComponent(emitter))
            .buildAndAttach()

        runOnce({
            spawnFireworksScatterAnim(e.position)
            e.removeFromWorld()
        }, Duration.seconds(random(1.0, 3.5)))
    }

    private fun spawnFireworksScatterAnim(pos: Point2D) {
        val color = FXGLMath.randomColor().brighter().brighter()

        val emitter = ParticleEmitters.newExplosionEmitter(random(30, 80)).apply {
            setExpireFunction { _ -> Duration.seconds(random(2.0, 4.5)) }
            interpolator = Interpolators.EXPONENTIAL.EASE_OUT()
            setAccelerationFunction { Point2D(random(0.5, 0.95), random(1.0, 10.5)) }
            blendMode = BlendMode.ADD
            setSize(0.3, 0.8)
            isAllowParticleRotation = false
            setColor(color)
        }

        entityBuilder()
            .at(pos)
            .with(ParticleComponent(emitter))
            .with(ExpireCleanComponent(Duration.seconds(5.0)).animateOpacity())
            .zIndex(100)
            .buildAndAttach()
    }

    private fun spawnSplashAnim(pos: Point2D) {
        val color = FXGLMath.randomColor().brighter().brighter()

        val emitter = ParticleEmitters.newExplosionEmitter(3).apply {
            numParticles = 150
            setExpireFunction { _ -> Duration.seconds(random(4.5, 6.0)) } // Longer lifespan
            interpolator = Interpolators.CIRCULAR.EASE_OUT()
            blendMode = BlendMode.ADD
            setSize(0.5, 1.2) // Slightly larger particles for slow movement
            isAllowParticleRotation = false
            setColor(color)

            // Emit randomly upwards with very slow movement
            setAccelerationFunction {
                val spread = 0.6 // Keep a natural spread
                val angle = atan2(-1.0, 0.0) // Upward direction
                val randomizedAngle = angle + random(-spread, spread) // Add randomness

                val speed = random(5.0, 15.0) // Very slow movement
                Point2D(
                    cos(randomizedAngle) * speed,
                    sin(randomizedAngle) * speed * 2.0 // Slight boost for height
                )
            }
        }

        entityBuilder()
            .at(Point2D(pos.x, pos.y - 2))
            .with(ParticleComponent(emitter))
            .with(ExpireCleanComponent(Duration.seconds(7.0)).animateOpacity()) // Extended lifespan
            .zIndex(100)
            .buildAndAttach()
    }



    private fun fireworksAnim(keyPosX: Double, keyPosY: Double, keyVelocity: Int){
        val emitter = ParticleEmitters.newExplosionEmitter(1).apply {
            numParticles = 50
            emissionRate = 0.001
            setSize(0.1, 0.5)
            blendMode = BlendMode.ADD
            setSpawnPointFunction(Function<Int, Point2D> { i: Int ->
                val vector = Point2D(cos(i.toDouble()), sin(i.toDouble()))
                (Point2D(0.0, 0.0)).add(vector.multiply(25.0))
            })
            setVelocityFunction { _ -> FXGLMath.randomPoint2D().multiply(FXGLMath.random(0.1, 10.0)).multiply(0.0003) }
            setExpireFunction { _ -> Duration.seconds(FXGLMath.random(3.0, 5.0)) }
            setColor(geto<Color>(PianoProperty.EXPLOSION_COLOR_START.name))
        }

        val particleSize = 0.1 //2 * getd(PianoProperty.EXPLOSION_PARTICLE_SIZE.name)

        // The animation of each particle of the explosion
        emitter.setControl { p ->
            val x = p.position.x
            val y = p.position.y

            val noiseValue = FXGLMath.noise2D(x * 0.008 * t, y * 0.002 * t)
            var angle = FXGLMath.toDegrees(40.0)
//            var angle = FXGLMath.toDegrees((noiseValue + 1) * Math.PI * 1.5)

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
            val vyMult = FXGLMath.map(keyVelocity.toDouble(), 1.0, 127.0, 0.5, 1.0) * -1
            p.velocity.y = Math.abs(vy) * vyMult.toFloat()

            // Lighting
            g.fill =
                getop<Color>(PianoProperty.EXPLOSION_COLOR_END.name).value

            // Draw the particle using JavaFX canvas
            g.fillOval(
                (x - 2).toDouble(),
                (y - 2).toDouble(),
                particleSize,
                particleSize
            )

            // Add a tail
            if (getd(PianoProperty.EXPLOSION_TAIL_NUMBER_OF_ARTICLES.name).toInt() > 0) {
                for (i in 1..getd(PianoProperty.EXPLOSION_TAIL_NUMBER_OF_ARTICLES.name).toInt()) {
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
            .at(keyPosX, keyPosY - 20)
            .zIndex(100)
            .with(ExpireCleanComponent(Duration.seconds(2.0)))
            .with(comp)
            .buildAndAttach()
    }

    companion object {
        private lateinit var canvas: Canvas
        private lateinit var g: GraphicsContext
        val keys: MutableMap<Note, PianoKey> =
            mutableMapOf()
        const val PIANO_WIDTH = 800
        const val PIANO_HEIGHT = 600
        const val PIANO_WHITE_KEY_WIDTH = 15.37
        const val PIANO_WHITE_KEY_HEIGHT = 120.0
        const val PIANO_BLACK_KEY_WIDTH = 5.0
        const val PIANO_BLACK_KEY_HEIGHT = 80.0
    }
}
