package be.codewriter.melodymatrix.view.stage.piano.component

import be.codewriter.melodymatrix.view.stage.piano.component.PianoGenerator.Companion.PIANO_HEIGHT
import be.codewriter.melodymatrix.view.stage.piano.component.PianoGenerator.Companion.PIANO_WIDTH
import javafx.scene.paint.Color

class DefaultValues {

    companion object {
        fun setDefaults(vars: MutableMap<String, Any>) {
            vars.put(
                be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.PIANO_WHITE_KEY_COLOR.name,
                Color.WHITE
            )
            vars.put(
                be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.PIANO_WHITE_KEY_ACTIVE_COLOR.name,
                Color.ORANGE
            )
            vars.put(
                be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.PIANO_WHITE_KEY_NAME_VISIBLE.name,
                false
            )
            vars.put(
                be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.PIANO_WHITE_KEY_NAME_COLOR.name,
                Color.BLACK
            )
            vars.put(
                be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.PIANO_BLACK_KEY_COLOR.name,
                Color.BLACK
            )
            vars.put(
                be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.PIANO_BLACK_KEY_ACTIVE_COLOR.name,
                Color.CYAN
            )

            vars.put(
                be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.BACKGROUND_COLOR.name,
                Color.DARKGRAY
            )
            vars.put(
                be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.BACKGROUND_IMAGE.name,
                be.codewriter.melodymatrix.view.stage.piano.data.PianoBackgroundImage.NONE
            )
            vars.put(
                be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.BACKGROUND_IMAGE_TRANSPARENCY.name,
                1.0
            )

            vars.put(be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.LOGO_VISIBLE.name, true)
            vars.put(be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.LOGO_TRANSPARENCY.name, 1.0)
            vars.put(
                be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.LOGO_WIDTH.name,
                PIANO_WIDTH - 100.0
            )
            vars.put(be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.LOGO_LEFT.name, 50.0)
            vars.put(
                be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.LOGO_TOP.name,
                (PIANO_HEIGHT - 120.0 - 150.0) / 2
            )

            vars.put(be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.EXPLOSION_ENABLED.name, true)
            vars.put(
                be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.EXPLOSION_COLOR_END.name,
                Color.YELLOW
            )
            vars.put(
                be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.EXPLOSION_COLOR_START.name,
                Color.RED
            )
            vars.put(be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.EXPLOSION_RADIUS.name, 100.0)
            vars.put(
                be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.EXPLOSION_NUMBER_OF_PARTICLES.name,
                25.0
            )
            vars.put(
                be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.EXPLOSION_PARTICLE_SIZE.name,
                5.0
            )
            vars.put(
                be.codewriter.melodymatrix.view.stage.piano.component.PianoProperty.EXPLOSION_TAIL_NUMBER_OF_ARTICLES.name,
                0.0
            )
        }
    }
}