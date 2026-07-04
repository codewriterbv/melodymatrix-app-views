package be.codewriter.melodymatrix.view.view.piano.scene

import be.codewriter.melodymatrix.view.definition.MidiEvent
import be.codewriter.melodymatrix.view.definition.Note
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import be.codewriter.melodymatrix.view.event.MidiDataEvent
import be.codewriter.melodymatrix.view.view.piano.PianoWithEffectsView.Companion.PIANO_BACKGROUND_HEIGHT
import be.codewriter.melodymatrix.view.view.piano.PianoWithEffectsView.Companion.PIANO_WIDTH
import be.codewriter.melodymatrix.view.view.piano.animation.AnimationCalculator
import be.codewriter.melodymatrix.view.view.piano.animation.AnimationState
import be.codewriter.melodymatrix.view.view.piano.animation.NoteBlockConfig
import be.codewriter.melodymatrix.view.view.piano.animation.NoteBlockData
import be.codewriter.melodymatrix.view.view.piano.data.PianoBackgroundImage
import be.codewriter.melodymatrix.view.view.piano.data.PianoConfiguration
import javafx.animation.AnimationTimer
import javafx.geometry.Point2D
import javafx.geometry.Rectangle2D
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.RadialGradient
import javafx.scene.paint.Stop

/**
 * JavaFX [Canvas] that renders the animated piano scene above the keyboard.
 *
 * Hosts an [AnimationCalculator] that runs on a virtual thread at ~60 FPS, producing
 * [AnimationState] snapshots. On each JavaFX pulse an [AnimationTimer] reads the latest
 * snapshot and redraws the canvas: background colour/image, logo, above-key smoke,
 * explosion particles, and fireworks particles.
 *
 * @param config Observable configuration controlling all visual aspects of the scene
 * @see PianoStage
 * @see AnimationCalculator
 */
class PianoCanvas(val config: PianoConfiguration) : Canvas() {

    // Animation calculator with virtual thread
    private var animationCalculator: AnimationCalculator? = null
    private var ctx: GraphicsContext
    private val animationTimer: AnimationTimer

    @Volatile
    private var latestAnimationState: AnimationState? = null

    // Cached images to avoid recreating them every frame
    private var cachedBackgroundImage: Image? = null
    private var cachedBackgroundImageKey: PianoBackgroundImage? = null
    private var cachedLogoImage: Image? = null
    private var cachedLogoWidth: Double = -1.0

    init {
        width = PIANO_WIDTH
        height = PIANO_BACKGROUND_HEIGHT

        ctx = graphicsContext2D

        // Invalidate cached background image when the source changes
        config.backgroundImage.addListener { _, _, _ ->
            cachedBackgroundImage = null
            cachedBackgroundImageKey = null
        }

        // Invalidate cached logo image when the width changes (affects rendered size)
        config.logoWidth.addListener { _, _, _ ->
            cachedLogoImage = null
            cachedLogoWidth = -1.0
        }

        // Start animation calculator
        animationCalculator = AnimationCalculator { state ->
            latestAnimationState = state
        }
        animationCalculator?.start()

        // Drop any in-flight note blocks when the corresponding toggle is switched off,
        // so previously-visible blocks disappear immediately instead of finishing their trajectory.
        config.fallingBlocksEnabled.addListener { _, _, enabled ->
            if (enabled != true) animationCalculator?.clearNoteBlocks(clearFalling = true, clearRising = false)
        }
        config.risingBlocksEnabled.addListener { _, _, enabled ->
            if (enabled != true) animationCalculator?.clearNoteBlocks(clearFalling = false, clearRising = true)
        }

        // Create animation timer for 60 FPS updates
        animationTimer = object : AnimationTimer() {
            override fun handle(now: Long) {
                update()
            }
        }
        animationTimer.start()
    }

    /**
     * Stops the animation timer and shuts down the background [AnimationCalculator].
     *
     * Should be called when the stage is closed to release the virtual-thread executor.
     */
    fun stop() {
        animationTimer.stop()
        animationCalculator?.stop()
    }

    /**
     * Reacts to a MIDI note event by forwarding key-press state and spawning particle effects.
     *
     * On NOTE_ON, queues an explosion and/or fireworks burst (if enabled in [config]).
     * On NOTE_OFF only the key-press state is updated.
     *
     * @param midiDataEvent The MIDI event to process
     * @param keyOrigin     The on-screen position of the pressed key (used as effect origin)
     */
    fun playNote(midiDataEvent: MidiDataEvent, keyOrigin: Point2D) {
        val isPressed = midiDataEvent.event == MidiEvent.NOTE_ON
        val sceneY = PIANO_BACKGROUND_HEIGHT + keyOrigin.y

        animationCalculator?.updateKeyPress(midiDataEvent.note, isPressed)

        if (!isPressed) {
            return
        }

        if (config.explosionEnabled.value) {
            animationCalculator?.addExplosion(
                x = keyOrigin.x,
                y = sceneY,
                velocity = midiDataEvent.velocity,
                radius = config.explosionRadius.value,
                color = config.explosionColor.value,
                particleCount = config.explosionNumberOfParticles.value,
                particleSize = config.explosionParticleSize.value,
                tailParticleCount = config.explosionTailNumberOfParticles.value,
                liftMultiplier = config.explosionLiftMultiplier.value,
                randomColor = config.explosionRandomColor.value
            )
        }

        if (config.fireworksEnabled.value) {
            animationCalculator?.addFireworks(
                x = keyOrigin.x,
                y = sceneY,
                velocity = midiDataEvent.velocity,
                radius = config.fireworksRadius.value,
                color = config.fireworksColor.value,
                particleCount = config.fireworksNumberOfParticles.value,
                particleSize = config.fireworksParticleSize.value,
                randomColor = config.fireworksRandomColor.value,
                tailParticleCount = config.fireworksTailNumberOfParticles.value,
                launchHeightMultiplier = config.fireworksLaunchHeightMultiplier.value,
                liftMultiplier = config.fireworksLiftMultiplier.value,
                explosionType = config.fireworksExplosionType.value
            )
        }
        }

        /**
        * Schedules a Synthesia-style falling note block that lands on the given [note]'s
        * key column at [landTimestampMs].
        *
        * @param note            Target note (identifies the key column)
        * @param landTimestampMs Absolute wall-clock time (System.currentTimeMillis()) when
        *                        the bottom of the block should reach the top of the keyboard
        * @param durationMs      Sustain length in milliseconds; determines block height
        * @param velocity        MIDI velocity (0–127)
        * @param channel         MIDI channel (0–15)
        * @param keyRect         Key column rectangle from `KeyboardView.getKeyBlockRect`
        */
        fun scheduleFallingNote(
            note: Note,
            landTimestampMs: Long,
            durationMs: Long,
            velocity: Int,
            channel: Int,
            keyRect: Rectangle2D
        ) {
            if (animationCalculator == null) {
                logger.warn("[FALLING] scheduleFallingNote called but animationCalculator is null")
                return
            }
            logger.debug(
                "[FALLING] canvas.scheduleFallingNote: note={} land=+{}ms durationMs={} keyRect=[x={}, w={}]",
                note, landTimestampMs - System.currentTimeMillis(), durationMs, keyRect.minX, keyRect.width
            )
            animationCalculator?.scheduleFallingBlock(
                note = note,
                landTimestampMs = landTimestampMs,
                durationMs = durationMs,
                velocity = velocity,
                channel = channel,
                keyRect = keyRect
            )
        }

        /** Starts a rising note block for [note], growing upward until [endRisingNote] is called. */
        fun beginRisingNote(note: Note, velocity: Int, channel: Int, keyRect: Rectangle2D) {
        animationCalculator?.startRisingBlock(note, velocity, channel, keyRect)
        }

        /** Marks the open rising block for [note] as released; it will drift up and off. */
        fun endRisingNote(note: Note) {
            animationCalculator?.endRisingBlock(note)
        }

        /**
         * Discards every scheduled falling block. Called when playback is stopped mid-recording
         * so pre-scheduled blocks do not keep animating past the stop.
         */
        fun clearScheduledNotes() {
            logger.info("[FALLING] clearScheduledNotes: dropping all falling blocks")
            animationCalculator?.clearNoteBlocks(clearFalling = true, clearRising = false)
        }

    /** Redraws the full canvas for the current animation frame. Called by the [AnimationTimer]. */
    private fun update() {
        val state = latestAnimationState

        animationCalculator?.updateAboveKeyEffect(
            enabled = config.cloudEnabled.value,
            startColor = config.cloudColorStart.value,
            endColor = config.cloudColorEnd.value,
            particleCount = config.cloudParticleCount.value,
            particleSize = config.cloudParticleSize.value,
            driftSpeed = config.cloudDriftSpeed.value,
            wobbleAmplitude = config.cloudWobbleAmplitude.value,
            opacity = config.cloudOpacity.value,
            spawnRadius = config.cloudSpawnRadius.value
        )

        animationCalculator?.updateNoteBlockConfig(
            NoteBlockConfig(
                lookAheadSeconds = config.noteBlockLookAheadSeconds.value,
                colorMode = config.noteBlockColorMode.value,
                fixedColor = config.noteBlockFixedColor.value,
                lowVelocityColor = config.noteBlockLowVelocityColor.value,
                highVelocityColor = config.noteBlockHighVelocityColor.value,
                opacity = config.noteBlockOpacity.value
            )
        )

        // Apply state to visual elements
        ctx.clearRect(0.0, 0.0, width, height)
        ctx.fill = config.backgroundColor.value
        ctx.fillRect(0.0, 0.0, width, height)

        // Background image
        if (config.backgroundImageEnabled.value) {
            val bgKey = config.backgroundImage.value
            if (cachedBackgroundImage == null || cachedBackgroundImageKey != bgKey) {
                cachedBackgroundImageKey = bgKey
                cachedBackgroundImage = Image(bgKey.file, PIANO_WIDTH, PIANO_BACKGROUND_HEIGHT, false, true)
            }
            val savedAlpha = ctx.globalAlpha
            ctx.globalAlpha = config.backgroundImageTransparency.value
            ctx.drawImage(cachedBackgroundImage, 0.0, 0.0)
            ctx.globalAlpha = savedAlpha
        }

        // Note blocks sit in front of the background but behind particle effects
        // (clouds, explosions, fireworks) so effects still "pop" over the falling grid.
        state?.let(::drawNoteBlocks)
        state?.let(::drawAboveKeyParticles)
        state?.let(::drawParticles)

        // Logo image
        if (config.logoVisible.value) {
            val w = config.logoWidth.value
            val h = (w / 796.0) * 164
            if (cachedLogoImage == null || cachedLogoWidth != w) {
                cachedLogoWidth = w
                cachedLogoImage = Image("logo/heavy-melodymatrix.png", w, h, false, true)
            }
            val savedAlpha = ctx.globalAlpha
            ctx.globalAlpha = config.logoTransparency.value
            ctx.drawImage(cachedLogoImage, config.logoLeft.value, config.logoTop.value)
            ctx.globalAlpha = savedAlpha
        }
    }

    /** Draws all explosion/fireworks particles from the given [AnimationState]. */
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

    /**
     * Draws Synthesia-style falling and rising note blocks from the given [AnimationState].
     *
     * Falling blocks (playback reference) are drawn first, then rising blocks (user input)
     * on top so the performer's own notes remain visible over the incoming reference.
     */
    private fun drawNoteBlocks(state: AnimationState) {
        if (state.fallingBlocks.isEmpty() && state.risingBlocks.isEmpty()) return

        val cornerRadius = config.noteBlockCornerRadius.value
        val arcDiameter = (cornerRadius * 2.0).coerceAtLeast(0.0)
        val outline = config.noteBlockOutlineEnabled.value
        val outlineColor = config.noteBlockOutlineColor.value
        val outlineWidth = config.noteBlockOutlineWidth.value

        val savedAlpha = ctx.globalAlpha
        val savedLineWidth = ctx.lineWidth

        val drawnFalling = drawBlockList(state.fallingBlocks, arcDiameter, outline, outlineColor, outlineWidth)
        val drawnRising = drawBlockList(state.risingBlocks, arcDiameter, outline, outlineColor, outlineWidth)

        val nowMs = System.currentTimeMillis()
        if (state.fallingBlocks.isNotEmpty() && nowMs - lastDrawHeartbeatMs > 500L) {
            lastDrawHeartbeatMs = nowMs
            val first = state.fallingBlocks.first()
            logger.info(
                "[FALLING] draw: falling total={} drawn={} rising drawn={}; first block: x={} y={} w={} h={} colour={} opacity={}",
                state.fallingBlocks.size, drawnFalling, drawnRising,
                "%.1f".format(first.x), "%.1f".format(first.y),
                "%.1f".format(first.width), "%.1f".format(first.height),
                first.color, "%.2f".format(first.opacity)
            )
        }

        ctx.globalAlpha = savedAlpha
        ctx.lineWidth = savedLineWidth
    }

    /** Returns the number of blocks actually drawn (after visibility clipping). */
    private fun drawBlockList(
        blocks: List<NoteBlockData>,
        arcDiameter: Double,
        outline: Boolean,
        outlineColor: Color,
        outlineWidth: Double
    ): Int {
        var drawn = 0
        blocks.forEach { block ->
            if (block.height <= 0.0 || block.width <= 0.0) return@forEach
            // Skip fully off-canvas blocks (should be rare, physics prunes most).
            if (block.y + block.height < 0.0 || block.y > PIANO_BACKGROUND_HEIGHT) return@forEach

            ctx.globalAlpha = block.opacity.coerceIn(0.0, 1.0)
            ctx.fill = block.color
            ctx.fillRoundRect(block.x, block.y, block.width, block.height, arcDiameter, arcDiameter)

            if (outline && outlineWidth > 0.0) {
                ctx.stroke = outlineColor
                ctx.lineWidth = outlineWidth
                ctx.strokeRoundRect(block.x, block.y, block.width, block.height, arcDiameter, arcDiameter)
            }
            drawn++
        }
        return drawn
    }

    private var lastDrawHeartbeatMs = 0L

    companion object {
        private val logger: Logger = LogManager.getLogger(PianoCanvas::class.java.name)
    }

    /** Draws all above-key smoke particles from the given [AnimationState]. */
    private fun drawAboveKeyParticles(state: AnimationState) {
        state.aboveKeyParticles.forEach { particle ->
            drawSoftParticle(particle.x, particle.y, particle.size, particle.color, particle.opacity)
        }
    }

    /**
     * Draws a multi-layer radial-gradient "cloud" particle at the given position.
     *
     * Renders four overlapping ellipses with decreasing opacity to produce a soft, volumetric look.
     *
     * @param x       Centre X position
     * @param y       Centre Y position
     * @param size    Nominal diameter of the particle cloud
     * @param color   Base colour for the gradient fills
     * @param opacity Overall opacity multiplier (0.0–1.0)
     */
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
            ctx.fillOval(
                x - scaledSize * 0.58 + xDrift,
                y - scaledSize * 0.48 - offset + yDrift,
                scaledSize,
                scaledSize * 0.72
            )
            ctx.fillOval(
                x - scaledSize * 0.14 - xDrift * 0.35,
                y - scaledSize * 0.31 - offset * 0.55,
                scaledSize * 0.78,
                scaledSize * 0.52
            )
        }
    }
}