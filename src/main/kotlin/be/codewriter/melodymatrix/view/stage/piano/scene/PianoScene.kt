package be.codewriter.melodymatrix.view.stage.piano.scene

import be.codewriter.melodymatrix.view.stage.piano.PianoStage.Companion.PIANO_BACKGROUND_HEIGHT
import be.codewriter.melodymatrix.view.stage.piano.PianoStage.Companion.PIANO_WIDTH
import be.codewriter.melodymatrix.view.stage.piano.animation.AnimationCalculator
import be.codewriter.melodymatrix.view.stage.piano.animation.AnimationState
import be.codewriter.melodymatrix.view.stage.piano.data.PianoBackgroundImage
import be.codewriter.melodymatrix.view.stage.piano.data.PianoConfiguration
import javafx.animation.AnimationTimer
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image

class PianoScene(val config: PianoConfiguration) : Canvas() {

    // Animation calculator with virtual thread
    private var animationCalculator: AnimationCalculator? = null
    private var ctx: GraphicsContext
    private val animationTimer: AnimationTimer

    @Volatile
    private var latestAnimationState: AnimationState? = null
    private var lastFrameTime = 0L

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
                update()
            }
        }
        animationTimer.start()
    }

    fun stop() {
        animationCalculator?.stop()
    }

    private fun update() {
        // Read latest calculated state
        latestAnimationState ?: return

        // Apply state to visual elements
        ctx.clearRect(0.0, 0.0, width, height)

        // Background image
        if (config.backgroundImage.value != PianoBackgroundImage.NONE) {
            val image = Image(
                config.backgroundImage.value.file,
                PIANO_WIDTH,
                PIANO_BACKGROUND_HEIGHT,
                false,
                true
            ).apply {
                opacity = config.backgroundImageTransparency.value
            }
            ctx.drawImage(image, 0.0, 0.0)
        }

        // Logo image
        if (config.logoVisible.value) {
            val w = config.logoWidth.value
            val h = (w / 796.0) * 164
            val image = Image(
                "logo/heavy-melodymatrix.png",
                w,
                h,
                false,
                true
            ).apply {
                opacity = config.logoTransparency.value
            }
            ctx.drawImage(image, config.logoLeft.value, config.logoTop.value)
        }
    }
}