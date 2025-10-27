package be.codewriter.melodymatrix.view.stage.piano.scene

import be.codewriter.melodymatrix.view.stage.piano.PianoStage.Companion.PIANO_BACKGROUND_HEIGHT
import be.codewriter.melodymatrix.view.stage.piano.PianoStage.Companion.PIANO_WIDTH
import be.codewriter.melodymatrix.view.stage.piano.animation.AnimationCalculator
import be.codewriter.melodymatrix.view.stage.piano.animation.AnimationState
import be.codewriter.melodymatrix.view.stage.piano.animation.ParticleEmitter
import be.codewriter.melodymatrix.view.stage.piano.animation.ParticleEmitters
import be.codewriter.melodymatrix.view.stage.piano.data.PianoBackgroundImage
import be.codewriter.melodymatrix.view.stage.piano.data.PianoConfiguration
import javafx.animation.AnimationTimer
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.beans.value.WritableValue
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.util.Duration
import kotlin.random.Random

class PianoScene(val config: PianoConfiguration) : Canvas() {

    // Animation calculator with virtual thread
    private var animationCalculator: AnimationCalculator? = null
    private var ctx: GraphicsContext
    private val animationTimer: AnimationTimer

    @Volatile
    private var latestAnimationState: AnimationState? = null
    private var lastFrameTime = 0L

    private var currentBackgroundImage = PianoBackgroundImage.NONE
    private var backgroundImage: Image? = null

    private var fireEmitter: ParticleEmitter? = null
    private var fireUpdateTimer: Timeline? = null
    private var fireStateUpdateTimer: Timeline? = null

    private var logoImage = Image(
        "logo/heavy-melodymatrix.png",
        796.0,
        164.0,
        false,
        true
    )

    init {
        width = PIANO_WIDTH
        height = PIANO_BACKGROUND_HEIGHT

        ctx = graphicsContext2D

        // Start animation calculator
        animationCalculator = AnimationCalculator { state ->
            latestAnimationState = state
        }
        animationCalculator?.start()

        // Create animation timer for 60 FPS updates
        animationTimer = object : AnimationTimer() {
            override fun handle(now: Long) {
                val deltaTime = if (lastFrameTime == 0L) {
                    0.016 // ~60 FPS
                } else {
                    (now - lastFrameTime) / 1_000_000_000.0
                }
                lastFrameTime = now

                update(deltaTime)
            }
        }
        animationTimer.start()
    }

    fun stop() {
        animationCalculator?.stop()
        animationTimer.stop()
        fireUpdateTimer?.stop()
        fireStateUpdateTimer?.stop()
    }

    private fun update(deltaTime: Double) {
        // Read latest calculated state
        latestAnimationState ?: return

        // Apply state to visual elements
        ctx.clearRect(0.0, 0.0, width, height)

        addBackgroundImage()
        addLogoImage()
        addKeyEffect(deltaTime)

    }

    private fun addKeyEffect(deltaTime: Double) {
        if (config.aboveKeyEnabled.value) {
            // Initialize fire emitter if not already created
            if (fireEmitter == null) {
                fireEmitter = ParticleEmitters.newFireEmitter()
                configureFireEmitter()

                // Position the emitter
                val randomYOffset = Random.nextDouble(-10.0, 10.0)
                fireEmitter?.x = width / 2.0 // Center horizontally
                fireEmitter?.y = height - 50.0 + randomYOffset

                // Start fire effect with a smooth fade-in
                fadeInFire()

                // Ensure updateFireState is called every 1 second to adjust fire properties
                startFireStateUpdates()
            }

            // Update particles
            fireEmitter?.update(deltaTime)

            // Render particles
            renderFireParticles()
        } else {
            // Clear fire emitter when disabled
            if (fireEmitter != null) {
                fireEmitter = null
                fireUpdateTimer?.stop()
                fireStateUpdateTimer?.stop()
            }
        }
    }

    private fun configureFireEmitter() {
        fireEmitter?.apply {
            startColor = config.aboveKeyColorStart.value
            endColor = config.aboveKeyColorEnd.value
            emissionRate = 80.0
            minVelocityY = -100.0
            maxVelocityY = -60.0
        }
    }

    private fun fadeInFire() {
        fireEmitter?.let { emitter ->
            emitter.opacity = 0.0

            val timeline = Timeline(
                KeyFrame(
                    Duration.seconds(1.5),
                    KeyValue(object : WritableValue<Double> {
                        override fun getValue() = emitter.opacity
                        override fun setValue(value: Double) {
                            emitter.opacity = value
                        }
                    }, 1.0)
                )
            )
            timeline.play()
        }
    }

    private fun startFireStateUpdates() {
        fireStateUpdateTimer?.stop()

        fireStateUpdateTimer = Timeline(
            KeyFrame(Duration.seconds(1.0), {
                updateFireState()
            })
        )
        fireStateUpdateTimer?.cycleCount = Timeline.INDEFINITE
        fireStateUpdateTimer?.play()
    }

    private fun updateFireState() {
        fireEmitter?.apply {
            // Add some variation to fire intensity
            val intensity = Random.nextDouble(0.8, 1.2)
            emissionRate = 80.0 * intensity
            minVelocityY = -100.0 * intensity
            maxVelocityY = -60.0 * intensity

            // Update colors from config
            startColor = config.aboveKeyColorStart.value
            endColor = config.aboveKeyColorEnd.value
        }
    }

    private fun renderFireParticles() {
        fireEmitter?.let { emitter ->
            val particles = emitter.getParticles()

            particles.forEach { particle ->
                val color = particle.getColor()
                val size = particle.getSize()
                val opacity = particle.getOpacity() * emitter.opacity

                // Apply opacity to the color
                ctx.fill = Color(
                    color.red,
                    color.green,
                    color.blue,
                    color.opacity * opacity
                )

                // Draw particle as a circle
                ctx.fillOval(
                    particle.x - size / 2,
                    particle.y - size / 2,
                    size,
                    size
                )
            }
        }
    }

    private fun addBackgroundImage() {
        if (config.backgroundImage.value != currentBackgroundImage) {
            currentBackgroundImage = config.backgroundImage.value
            if (currentBackgroundImage == PianoBackgroundImage.NONE) {
                backgroundImage = null
            } else {
                backgroundImage = Image(
                    config.backgroundImage.value.file,
                    PIANO_WIDTH,
                    PIANO_BACKGROUND_HEIGHT,
                    false,
                    true
                )
            }
        }

        if (backgroundImage != null) {
            ctx.globalAlpha = config.backgroundImageTransparency.value
            ctx.drawImage(backgroundImage, 0.0, 0.0)
            ctx.globalAlpha = 1.0
        }
    }

    private fun addLogoImage() {
        if (config.logoVisible.value) {
            ctx.globalAlpha = config.logoTransparency.value
            ctx.drawImage(
                logoImage, config.logoLeft.value, config.logoTop.value,
                config.logoWidth.value, (config.logoWidth.value / 796.0) * 164.0
            )
            ctx.globalAlpha = 1.0
        }
    }
}