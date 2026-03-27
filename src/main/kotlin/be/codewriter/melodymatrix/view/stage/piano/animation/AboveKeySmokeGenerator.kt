package be.codewriter.melodymatrix.view.stage.piano.animation

import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.stage.piano.PianoView.Companion.PIANO_BACKGROUND_HEIGHT
import be.codewriter.melodymatrix.view.stage.piano.PianoView.Companion.PIANO_WIDTH
import javafx.scene.paint.Color
import kotlin.math.sin
import kotlin.random.Random

/**
 * Generates and updates the soft smoke/steam particles that drift above the piano keyboard.
 *
 * Particles slowly drift horizontally and wobble vertically. When a key is pressed, nearby
 * particles fade toward the end colour; otherwise they gradually return to the start colour.
 * The particle field is re-seeded whenever the effect is toggled on or the colours change.
 *
 * @see AnimationCalculator
 */
class AboveKeySmokeGenerator {
    /**
     * Immutable configuration snapshot for the smoke effect.
     *
     * @property enabled    Whether the effect is active
     * @property startColor Base particle colour (also the "idle" colour)
     * @property endColor   Colour particles shift toward when a nearby key is pressed
     */
    private data class SmokeConfig(
        val enabled: Boolean = true,
        val startColor: Color = Color.rgb(255, 0, 0),
        val endColor: Color = Color.rgb(0, 0, 2)
    )

    private var config = SmokeConfig()

    /**
     * Applies a new configuration, clearing or re-seeding the particle list as needed.
     *
     * @param enabled   Whether the effect should be active
     * @param startColor Base/idle particle colour
     * @param endColor   Colour particles shift toward near active keys
     * @param particles  The mutable particle list to update in place
     */
    fun updateConfig(
        enabled: Boolean,
        startColor: Color,
        endColor: Color,
        particles: MutableList<AnimationCalculator.AboveKeyParticleInfo>
    ) {
        val previousEnabled = config.enabled
        val previousStart = config.startColor
        val previousEnd = config.endColor

        config = SmokeConfig(enabled, startColor, endColor)

        if (!enabled) {
            particles.clear()
        } else if (!previousEnabled || previousStart != startColor || previousEnd != endColor) {
            seedParticles(particles)
        }
    }

    /**
     * Advances all particles by [deltaTime] seconds.
     *
     * Particles drift, wobble, and shift colour based on proximity to active keys.
     * If the particle list is empty it is re-seeded first.
     *
     * @param deltaTime   Elapsed time since last update in seconds
     * @param activeNotes The set of notes currently held down
     * @param particles   The mutable particle list to update in place
     */
    fun update(
        deltaTime: Double,
        activeNotes: Set<Note>,
        particles: MutableList<AnimationCalculator.AboveKeyParticleInfo>
    ) {
        if (!config.enabled) {
            particles.clear()
            return
        }

        if (particles.isEmpty()) {
            seedParticles(particles)
        }

        val activeKeyXs = activeNotes.map { note ->
            val idx = note.ordinal
            val totalKeys = 88
            (idx.toDouble() / totalKeys) * PIANO_WIDTH
        }

        particles.forEach { particle ->
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

            val nearActiveKey = activeKeyXs.any { keyX -> kotlin.math.abs(particle.x - keyX) < 80.0 }
            if (nearActiveKey) {
                particle.color = blend(particle.color, config.endColor, 0.12)
            } else {
                particle.color = blend(particle.color, config.startColor, 0.04)
            }
        }
    }

    /**
     * Populates the particle list with 32 randomly distributed particles spanning the full keyboard width.
     *
     * @param particles The list to populate; it is cleared before adding new particles
     */
    private fun seedParticles(particles: MutableList<AnimationCalculator.AboveKeyParticleInfo>) {
        particles.clear()
        repeat(32) {
            particles.add(createParticle(Random.nextDouble(0.0, PIANO_WIDTH)))
        }
    }

    /**
     * Creates a single smoke particle at the given X position with randomised properties.
     *
     * @param initialX Starting X position in scene coordinates
     * @return A fully initialised [AnimationCalculator.AboveKeyParticleInfo]
     */
    private fun createParticle(initialX: Double): AnimationCalculator.AboveKeyParticleInfo {
        val size = Random.nextDouble(100.0, 220.0)
        return AnimationCalculator.AboveKeyParticleInfo(
            x = initialX,
            y = 0.0,
            baseY = Random.nextDouble(PIANO_BACKGROUND_HEIGHT - 50.0, PIANO_BACKGROUND_HEIGHT - 15.0),
            size = size,
            color = blend(config.startColor, config.endColor, Random.nextDouble(0.0, 1.0)),
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

    /**
     * Linearly interpolates between two colours.
     *
     * @param startColor Colour at factor 0.0
     * @param endColor   Colour at factor 1.0
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

