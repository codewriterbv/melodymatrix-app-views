package be.codewriter.melodymatrix.view.definition

/**
 * Enumeration of chord alterations.
 *
 * Describes alterations to the basic chord structure, such as flatting or sharping
 * the fifth of the chord. Combined with chord quality and extension, this fully defines a chord.
 *
 * @property label A human-readable label for the chord alteration
 *
 * @see Chord
 * @see ChordQuality
 * @see ChordExtension
 */
enum class ChordAlteration(val label: String) {
    /** No alteration to the chord */
    NONE(""),

    /** Flat fifth (diminished fifth) */
    FLAT_FIFTH("b5"),

    /** Sharp fifth (augmented fifth) */
    SHARP_FIFTH("#5")
}

