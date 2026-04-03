package be.codewriter.melodymatrix.view.view.piano.data

import be.codewriter.melodymatrix.view.helper.RegistryHelper
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
 */
class PianoConfiguration(val viewName: String) {
    /** Enables extra debug information in the piano view. */
    var showDebugInfo = SimpleBooleanProperty(true)

    // Piano Key settings
    /** Fill color for idle white keys. */
    var pianoWhiteKeyColor = SimpleObjectProperty(Color.WHITE)

    /** Fill color for pressed white keys. */
    var pianoWhiteKeyActiveColor = SimpleObjectProperty(Color.web("#4d66cc"))

    /** Intensity multiplier for white-key 3D press depth (0.0 = off, 2.0 = strong). */
    var pianoWhiteKeyDepth = SimpleDoubleProperty(1.0)

    /** Fill color for idle black keys. */
    var pianoBlackKeyColor = SimpleObjectProperty(Color.BLACK)

    /** Fill color for pressed black keys. */
    var pianoBlackKeyActiveColor = SimpleObjectProperty(Color.web("#e64d4d"))

    /** Intensity multiplier for black-key 3D depth and bevel contrast (0.0 = off, 2.0 = strong). */
    var pianoBlackKeyDepth = SimpleDoubleProperty(1.0)

    /** Text color used for key labels (note names). */
    var pianoKeyNameColor = SimpleObjectProperty(Color.BLACK)

    /** Font size (in points) for key name labels. */
    var pianoKeyNameFontSize = SimpleDoubleProperty(11.0)

    /** Shows or hides key labels on the keyboard. */
    var pianoKeyNameVisible = SimpleBooleanProperty(false)

    // Background settings
    /** Solid background color behind particles and keyboard. */
    var backgroundColor = SimpleObjectProperty(Color.BLACK)

    /** Enables rendering of the selected background image. */
    var backgroundImageEnabled = SimpleBooleanProperty(false)

    /** Selected background image preset. */
    var backgroundImage = SimpleObjectProperty(PianoBackgroundImage.ABSTRACT_1)

    /** Background image opacity from 0.0 (transparent) to 1.0 (opaque). */
    var backgroundImageTransparency = SimpleDoubleProperty(1.0)

    // Logo settings
    /** Horizontal position of the logo in pixels from the left edge. */
    var logoLeft = SimpleDoubleProperty(10.0)

    /** Vertical position of the logo in pixels from the top edge. */
    var logoTop = SimpleDoubleProperty(10.0)

    /** Logo opacity from 0.0 (transparent) to 1.0 (opaque). */
    var logoTransparency = SimpleDoubleProperty(1.0)

    /** Shows or hides the logo overlay. */
    var logoVisible = SimpleBooleanProperty(true)

    /** Rendered logo width in pixels (height keeps image ratio). */
    var logoWidth = SimpleDoubleProperty(300.0)

    // Fireworks settings
    /** Enables the fireworks effect triggered by note events. */
    var fireworksEnabled = SimpleBooleanProperty(false)

    /** Fixed fireworks color when random mode is disabled. */
    var fireworksColor = SimpleObjectProperty(Color.RED)

    /** Uses randomized fireworks colors instead of [fireworksColor]. */
    var fireworksRandomColor = SimpleBooleanProperty(true)

    /** Base spread radius for fireworks particles. */
    var fireworksRadius = SimpleDoubleProperty(5.0)

    /** Number of particles emitted per fireworks burst. */
    var fireworksNumberOfParticles = SimpleIntegerProperty(80)

    /** Size of each fireworks particle in pixels. */
    var fireworksParticleSize = SimpleDoubleProperty(5.0)

    /** Number of particles used for the fireworks launch tail. */
    var fireworksTailNumberOfParticles = SimpleIntegerProperty(10)

    /** Vertical launch height multiplier before explosion. */
    var fireworksLaunchHeightMultiplier = SimpleDoubleProperty(1.0)

    /** Additional upward lift multiplier for fireworks motion. */
    var fireworksLiftMultiplier = SimpleDoubleProperty(1.0)

    /** Fireworks explosion pattern shape. */
    var fireworksExplosionType = SimpleObjectProperty(FireworksExplosionType.CLASSIC)

    // Explosion settings
    /** Enables radial explosion particles on note events. */
    var explosionEnabled = SimpleBooleanProperty(true)

    /** Fixed explosion color when random mode is disabled. */
    var explosionColor = SimpleObjectProperty(Color.YELLOW)

    /** Uses randomized explosion colors instead of [explosionColor]. */
    var explosionRandomColor = SimpleBooleanProperty(false)

    /** Base explosion radius for particle spread. */
    var explosionRadius = SimpleDoubleProperty(5.0)

    /** Number of particles emitted per explosion. */
    var explosionNumberOfParticles = SimpleIntegerProperty(80)

    /** Size of each explosion particle in pixels. */
    var explosionParticleSize = SimpleDoubleProperty(5.0)

    /** Additional upward lift multiplier for explosion particles. */
    var explosionLiftMultiplier = SimpleDoubleProperty(1.0)

    /** Number of trailing particles emitted with each explosion. */
    var explosionTailNumberOfParticles = SimpleIntegerProperty(10)

    // Cloud settings
    /** Enables clouds/smoke rendered above the keys. */
    var cloudEnabled = SimpleBooleanProperty(true)

    /** Cloud color used as the default/idle tone. */
    var cloudColorStart = SimpleObjectProperty(Color.RED)

    /** Cloud color blend target when notes are active nearby. */
    var cloudColorEnd = SimpleObjectProperty(Color.YELLOW)

    /** Number of cloud particles seeded across the keyboard. */
    var cloudParticleCount = SimpleIntegerProperty(32)

    /** Nominal cloud blob size (actual size varies ±40% around this value). */
    var cloudParticleSize = SimpleDoubleProperty(160.0)

    /** Maximum horizontal drift speed in pixels/second. */
    var cloudDriftSpeed = SimpleDoubleProperty(22.0)

    /** Vertical wobble amplitude in pixels. */
    var cloudWobbleAmplitude = SimpleDoubleProperty(15.0)

    /** Base opacity of cloud particles (actual opacity varies ±50% around this value). */
    var cloudOpacity = SimpleDoubleProperty(0.12)

    /** X-radius around a pressed key within which new particles are spawned. */
    var cloudSpawnRadius = SimpleDoubleProperty(160.0)

    fun restoreSettings() {
        RegistryHelper.bindBoolean(showDebugInfo, registryKey("showDebugInfo"))

        RegistryHelper.bindColor(pianoWhiteKeyColor, registryKey("pianoWhiteKeyColor"))
        RegistryHelper.bindColor(pianoWhiteKeyActiveColor, registryKey("pianoWhiteKeyActiveColor"))
        RegistryHelper.bindDouble(pianoWhiteKeyDepth, registryKey("pianoWhiteKeyDepth"))
        RegistryHelper.bindColor(pianoBlackKeyColor, registryKey("pianoBlackKeyColor"))
        RegistryHelper.bindColor(pianoBlackKeyActiveColor, registryKey("pianoBlackKeyActiveColor"))
        RegistryHelper.bindDouble(pianoBlackKeyDepth, registryKey("pianoBlackKeyDepth"))
        RegistryHelper.bindColor(pianoKeyNameColor, registryKey("pianoKeyNameColor"))
        RegistryHelper.bindDouble(pianoKeyNameFontSize, registryKey("pianoKeyNameFontSize"))
        RegistryHelper.bindBoolean(pianoKeyNameVisible, registryKey("pianoKeyNameVisible"))

        RegistryHelper.bindColor(backgroundColor, registryKey("backgroundColor"))
        RegistryHelper.bindBoolean(backgroundImageEnabled, registryKey("backgroundImageEnabled"))
        RegistryHelper.bindEnum(backgroundImage, registryKey("backgroundImage"), PianoBackgroundImage::class.java)
        RegistryHelper.bindDouble(backgroundImageTransparency, registryKey("backgroundImageTransparency"))

        RegistryHelper.bindDouble(logoLeft, registryKey("logoLeft"))
        RegistryHelper.bindDouble(logoTop, registryKey("logoTop"))
        RegistryHelper.bindDouble(logoTransparency, registryKey("logoTransparency"))
        RegistryHelper.bindBoolean(logoVisible, registryKey("logoVisible"))
        RegistryHelper.bindDouble(logoWidth, registryKey("logoWidth"))

        RegistryHelper.bindBoolean(fireworksEnabled, registryKey("fireworksEnabled"))
        RegistryHelper.bindColor(fireworksColor, registryKey("fireworksColor"))
        RegistryHelper.bindBoolean(fireworksRandomColor, registryKey("fireworksRandomColor"))
        RegistryHelper.bindDouble(fireworksRadius, registryKey("fireworksRadius"))
        RegistryHelper.bindInt(fireworksNumberOfParticles, registryKey("fireworksNumberOfParticles"))
        RegistryHelper.bindDouble(fireworksParticleSize, registryKey("fireworksParticleSize"))
        RegistryHelper.bindInt(fireworksTailNumberOfParticles, registryKey("fireworksTailNumberOfParticles"))
        RegistryHelper.bindDouble(fireworksLaunchHeightMultiplier, registryKey("fireworksLaunchHeightMultiplier"))
        RegistryHelper.bindDouble(fireworksLiftMultiplier, registryKey("fireworksLiftMultiplier"))
        RegistryHelper.bindEnum(
            fireworksExplosionType,
            registryKey("fireworksExplosionType"),
            FireworksExplosionType::class.java
        )

        RegistryHelper.bindBoolean(explosionEnabled, registryKey("explosionEnabled"))
        RegistryHelper.bindColor(explosionColor, registryKey("explosionColor"))
        RegistryHelper.bindBoolean(explosionRandomColor, registryKey("explosionRandomColor"))
        RegistryHelper.bindDouble(explosionRadius, registryKey("explosionRadius"))
        RegistryHelper.bindInt(explosionNumberOfParticles, registryKey("explosionNumberOfParticles"))
        RegistryHelper.bindDouble(explosionParticleSize, registryKey("explosionParticleSize"))
        RegistryHelper.bindDouble(explosionLiftMultiplier, registryKey("explosionLiftMultiplier"))
        RegistryHelper.bindInt(explosionTailNumberOfParticles, registryKey("explosionTailNumberOfParticles"))

        RegistryHelper.bindBoolean(cloudEnabled, registryKey("cloudEnabled"))
        RegistryHelper.bindColor(cloudColorStart, registryKey("cloudColorStart"))
        RegistryHelper.bindColor(cloudColorEnd, registryKey("cloudColorEnd"))
        RegistryHelper.bindInt(cloudParticleCount, registryKey("cloudParticleCount"))
        RegistryHelper.bindDouble(cloudParticleSize, registryKey("cloudParticleSize"))
        RegistryHelper.bindDouble(cloudDriftSpeed, registryKey("cloudDriftSpeed"))
        RegistryHelper.bindDouble(cloudWobbleAmplitude, registryKey("cloudWobbleAmplitude"))
        RegistryHelper.bindDouble(cloudOpacity, registryKey("cloudOpacity"))
        RegistryHelper.bindDouble(cloudSpawnRadius, registryKey("cloudSpawnRadius"))
    }

    private fun registryKey(name: String): String = "view.piano.$viewName.$name"

}