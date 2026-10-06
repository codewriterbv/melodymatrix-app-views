package be.codewriter.melodymatrix.view.color

import be.codewriter.melodymatrix.view.definition.MainNote
import be.codewriter.melodymatrix.view.definition.Octave
import be.codewriter.melodymatrix.view.definition.RelationshipType
import javafx.scene.paint.Color
import javafx.scene.paint.Stop

/*
 * JavaFX colours for the (JavaFX-free) domain model. The model only stores colour values;
 * these extensions turn them into JavaFX paint, cached per enum constant.
 */

private val chartColors = MainNote.entries.associateWith { Color.web(it.chartColorHex) }
private val labelColors = MainNote.entries.associateWith { Color.web(it.labelColorHex) }
private val relationshipColors = RelationshipType.entries.associateWith { Color.web(it.colorHex) }
private val octaveStops = Octave.entries.associateWith { octave ->
    val rgb = octave.rgb ?: return@associateWith emptyArray<Stop>()
    val r = (rgb shr 16) and 0xFF
    val g = (rgb shr 8) and 0xFF
    val b = rgb and 0xFF
    arrayOf(
        Stop(0.0, Color.rgb(r, g, b, 0.2)),
        Stop(0.5, Color.rgb(r, g, b, 0.5)),
        Stop(1.0, Color.rgb(r, g, b, 0.8))
    )
}

/** The colour used to represent this note in charts. */
val MainNote.chartColor: Color get() = chartColors.getValue(this)

/** The colour of this note's label text on the piano keyboard. */
val MainNote.labelColor: Color get() = labelColors.getValue(this)

/** The colour used for the arrow and badge of this chord relationship. */
val RelationshipType.color: Color get() = relationshipColors.getValue(this)

/** Gradient stops (20%, 50%, 80% opacity of the octave colour) used for this octave in charts. */
val Octave.gradientStops: Array<Stop> get() = octaveStops.getValue(this)
