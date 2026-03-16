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
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.Color
import javafx.scene.paint.RadialGradient
import javafx.scene.paint.Stop

class PianoScene(val config: PianoConfiguration) : Canvas() {

    // Animation calculator with virtual thread
    private var animationCalculator: AnimationCalculator? = null
    private var ctx: GraphicsContext
    private val animationTimer: AnimationTimer

    // Cached images to avoid recreating them on every frame
    private val backgroundImageCache = mutableMapOf<PianoBackgroundImage, Image>()
    private var cachedLogoImage: Image? = null

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
                tailParticleCount = config.fireworksTailNumberOfParticles.value,
                launchHeightMultiplier = config.fireworksLaunchHeightMultiplier.value,
                explosionType = config.fireworksExplosionType.value
            )
        }
    }

    private fun update() {
        val state = latestAnimationState

        animationCalculator?.updateAboveKeyEffect(
            enabled = config.aboveKeyEnabled.value,
            startColor = config.aboveKeyColorStart.value,
            endColor = config.aboveKeyColorEnd.value
        )

        // Apply state to visual elements
        ctx.clearRect(0.0, 0.0, width, height)
        ctx.fill = config.backgroundColor.value
        ctx.fillRect(0.0, 0.0, width, height)

        // Background image
        if (config.backgroundImage.value != PianoBackgroundImage.NONE) {
            val image = backgroundImageCache.getOrPut(config.backgroundImage.value) {
                Image(config.backgroundImage.value.file, PIANO_WIDTH, PIANO_BACKGROUND_HEIGHT, false, true)
            }
            val previousAlpha = ctx.globalAlpha
            ctx.globalAlpha = config.backgroundImageTransparency.value
            ctx.drawImage(image, 0.0, 0.0)
            ctx.globalAlpha = previousAlpha
        }

        state?.let(::drawAboveKeyParticles)
        state?.let(::drawParticles)

        // Logo image
        if (config.logoVisible.value) {
            val w = config.logoWidth.value
            val h = (w / 796.0) * 164
            val image = cachedLogoImage ?: Image("logo/heavy-melodymatrix.png").also {
                cachedLogoImage = it
            }
            val previousAlpha = ctx.globalAlpha
            ctx.globalAlpha = config.logoTransparency.value
            ctx.drawImage(image, config.logoLeft.value, config.logoTop.value, w, h)
            ctx.globalAlpha = previousAlpha
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

    private fun drawAboveKeyParticles(state: AnimationState) {
        state.aboveKeyParticles.forEach { particle ->
            drawSoftParticle(particle.x, particle.y, particle.size, particle.color, particle.opacity)
        }
    }

    private fun drawSoftParticle(x: Double, y: Double, size: Double, color: Color, opacity: Double) {
        val cloudLayers = listOf(
            Triple(1.55, opacity * 0.16, 0.25),
            Triple(1.25, opacity * 0.24, 0.16),
            Triple(0.98, opacity * 0.34, 0.10),
            Triple(0.78, opacity * 0.46, 0.05)
        )

        cloudLayers.forEachIndexed { idx, (scale, alpha, offsetFactor) ->
            val scaledSize = size * scale
            val offset = size * offsetFactor
            val xDrift = kotlin.math.sin((x + y) * 0.011 + idx) * size * 0.09
            val yDrift = kotlin.math.cos((x - y) * 0.009 + idx) * size * 0.06

            val gradient = RadialGradient(
                0.0,
                0.0,
                x + xDrift,
                y - offset + yDrift,
                scaledSize * 0.55,
                false,
                CycleMethod.NO_CYCLE,
                listOf(
                    Stop(0.0, Color.color(color.red, color.green, color.blue, (alpha * 1.05).coerceIn(0.0, 1.0))),
                    Stop(0.45, Color.color(color.red, color.green, color.blue, (alpha * 0.45).coerceIn(0.0, 1.0))),
                    Stop(1.0, Color.color(color.red, color.green, color.blue, 0.0))
                )
            )

            ctx.fill = gradient
            // Two shifted ellipses per layer to create a soft amorphous cloud silhouette.
            ctx.fillOval(x - scaledSize * 0.58 + xDrift, y - scaledSize * 0.48 - offset + yDrift, scaledSize, scaledSize * 0.72)
            ctx.fillOval(x - scaledSize * 0.14 - xDrift * 0.35, y - scaledSize * 0.31 - offset * 0.55, scaledSize * 0.78, scaledSize * 0.52)
        }
    }
}