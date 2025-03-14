package be.codewriter.melodymatrix.fxview.viewer

import be.codewriter.melodymatrix.view.definition.Note
import be.codewriter.melodymatrix.view.definition.PianoKeyType
import com.almasb.fxgl.animation.Interpolators
import com.almasb.fxgl.app.GameApplication
import com.almasb.fxgl.app.GameSettings
import com.almasb.fxgl.core.math.FXGLMath
import com.almasb.fxgl.core.math.FXGLMath.random
import com.almasb.fxgl.core.math.Vec2
import com.almasb.fxgl.dsl.*
import com.almasb.fxgl.dsl.components.ExpireCleanComponent
import com.almasb.fxgl.entity.component.Component
import com.almasb.fxgl.particle.ParticleComponent
import com.almasb.fxgl.particle.ParticleEmitters
import javafx.event.EventHandler
import javafx.geometry.Point2D
import javafx.scene.Parent
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.effect.BlendMode
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import javafx.util.Duration
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger


class AlmasVersion : GameApplication() {
    init {
        //val label = Label("Piano view - Not implemented yet")
        //val vbox = VBox(label)
        //title = "See your music being played on a piano..."
        //scene = Scene(vbox, 800.0, 600.0)
        //show()

        canvas = Canvas(800.0, 600.0)
        g = canvas.getGraphicsContext2D()
    }

    override fun initSettings(settings: GameSettings) {
        settings.width = 800
        settings.height = 600
        settings.ticksPerSecond = 60
    }

    private var i = 0

    override fun initGame() {
        i = 0

        getGameScene().setBackgroundColor(Color.DARKGRAY.darker().darker().darker().desaturate())

        entityBuilder()
            .view(canvas)
            .with(object : Component() {
                override fun onUpdate(tpf: Double) {
                    g.clearRect(0.0, 0.0, getAppWidth().toDouble(), getAppHeight().toDouble())
                }
            })
            .buildAndAttach()

        Note.entries.toTypedArray().forEach { note ->
            logger.info("Adding piano key for note {}", note.name)
            val view = PianoKeyView(note.mainNote.pianoKeyType)
            val x = i * 25.0
            val y = getAppHeight() - 165.0

            view.onMouseClicked = EventHandler { e: MouseEvent? ->
                view.shape?.let { shape ->
                    animationBuilder()
                        .duration(Duration.seconds(0.15))
                        .interpolator(Interpolators.ELASTIC.EASE_OUT())
                        .autoReverse(true)
                        .repeat(2)
                        .animate(shape.fillProperty())
                        .from(Color.WHITE)
                        .to(Color.hsb(65.0, 0.7, 1.0).brighter().brighter().brighter())
                        .buildAndPlay()
                }
                initParticles(x, y)
            }

            addUINode(view, x, y)
            i++
        }

        val emitter = ParticleEmitters.newFireEmitter(getAppWidth() + 100)
        emitter.startColor = Color.RED.brighter().brighter()
        emitter.endColor = Color.YELLOW
        emitter.setSize(2.0, 24.0)
        emitter.setVelocityFunction { i -> Point2D(random(-1, 1) * 2.5, -FXGLMath.randomDouble() * random(0.2, 0.5)) }

        entityBuilder()
            .at(-50.0, getAppHeight() - 170.0)
            .with(ParticleComponent(emitter))
            .zIndex(1000)
            .buildAndAttach()
    }

    private var t = 4.0
    private val light: Color = Color.BLUE.brighter().brighter()

    override fun onUpdate(tpf: Double) {
        t += tpf

        if (t > 12) {
            t = 4.0
        }
    }

    private fun initParticles(xx: Double, yy: Double) {
        val emitter = ParticleEmitters.newExplosionEmitter(200)

        emitter.maxEmissions = Int.MAX_VALUE
        emitter.blendMode = BlendMode.ADD
        emitter.numParticles = 180
        emitter.emissionRate = 0.86
        emitter.maxEmissions = 1
        emitter.setSize(1.0, 2.0)
        emitter.setScaleFunction { i -> FXGLMath.randomPoint2D().multiply(0.01) }
        emitter.setExpireFunction { i -> Duration.seconds(random(0.25, 3.5)) }
        emitter.setAccelerationFunction { Point2D.ZERO }
        emitter.setVelocityFunction { i -> Point2D.ZERO }
        emitter.setVelocityFunction { i -> FXGLMath.randomPoint2D().multiply(random(1.0, 45.0)).multiply(0.0003) }

        emitter.setColor(Color.color(1.0, 1.0, 1.0, 0.8))

        emitter.isAllowParticleRotation = true

        emitter.setControl { p ->
            val x = p.position.x
            val y = p.position.y

            val noiseValue = FXGLMath.noise2D(x * 0.008 * t, y * 0.002 * t)
            var angle = FXGLMath.toDegrees((noiseValue + 1) * Math.PI * 1.5)

            angle %= 360.0

            val v = Vec2.fromAngle(angle)
                .normalizeLocal()
                .mulLocal(random(1.0, 25.0))
                .mulLocal(0.5)

            val vx = p.velocity.x * 0.8f + v.x * 0.2f
            val vy = p.velocity.y * 0.8f + v.y * 0.2f

            p.velocity.x = vx * 0.8f + 0.2f
            p.velocity.y = Math.abs(vy) * -1.1f

            // lighting
            g.fill = light


            //g.setGlobalBlendMode(BlendMode.SRC_OVER);
            var layer = 1
            while (layer <= 2) {
                g.globalAlpha = 2.0 / layer * p.life
                g.fillOval(
                    (x - layer).toDouble(),
                    (y - layer).toDouble(),
                    (layer * 2).toDouble(),
                    (layer * 2).toDouble()
                )
                layer += 1
            }
        }

        val comp = ParticleComponent(emitter)

        //comp.getParent().getViewComponent().getParent().setEffect(new Glow());
        val e = entityBuilder()
            .at(xx + 25 / 2.0, yy)
            .zIndex(100)
            .with(ExpireCleanComponent(Duration.seconds(4.0)))
            .with(comp)
            .buildAndAttach()
    }

    private class PianoKeyView(pianoKeyType: PianoKeyType) : Parent() {
        var shape: Shape? = null

        init {
            val bg = Rectangle(25.0, 155.0)

            children.add(bg)

            if (pianoKeyType == PianoKeyType.LEFT) {
                val rect = Rectangle(25.0, 155.0)
                val sub = Rectangle(5.0, 100.0)
                sub.translateX = 20.0

                val finalShape = Shape.subtract(rect, sub)
                finalShape.fill = Color.WHITE
                finalShape.strokeWidth = 1.5
                finalShape.stroke = Color.BLACK

                shape = finalShape
                children.add(finalShape)
            }

            if (pianoKeyType == PianoKeyType.RIGHT) {
                val rect = Rectangle(25.0, 155.0)
                val sub = Rectangle(5.0, 100.0)
                sub.translateX = 0.0

                val finalShape = Shape.subtract(rect, sub)
                finalShape.fill = Color.WHITE
                finalShape.strokeWidth = 1.5
                finalShape.stroke = Color.BLACK

                shape = finalShape
                children.add(finalShape)
            }

            if (pianoKeyType == PianoKeyType.BOTH) {
                val rect = Rectangle(25.0, 155.0)
                val sub = Rectangle(5.0, 100.0)
                sub.translateX = 0.0

                val sub2 = Rectangle(5.0, 100.0)
                sub2.translateX = 20.0

                var finalShape = Shape.subtract(rect, sub)
                finalShape = Shape.subtract(finalShape, sub2)

                finalShape.fill = Color.WHITE
                finalShape.strokeWidth = 1.5
                finalShape.stroke = Color.BLACK

                shape = finalShape
                children.add(finalShape)
            }
        }
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(AlmasVersion::class.java.name)
        private lateinit var canvas: Canvas
        private lateinit var g: GraphicsContext
    }
}

fun main(args: Array<String>) {
    GameApplication.launch(AlmasVersion::class.java, args)
}