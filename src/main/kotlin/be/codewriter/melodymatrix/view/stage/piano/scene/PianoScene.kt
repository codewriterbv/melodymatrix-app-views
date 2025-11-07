package be.codewriter.melodymatrix.view.stage.piano.scene

import be.codewriter.melodymatrix.view.stage.piano.PianoStage.Companion.PIANO_BACKGROUND_HEIGHT
import be.codewriter.melodymatrix.view.stage.piano.PianoStage.Companion.PIANO_WIDTH
import be.codewriter.melodymatrix.view.stage.piano.animation.AnimationCalculator
import be.codewriter.melodymatrix.view.stage.piano.data.PianoBackgroundImage
import be.codewriter.melodymatrix.view.stage.piano.data.PianoConfiguration
import be.codewriter.melodymatrix.view.stage.piano.particle.ParticleEmitter
import javafx.animation.AnimationTimer
import javafx.animation.Timeline
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image

class PianoScene(val config: PianoConfiguration, val animationCalculator: AnimationCalculator) : Canvas() {

    // Animation calculator with virtual thread
    private var ctx: GraphicsContext
    private val animationTimer: AnimationTimer

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
        // animationCalculator.state ?: return

        // Apply state to visual elements
        ctx.clearRect(0.0, 0.0, width, height)

        addBackgroundImage()
        addLogoImage()
        addKeyEffect(deltaTime)
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