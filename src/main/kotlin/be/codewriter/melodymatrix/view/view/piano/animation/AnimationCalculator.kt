package be.codewriter.melodymatrix.view.view.piano.animation

import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.view.piano.PianoWithEffectsView.Companion.PIANO_BACKGROUND_HEIGHT
import be.codewriter.melodymatrix.view.view.piano.data.FireworksExplosionType
import javafx.geometry.Point2D
import javafx.geometry.Rectangle2D
import javafx.scene.paint.Color
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Off-thread physics engine that calculates the piano animation state at ~60 FPS.
 *
 * Runs a [ScheduledExecutorService] loop on a virtual thread. Each frame it drains a
 * lock-free command queue, advances particle physics, updates key-press animations, and
 * produces an immutable [AnimationState] snapshot that is forwarded to [updateCallback]
 * for rendering on the JavaFX thread.
 *
 * Commands (key presses, explosions, fireworks) are submitted via thread-safe queue methods;
 * they are applied at the start of the next frame inside the state lock.
 *
 * @param updateCallback Invoked on every calculated frame with the new [AnimationState]
 * @see AnimationState
 * @see ExplosionGenerator
 * @see FireworksGenerator
 * @see CloudGenerator
 */
class AnimationCalculator(
    private val updateCallback: (AnimationState) -> Unit
) {
    /**
     * Sealed hierarchy of commands submitted to the calculator's command queue.
     */
    private sealed interface AnimationCommand {
        /** Notifies the calculator that a key was pressed or released. */
        data class KeyPress(val note: Note, val isPressed: Boolean) : AnimationCommand

        /** Requests a radial explosion burst at the given position. */
        data class Explosion(
            val x: Double,
            val y: Double,
            val velocity: Int,
            val radius: Double,
            val color: Color,
            val particleCount: Int,
            val particleSize: Double,
            val tailParticleCount: Int,
            val liftMultiplier: Double,
            val randomColor: Boolean
        ) : AnimationCommand

        /** Requests a fireworks burst launched from the given position. */
        data class Fireworks(
            val x: Double,
            val y: Double,
            val velocity: Int,
            val radius: Double,
            val color: Color,
            val particleCount: Int,
            val particleSize: Double,
            val randomColor: Boolean,
            val tailParticleCount: Int,
            val launchHeightMultiplier: Double,
            val liftMultiplier: Double,
            val explosionType: FireworksExplosionType
        ) : AnimationCommand

        /** Schedules a falling note block that will "land" on the keyboard at [landTimestampMs]. */
        data class ScheduleFallingBlock(
            val note: Note,
            val landTimestampMs: Long,
            val durationMs: Long,
            val velocity: Int,
            val channel: Int,
            val keyRect: Rectangle2D
        ) : AnimationCommand

        /** Starts a rising note block that grows upward from the top of the keyboard. */
        data class StartRisingBlock(
            val note: Note,
            val velocity: Int,
            val channel: Int,
            val keyRect: Rectangle2D,
            val startTimestampMs: Long
        ) : AnimationCommand

        /** Marks the rising block for [note] as closed so its height stops growing. */
        data class EndRisingBlock(val note: Note, val endTimestampMs: Long) : AnimationCommand

        /** Clears scheduled falling blocks and/or in-flight rising blocks. */
        data class ClearNoteBlocks(val clearFalling: Boolean, val clearRising: Boolean) : AnimationCommand
    }

    private val executor: ScheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor { task ->
            Thread.ofVirtual().name("animation-calculator").unstarted(task)
        }

    private val isRunning = AtomicBoolean(false)
    private val stateLock = Any()
    private var lastUpdateTime = System.nanoTime()
    private val commandQueue = ConcurrentLinkedQueue<AnimationCommand>()

    // State tracking
    private val activeParticles = mutableListOf<ParticleInfo>()
    private val aboveKeyParticles = mutableListOf<AboveKeyParticleInfo>()
    private val activeKeys = mutableMapOf<Note, KeyAnimationInfo>()
    private val cloudGenerator = CloudGenerator()
    private val fireworksGenerator = FireworksGenerator()
    private val explosionGenerator = ExplosionGenerator()

    private val fallingBlocks = mutableListOf<FallingBlockInfo>()
    private val risingBlocks = mutableListOf<RisingBlockInfo>()
    private var lastFallingHeartbeatMs = 0L

    @Volatile
    private var noteBlockConfig: NoteBlockConfig = NoteBlockConfig.DEFAULT

    /**
     * Starts the 60 FPS animation loop. No-ops if already running.
     */
    fun start() {
        if (isRunning.compareAndSet(false, true)) {
            // Schedule at 60 FPS (16.67ms per frame)
            executor.scheduleAtFixedRate(
                ::calculateFrame,
                0,
                16_667_000, // nanoseconds
                TimeUnit.NANOSECONDS
            )
        }
    }

    /**
     * Stops the animation loop and shuts down the executor. No-ops if already stopped.
     */
    fun stop() {
        if (isRunning.compareAndSet(true, false)) {
            executor.shutdown()
        }
    }

    private fun calculateFrame() {
        val currentTime = System.nanoTime()
        val deltaTime = (currentTime - lastUpdateTime) / 1_000_000_000.0 // Convert to seconds
        lastUpdateTime = currentTime

        val nowMs = System.currentTimeMillis()
        val cfg = noteBlockConfig

        val state = synchronized(stateLock) {
            drainQueuedCommands()
            // Perform heavy calculations here
            updateParticles(deltaTime)
            updateKeyAnimations(deltaTime)
            updateFireEffect(deltaTime)
            updateNoteBlocks(deltaTime, nowMs, cfg)

            // Create immutable state snapshot
            AnimationState(
                timestamp = currentTime,
                particlePositions = activeParticles.map { it.toData() },
                aboveKeyParticles = aboveKeyParticles.map { it.toData() },
                fireEmitterState = calculateFireState(),
                keyStates = activeKeys.mapValues { it.value.toState() },
                fallingBlocks = fallingBlocks.map { it.toData(nowMs, cfg) },
                risingBlocks = risingBlocks.map { it.toData(cfg) }
            )
        }

        // Send to JavaFX thread
        updateCallback(state)
    }

    fun addParticle(x: Double, y: Double, velocity: Point2D, color: Color, lifespan: Double) {
        synchronized(stateLock) {
            activeParticles.add(ParticleInfo(x, y, velocity, color, lifespan, 5.0))
        }
    }

    /**
     * Enqueues a key-press or key-release command to be applied on the next frame.
     *
     * @param note      The note whose key state changed
     * @param isPressed True for key-down, false for key-up
     */
    fun updateKeyPress(note: Note, isPressed: Boolean) {
        commandQueue.add(AnimationCommand.KeyPress(note, isPressed))
    }

    /**
     * Updates the above-key smoke effect configuration immediately (thread-safe).
     *
     * @param enabled         Whether the smoke effect should be active
     * @param startColor      Base/idle smoke colour
     * @param endColor        Colour smoke shifts toward near active keys
     * @param particleCount   Number of particles seeded across the keyboard
     * @param particleSize    Nominal blob size (actual size varies ±40 % around this value)
     * @param driftSpeed      Maximum horizontal drift speed in pixels/second
     * @param wobbleAmplitude Vertical wobble amplitude in pixels
     * @param opacity         Base opacity (actual opacity varies ±50 % around this value)
     * @param spawnRadius     X-radius around a pressed key used for density checks and spawning
     */
    fun updateAboveKeyEffect(
        enabled: Boolean,
        startColor: Color,
        endColor: Color,
        particleCount: Int,
        particleSize: Double,
        driftSpeed: Double,
        wobbleAmplitude: Double,
        opacity: Double,
        spawnRadius: Double
    ) {
        synchronized(stateLock) {
            cloudGenerator.updateConfig(
                enabled, startColor, endColor,
                particleCount, particleSize, driftSpeed, wobbleAmplitude, opacity, spawnRadius,
                aboveKeyParticles
            )
        }
    }

    /**
     * Enqueues a radial explosion command to be processed on the next animation frame.
     *
     * @param x            Horizontal origin in scene coordinates
     * @param y            Vertical origin in scene coordinates
     * @param velocity     MIDI velocity (1–127)
     * @param radius       Base spread radius
     * @param color        Base particle colour
     * @param particleCount Number of particles to generate
     * @param particleSize  Base particle diameter in pixels
     * @param tailParticleCount Number of trailing particles emitted upward from the key
     * @param liftMultiplier Multiplier for vertical lift strength (higher = taller bursts)
     * @param randomColor  When true uses a cinematic colour palette
     */
    fun addExplosion(
        x: Double,
        y: Double,
        velocity: Int,
        radius: Double,
        color: Color,
        particleCount: Int,
        particleSize: Double,
        tailParticleCount: Int,
        liftMultiplier: Double,
        randomColor: Boolean
    ) {
        commandQueue.add(
            AnimationCommand.Explosion(
                x = x,
                y = y,
                velocity = velocity,
                radius = radius,
                color = color,
                particleCount = particleCount,
                particleSize = particleSize,
                tailParticleCount = tailParticleCount,
                liftMultiplier = liftMultiplier,
                randomColor = randomColor
            )
        )
    }

    /**
     * Enqueues a fireworks launch command to be processed on the next animation frame.
     *
     * @param x                     Horizontal launch origin in scene coordinates
     * @param y                     Vertical launch origin in scene coordinates
     * @param velocity               MIDI velocity (1–127)
     * @param radius                 Burst spread radius
     * @param color                  Base colour for the burst
     * @param particleCount          Number of burst particles
     * @param particleSize           Base particle diameter in pixels
     * @param randomColor            When true uses a cinematic colour palette
     * @param tailParticleCount      Trailing particles emitted during ascent
     * @param launchHeightMultiplier Multiplier applied to the launch height (0.6–2.8)
     * @param liftMultiplier          Multiplier applied to upward fireworks lift (0.6–1.8)
     * @param explosionType          The burst pattern style
     */
    fun addFireworks(
        x: Double,
        y: Double,
        velocity: Int,
        radius: Double,
        color: Color,
        particleCount: Int,
        particleSize: Double,
        randomColor: Boolean,
        tailParticleCount: Int,
        launchHeightMultiplier: Double,
        liftMultiplier: Double,
        explosionType: FireworksExplosionType
    ) {
        commandQueue.add(
            AnimationCommand.Fireworks(
                x = x,
                y = y,
                velocity = velocity,
                radius = radius,
                color = color,
                particleCount = particleCount,
                particleSize = particleSize,
                randomColor = randomColor,
                tailParticleCount = tailParticleCount,
                launchHeightMultiplier = launchHeightMultiplier,
                liftMultiplier = liftMultiplier,
                explosionType = explosionType
            )
        )
    }

    /**
     * Enqueues a Synthesia-style falling block that lands on [note]'s key at [landTimestampMs].
     *
     * @param note             Target note; used purely to identify the target key column
     * @param landTimestampMs  Absolute wall-clock time (System.currentTimeMillis()) at which
     *                         the bottom of the block should align with the top of the keyboard
     * @param durationMs       Sustain length of the source note; determines block height
     * @param velocity         MIDI velocity 0–127 (used for BY_VELOCITY colouring)
     * @param channel          MIDI channel 0–15 (used for BY_CHANNEL colouring)
     * @param keyRect          Key column rectangle (x + width) from `KeyboardView.getKeyBlockRect`
     */
    fun scheduleFallingBlock(
        note: Note,
        landTimestampMs: Long,
        durationMs: Long,
        velocity: Int,
        channel: Int,
        keyRect: Rectangle2D
    ) {
        logger.debug(
            "[FALLING] enqueue command: note={} land=+{}ms durationMs={}",
            note, landTimestampMs - System.currentTimeMillis(), durationMs
        )
        commandQueue.add(
            AnimationCommand.ScheduleFallingBlock(
                note = note,
                landTimestampMs = landTimestampMs,
                durationMs = durationMs,
                velocity = velocity,
                channel = channel,
                keyRect = keyRect
            )
        )
    }

    /**
     * Enqueues the start of a rising block for the given [note].
     *
     * The block sprouts at the top of the keyboard and grows upward each frame
     * until [endRisingBlock] is called for the same [note], after which it
     * detaches and drifts upward until it leaves the canvas.
     */
    fun startRisingBlock(note: Note, velocity: Int, channel: Int, keyRect: Rectangle2D) {
        commandQueue.add(
            AnimationCommand.StartRisingBlock(
                note = note,
                velocity = velocity,
                channel = channel,
                keyRect = keyRect,
                startTimestampMs = System.currentTimeMillis()
            )
        )
    }

    /** Enqueues the release of the currently-open rising block for [note]. */
    fun endRisingBlock(note: Note) {
        commandQueue.add(AnimationCommand.EndRisingBlock(note, System.currentTimeMillis()))
    }

    /**
     * Clears scheduled falling blocks and/or in-flight rising blocks.
     * Called when the corresponding user toggle flips from enabled→disabled,
     * and also useful for future wiring of playback pause / seek.
     */
    fun clearNoteBlocks(clearFalling: Boolean = true, clearRising: Boolean = false) {
        commandQueue.add(AnimationCommand.ClearNoteBlocks(clearFalling, clearRising))
    }

    /**
     * Updates the appearance/timing configuration used by note-block rendering.
     * Meant to be called every frame with the current settings snapshot.
     */
    fun updateNoteBlockConfig(config: NoteBlockConfig) {
        noteBlockConfig = config
    }

    /** Drains all queued commands and applies them to the current animation state. */
    private fun drainQueuedCommands() {
        while (true) {
            val command = commandQueue.poll() ?: break
            when (command) {
                is AnimationCommand.KeyPress -> {
                    if (command.isPressed) {
                        activeKeys[command.note] = KeyAnimationInfo(command.note, isPressed = true)
                    } else {
                        activeKeys[command.note]?.isPressed = false
                    }
                }

                is AnimationCommand.Explosion -> {
                    activeParticles.addAll(
                        explosionGenerator.generate(
                            x = command.x,
                            y = command.y,
                            velocity = command.velocity,
                            radius = command.radius,
                            color = command.color,
                            particleCount = command.particleCount,
                            particleSize = command.particleSize,
                            tailParticleCount = command.tailParticleCount,
                            liftMultiplier = command.liftMultiplier,
                            randomColor = command.randomColor
                        )
                    )
                }

                is AnimationCommand.Fireworks -> {
                    activeParticles.addAll(
                        fireworksGenerator.generate(
                            x = command.x,
                            y = command.y,
                            velocity = command.velocity,
                            radius = command.radius,
                            color = command.color,
                            particleCount = command.particleCount,
                            particleSize = command.particleSize,
                            randomColor = command.randomColor,
                            tailParticleCount = command.tailParticleCount,
                            launchHeightMultiplier = command.launchHeightMultiplier,
                            liftMultiplier = command.liftMultiplier,
                            explosionType = command.explosionType
                        )
                    )
                }

                is AnimationCommand.ScheduleFallingBlock -> {
                    val info = FallingBlockInfo(
                        note = command.note,
                        landTimestampMs = command.landTimestampMs,
                        durationMs = command.durationMs,
                        velocity = command.velocity,
                        channel = command.channel,
                        x = command.keyRect.minX,
                        width = command.keyRect.width
                    )
                    fallingBlocks.add(info)
                    logger.debug(
                        "[FALLING] applied to state: note={} land=+{}ms durationMs={} totalFalling={}",
                        info.note, info.landTimestampMs - System.currentTimeMillis(),
                        info.durationMs, fallingBlocks.size
                    )
                }

                is AnimationCommand.StartRisingBlock -> {
                    // Replace any existing open block for the same note (stuck notes).
                    risingBlocks.removeAll { it.note == command.note && it.isOpen }
                    risingBlocks.add(
                        RisingBlockInfo(
                            note = command.note,
                            velocity = command.velocity,
                            channel = command.channel,
                            x = command.keyRect.minX,
                            width = command.keyRect.width,
                            y = PIANO_BACKGROUND_HEIGHT,
                            height = 0.0,
                            isOpen = true
                        )
                    )
                }

                is AnimationCommand.EndRisingBlock -> {
                    risingBlocks.firstOrNull { it.note == command.note && it.isOpen }?.isOpen = false
                }

                is AnimationCommand.ClearNoteBlocks -> {
                    if (command.clearFalling) fallingBlocks.clear()
                    if (command.clearRising) risingBlocks.clear()
                }
            }
        }
    }

    /** Advances all active explosion/fireworks particles by [deltaTime] seconds, removing expired ones. */
    private fun updateParticles(deltaTime: Double) {
        activeParticles.removeIf { particle ->
            particle.age += deltaTime
            particle.age >= particle.lifespan
        }

        activeParticles.forEach { particle ->
            // Physics calculations
            particle.x += particle.velocity.x * deltaTime
            particle.y += particle.velocity.y * deltaTime
            particle.velocity = particle.velocity.add(0.0, 9.8 * deltaTime) // Gravity
        }
    }

    /** Advances above-key smoke particles, delegating to [CloudGenerator]. */
    private fun updateAboveKeyParticles(deltaTime: Double) {
        val activeNotes = activeKeys.filter { it.value.isPressed }.keys
        cloudGenerator.update(deltaTime, activeNotes, aboveKeyParticles)
    }

    /** Advances key-press animation progress values for all tracked keys. */
    private fun updateKeyAnimations(deltaTime: Double) {
        activeKeys.values.forEach { key ->
            key.animationProgress += deltaTime * 5.0 // Animation speed
            if (!key.isPressed && key.animationProgress >= 1.0) {
                key.animationProgress = 0.0
            }
        }
    }

    /** Updates above-key particles and returns the current [FireState]. */
    private fun updateFireEffect(deltaTime: Double): FireState {
        updateAboveKeyParticles(deltaTime)
        // Calculate fire animation state
        return FireState(0.0, 0.0, 1.0)
    }

    /** Returns the current fire emitter state anchored at the bottom of the piano scene. */
    private fun calculateFireState(): FireState {
        // Simplified fire state calculation
        return FireState(0.0, PIANO_BACKGROUND_HEIGHT - 5.0, 1.0)
    }

    /**
     * Advances falling and rising note blocks by [deltaTime] seconds using [cfg]'s
     * look-ahead-derived scroll rate, and prunes blocks that have left the canvas.
     */
    private fun updateNoteBlocks(deltaTime: Double, nowMs: Long, cfg: NoteBlockConfig) {
        val lookAhead = cfg.lookAheadSeconds.coerceAtLeast(0.1)
        val pxPerSecond = PIANO_BACKGROUND_HEIGHT / lookAhead

        // Falling blocks: purely time-driven; nothing to mutate per frame, but drop
        // blocks whose top has scrolled off the bottom of the canvas.
        val beforeCount = fallingBlocks.size
        fallingBlocks.removeAll { block ->
            val elapsedSincePlan = (nowMs - block.landTimestampMs) / 1000.0
            val bottomY = PIANO_BACKGROUND_HEIGHT + elapsedSincePlan * pxPerSecond
            val topY = bottomY - block.durationMs / 1000.0 * pxPerSecond
            topY > PIANO_BACKGROUND_HEIGHT
        }
        if (beforeCount > 0 && fallingBlocks.size != beforeCount) {
            logger.debug(
                "[FALLING] pruned {} block(s), {} remaining",
                beforeCount - fallingBlocks.size,
                fallingBlocks.size
            )
        }
        // Periodic heartbeat when blocks are active, throttled to once per ~500ms.
        if (fallingBlocks.isNotEmpty() && nowMs - lastFallingHeartbeatMs > 500L) {
            lastFallingHeartbeatMs = nowMs
            val sample = fallingBlocks.first()
            val bottomY = PIANO_BACKGROUND_HEIGHT + (nowMs - sample.landTimestampMs) / 1000.0 * pxPerSecond
            val topY = bottomY - sample.durationMs / 1000.0 * pxPerSecond
            logger.debug(
                "[FALLING] state: {} block(s); first: note={} land=+{}ms topY={} bottomY={} pxPerSec={}",
                fallingBlocks.size, sample.note, sample.landTimestampMs - nowMs,
                "%.1f".format(topY), "%.1f".format(bottomY), "%.1f".format(pxPerSecond)
            )
        }

        // Rising blocks: while open, height grows; while closed, block drifts upward.
        val riseIter = risingBlocks.iterator()
        while (riseIter.hasNext()) {
            val block = riseIter.next()
            if (block.isOpen) {
                val grow = deltaTime * pxPerSecond
                block.height += grow
                block.y = PIANO_BACKGROUND_HEIGHT - block.height
            } else {
                block.y -= deltaTime * pxPerSecond
                if (block.y + block.height < 0.0) {
                    riseIter.remove()
                }
            }
        }
    }


    // Helper classes
    /**
     * Mutable runtime state for a single explosion/fireworks particle.
     *
     * @property x        Current X position
     * @property y        Current Y position
     * @property velocity Current velocity vector (pixels/second)
     * @property color    Particle colour
     * @property lifespan Total lifespan in seconds
     * @property size     Particle diameter in pixels
     * @property age      Elapsed age in seconds (0 → lifespan)
     */
    data class ParticleInfo(
        var x: Double,
        var y: Double,
        var velocity: Point2D,
        val color: Color,
        val lifespan: Double,
        val size: Double,
        var age: Double = 0.0
    ) {
        fun toData() = ParticleData(x, y, color, size, (1.0 - (age / lifespan)).coerceIn(0.0, 1.0))
    }

    /**
     * Mutable runtime state for a single above-key smoke particle.
     *
     * @property x               Current X position
     * @property y               Computed Y position (baseY + wobble)
     * @property baseY           Anchor Y position near the top of the keyboard
     * @property size            Particle diameter in pixels
     * @property color           Current interpolated colour
     * @property opacity         Current opacity (0.0–1.0)
     * @property baseOpacity     Mid-point opacity around which [opacityPhase] oscillates
     * @property driftSpeed      Horizontal drift speed in pixels/second
     * @property phase           Current wobble phase angle (radians)
     * @property phaseSpeed      Wobble phase advance rate (radians/second)
     * @property wobbleAmplitude Maximum vertical wobble displacement in pixels
     * @property opacityPhase    Current opacity oscillation phase (radians)
     * @property opacitySpeed    Opacity phase advance rate (radians/second)
     */
    data class AboveKeyParticleInfo(
        var x: Double,
        var y: Double,
        val baseY: Double,
        val size: Double,
        var color: Color,
        var opacity: Double,
        val baseOpacity: Double,
        val driftSpeed: Double,
        var phase: Double,
        val phaseSpeed: Double,
        val wobbleAmplitude: Double,
        var opacityPhase: Double,
        val opacitySpeed: Double
    ) {
        fun toData() = AboveKeyParticleData(x, y, color, size, opacity)
    }

    /**
     * Mutable runtime tracking for a single piano key's press animation.
     *
     * @property note              The musical note this key represents
     * @property isPressed         Whether the key is currently held down
     * @property animationProgress Progress of the press animation (0.0 = start, 1.0 = complete)
     */
    data class KeyAnimationInfo(
        val note: Note,
        var isPressed: Boolean,
        var animationProgress: Double = 0.0
    ) {
        fun toState() = KeyState(isPressed, animationProgress)
    }

    private companion object {
        val logger: Logger = LogManager.getLogger(AnimationCalculator::class.java.name)
    }

    /**
     * Runtime state for a scheduled falling block. Position is derived every frame
     * from wall-clock delta to [landTimestampMs] so the block always lands accurately
     * regardless of frame jitter.
     */
    data class FallingBlockInfo(
        val note: Note,
        val landTimestampMs: Long,
        val durationMs: Long,
        val velocity: Int,
        val channel: Int,
        val x: Double,
        val width: Double
    ) {
        fun toData(nowMs: Long, cfg: NoteBlockConfig): NoteBlockData {
            val pxPerSecond = PIANO_BACKGROUND_HEIGHT / cfg.lookAheadSeconds.coerceAtLeast(0.1)
            val heightPx = durationMs / 1000.0 * pxPerSecond
            // Y-down canvas: bottom of block increases as time approaches landTimestampMs.
            //   nowMs << landTimestampMs → bottomY near 0 (top of canvas)
            //   nowMs == landTimestampMs → bottomY == PIANO_BACKGROUND_HEIGHT (top of keyboard)
            //   nowMs >  landTimestampMs → bottomY > PIANO_BACKGROUND_HEIGHT (off canvas)
            val elapsedSincePlan = (nowMs - landTimestampMs) / 1000.0
            val bottomY = PIANO_BACKGROUND_HEIGHT + elapsedSincePlan * pxPerSecond
            val topY = bottomY - heightPx
            return NoteBlockData(
                x = x,
                y = topY,
                width = width,
                height = heightPx,
                color = cfg.resolveColor(velocity, channel),
                opacity = cfg.opacity
            )
        }
    }

    /**
     * Runtime state for a rising block. While [isOpen] is true, [height] grows every
     * frame; once closed, the block drifts upward until it leaves the canvas.
     */
    data class RisingBlockInfo(
        val note: Note,
        val velocity: Int,
        val channel: Int,
        val x: Double,
        val width: Double,
        var y: Double,
        var height: Double,
        var isOpen: Boolean
    ) {
        fun toData(cfg: NoteBlockConfig): NoteBlockData = NoteBlockData(
            x = x,
            y = y,
            width = width,
            height = height,
            color = cfg.resolveColor(velocity, channel),
            opacity = cfg.opacity
        )
    }
}