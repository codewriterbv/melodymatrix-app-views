package be.codewriter.melodymatrix.view.view.piano.data

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.paint.Color

/**
 * Observable configuration model for the piano visualizer stage.
 *
 * All properties are JavaFX observable so that UI controls in the settings accordion
 * (background, key colours, effects) can bind to them directly. Changes made in the
 * settings panel are automatically reflected in the piano scene and keyboard view.
 *
 * Groups of settings:
 * - **Above-key effect** – smoke/steam particles that rise above pressed keys
 * - **Background** – solid colour or image displayed behind the piano
 * - **Fireworks** – particle fireworks triggered on key press
 * - **Explosion** – radial explosion particles triggered on key press
 * - **Logo** – optional overlay logo image
 * - **Piano keys** – colours for white/black keys in normal and pressed states
 *
 * @see PianoStage
 * @see PianoBackgroundImage
 * @see FireworksExplosionType
 */
class PianoConfiguration {
    var showDebugInfo = SimpleBooleanProperty(true)

    // Above Key settings
    var aboveKeyEnabled = SimpleBooleanProperty(true)
    var aboveKeyColorStart = SimpleObjectProperty(Color.RED)
    var aboveKeyColorEnd = SimpleObjectProperty(Color.YELLOW)

    // Background settings
    var backgroundColor = SimpleObjectProperty(Color.BLACK)
    var backgroundImageEnabled = SimpleBooleanProperty(false)
    var backgroundImage = SimpleObjectProperty(PianoBackgroundImage.ABSTRACT_1)
    var backgroundImageTransparency = SimpleDoubleProperty(1.0)

    // Fireworks settings
    var fireworksEnabled = SimpleBooleanProperty(false)
    var fireworksColor = SimpleObjectProperty(Color.YELLOW)
    var fireworksRandomColor = SimpleBooleanProperty(false)
    var fireworksRadius = SimpleDoubleProperty(5.0)
    var fireworksNumberOfParticles = SimpleIntegerProperty(80)
    var fireworksParticleSize = SimpleDoubleProperty(5.0)
    var fireworksTailNumberOfParticles = SimpleIntegerProperty(10)
    var fireworksLaunchHeightMultiplier = SimpleDoubleProperty(1.0)
    var fireworksLiftMultiplier = SimpleDoubleProperty(1.0)
    var fireworksExplosionType = SimpleObjectProperty(FireworksExplosionType.CLASSIC)

    // Explosion settings
    var explosionEnabled = SimpleBooleanProperty(true)
    var explosionColor = SimpleObjectProperty(Color.YELLOW)
    var explosionRandomColor = SimpleBooleanProperty(false)
    var explosionRadius = SimpleDoubleProperty(5.0)
    var explosionNumberOfParticles = SimpleIntegerProperty(80)
    var explosionParticleSize = SimpleDoubleProperty(5.0)
    var explosionLiftMultiplier = SimpleDoubleProperty(1.0)
    var explosionTailNumberOfParticles = SimpleIntegerProperty(10)

    // Logo settings
    var logoLeft = SimpleDoubleProperty(10.0)
    var logoTop = SimpleDoubleProperty(10.0)
    var logoTransparency = SimpleDoubleProperty(1.0)
    var logoVisible = SimpleBooleanProperty(true)
    var logoWidth = SimpleDoubleProperty(300.0)

    // Piano Key settings
    var pianoWhiteKeyColor = SimpleObjectProperty(Color.WHITE)
    var pianoWhiteKeyActiveColor = SimpleObjectProperty(Color.RED)
    var pianoBlackKeyColor = SimpleObjectProperty(Color.BLACK)
    var pianoBlackKeyActiveColor = SimpleObjectProperty(Color.YELLOW)
    var pianoKeyNameColor = SimpleObjectProperty(Color.BLACK)
    var pianoKeyNameVisible = SimpleBooleanProperty(false)
}