package be.codewriter.melodymatrix.view.view.piano.data

/**
 * Enumeration of fireworks explosion styles for the piano visualizer effect.
 *
 * Each value corresponds to a distinct particle burst pattern that can be triggered
 * when a piano key is pressed.
 *
 * @see PianoConfiguration
 */
enum class FireworksExplosionType {
    /** Classic symmetrical radial burst */
    CLASSIC,

    /** Particles spread in a circular ring pattern */
    RING,

    /** Drooping branches falling downward like a willow tree */
    WILLOW,

    /** Dense spherical burst resembling a chrysanthemum flower */
    CHRYSANTHEMUM,

    /** Rising stems that burst outward at the top like a palm tree */
    PALM,

    /** Random crackling sparks */
    CRACKLE
}

