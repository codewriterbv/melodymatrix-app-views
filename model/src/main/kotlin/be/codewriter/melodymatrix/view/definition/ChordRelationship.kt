package be.codewriter.melodymatrix.view.definition

/**
 * Classifies the functional/harmonic relationship between two chords.
 *
 * Each type carries a short Roman-numeral [shortLabel] shown on the connecting arrow,
 * a human-readable [description], and a distinct [colorHex] used for both the arrow line
 * and the relationship badge on the target node.
 */
enum class RelationshipType(
    val shortLabel: String,
    val description: String,
    /** CSS-style hex colour (#RRGGBB), turned into a JavaFX colour by the views module. */
    val colorHex: String
) {
    /** Perfect-fifth dominant resolution (V→I or V→i). The strongest harmonic pull. */
    DOMINANT("V", "Dominant", "#60A5FA"),          // blue

    /** Major dominant used in minor context (raised VII → harmonic minor). */
    MAJOR_DOMINANT("V", "Major Dominant", "#818CF8"),   // indigo

    /** Natural (minor) dominant – diatonic to natural/aeolian minor. */
    MINOR_DOMINANT("v", "Natural Dominant", "#A78BFA"), // violet

    /** Perfect-fourth subdominant (IV or iv). */
    SUBDOMINANT("IV", "Subdominant", "#34D399"),        // emerald

    /** Minor subdominant (iv in major or borrowed). */
    MINOR_SUBDOMINANT("iv", "Minor Subdominant", "#6EE7B7"), // teal

    /** Relative minor (vi) – shares notes with the major tonic. */
    RELATIVE_MINOR("vi", "Relative Minor", "#FBBF24"), // amber

    /** Relative major (♭III) – shares notes with the minor tonic. */
    RELATIVE_MAJOR("♭III", "Relative Major", "#F59E0B"), // amber-darker

    /** Same root, opposite quality (major↔minor). */
    PARALLEL("par.", "Parallel", "#94A3B8"),             // slate

    /** Supertonic minor (ii) – common pre-dominant chord. */
    SUPERTONIC("ii", "Supertonic", "#C084FC"),          // purple

    /** Supertonic diminished (ii°) – diatonic to natural minor. */
    SUPERTONIC_DIM("ii°", "Supertonic dim.", "#E879F9"), // fuchsia

    /** Leading-tone diminished triad (vii°) – tension before tonic. */
    LEADING_TONE("vii°", "Leading Tone", "#F87171"),    // red

    /** Subtonic major (♭VII) – common in rock/modal contexts. */
    FLAT_SEVENTH("♭VII", "Subtonic", "#4ADE80"),        // green

    /** Flat-sixth major (♭VI) – borrowed from parallel minor. */
    FLAT_SIXTH("♭VI", "Flat 6th", "#2DD4BF"),          // cyan
}

