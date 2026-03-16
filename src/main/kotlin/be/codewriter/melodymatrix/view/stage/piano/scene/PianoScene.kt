package be.codewriter.melodymatrix.view.stage.piano.scene

import be.codewriter.melodymatrix.view.stage.piano.PianoStage.Companion.PIANO_BACKGROUND_HEIGHT
import be.codewriter.melodymatrix.view.stage.piano.PianoStage.Companion.PIANO_WIDTH
import be.codewriter.melodymatrix.view.stage.piano.animation.AnimationCalculator
import be.codewriter.melodymatrix.view.stage.piano.animation.AnimationState
import be.codewriter.melodymatrix.view.stage.piano.data.PianoConfiguration
import be.codewriter.melodymatrix.view.data.MidiData
import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.stage.piano.data.PianoBackgroundImage
import javafx.geometry.Point2D
import javafx.animation.AnimationTimer
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image
import javafx.scene.paint.Color

class PianoScene(val config: PianoConfiguration) : Canvas() {

    // Animation calculator with virtual thread
    private var animationCalculator: AnimationCalculator? = null
    private var ctx: GraphicsContext
    private val animationTimer: AnimationTimer

    @Volatile
    private var latestAnimationState: AnimationState? = null

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

    fun playNote(midiData: MidiData, keyOrigin: Point2D) {
        val isPressed = midiData.event == MidiEvent.NOTE_ON
        val sceneY = PIANO_BACKGROUND_HEIGHT + keyOrigin.y

        animationCalculator?.updateKeyPress(midiData.note, isPressed)

        if (!isPressed) {
            return
        }

        if (config.explosionEnabled.value) {
            animationCalculator?.addExplosion(
                x = keyOrigin.x,
                y = sceneY,
                velocity = midiData.velocity,
                radius = config.explosionRadius.value,
                color = config.explosionColor.value,
                particleCount = config.explosionNumberOfParticles.value,
                particleSize = config.explosionParticleSize.value,
                randomColor = config.explosionRandomColor.value
            )
        }

        if (config.fireworksEnabled.value) {
            animationCalculator?.addFireworks(
                x = keyOrigin.x,
                y = sceneY,
                velocity = midiData.velocity,
                radius = config.fireworksRadius.value,
                color = config.fireworksColor.value,
                particleCount = config.fireworksNumberOfParticles.value,
                particleSize = config.fireworksParticleSize.value,
                randomColor = config.fireworksRandomColor.value,
                tailParticleCount = config.fireworksTailNumberOfParticles.value
            )
        }
    }

    private fun update() {
        val state = latestAnimationState

        // Apply state to visual elements
        ctx.clearRect(0.0, 0.0, width, height)
        ctx.fill = config.backgroundColor.value
        ctx.fillRect(0.0, 0.0, width, height)

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

        state?.let(::drawParticles)

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

    private fun drawParticles(state: AnimationState) {
        state.particlePositions.forEach { particle ->
            ctx.fill = Color.color(
                particle.color.red,
                particle.color.green,
                particle.color.blue,
                particle.opacity
            )
            ctx.fillOval(particle.x, particle.y, particle.size, particle.size)
        }
    }
}