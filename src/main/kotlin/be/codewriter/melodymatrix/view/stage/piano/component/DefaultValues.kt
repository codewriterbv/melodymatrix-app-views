package be.codewriter.melodymatrix.view.stage.piano.component

import be.codewriter.melodymatrix.view.stage.piano.component.PianoGenerator.Companion.PIANO_HEIGHT
import be.codewriter.melodymatrix.view.stage.piano.component.PianoGenerator.Companion.PIANO_WIDTH
import be.codewriter.melodymatrix.view.stage.piano.data.PianoBackgroundImage
import javafx.scene.paint.Color


class DefaultValues {

    companion object {
        fun setDefaults(vars: MutableMap<String, Any>) {
            vars.put(PianoProperty.PIANO_WHITE_KEY_COLOR.name, Color.WHITE)
            vars.put(PianoProperty.PIANO_WHITE_KEY_ACTIVE_COLOR.name, Color.ORANGE)
            vars.put(PianoProperty.PIANO_WHITE_KEY_NAME_VISIBLE.name, false)
            vars.put(PianoProperty.PIANO_WHITE_KEY_NAME_COLOR.name, Color.BLACK)
            vars.put(PianoProperty.PIANO_BLACK_KEY_COLOR.name, Color.BLACK)
            vars.put(PianoProperty.PIANO_BLACK_KEY_ACTIVE_COLOR.name, Color.CYAN)

            vars.put(PianoProperty.BACKGROUND_COLOR.name, Color.DARKGRAY)
            vars.put(PianoProperty.BACKGROUND_IMAGE.name, PianoBackgroundImage.NONE)
            vars.put(PianoProperty.BACKGROUND_IMAGE_TRANSPARENCY.name, 1.0)

            vars.put(PianoProperty.LOGO_VISIBLE.name, true)
            vars.put(PianoProperty.LOGO_TRANSPARENCY.name, 1.0)
            vars.put(PianoProperty.LOGO_WIDTH.name, PIANO_WIDTH - 100.0)
            vars.put(PianoProperty.LOGO_LEFT.name, 50.0)
            vars.put(PianoProperty.LOGO_TOP.name, (PIANO_HEIGHT - 120.0 - 150.0) / 2)

            vars.put(PianoProperty.EXPLOSION_ENABLED.name, true)
            vars.put(PianoProperty.EXPLOSION_TYPE.name, true)
            vars.put(PianoProperty.EXPLOSION_COLOR.name, Color.YELLOW)
            vars.put(PianoProperty.EXPLOSION_RANDOM_COLOR.name, false)
            vars.put(PianoProperty.EXPLOSION_RADIUS.name, 30.0)
            vars.put(PianoProperty.EXPLOSION_NUMBER_OF_PARTICLES.name, 25.0)
            vars.put(PianoProperty.EXPLOSION_PARTICLE_SIZE.name, 5.0)
            vars.put(PianoProperty.EXPLOSION_TAIL_NUMBER_OF_ARTICLES.name, 0.0)
            
            vars.put(PianoProperty.ABOVE_KEY_ENABLED.name, true)
            vars.put(PianoProperty.ABOVE_KEY_COLOR_START.name, Color.YELLOW)
            vars.put(PianoProperty.ABOVE_KEY_COLOR_END.name, Color.BLACK)
        }
    }
}