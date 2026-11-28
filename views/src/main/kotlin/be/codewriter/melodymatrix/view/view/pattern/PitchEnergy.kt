package be.codewriter.melodymatrix.view.view.pattern

import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

/**
 * Keeps one decaying energy value (0..1) for each of the 88 piano keys, like a spectrum analyser that
 * is fed by notes instead of by audio. A new note sets its key to the velocity; while the key is held
 * the energy only fades slowly, after the release it fades within the given release time.
 *
 * No JavaFX and no timing of its own: the caller passes the elapsed time to [step]. All methods are
 * synchronized because MIDI events and the animation timer run on different threads.
 */
class PitchEnergy {

    private val energy = DoubleArray(KEYS)
    private val held = BooleanArray(KEYS)

    @Synchronized
    fun noteOn(pitch: Int, velocity: Int) {
        val key = keyOf(pitch) ?: return
        if (velocity <= 0) return noteOff(pitch)
        energy[key] = max(energy[key], (velocity / MAX_VELOCITY).coerceIn(MIN_ENERGY, 1.0))
        held[key] = true
    }

    @Synchronized
    fun noteOff(pitch: Int) {
        val key = keyOf(pitch) ?: return
        held[key] = false
    }

    @Synchronized
    fun reset() {
        energy.fill(0.0)
        held.fill(false)
    }

    /** Lets all energies fade for [dt] seconds. Held keys sink slowly towards [HELD_FLOOR] of their level. */
    @Synchronized
    fun step(dt: Double, releaseSeconds: Double) {
        val release = exp(-dt / releaseSeconds.coerceAtLeast(MIN_RELEASE))
        val sustain = exp(-dt / HELD_DECAY_SECONDS)
        for (key in 0 until KEYS) {
            energy[key] = if (held[key]) max(energy[key] * sustain, HELD_FLOOR) else energy[key] * release
            if (energy[key] < EPSILON) energy[key] = 0.0
        }
    }

    /** Copies the 88 energies into [out]. */
    @Synchronized
    fun copyTo(out: DoubleArray) {
        energy.copyInto(out, 0, 0, min(KEYS, out.size))
    }

    /** `true` when no key is held and nothing is audible any more. */
    @Synchronized
    fun isSilent(): Boolean = energy.all { it == 0.0 }

    companion object {
        const val KEYS = 88
        const val LOWEST_PITCH = 21

        private const val MAX_VELOCITY = 127.0
        private const val MIN_ENERGY = 0.15
        private const val HELD_FLOOR = 0.35
        private const val HELD_DECAY_SECONDS = 3.0
        private const val MIN_RELEASE = 0.05
        private const val EPSILON = 0.002

        private fun keyOf(pitch: Int): Int? = (pitch - LOWEST_PITCH).takeIf { it in 0 until KEYS }

        /** Reduces [levels] to [count] columns, every column keeping the loudest key it covers. */
        fun pool(levels: DoubleArray, count: Int, out: DoubleArray) {
            out.fill(0.0, 0, min(count, out.size))
            for (key in levels.indices) {
                val column = min(count - 1, key * count / levels.size)
                out[column] = max(out[column], levels[key])
            }
        }

        /**
         * Lets every column share part of its energy with its neighbours, so a few notes still give a full shape.
         * [amount] 0 leaves the values as they are, 1 gives the widest spread.
         */
        fun spread(values: DoubleArray, count: Int, amount: Double, scratch: DoubleArray) {
            if (amount <= 0.0) return
            values.copyInto(scratch, 0, 0, count)
            for (i in 0 until count) {
                var v = scratch[i]
                if (i > 0) v = max(v, scratch[i - 1] * NEIGHBOUR_1 * amount)
                if (i < count - 1) v = max(v, scratch[i + 1] * NEIGHBOUR_1 * amount)
                if (i > 1) v = max(v, scratch[i - 2] * NEIGHBOUR_2 * amount)
                if (i < count - 2) v = max(v, scratch[i + 2] * NEIGHBOUR_2 * amount)
                values[i] = v
            }
        }

        private const val NEIGHBOUR_1 = 0.65
        private const val NEIGHBOUR_2 = 0.3
    }
}
