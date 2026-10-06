package be.codewriter.melodymatrix.view.event

/**
 * A live audio-spectrum event carrying FFT magnitude bins computed from the audio input.
 *
 * [magnitudes] contains normalized magnitudes in the range 0..1, where index `i`
 * represents the frequency band centered around `i * binHz`. [sampleRate] is the
 * sample rate of the source audio in Hertz and [binHz] is the width of each FFT bin
 * in Hertz (i.e. `sampleRate / fftSize`).
 *
 * Consumers must treat [magnitudes] as read-only; if long-term storage is required
 * make a defensive copy. The producer creates a fresh array per event so retaining
 * the reference is safe within a single event handler invocation.
 *
 * @property magnitudes Normalized FFT bin magnitudes in the range 0..1
 * @property sampleRate The audio sample rate in Hz used to compute the spectrum
 * @property binHz      The width of each FFT bin in Hz (`sampleRate / fftSize`)
 * @property timestamp  The time when the spectrum event was generated
 *
 * @see MmxEvent
 */
class AudioSpectrumEvent(
    val magnitudes: FloatArray,
    val sampleRate: Float,
    val binHz: Float,
    override val timestamp: Long = System.currentTimeMillis()
) : MmxEvent {
    override val type: MmxEventType = MmxEventType.AUDIO_SPECTRUM
}
