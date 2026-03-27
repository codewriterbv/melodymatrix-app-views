package be.codewriter.melodymatrix.view.view.piano.animation

import be.codewriter.melodymatrix.view.view.piano.data.FireworksExplosionType
import javafx.geometry.Point2D
import javafx.scene.paint.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Generates fireworks-style particle bursts for the piano key-press effect.
 *
 * Produces a rising tail of particles followed by a burst at the apex.
 * The burst pattern is determined by [FireworksExplosionType] and can be classic,
 * ring, willow, chrysanthemum, palm, or crackle.
 *
 * @see AnimationCalculator
 * @see ExplosionGenerator
 * @see FireworksExplosionType
 */
class FireworksGenerator {

    /**
     * Generates all particles for a single fireworks event.
     *
     * @param x                     Horizontal launch origin in scene coordinates
     * @param y                     Vertical launch origin in scene coordinates
     * @param velocity               MIDI velocity (1–127); higher values launch the firework higher
     * @param radius                 Burst spread radius
     * @param color                  Base colour for the burst
     * @param particleCount          Number of burst particles at the apex
     * @param particleSize           Base diameter of each particle in pixels
     * @param randomColor            When true, uses a random cinematic palette for the burst
     * @param tailParticleCount      Number of trailing particles emitted during ascent
     * @param launchHeightMultiplier Multiplier applied to the launch height (0.6–2.8)
     * @param liftMultiplier         Multiplier applied to upward fireworks lift (0.6–1.8)
     * @param explosionType          The burst pattern style
     * @return A list of [AnimationCalculator.ParticleInfo] for both tail and burst particles
     */
    fun generate(
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
    ): List<AnimationCalculator.ParticleInfo> {
        val particles = mutableListOf<AnimationCalculator.ParticleInfo>()
        val clampedVelocity = velocity.coerceAtLeast(1)
        val launchFactor = launchHeightMultiplier.coerceIn(0.6, 2.8)
        val effectiveLiftMultiplier = liftMultiplier.coerceIn(0.6, 1.8)
        val burstY = (y - (100.0 + clampedVelocity * 1.6) * launchFactor).coerceAtLeast(40.0)

        repeat(tailParticleCount.coerceAtLeast(1)) {
            val progress = it.toDouble() / tailParticleCount.coerceAtLeast(1)
            particles.add(
                AnimationCalculator.ParticleInfo(
                    x = x + Random.nextDouble(-4.0, 4.0),
                    y = y - progress * (y - burstY),
                    velocity = Point2D(
                        Random.nextDouble(-10.0, 10.0),
                        -Random.nextDouble(50.0, 130.0) * effectiveLiftMultiplier
                    ),
                    color = resolveColor(color, randomColor),
                    lifespan = Random.nextDouble(0.2, 0.6),
                    size = (particleSize * 0.7).coerceAtLeast(1.0)
                )
            )
        }

        repeat(particleCount.coerceAtLeast(1)) { idx ->
            val angle = when (explosionType) {
                FireworksExplosionType.CLASSIC -> Random.nextDouble(0.0, Math.PI * 2)
                FireworksExplosionType.RING -> (idx.toDouble() / particleCount.coerceAtLeast(1)
                    .toDouble()) * Math.PI * 2

                FireworksExplosionType.WILLOW -> Random.nextDouble(-Math.PI * 0.9, -Math.PI * 0.1)
                FireworksExplosionType.CHRYSANTHEMUM -> Random.nextDouble(0.0, Math.PI * 2)
                FireworksExplosionType.PALM -> Random.nextDouble(-Math.PI * 0.72, -Math.PI * 0.28)
                FireworksExplosionType.CRACKLE -> Random.nextDouble(0.0, Math.PI * 2)
            }
            val speed = when (explosionType) {
                FireworksExplosionType.CLASSIC -> Random.nextDouble(
                    radius * 7.0,
                    radius * 13.0
                ) * (0.30 + clampedVelocity / 127.0)

                FireworksExplosionType.RING -> Random.nextDouble(
                    radius * 8.0,
                    radius * 11.5
                ) * (0.30 + clampedVelocity / 127.0)

                FireworksExplosionType.WILLOW -> Random.nextDouble(
                    radius * 4.0,
                    radius * 8.5
                ) * (0.30 + clampedVelocity / 127.0)

                FireworksExplosionType.CHRYSANTHEMUM -> Random.nextDouble(
                    radius * 6.5,
                    radius * 12.5
                ) * (0.34 + clampedVelocity / 127.0)

                FireworksExplosionType.PALM -> Random.nextDouble(
                    radius * 5.0,
                    radius * 9.2
                ) * (0.30 + clampedVelocity / 127.0)

                FireworksExplosionType.CRACKLE -> Random.nextDouble(
                    radius * 8.8,
                    radius * 15.2
                ) * (0.36 + clampedVelocity / 127.0)
            }
            val velocityY = when (explosionType) {
                FireworksExplosionType.CLASSIC -> sin(angle) * speed
                FireworksExplosionType.RING -> sin(angle) * speed * 0.6
                FireworksExplosionType.WILLOW -> sin(angle) * speed - Random.nextDouble(12.0, 42.0)
                FireworksExplosionType.CHRYSANTHEMUM -> sin(angle) * speed * Random.nextDouble(0.88, 1.08)
                FireworksExplosionType.PALM -> sin(angle) * speed - Random.nextDouble(30.0, 78.0)
                FireworksExplosionType.CRACKLE -> sin(angle) * speed * Random.nextDouble(0.78, 1.25)
            } * effectiveLiftMultiplier
            particles.add(
                AnimationCalculator.ParticleInfo(
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
                    particles.add(
                        AnimationCalculator.ParticleInfo(
                            x = x + Random.nextDouble(-3.0, 3.0),
                            y = burstY + Random.nextDouble(-3.0, 3.0),
                            velocity = Point2D(cos(fragmentAngle) * fragmentSpeed, sin(fragmentAngle) * fragmentSpeed),
                            color = blendToWhite(crackleBaseColor, Random.nextDouble(0.28, 0.58)),
                            lifespan = Random.nextDouble(0.12, 0.32),
                            size = (particleSize * Random.nextDouble(0.25, 0.55)).coerceAtLeast(0.7)
                        )
                    )
                }
            }
        }

        return particles
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

    private fun blendToWhite(color: Color, factor: Double): Color {
        val clampedFactor = factor.coerceIn(0.0, 1.0)
        return Color.color(
            color.red + (1.0 - color.red) * clampedFactor,
            color.green + (1.0 - color.green) * clampedFactor,
            color.blue + (1.0 - color.blue) * clampedFactor
        )
    }
}

