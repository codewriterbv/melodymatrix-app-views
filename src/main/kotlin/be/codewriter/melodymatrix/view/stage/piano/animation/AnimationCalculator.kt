package be.codewriter.melodymatrix.view.stage.piano.animation

import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.stage.piano.data.FireworksExplosionType
import be.codewriter.melodymatrix.view.stage.piano.PianoStage.Companion.PIANO_BACKGROUND_HEIGHT
import be.codewriter.melodymatrix.view.stage.piano.PianoStage.Companion.PIANO_WIDTH
import javafx.geometry.Point2D
import javafx.scene.paint.Color
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class AnimationCalculator(
    private val updateCallback: (AnimationState) -> Unit
) {
    private data class AboveKeyEffectConfig(
        val enabled: Boolean = true,
        val startColor: Color = Color.rgb(255, 0, 0),
        val endColor: Color = Color.rgb(0, 0, 2)
    )

    private val executor: ScheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor { task ->
            Thread.ofVirtual().name("animation-calculator").unstarted(task)
        }

    private val isRunning = AtomicBoolean(false)
    private val stateLock = Any()
    private var lastUpdateTime = System.nanoTime()

    // State tracking
    private val activeParticles = mutableListOf<ParticleInfo>()
    private val aboveKeyParticles = mutableListOf<AboveKeyParticleInfo>()
    private val activeKeys = mutableMapOf<Note, KeyAnimationInfo>()
    private var aboveKeyConfig = AboveKeyEffectConfig()

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

    fun stop() {
        if (isRunning.compareAndSet(true, false)) {
            executor.shutdown()
        }
    }

    private fun calculateFrame() {
        val currentTime = System.nanoTime()
        val deltaTime = (currentTime - lastUpdateTime) / 1_000_000_000.0 // Convert to seconds
        lastUpdateTime = currentTime

        val state = synchronized(stateLock) {
            // Perform heavy calculations here
            updateParticles(deltaTime)
            updateKeyAnimations(deltaTime)
            updateFireEffect(deltaTime)

            // Create immutable state snapshot
            AnimationState(
                timestamp = currentTime,
                particlePositions = activeParticles.map { it.toData() },
                aboveKeyParticles = aboveKeyParticles.map { it.toData() },
                fireEmitterState = calculateFireState(),
                keyStates = activeKeys.mapValues { it.value.toState() }
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

    fun updateKeyPress(note: Note, isPressed: Boolean) {
        synchronized(stateLock) {
            if (isPressed) {
                activeKeys[note] = KeyAnimationInfo(note, isPressed = true)
            } else {
                activeKeys[note]?.isPressed = false
            }
        }
    }

    fun updateAboveKeyEffect(enabled: Boolean, startColor: Color, endColor: Color) {
        synchronized(stateLock) {
            val previousEnabled = aboveKeyConfig.enabled
            val previousStart = aboveKeyConfig.startColor
            val previousEnd = aboveKeyConfig.endColor

            aboveKeyConfig = AboveKeyEffectConfig(enabled, startColor, endColor)

            if (!enabled) {
                aboveKeyParticles.clear()
            } else if (!previousEnabled || previousStart != startColor || previousEnd != endColor) {
                seedAboveKeyParticles()
            }
        }
    }

    fun addExplosion(
        x: Double,
        y: Double,
        velocity: Int,
        radius: Double,
        color: Color,
        particleCount: Int,
        particleSize: Double,
        randomColor: Boolean
    ) {
        synchronized(stateLock) {
            val clampedVelocity = velocity.coerceAtLeast(1)
            val intensity = clampedVelocity / 127.0
            val effectiveCount = particleCount.coerceAtLeast(1)
            val effectiveRadius = radius.coerceAtLeast(1.0)
            val baseSize = particleSize.coerceAtLeast(1.0)
            val palette = buildExplosionPalette(color, randomColor)
            val baseLift = 1.0 + (intensity * intensity) * 2.8
            val epicPhase = ((intensity - 0.55) / 0.45).coerceIn(0.0, 1.0)
            val epicBoost = 1.0 + (epicPhase * epicPhase) * 1.2
            val velocityLift = baseLift * epicBoost

            val coreCount = (effectiveCount * 0.45).toInt().coerceAtLeast(1)
            val sparkCount = (effectiveCount * 0.30).toInt().coerceAtLeast(1)
            val mistCount = (effectiveCount - coreCount - sparkCount).coerceAtLeast(1)

            // Bright center bloom: short-lived, larger particles near the key.
            repeat(coreCount) {
                val speed = Random.nextDouble(effectiveRadius * 1.6, effectiveRadius * 4.4) * (0.50 + intensity * 0.70)
                val angle = Random.nextDouble(-Math.PI * 0.85, -Math.PI * 0.15)
                activeParticles.add(
                    ParticleInfo(
                        x = x + Random.nextDouble(-3.0, 3.0),
                        y = y + Random.nextDouble(-2.0, 2.0),
                        velocity = Point2D(
                            cos(angle) * speed,
                            sin(angle) * speed - Random.nextDouble(24.0, 46.0) * (0.95 + velocityLift * 0.85)
                        ),
                        color = blend(palette.random(), Color.WHITE, Random.nextDouble(0.35, 0.62)),
                        lifespan = Random.nextDouble(0.28, 0.58) + intensity * 0.18,
                        size = baseSize * Random.nextDouble(1.25, 1.95)
                    )
                )
            }

            // Directional sparks: narrower upward cone for cleaner cinematic arcs.
            repeat(sparkCount) {
                val speed = Random.nextDouble(effectiveRadius * 4.8, effectiveRadius * 10.8) * (0.52 + intensity * 0.95)
                val angle = Random.nextDouble(-Math.PI * 0.72, -Math.PI * 0.28)
                activeParticles.add(
                    ParticleInfo(
                        x = x,
                        y = y,
                        velocity = Point2D(
                            cos(angle) * speed,
                            sin(angle) * speed - Random.nextDouble(12.0, 40.0) * (0.9 + velocityLift * 1.15)
                        ),
                        color = jitterColor(palette.random()),
                        lifespan = Random.nextDouble(0.42, 0.88) + intensity * 0.22,
                        size = baseSize * Random.nextDouble(0.62, 1.12)
                    )
                )
            }

            // Soft mist layer: slower and larger particles to fake blur with circle rendering.
            repeat(mistCount) {
                val speed = Random.nextDouble(effectiveRadius * 1.1, effectiveRadius * 3.5) * (0.36 + intensity * 0.52)
                val angle = Random.nextDouble(-Math.PI * 0.92, -Math.PI * 0.08)
                activeParticles.add(
                    ParticleInfo(
                        x = x + Random.nextDouble(-7.0, 7.0),
                        y = y + Random.nextDouble(-4.0, 2.0),
                        velocity = Point2D(
                            cos(angle) * speed,
                            sin(angle) * speed - Random.nextDouble(18.0, 34.0) * (0.9 + velocityLift * 0.95)
                        ),
                        color = blend(palette.first(), palette.last(), Random.nextDouble(0.18, 0.78)),
                        lifespan = Random.nextDouble(0.72, 1.35) + intensity * 0.28,
                        size = baseSize * Random.nextDouble(1.45, 2.5)
                    )
                )
            }
        }
    }

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
        explosionType: FireworksExplosionType
    ) {
        synchronized(stateLock) {
            val clampedVelocity = velocity.coerceAtLeast(1)
            val launchFactor = launchHeightMultiplier.coerceIn(0.6, 2.8)
            val burstY = (y - (100.0 + clampedVelocity * 1.6) * launchFactor).coerceAtLeast(40.0)

            repeat(tailParticleCount.coerceAtLeast(1)) {
                val progress = it.toDouble() / tailParticleCount.coerceAtLeast(1)
                activeParticles.add(
                    ParticleInfo(
                        x = x + Random.nextDouble(-4.0, 4.0),
                        y = y - progress * (y - burstY),
                        velocity = Point2D(Random.nextDouble(-10.0, 10.0), -Random.nextDouble(50.0, 130.0)),
                        color = resolveColor(color, randomColor),
                        lifespan = Random.nextDouble(0.2, 0.6),
                        size = (particleSize * 0.7).coerceAtLeast(1.0)
                    )
                )
            }

            repeat(particleCount.coerceAtLeast(1)) {
                val angle = when (explosionType) {
                    FireworksExplosionType.CLASSIC -> Random.nextDouble(0.0, Math.PI * 2)
                    FireworksExplosionType.RING -> (it.toDouble() / particleCount.coerceAtLeast(1).toDouble()) * Math.PI * 2
                    FireworksExplosionType.WILLOW -> Random.nextDouble(-Math.PI * 0.9, -Math.PI * 0.1)
                    FireworksExplosionType.CHRYSANTHEMUM -> Random.nextDouble(0.0, Math.PI * 2)
                    FireworksExplosionType.PALM -> Random.nextDouble(-Math.PI * 0.72, -Math.PI * 0.28)
                    FireworksExplosionType.CRACKLE -> Random.nextDouble(0.0, Math.PI * 2)
                }
                val speed = when (explosionType) {
                    FireworksExplosionType.CLASSIC -> Random.nextDouble(radius * 7.0, radius * 13.0) * (0.30 + clampedVelocity / 127.0)
                    FireworksExplosionType.RING -> Random.nextDouble(radius * 8.0, radius * 11.5) * (0.30 + clampedVelocity / 127.0)
                    FireworksExplosionType.WILLOW -> Random.nextDouble(radius * 4.0, radius * 8.5) * (0.30 + clampedVelocity / 127.0)
                    FireworksExplosionType.CHRYSANTHEMUM -> Random.nextDouble(radius * 6.5, radius * 12.5) * (0.34 + clampedVelocity / 127.0)
                    FireworksExplosionType.PALM -> Random.nextDouble(radius * 5.0, radius * 9.2) * (0.30 + clampedVelocity / 127.0)
                    FireworksExplosionType.CRACKLE -> Random.nextDouble(radius * 8.8, radius * 15.2) * (0.36 + clampedVelocity / 127.0)
                }
                val velocityY = when (explosionType) {
                    FireworksExplosionType.CLASSIC -> sin(angle) * speed
                    FireworksExplosionType.RING -> sin(angle) * speed * 0.6
                    FireworksExplosionType.WILLOW -> sin(angle) * speed - Random.nextDouble(12.0, 42.0)
                    FireworksExplosionType.CHRYSANTHEMUM -> sin(angle) * speed * Random.nextDouble(0.88, 1.08)
                    FireworksExplosionType.PALM -> sin(angle) * speed - Random.nextDouble(30.0, 78.0)
                    FireworksExplosionType.CRACKLE -> sin(angle) * speed * Random.nextDouble(0.78, 1.25)
                }
                activeParticles.add(
                    ParticleInfo(
                        x = x,
                        y = burstY,
                        velocity = Point2D(cos(angle) * speed, velocityY),
                        color = resolveColor(color, randomColor),
                        lifespan = when (explosionType) {
                            FireworksExplosionType.WILLOW -> Random.nextDouble(1.0, 2.0)
                            FireworksExplosionType.CHRYSANTHEMUM -> Random.nextDouble(0.95, 1.9)
                            FireworksExplosionType.PALM -> Random.nextDouble(1.25, 2.35)
                            FireworksExplosionType.CRACKLE -> Random.nextDouble(0.25, 0.62)
                            else -> Random.nextDouble(0.6, 1.6)
                        },
                        size = when (explosionType) {
                            FireworksExplosionType.CHRYSANTHEMUM -> particleSize * Random.nextDouble(0.95, 1.35)
                            FireworksExplosionType.PALM -> particleSize * Random.nextDouble(1.15, 1.65)
                            FireworksExplosionType.CRACKLE -> particleSize * Random.nextDouble(0.45, 0.85)
                            else -> particleSize
                        }
                    )
                )

                if (explosionType == FireworksExplosionType.CRACKLE && Random.nextDouble() < 0.42) {
                    val crackleBaseColor = resolveColor(color, randomColor)
                    repeat(2) {
                        val fragmentAngle = angle + Random.nextDouble(-0.45, 0.45)
                        val fragmentSpeed = speed * Random.nextDouble(0.32, 0.58)
                        activeParticles.add(
                            ParticleInfo(
                                x = x + Random.nextDouble(-3.0, 3.0),
                                y = burstY + Random.nextDouble(-3.0, 3.0),
                                velocity = Point2D(cos(fragmentAngle) * fragmentSpeed, sin(fragmentAngle) * fragmentSpeed),
                                color = blend(crackleBaseColor, Color.WHITE, Random.nextDouble(0.28, 0.58)),
                                lifespan = Random.nextDouble(0.12, 0.32),
                                size = (particleSize * Random.nextDouble(0.25, 0.55)).coerceAtLeast(0.7)
                            )
                        )
                    }
                }
            }
        }
    }

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

    private fun updateAboveKeyParticles(deltaTime: Double) {
        if (!aboveKeyConfig.enabled) {
            aboveKeyParticles.clear()
            return
        }

        if (aboveKeyParticles.isEmpty()) {
            seedAboveKeyParticles()
        }

        // Animate color transitions for smoke particles near played keys
        val activeKeyXs = activeKeys.filter { it.value.isPressed }.map { noteInfo ->
            // Map note to x position (approximate)
            val note = noteInfo.key
            val idx = note.ordinal // Assuming Note is enum
            val totalKeys = 88 // Standard piano
            val x = (idx.toDouble() / totalKeys) * PIANO_WIDTH
            x
        }

        aboveKeyParticles.forEach { particle ->
            particle.x += particle.driftSpeed * deltaTime
            particle.phase += deltaTime * particle.phaseSpeed
            particle.opacityPhase += deltaTime * particle.opacitySpeed

            if (particle.x < -particle.size) {
                particle.x = PIANO_WIDTH + particle.size * 0.5
            } else if (particle.x > PIANO_WIDTH + particle.size) {
                particle.x = -particle.size * 0.5
            }

            particle.y = particle.baseY + sin(particle.phase) * particle.wobbleAmplitude
            particle.opacity = (particle.baseOpacity + sin(particle.opacityPhase) * 0.035).coerceIn(0.04, 0.22)

            // If near a pressed key, animate color toward endColor
            val proximity = activeKeyXs.any { keyX -> Math.abs(particle.x - keyX) < 80.0 }
            if (proximity) {
                // Blend color toward endColor
                val blendFactor = 0.12
                particle.color = blend(particle.color, aboveKeyConfig.endColor, blendFactor)
            } else {
                // Slowly revert to startColor
                val blendFactor = 0.04
                particle.color = blend(particle.color, aboveKeyConfig.startColor, blendFactor)
            }
        }
    }

    private fun updateKeyAnimations(deltaTime: Double) {
        activeKeys.values.forEach { key ->
            key.animationProgress += deltaTime * 5.0 // Animation speed
            if (!key.isPressed && key.animationProgress >= 1.0) {
                key.animationProgress = 0.0
            }
        }
    }

    private fun updateFireEffect(deltaTime: Double): FireState {
        updateAboveKeyParticles(deltaTime)
        // Calculate fire animation state
        return FireState(0.0, 0.0, 1.0)
    }

    private fun calculateFireState(): FireState {
        // Simplified fire state calculation
        return FireState(0.0, PIANO_BACKGROUND_HEIGHT - 5.0, 1.0)
    }

    private fun resolveColor(baseColor: Color, randomColor: Boolean): Color {
        if (!randomColor) {
            return baseColor
        }

        return Color.color(
            Random.nextDouble(0.4, 1.0),
            Random.nextDouble(0.4, 1.0),
            Random.nextDouble(0.4, 1.0)
        )
    }

    private fun buildExplosionPalette(baseColor: Color, randomColor: Boolean): List<Color> {
        if (!randomColor) {
            return listOf(
                blend(baseColor, Color.WHITE, 0.55),
                baseColor,
                blend(baseColor, Color.color(0.0, 0.0, 0.0), 0.30)
            )
        }

        val cinematicPalettes = listOf(
            listOf(Color.rgb(66, 214, 255), Color.rgb(167, 102, 255), Color.rgb(255, 80, 176)),
            listOf(Color.rgb(74, 255, 185), Color.rgb(54, 170, 255), Color.rgb(147, 92, 255)),
            listOf(Color.rgb(255, 170, 76), Color.rgb(255, 92, 129), Color.rgb(182, 96, 255))
        )
        val palette = cinematicPalettes.random()
        return palette.map { blend(it, baseColor, 0.18) }
    }

    private fun jitterColor(color: Color): Color {
        val amount = 0.09
        fun jitter(value: Double): Double {
            return (value + Random.nextDouble(-amount, amount)).coerceIn(0.0, 1.0)
        }
        return Color.color(jitter(color.red), jitter(color.green), jitter(color.blue))
    }

    private fun seedAboveKeyParticles() {
        aboveKeyParticles.clear()
        // Increase particle count for denser smoke
        repeat(32) {
            aboveKeyParticles.add(createAboveKeyParticle(Random.nextDouble(0.0, PIANO_WIDTH)))
        }
    }

    private fun createAboveKeyParticle(initialX: Double): AboveKeyParticleInfo {
        val size = Random.nextDouble(100.0, 220.0) // Larger, more irregular
        return AboveKeyParticleInfo(
            x = initialX,
            y = 0.0,
            baseY = Random.nextDouble(PIANO_BACKGROUND_HEIGHT - 50.0, PIANO_BACKGROUND_HEIGHT - 15.0),
            size = size,
            color = blend(aboveKeyConfig.startColor, aboveKeyConfig.endColor, Random.nextDouble(0.0, 1.0)),
            opacity = Random.nextDouble(0.06, 0.18),
            baseOpacity = Random.nextDouble(0.06, 0.18),
            driftSpeed = Random.nextDouble(-22.0, 22.0),
            phase = Random.nextDouble(0.0, Math.PI * 2),
            phaseSpeed = Random.nextDouble(0.22, 0.55),
            wobbleAmplitude = Random.nextDouble(8.0, 22.0),
            opacityPhase = Random.nextDouble(0.0, Math.PI * 2),
            opacitySpeed = Random.nextDouble(0.18, 0.45)
        ).apply {
            y = baseY + sin(phase) * wobbleAmplitude
        }
    }

    private fun blend(startColor: Color, endColor: Color, factor: Double): Color {
        val clampedFactor = factor.coerceIn(0.0, 1.0)
        return Color.color(
            startColor.red + (endColor.red - startColor.red) * clampedFactor,
            startColor.green + (endColor.green - startColor.green) * clampedFactor,
            startColor.blue + (endColor.blue - startColor.blue) * clampedFactor
        )
    }

    // Helper classes
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

    data class KeyAnimationInfo(
        val note: Note,
        var isPressed: Boolean,
        var animationProgress: Double = 0.0
    ) {
        fun toState() = KeyState(isPressed, animationProgress)
    }
}