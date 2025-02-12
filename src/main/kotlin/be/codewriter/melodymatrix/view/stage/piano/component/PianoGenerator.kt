package be.codewriter.melodymatrix.view.stage.piano.component

import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.stage.piano.data.PianoBackgroundImage
import com.almasb.fxgl.animation.Interpolators
import com.almasb.fxgl.app.GameApplication
import com.almasb.fxgl.app.GameSettings
import com.almasb.fxgl.core.math.FXGLMath
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
import kotlin.collections.set
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class PianoGenerator(
    val configuratorBackground: ConfiguratorBackground,
    val configuratorEffectParticle: ConfiguratorEffectParticle,
    val configuratorEffectAboveKey: ConfiguratorEffectAboveKey,
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
        configuratorEffectParticle.createBindings()
        configuratorEffectAboveKey.createBindings()
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

        initAnimationAboveKeys()
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

    private lateinit var fireEmitter: ParticleEmitter
    private var fireEntity: Entity? = null
    private var targetEmissionRate = 0.05 // 🔥 Intensity of fire
    private var currentEmissionRate = 0.0 // 🔥 Start with no emission
    private lateinit var startColor: Color
    private lateinit var endColor: Color

    private fun initAnimationAboveKeys() {
        startColor = geto<Color>(PianoProperty.ABOVE_KEY_COLOR_START.name)
        endColor = geto<Color>(PianoProperty.ABOVE_KEY_COLOR_END.name)

        fireEmitter = ParticleEmitters.newFireEmitter() // 🔥 Fire particle system
        configureFireEmitter() // 🔥 Set properties

        fireEntity = entityBuilder()
            .at(
                -100.0,
                getAppHeight() - PIANO_WHITE_KEY_HEIGHT - 5.0 + FXGLMath.random(-10.0, 10.0)
            ) // 🔥 Random slight height variation
            .with(ParticleComponent(fireEmitter))
            .zIndex(1000)
            .buildAndAttach()

        // 🔥 Start fire effect with a smooth fade-in
        fadeInFire()

        // 🔥 Ensure updateFireState is called every 1 second to adjust fire properties
        run({ updateFireState() }, Duration.seconds(1.0))
    }

    // 🔥 Configure fire emitter with random heights
    private fun configureFireEmitter() {
        fireEmitter.setSize(getAppWidth().toDouble() + 100.0, 80.0) // 🔥 Wider but controlled height
        fireEmitter.numParticles = 15 // 🔥 Fewer particles for a steady effect
        fireEmitter.emissionRate = 0.005 // 🔥 Very slow particle emission
        fireEmitter.setExpireFunction { Duration.seconds(6.0) } // 🔥 Longer lifespan for static feel
        fireEmitter.blendMode = BlendMode.ADD // 🔥 Glow effect

        fireEmitter.startColor =
            Color.color(startColor.red, startColor.green, startColor.blue, 0.2) // 🔥 Orange soft glow
        fireEmitter.endColor = Color.color(endColor.red, endColor.green, endColor.blue, 0.1) // 🔥 Deep orange fade-out

        fireEmitter.setVelocityFunction {
            Point2D(
                FXGLMath.random(-0.002, 0.002), // 🔥 Very slight horizontal flicker
                -FXGLMath.randomDouble() * FXGLMath.random(0.002, 0.008) // 🔥 Super slow upward movement
            )
        }
    }

    // 🔥 Smooth fade-in effect for fire emission
    private fun fadeInFire() {
        currentEmissionRate = 0.0
        run({
            if (currentEmissionRate < targetEmissionRate) {
                currentEmissionRate += 0.01 // 🔥 Increase gradually
                fireEmitter.emissionRate = currentEmissionRate
            }
        }, Duration.seconds(0.1), 5) // 🔥 Updates every 0.1s (~0.5s fade-in)
    }

    // 🔥 Smoothly update fire properties
    private fun updateFireState() {
        if (!this::fireEmitter.isInitialized) return

        val isEnabled = getb(PianoProperty.ABOVE_KEY_ENABLED.name)

        if (!isEnabled) {
            fireEmitter.emissionRate = 0.0 // 🔴 Stop new flames
            fireEntity?.opacity = 0.0 // 🔴 Let remaining flames fade naturally
            return
        }

        // 🔥 Restart with fade-in when re-enabled
        if (fireEmitter.emissionRate == 0.0) {
            fadeInFire()
        }

        fireEntity?.opacity = 1.0 // 🔥 Ensure flames are fully visible

        startColor = geto<Color>(PianoProperty.ABOVE_KEY_COLOR_START.name)
        endColor = geto<Color>(PianoProperty.ABOVE_KEY_COLOR_END.name)
        fireEmitter.startColor =
            Color.color(startColor.red, startColor.green, startColor.blue, 0.2) // 🔥 Orange soft glow
        fireEmitter.endColor = Color.color(endColor.red, endColor.green, endColor.blue, 0.1) // 🔥 Deep orange fade-out
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
        // if true, play fireworks animation
        if (getb(PianoProperty.EXPLOSION_TYPE.name)) {
            spawnFireworksStarterAnim(Point2D(keyPosX, keyPosY), keyVelocity)
        } else {
            spawnSplashAnim(Point2D(keyPosX, keyPosY), keyVelocity)
        }
    }

    private fun spawnFireworksStarterAnim(pos: Point2D, keyVelocity: Int) {
        val color = if (getb(PianoProperty.EXPLOSION_RANDOM_COLOR.name)) {
            FXGLMath.randomColor().brighter().brighter()
        } else {
            geto<Color>(PianoProperty.EXPLOSION_COLOR.name)
        }

        val emitter = ParticleEmitters.newFireEmitter().apply {
            numParticles = 1
            emissionRate = 0.5
            setSize(0.2, 1.0)
            setExpireFunction { _ -> Duration.seconds(0.5 + keyVelocity * 0.005) }
            blendMode = BlendMode.SRC_OVER
            setColor(color)
            isAllowParticleRotation = true
        }
        val speed = keyVelocity * 1.5

        val e = entityBuilder()
            .at(pos)
            .view(Circle(1.0, 1.0, 1.0, Color.WHITE))
            .with(ProjectileComponent(Point2D(random(-0.8, 0.8), -1.0), speed).allowRotation(true))
            .with(ParticleComponent(emitter))
            .buildAndAttach()

        runOnce({
            spawnFireworksScatterAnim(e.position, keyVelocity)
            e.removeFromWorld()
        }, Duration.seconds(0.5))
    }

    private fun spawnFireworksScatterAnim(pos: Point2D, keyVelocity: Int) {
        val color = if (getb(PianoProperty.EXPLOSION_RANDOM_COLOR.name)) {
            FXGLMath.randomColor().brighter().brighter()
        } else {
            geto<Color>(PianoProperty.EXPLOSION_COLOR.name)
        }

        val emitter =
            ParticleEmitters.newExplosionEmitter((getd(PianoProperty.EXPLOSION_RADIUS.name) * (keyVelocity * 0.02)).toInt())
                .apply {
                    setExpireFunction { _ -> Duration.seconds(random(2.0, 4.5)) }
                    interpolator = Interpolators.EXPONENTIAL.EASE_OUT()
                    setAccelerationFunction { Point2D(random(0.5, 0.95), random(1.0, 10.5)) }
                    blendMode = BlendMode.ADD
                    setSize(
                        getd(PianoProperty.EXPLOSION_PARTICLE_SIZE.name) / 2,
                        getd(PianoProperty.EXPLOSION_PARTICLE_SIZE.name)
                    )
                    numParticles = getd(PianoProperty.EXPLOSION_NUMBER_OF_PARTICLES.name).toInt()
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

    private fun spawnSplashAnim(pos: Point2D, keyVelocity: Int) {
        val color = if (getb(PianoProperty.EXPLOSION_RANDOM_COLOR.name)) {
            FXGLMath.randomColor().brighter().brighter()
        } else {
            geto<Color>(PianoProperty.EXPLOSION_COLOR.name)
        }

        val emitter =
            ParticleEmitters.newExplosionEmitter((getd(PianoProperty.EXPLOSION_RADIUS.name) * (keyVelocity * 0.04)).toInt())
                .apply {
                    numParticles = getd(PianoProperty.EXPLOSION_NUMBER_OF_PARTICLES.name).toInt()
                    setSize(
                        getd(PianoProperty.EXPLOSION_PARTICLE_SIZE.name) / 2,
                        getd(PianoProperty.EXPLOSION_PARTICLE_SIZE.name)
                    ) // Slightly larger particles for slow movement
                    setExpireFunction { _ -> Duration.seconds(keyVelocity * 0.07) } // random(4.5, 6.0)
                    interpolator = Interpolators.CIRCULAR.EASE_OUT()
                    blendMode = BlendMode.ADD
                    isAllowParticleRotation = false
                    setColor(color)

                    // Emit randomly upwards with very slow movement
                    setAccelerationFunction {
                        val spread = 0.6 // Keep a natural spread
                        val angle = atan2(-1.0, 0.0) // Upward direction
                        val randomizedAngle = angle + random(-spread, spread) // Add randomness

                        val speed = keyVelocity * 0.15 // random(5.0, 15.0) -  Very slow movement
                        Point2D(
                            cos(randomizedAngle) * speed,
                            sin(randomizedAngle) * speed * 1.2 // 2.0 - Slight boost for height
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
