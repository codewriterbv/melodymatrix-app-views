package be.codewriter.melodymatrix.view.stage.piano.configurator

import atlantafx.base.controls.Spacer
import atlantafx.base.controls.ToggleSwitch
import be.codewriter.melodymatrix.view.data.LicenseStatus
import be.codewriter.melodymatrix.view.stage.piano.PianoView.Companion.PIANO_BACKGROUND_HEIGHT
import be.codewriter.melodymatrix.view.stage.piano.PianoView.Companion.PIANO_WIDTH
import be.codewriter.melodymatrix.view.stage.piano.data.PianoBackgroundImage
import be.codewriter.melodymatrix.view.stage.piano.data.PianoConfiguration
import javafx.beans.binding.Bindings
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.control.ColorPicker
import javafx.scene.control.Label
import javafx.scene.control.Slider
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.FlowPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox


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
 * @see PianoStage
 * @see PianoConfiguration
 */
class BackgroundScene(config: PianoConfiguration, licenseStatus: LicenseStatus) : VBox() {

    init {
        val backgroundColor = ColorPicker().apply {
            valueProperty().bindBidirectional(config.backgroundColor)
        }
        val imageSelectionPane = FlowPane().apply {
            hgap = 5.0
            vgap = 5.0
            for (backgroundImage: PianoBackgroundImage in PianoBackgroundImage.entries) {
                val imageView: ImageView = ImageView().apply {
                    image = Image(backgroundImage.file)
                    cursor = Cursor.HAND
                    fitWidth = 40.0
                    isPreserveRatio = true
                    setOnMouseClicked {
                        config.backgroundImage.value = backgroundImage
                    }
                }
                children.add(imageView)
            }
        }
        val imageTransparency = Slider().apply {
            min = 0.1
            max = 1.0
            majorTickUnit = 0.1
            disableProperty().bind(config.backgroundImage.isEqualTo(PianoBackgroundImage.NONE))
            valueProperty().bindBidirectional(config.backgroundImageTransparency)
        }
        val logoVisible = ToggleSwitch().apply {
            disableProperty().bind(Bindings.not(licenseStatus.isValid))
            selectedProperty().bindBidirectional(config.logoVisible)
        }
        val logoTransparency = Slider().apply {
            min = 0.2
            max = 1.0
            majorTickUnit = 0.1
            valueProperty().bindBidirectional(config.logoTransparency)
        }
        val logoWidth = Slider().apply {
            min = 200.0
            max = PIANO_WIDTH - 100.0
            majorTickUnit = 1.0
            valueProperty().bindBidirectional(config.logoWidth)
        }
        val logoLeft = Slider().apply {
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
        val logoTop = Slider().apply {
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

        children.addAll(
            Label("Background color").apply {
                style = "-fx-font-size: 16px; -fx-font-weight: bold;"
            },
            backgroundColor,
            Spacer(20.0),
            Label("Background image").apply {
                style = "-fx-font-size: 16px; -fx-font-weight: bold;"
            },
            imageSelectionPane,
            getLabeledHolder("Transparency", imageTransparency),
            Spacer(20.0),
            Label("MelodyMatrix logo").apply {
                style = "-fx-font-size: 16px; -fx-font-weight: bold;"
            },
            getLabeledHolder("Visible", logoVisible),
            getLabeledHolder("Transparency", logoTransparency),
            getLabeledHolder("Size", logoWidth),
            getLabeledHolder("Left", logoLeft),
            getLabeledHolder("Top", logoTop),
        )

        spacing = 5.0
    }

    private fun getLabeledHolder(title: String, component: Node): HBox {
        val holder = HBox().apply {
            spacing = 5.0
        }
        val label = Label(title).apply {
            prefWidth = 100.0
        }
        holder.children.add(label)
        holder.children.add(component)
        return holder
    }

    /*private fun removeSelectedImage() {
        for (child in imageSelectionPane.children) {
            if (child is StackPane) {
                child.background = Background(BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY))
            }
        }
    }*/
}