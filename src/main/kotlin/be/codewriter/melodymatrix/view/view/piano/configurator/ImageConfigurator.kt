package be.codewriter.melodymatrix.view.view.piano.configurator

import atlantafx.base.controls.ToggleSwitch
import be.codewriter.melodymatrix.view.component.TickerSlider
import be.codewriter.melodymatrix.view.data.LicenseStatus
import be.codewriter.melodymatrix.view.view.piano.PianoWithEffectsView.Companion.PIANO_BACKGROUND_HEIGHT
import be.codewriter.melodymatrix.view.view.piano.PianoWithEffectsView.Companion.PIANO_WIDTH
import be.codewriter.melodymatrix.view.view.piano.data.PianoBackgroundImage
import be.codewriter.melodymatrix.view.view.piano.data.PianoConfiguration
import javafx.beans.binding.Bindings
import javafx.geometry.HorizontalDirection
import javafx.geometry.Insets
import javafx.scene.Cursor
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.FlowPane
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle

/**
 * Settings panel for configuring the piano scene background and logo overlay.
 *
 * Provides controls for solid background colour, background image selection (thumbnail grid),
 * image transparency, and logo visibility/position/transparency. The image and logo controls
 * are bidirectionally bound to [PianoConfiguration] properties. Logo controls are disabled
 * when the license is not valid.
 *
 * @param config        Observable configuration to bind to
 * @param licenseStatus The current license status; used to gate logo settings
 * @see PianoConfiguration
 */
class ImageConfigurator(config: PianoConfiguration, licenseStatus: LicenseStatus) : BaseConfigurator() {

    init {
        val backgroundImageVisible = ToggleSwitch().apply {
            textProperty().bind(selectedProperty().map { selected -> if (selected) "Visible" else "Hidden" })
            labelPosition = HorizontalDirection.RIGHT
            selectedProperty().bindBidirectional(config.backgroundImageEnabled)
        }
        val imageSelectionPane = FlowPane().apply {
            hgap = 5.0
            vgap = 5.0
            disableProperty().bind(config.backgroundImageEnabled.not())
            for (backgroundImage: PianoBackgroundImage in PianoBackgroundImage.entries) {
                val imageView = ImageView().apply {
                    image = Image(backgroundImage.file)
                    fitWidth = 40.0
                    isPreserveRatio = true
                }
                val border = Rectangle().apply {
                    widthProperty().bind(imageView.boundsInParentProperty().map { it.width + 4 })
                    heightProperty().bind(imageView.boundsInParentProperty().map { it.height + 4 })
                    fill = Color.TRANSPARENT
                    strokeWidth = 2.0
                    arcWidth = 4.0
                    arcHeight = 4.0
                    strokeProperty().bind(
                        Bindings.`when`(config.backgroundImage.isEqualTo(backgroundImage))
                            .then(Color.web("#4fc3f7"))
                            .otherwise(Color.TRANSPARENT)
                    )
                }
                val cell = StackPane(imageView, border).apply {
                    cursor = Cursor.HAND
                    padding = Insets(2.0)
                    setOnMouseClicked {
                        config.backgroundImage.value = backgroundImage
                    }
                }
                children.add(cell)
            }
        }
        val imageTransparency = TickerSlider().apply {
            min = 0.1
            max = 1.0
            majorTickUnit = 0.1
            disableProperty().bind(config.backgroundImageEnabled.not())
            valueProperty().bindBidirectional(config.backgroundImageTransparency)
        }
        val logoVisible = ToggleSwitch().apply {
            disableProperty().bind(Bindings.not(licenseStatus.isValid))
            selectedProperty().bindBidirectional(config.logoVisible)
            textProperty().bind(selectedProperty().map { selected -> if (selected) "Visible" else "Hidden" })
            labelPosition = HorizontalDirection.RIGHT
        }
        val logoTransparency = TickerSlider().apply {
            min = 0.2
            max = 1.0
            majorTickUnit = 0.1
            valueProperty().bindBidirectional(config.logoTransparency)
        }
        val logoWidth = TickerSlider().apply {
            min = 200.0
            max = PIANO_WIDTH - 100.0
            majorTickUnit = 1.0
            valueProperty().bindBidirectional(config.logoWidth)
        }
        val logoLeft = TickerSlider().apply {
            min = 0.0
            majorTickUnit = 1.0
            maxProperty().bind(Bindings.subtract(PIANO_WIDTH, logoWidth.valueProperty()))
            maxProperty().addListener { _, _, newVal ->
                if (value > newVal.toDouble()) {
                    value = newVal.toDouble()
                }
            }
            valueProperty().bindBidirectional(config.logoLeft)
        }
        val logoTop = TickerSlider().apply {
            min = 0.0
            majorTickUnit = 1.0
            val imageAspectRatio = 164.0 / 796.0
            maxProperty().bind(
                Bindings.subtract(
                    PIANO_BACKGROUND_HEIGHT,
                    Bindings.multiply(logoWidth.valueProperty(), imageAspectRatio)
                )
            )
            maxProperty().addListener { _, _, newVal ->
                if (value > newVal.toDouble()) {
                    value = newVal.toDouble()
                }
            }
            valueProperty().bindBidirectional(config.logoTop)
        }

        contentBox.children.addAll(
            sectionTitle("Background image"),
            backgroundImageVisible,
            labeledControl("Selection", imageSelectionPane),
            labeledControl("Transparency", imageTransparency),
            sectionTitle("MelodyMatrix logo"),
            labeledControl("Visible", logoVisible),
            labeledControl("Transparency", logoTransparency),
            labeledControl("Size", logoWidth),
            labeledControl("Left", logoLeft),
            labeledControl("Top", logoTop),
        )
    }
}