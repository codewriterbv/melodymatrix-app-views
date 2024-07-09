package be.codewriter.melodymatrix.view.stage.ledstrip

import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.GridPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.stage.Stage
import javafx.util.Duration

class ColorBoxHighlight : Application() {

    companion object {
        const val NUM_BOXES = 98
        const val ROWS = 7
        const val COLS = 14
        const val BOX_SIZE = 30.0
        val BASE_COLOR = Color.LIGHTBLUE
        val HIGHLIGHT_COLOR = Color.YELLOW
        val FADE_DURATION = Duration.millis(500.0)
    }

    private val boxes = Array(NUM_BOXES) { Rectangle(BOX_SIZE, BOX_SIZE, BASE_COLOR) }

    override fun start(primaryStage: Stage) {
        val grid = GridPane().apply {
            hgap = 2.0
            vgap = 2.0
        }

        boxes.forEachIndexed { index, box ->
            grid.add(box, index % COLS, index / COLS)
        }

        val scene = Scene(grid)
        primaryStage.apply {
            title = "Color Box Highlight"
            setScene(scene)
            show()
        }

        // Example: Continuously highlight random boxes
        Timeline(
            KeyFrame(Duration.seconds(0.5), {
                highlightBox((Math.random() * NUM_BOXES).toInt())
            })
        ).apply {
            cycleCount = Timeline.INDEFINITE
            play()
        }
    }

    private fun highlightBox(index: Int) {
        if (index !in 0 until NUM_BOXES) return

        boxes.forEachIndexed { i, box ->
            val distance = Math.abs(i - index)
            val opacity = (1 - distance / 10.0).coerceAtLeast(0.0)
            val highlightColor = HIGHLIGHT_COLOR.deriveColor(0.0, 1.0, 1.0, opacity)

            Timeline(
                KeyFrame(Duration.ZERO, KeyValue(box.fillProperty(), highlightColor)),
                KeyFrame(FADE_DURATION, KeyValue(box.fillProperty(), BASE_COLOR))
            ).play()
        }
    }
}

fun main() {
    Application.launch(ColorBoxHighlight::class.java)
}