package be.codewriter.melodymatrix.view.stage.piano.data

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.paint.Color

class PianoConfiguration {
    // Above Key settings
    var aboveKeyEnabled = SimpleBooleanProperty(true)
    var aboveKeyColorStart = SimpleObjectProperty(Color.RED)
    var aboveKeyColorEnd = SimpleObjectProperty(Color.YELLOW)

    // Background settings
    var backgroundColor = SimpleObjectProperty(Color.BLACK)
    var backgroundImage = SimpleObjectProperty(PianoBackgroundImage.NONE)
    var backgroundImageTransparency = SimpleDoubleProperty(1.0)

    // Fireworks settings
    var fireworksEnabled = SimpleBooleanProperty(false)
    var fireworksColor = SimpleObjectProperty(Color.YELLOW)
    var fireworksRandomColor = SimpleBooleanProperty(false)
    var fireworksRadius = SimpleDoubleProperty(50.0)
    var fireworksNumberOfParticles = SimpleIntegerProperty(30)
    var fireworksParticleSize = SimpleDoubleProperty(5.0)
    var fireworksTailNumberOfParticles = SimpleIntegerProperty(10)
    var fireworksLaunchHeightMultiplier = SimpleDoubleProperty(1.0)
    var fireworksExplosionType = SimpleObjectProperty(FireworksExplosionType.CLASSIC)

    // Explosion settings
    var explosionEnabled = SimpleBooleanProperty(false)
    var explosionColor = SimpleObjectProperty(Color.YELLOW)
    var explosionRandomColor = SimpleBooleanProperty(false)
    var explosionRadius = SimpleDoubleProperty(50.0)
    var explosionNumberOfParticles = SimpleIntegerProperty(30)
    var explosionParticleSize = SimpleDoubleProperty(5.0)
    var explosionTailNumberOfParticles = SimpleIntegerProperty(10)

    // Logo settings
    var logoLeft = SimpleDoubleProperty(10.0)
    var logoTop = SimpleDoubleProperty(10.0)
    var logoTransparency = SimpleDoubleProperty(1.0)
    var logoVisible = SimpleBooleanProperty(true)
    var logoWidth = SimpleDoubleProperty(300.0)

    // Piano Key settings
    var pianoWhiteKeyColor = SimpleObjectProperty(Color.WHITE)
    var pianoWhiteKeyActiveColor = SimpleObjectProperty(Color.LIGHTGRAY)
    var pianoBlackKeyColor = SimpleObjectProperty(Color.BLACK)
    var pianoBlackKeyActiveColor = SimpleObjectProperty(Color.DARKGRAY)
    var pianoKeyNameColor = SimpleObjectProperty(Color.BLACK)
    var pianoKeyNameVisible = SimpleBooleanProperty(false)
}