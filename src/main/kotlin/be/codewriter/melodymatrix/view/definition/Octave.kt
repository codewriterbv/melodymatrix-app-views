package be.codewriter.melodymatrix.view.definition

import javafx.scene.paint.Color
import javafx.scene.paint.Stop

enum class Octave(val octave: Int, val gradientStops: Array<Stop>) {
    UNDEFINED(0, arrayOf()),
    OCTAVE_0(
        0, arrayOf(
            Stop(0.0, Color.rgb(255, 0, 0, 0.2)),      // Dark Red
            Stop(0.5, Color.rgb(255, 0, 0, 0.5)),      // Medium Red
            Stop(1.0, Color.rgb(255, 0, 0, 0.8))       // Light Red
        )
    ),
    OCTAVE_1(
        1, arrayOf(
            Stop(0.0, Color.rgb(255, 127, 0, 0.2)),    // Dark Orange
            Stop(0.5, Color.rgb(255, 127, 0, 0.5)),    // Medium Orange
            Stop(1.0, Color.rgb(255, 127, 0, 0.8))     // Light Orange
        )
    ),
    OCTAVE_2(
        2, arrayOf(
            Stop(0.0, Color.rgb(255, 255, 0, 0.2)),    // Dark Yellow
            Stop(0.5, Color.rgb(255, 255, 0, 0.5)),    // Medium Yellow
            Stop(1.0, Color.rgb(255, 255, 0, 0.8))     // Light Yellow
        )
    ),
    OCTAVE_3(
        3, arrayOf(
            Stop(0.0, Color.rgb(0, 255, 0, 0.2)),      // Dark Green
            Stop(0.5, Color.rgb(0, 255, 0, 0.5)),      // Medium Green
            Stop(1.0, Color.rgb(0, 255, 0, 0.8))       // Light Green
        )
    ),
    OCTAVE_4(
        4, arrayOf(
            Stop(0.0, Color.rgb(0, 0, 255, 0.2)),      // Dark Blue
            Stop(0.5, Color.rgb(0, 0, 255, 0.5)),      // Medium Blue
            Stop(1.0, Color.rgb(0, 0, 255, 0.8))       // Light Blue
        )
    ),
    OCTAVE_5(
        5, arrayOf(
            Stop(0.0, Color.rgb(75, 0, 130, 0.2)),     // Dark Indigo
            Stop(0.5, Color.rgb(75, 0, 130, 0.5)),     // Medium Indigo
            Stop(1.0, Color.rgb(75, 0, 130, 0.8))      // Light Indigo
        )
    ),
    OCTAVE_6(
        6, arrayOf(
            Stop(0.0, Color.rgb(148, 0, 211, 0.2)),    // Dark Violet
            Stop(0.5, Color.rgb(148, 0, 211, 0.5)),    // Medium Violet
            Stop(1.0, Color.rgb(148, 0, 211, 0.8))     // Light Violet
        )
    ),
    OCTAVE_7(
        7, arrayOf(
            Stop(0.0, Color.rgb(255, 0, 0, 0.2)),      // Dark Red
            Stop(0.5, Color.rgb(255, 0, 0, 0.5)),      // Medium Red
            Stop(1.0, Color.rgb(255, 0, 0, 0.8))       // Light Red
        )
    ),
    OCTAVE_8(
        8, arrayOf(
            Stop(0.0, Color.rgb(255, 127, 0, 0.2)),    // Dark Orange
            Stop(0.5, Color.rgb(255, 127, 0, 0.5)),    // Medium Orange
            Stop(1.0, Color.rgb(255, 127, 0, 0.8))     // Light Orange
        )
    ),
    OCTAVE_9(
        9, arrayOf(
            Stop(0.0, Color.rgb(255, 255, 0, 0.2)),    // Dark Yellow
            Stop(0.5, Color.rgb(255, 255, 0, 0.5)),    // Medium Yellow
            Stop(1.0, Color.rgb(255, 255, 0, 0.8))     // Light Yellow
        )
    );
}