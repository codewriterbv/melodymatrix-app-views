package be.codewriter.melodymatrix.view.stage.piano.animation

import javafx.geometry.Point2D
import javafx.scene.paint.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Generates radial explosion particle bursts for the piano key-press effect.
 *
 * Produces three layers of particles — a dense core, fast sparks, and a diffuse mist —
 * whose sizes, speeds and directions are scaled by MIDI velocity so louder notes
 * produce more dramatic explosions.
 *
 * @see AnimationCalculator
 * @see FireworksGenerator
 */
class ExplosionGenerator {

    /**
     * Generates a list of particles for a single explosion event.
     *
     * @param x            Horizontal origin of the explosion in scene coordinates
     * @param y            Vertical origin of the explosion in scene coordinates
     * @param velocity     MIDI velocity value (1–127); higher values produce stronger bursts
     * @param radius       Base spread radius for the explosion
     * @param color        Base colour; may be randomised if [randomColor] is true
     * @param particleCount Total number of particles to generate
     * @param particleSize  Base diameter of each particle in pixels
     * @param tailParticleCount Number of trailing particles emitted upward from the key
     * @param liftMultiplier Multiplier for vertical lift strength (higher = taller bursts)
     * @param randomColor   When true, selects from a cinematic palette instead of [color]
     * @return A list of [AnimationCalculator.ParticleInfo] representing the explosion particles
     */
    fun generate(
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
    ): List<AnimationCalculator.ParticleInfo> {
        val particles = mutableListOf<AnimationCalculator.ParticleInfo>()
        val clampedVelocity = velocity.coerceAtLeast(1)
        val intensity = clampedVelocity / 127.0
        val effectiveCount = particleCount.coerceAtLeast(1)
        val effectiveTailCount = tailParticleCount.coerceAtLeast(0)
        val effectiveRadius = radius.coerceAtLeast(1.0)
        val baseSize = particleSize.coerceAtLeast(1.0)
        val palette = buildExplosionPalette(color, randomColor)
        val effectiveLiftMultiplier = liftMultiplier.coerceIn(0.6, 1.8)
        // Make radius visibly affect horizontal coverage in addition to speed.
        val spreadBoost = (0.45 + effectiveRadius / 4.5).coerceIn(0.8, 2.6)
        // Midpoint retune between the previous and current formulas.
        val baseLift = 1.0 + intensity * 3.9
        // Lower threshold from 0.55 → 0.30 so the epic boost activates around
        // velocity ~38 (below the real-world average of 56) instead of velocity ~70.
        val epicPhase = ((intensity - 0.30) / 0.70).coerceIn(0.0, 1.0)
        val epicBoost = 1.0 + (epicPhase * epicPhase) * 1.75
        val velocityLift = baseLift * epicBoost * effectiveLiftMultiplier

        val coreCount = (effectiveCount * 0.45).toInt().coerceAtLeast(1)
        val sparkCount = (effectiveCount * 0.30).toInt().coerceAtLeast(1)
        val mistCount = (effectiveCount - coreCount - sparkCount).coerceAtLeast(1)

        repeat(coreCount) {
            val speed = Random.nextDouble(effectiveRadius * 1.6, effectiveRadius * 4.4) * (0.50 + intensity * 0.70)
            val angle = Random.nextDouble(-Math.PI * 0.85, -Math.PI * 0.15)
            particles.add(
                AnimationCalculator.ParticleInfo(
                    x = x + Random.nextDouble(-3.0, 3.0),
                    y = y + Random.nextDouble(-2.0, 2.0),
                    velocity = Point2D(
                        cos(angle) * speed * spreadBoost,
                        sin(angle) * speed - Random.nextDouble(37.0, 68.0) * (0.975 + velocityLift * 1.325)
                    ),
                    color = blend(palette.random(), Color.WHITE, Random.nextDouble(0.35, 0.62)),
                    lifespan = Random.nextDouble(0.28, 0.58) + intensity * 0.18,
                    size = baseSize * Random.nextDouble(1.25, 1.95)
                )
            )
        }

        repeat(sparkCount) {
            val speed = Random.nextDouble(effectiveRadius * 4.8, effectiveRadius * 10.8) * (0.52 + intensity * 0.95)
            val angle = Random.nextDouble(-Math.PI * 0.72, -Math.PI * 0.28)
            particles.add(
                AnimationCalculator.ParticleInfo(
                    x = x,
                    y = y,
                    velocity = Point2D(
                        cos(angle) * speed * spreadBoost,
                        sin(angle) * speed - Random.nextDouble(21.0, 55.0) * (0.9 + velocityLift * 1.675)
                    ),
                    color = jitterColor(palette.random()),
                    lifespan = Random.nextDouble(0.42, 0.88) + intensity * 0.22,
                    size = baseSize * Random.nextDouble(0.62, 1.12)
                )
            )
        }

        repeat(mistCount) {
            val speed = Random.nextDouble(effectiveRadius * 1.1, effectiveRadius * 3.5) * (0.36 + intensity * 0.52)
            val angle = Random.nextDouble(-Math.PI * 0.92, -Math.PI * 0.08)
            particles.add(
                AnimationCalculator.ParticleInfo(
                    x = x + Random.nextDouble(-7.0, 7.0),
                    y = y + Random.nextDouble(-4.0, 2.0),
                    velocity = Point2D(
                        cos(angle) * speed * spreadBoost,
                        sin(angle) * speed - Random.nextDouble(29.0, 52.0) * (0.9 + velocityLift * 1.275)
                    ),
                    color = blend(palette.first(), palette.last(), Random.nextDouble(0.18, 0.78)),
                    lifespan = Random.nextDouble(0.72, 1.35) + intensity * 0.28,
                    size = baseSize * Random.nextDouble(1.45, 2.5)
                )
            )
        }

        repeat(effectiveTailCount) {
            val tailXOffset = Random.nextDouble(-effectiveRadius * 2.8, effectiveRadius * 2.8)
            val driftX = Random.nextDouble(-18.0, 18.0) * spreadBoost
            val upward = Random.nextDouble(42.0, 78.0) * (0.7 + velocityLift * 0.9)
            particles.add(
                AnimationCalculator.ParticleInfo(
                    x = x + tailXOffset,
                    y = y + Random.nextDouble(-4.0, 4.0),
                    velocity = Point2D(driftX, -upward),
                    color = blend(palette.random(), Color.WHITE, Random.nextDouble(0.22, 0.50)),
                    lifespan = Random.nextDouble(0.24, 0.58) + intensity * 0.12,
                    size = baseSize * Random.nextDouble(0.45, 0.90)
                )
            )
        }

        return particles
    }

    /**
     * Builds the colour palette used for explosion particles.
     *
     * @param baseColor   The user-configured particle colour
     * @param randomColor When true, picks a random cinematic palette and tints it with [baseColor]
     * @return A list of two or three [Color] values used to colour individual particles
     */
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

    /**
     * Adds a small random offset to each RGB channel of [color].
     *
     * @param color The colour to jitter
     * @return A new [Color] with each channel shifted by ±0.09
     */
    private fun jitterColor(color: Color): Color {
        val amount = 0.09
        fun jitter(value: Double): Double {
            return (value + Random.nextDouble(-amount, amount)).coerceIn(0.0, 1.0)
        }
        return Color.color(jitter(color.red), jitter(color.green), jitter(color.blue))
    }

    /**
     * Linearly interpolates between two colours.
     *
     * @param startColor The colour at factor 0.0
     * @param endColor   The colour at factor 1.0
     * @param factor     Blend factor clamped to [0.0, 1.0]
     * @return The blended [Color]
     */
    private fun blend(startColor: Color, endColor: Color, factor: Double): Color {
        val clampedFactor = factor.coerceIn(0.0, 1.0)
        return Color.color(
            startColor.red + (endColor.red - startColor.red) * clampedFactor,
            startColor.green + (endColor.green - startColor.green) * clampedFactor,
            startColor.blue + (endColor.blue - startColor.blue) * clampedFactor
        )
    }
}

