package be.codewriter.melodymatrix.view.view.piano.animation

import javafx.geometry.Point2D
import javafx.scene.paint.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Generates a swirling cloud of fine particles that rise from the key-press origin.
 *
 * Unlike [FireworksGenerator], there is no launch arc or apex burst: particles spawn
 * directly at the key level and drift upward with a tangential spin component, creating
 * a nebula/dust-cloud appearance.
 *
 * @see AnimationCalculator
 */
class ParticlesGenerator {

    /**
     * @param x            Horizontal origin in scene coordinates (key centre)
     * @param y            Vertical origin in scene coordinates (top of keyboard)
     * @param velocity     MIDI velocity (1–127); scales cloud height
     * @param color        Base colour; ignored when [randomColor] is true
     * @param particleCount Total particles to spawn
     * @param particleSize  Base diameter; actual size is 10–28 % of this value
     * @param randomColor   When true, each particle gets a random bright colour
     * @param spreadRadius  Horizontal radius of the spawn zone around [x]
     * @param upSpeed       Base upward speed in px/s; actual speed varies ±40 %
     * @param swirlSpeed    Tangential speed in px/s that creates the rotation
     * @param liftMultiplier Extra vertical lift multiplier (0.6–1.8)
     */
    fun generate(
        x: Double,
        y: Double,
        velocity: Int,
        color: Color,
        particleCount: Int,
        particleSize: Double,
        randomColor: Boolean,
        spreadRadius: Double,
        upSpeed: Double,
        swirlSpeed: Double,
        liftMultiplier: Double
    ): List<AnimationCalculator.ParticleInfo> {
        val particles = mutableListOf<AnimationCalculator.ParticleInfo>()
        val clampedVelocity = velocity.coerceAtLeast(1)
        val effectiveLift = liftMultiplier.coerceIn(0.6, 1.8)
        // Harder key presses → taller cloud (0.35 at velocity 1, ≈ 0.95 at velocity 127).
        val velocityFactor = 0.35 + clampedVelocity / 210.0
        // Randomise swirl direction per note so consecutive notes alternate CW / CCW.
        val swirlDir = if (Random.nextBoolean()) 1.0 else -1.0

        repeat(particleCount.coerceAtLeast(1)) {
            val spawnAngle = Random.nextDouble(0.0, Math.PI * 2)
            val spawnRadius = Random.nextDouble(0.0, spreadRadius)
            val effectiveSwirlSpeed = swirlSpeed * Random.nextDouble(0.6, 1.4)
            val effectiveUpSpeed = upSpeed * Random.nextDouble(0.6, 1.4) * effectiveLift * velocityFactor

            particles.add(
                AnimationCalculator.ParticleInfo(
                    x = x + cos(spawnAngle) * spawnRadius,
                    y = y - Random.nextDouble(0.0, 10.0),
                    velocity = Point2D(
                        // Tangential component (⊥ to spawn radius) creates the swirl.
                        -sin(spawnAngle) * effectiveSwirlSpeed * swirlDir + Random.nextDouble(-4.0, 4.0),
                        -effectiveUpSpeed
                    ),
                    color = resolveColor(color, randomColor),
                    lifespan = Random.nextDouble(2.2, 5.0),
                    size = (particleSize * Random.nextDouble(0.10, 0.28)).coerceAtLeast(0.4)
                )
            )
        }

        return particles
    }

    private fun resolveColor(baseColor: Color, randomColor: Boolean): Color {
        if (!randomColor) return baseColor
        return Color.color(
            Random.nextDouble(0.4, 1.0),
            Random.nextDouble(0.4, 1.0),
            Random.nextDouble(0.4, 1.0)
        )
    }
}
