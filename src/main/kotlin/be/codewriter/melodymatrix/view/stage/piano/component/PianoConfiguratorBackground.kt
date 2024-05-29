package be.codewriter.melodymatrix.view.stage.piano.component

import be.codewriter.melodymatrix.view.stage.piano.data.PianoBackgroundImage
import com.almasb.fxgl.dsl.FXGL.Companion.getdp
import com.almasb.fxgl.dsl.FXGL.Companion.getop
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.property.ObjectProperty
import javafx.geometry.Insets
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.control.ColorPicker
import javafx.scene.control.Label
import javafx.scene.control.Slider
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.paint.Color
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class PianoConfiguratorBackground : VBox() {
    companion object {
        private val logger: Logger = LogManager.getLogger(PianoConfiguratorBackground::class.java.name)
        private val backgroundColor = ColorPicker()
        private val imageSelectionPane = FlowPane()
        private val imageTransparency = Slider()
        private val logoTransparency = Slider()
        private val logoWidth = Slider()
        private val logoLeft = Slider()
        private val logoTop = Slider()
    }

    init {
        imageSelectionPane.apply {
            hgap = 5.0
            vgap = 5.0
        }
        imageTransparency.apply {
            min = 0.1
            max = 1.0
            value = 1.0
            majorTickUnit = 0.1
            isDisable = true
        }
        logoTransparency.apply {
            min = 0.2
            max = 1.0
            value = 1.0
            majorTickUnit = 0.1
        }
        logoWidth.apply {
            min = 50.0
            max = 800.0
            value = 800.0
            majorTickUnit = 1.0
        }
        logoLeft.apply {
            min = 0.0
            value = 0.0
            majorTickUnit = 1.0
            maxProperty().bind(Bindings.subtract(900, logoWidth.valueProperty()).divide(2))
            maxProperty().addListener { _, _, newVal ->
                logger.info("New max for left: {}", newVal)
                value = newVal.toDouble()
            }
        }
        logoTop.apply {
            min = 0.0
            value = 0.0
            majorTickUnit = 1.0
            maxProperty().bind(
                Bindings.subtract(600, Bindings.divide(logoWidth.valueProperty(), 800).multiply(600)).divide(2)
            )
            maxProperty().addListener { _, _, newVal ->
                logger.info("New max for top: {}", newVal)
                value = newVal.toDouble()
            }
        }
        for (backgroundImage: PianoBackgroundImage in PianoBackgroundImage.entries) {
            val imageView: ImageView = ImageView().apply {
                image = Image(backgroundImage.file)
                cursor = Cursor.HAND
                fitWidth = 40.0
                isPreserveRatio = true
            }
            val stackPane = StackPane(imageView).apply {
                background = Background(
                    BackgroundFill(
                        if (backgroundImage == PianoBackgroundImage.NONE) Color.RED else Color.BLACK,
                        CornerRadii.EMPTY,
                        Insets.EMPTY
                    )
                )
            }
            StackPane.setMargin(imageView, Insets(2.0))
            imageView.setOnMouseClicked {
                (getop<ObjectProperty<*>>(PianoGenerator.PianoProperty.BACKGROUND_IMAGE.name) as ObjectProperty<PianoBackgroundImage>).set(
                    backgroundImage
                )
                removeSelectedImage()
                stackPane.background = Background(BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY))
                imageTransparency.isDisable = backgroundImage == PianoBackgroundImage.NONE
            }
            imageSelectionPane.children.add(stackPane)
        }
        children.addAll(
            Label("Background color"),
            backgroundColor,
            Label("Background image"),
            imageSelectionPane,
            getLabeledHolder("Transparency", imageTransparency),
            Label("MelodyMatrix logo"),
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

    private fun removeSelectedImage() {
        for (child in imageSelectionPane.children) {
            if (child is StackPane) {
                child.background = Background(BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY))
            }
        }
    }

    /**
     * FXGL properties can only be used after FXGL has started.
     * So in the PianoGenerator/GameApplication class a callback is done in the initGame method to this method.
     */
    fun createBindings() {
        Platform.runLater {
            backgroundColor.valueProperty().bindBidirectional(getop(PianoGenerator.PianoProperty.BACKGROUND_COLOR.name))
            imageTransparency.valueProperty()
                .bindBidirectional(getdp(PianoGenerator.PianoProperty.BACKGROUND_IMAGE_TRANSPARENCY.name))
            logoTransparency.valueProperty()
                .bindBidirectional(getdp(PianoGenerator.PianoProperty.LOGO_TRANSPARENCY.name))
            logoWidth.valueProperty()
                .bindBidirectional(getdp(PianoGenerator.PianoProperty.LOGO_WIDTH.name))
            logoLeft.valueProperty()
                .bindBidirectional(getdp(PianoGenerator.PianoProperty.LOGO_LEFT.name))
            logoTop.valueProperty()
                .bindBidirectional(getdp(PianoGenerator.PianoProperty.LOGO_TOP.name))
        }
    }
}