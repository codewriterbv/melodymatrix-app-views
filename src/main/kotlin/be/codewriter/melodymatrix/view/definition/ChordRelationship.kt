package be.codewriter.melodymatrix.view.definition

import javafx.scene.paint.Color

/**
 * Classifies the functional/harmonic relationship between two chords.
 *
 * Each type carries a short Roman-numeral [shortLabel] shown on the connecting arrow,
 * a human-readable [description], and a distinct [color] used for both the arrow line
 * and the relationship badge on the target node.
 */
enum class RelationshipType(
    val shortLabel: String,
    val description: String,
    val color: Color
) {
    /** Perfect-fifth dominant resolution (V→I or V→i). The strongest harmonic pull. */
    DOMINANT("V", "Dominant", Color.web("#60A5FA")),          // blue

    /** Major dominant used in minor context (raised VII → harmonic minor). */
    MAJOR_DOMINANT("V", "Major Dominant", Color.web("#818CF8")),   // indigo

    /** Natural (minor) dominant – diatonic to natural/aeolian minor. */
    MINOR_DOMINANT("v", "Natural Dominant", Color.web("#A78BFA")), // violet

    /** Perfect-fourth subdominant (IV or iv). */
    SUBDOMINANT("IV", "Subdominant", Color.web("#34D399")),        // emerald

    /** Minor subdominant (iv in major or borrowed). */
    MINOR_SUBDOMINANT("iv", "Minor Subdominant", Color.web("#6EE7B7")), // teal

    /** Relative minor (vi) – shares notes with the major tonic. */
    RELATIVE_MINOR("vi", "Relative Minor", Color.web("#FBBF24")), // amber

    /** Relative major (♭III) – shares notes with the minor tonic. */
    RELATIVE_MAJOR("♭III", "Relative Major", Color.web("#F59E0B")), // amber-darker

    /** Same root, opposite quality (major↔minor). */
    PARALLEL("par.", "Parallel", Color.web("#94A3B8")),             // slate

    /** Supertonic minor (ii) – common pre-dominant chord. */
    SUPERTONIC("ii", "Supertonic", Color.web("#C084FC")),          // purple

    /** Supertonic diminished (ii°) – diatonic to natural minor. */
    SUPERTONIC_DIM("ii°", "Supertonic dim.", Color.web("#E879F9")), // fuchsia

    /** Leading-tone diminished triad (vii°) – tension before tonic. */
    LEADING_TONE("vii°", "Leading Tone", Color.web("#F87171")),    // red

    /** Subtonic major (♭VII) – common in rock/modal contexts. */
    FLAT_SEVENTH("♭VII", "Subtonic", Color.web("#4ADE80")),        // green

    /** Flat-sixth major (♭VI) – borrowed from parallel minor. */
    FLAT_SIXTH("♭VI", "Flat 6th", Color.web("#2DD4BF")),          // cyan
}

