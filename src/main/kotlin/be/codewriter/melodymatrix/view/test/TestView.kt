package be.codewriter.melodymatrix.view.test

import be.codewriter.melodymatrix.view.VisualizerStage
import be.codewriter.melodymatrix.view.component.ScaledContentPane
import be.codewriter.melodymatrix.view.data.LicenseStatus
import be.codewriter.melodymatrix.view.stage.chart.ChartsStage
import be.codewriter.melodymatrix.view.stage.midi.MidiStage
import be.codewriter.melodymatrix.view.stage.piano.PianoStage
import be.codewriter.melodymatrix.view.video.DummyVideoRecorder
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.stage.WindowEvent
import software.coley.bentofx.Bento
import software.coley.bentofx.dockable.Dockable
import stage.drum.DrumStage
import stage.ledstrip.LedStripStage
import stage.scale.ScaleStage

class TestView : VBox() {

    private val midiSimulator = MidiSimulator()
    private val licenseStatus = LicenseStatus()

    private val bento = Bento()
    private val rootContainer = bento.dockBuilding().root("test-root")
    private val stageSelectorLeaf = bento.dockBuilding().leaf("stage-selector-leaf")
    private val visualizerTabs = bento.dockBuilding().leaf("visualizer-tabs")
    private val rightColumnBranch = bento.dockBuilding().branch("right-column-branch")
    private val midiEventsLeaf = bento.dockBuilding().leaf("midi-events-leaf")
    private val licenseLeaf = bento.dockBuilding().leaf("license-leaf")

    private val stageOptions = listOf(
        StageOption("Midi", "tab-midi") { MidiStage() },
        StageOption("Piano", "tab-piano") { PianoStage(licenseStatus, DummyVideoRecorder(), true) },
        StageOption("Charts", "tab-charts") { ChartsStage() },
        StageOption("Scale", "tab-scale") { ScaleStage() },
        StageOption("Drum", "tab-drum") { DrumStage() },
        StageOption("LED Strip", "tab-led-strip") { LedStripStage() }
    )

    private val optionsByLabel = stageOptions.associateBy { it.label }
    private val activeVisualizers: MutableMap<String, ActiveVisualizer> = linkedMapOf()
    private val stageSelector = TestViewStages(stageOptions.map { it.label }, ::toggleVisualizer)

    init {
        spacing = 10.0
        padding = Insets(20.0)

        setupDockLayout()
        bento.registerRoot(rootContainer)

        children.add(rootContainer)
    }

    private fun setupDockLayout() {
        val stageSelectorDockable = createFixedDockable(
            id = "stage-selector",
            title = "Stages",
            content = stageSelector
        )

        val midiEventsDockable = createFixedDockable(
            id = "midi-events",
            title = "MIDI Events",
            content = TestViewMidiEvents(midiSimulator)
        )

        val licenseDockable = createFixedDockable(
            id = "license-status",
            title = "License",
            content = TestViewLicense(licenseStatus)
        )

        rightColumnBranch.orientation = Orientation.VERTICAL
        rightColumnBranch.setPruneWhenEmpty(false)
        rightColumnBranch.addContainers(midiEventsLeaf, licenseLeaf)
        rightColumnBranch.setDividerPositions(0.66)

        visualizerTabs.setPruneWhenEmpty(false)
        rootContainer.addContainers(stageSelectorLeaf, visualizerTabs, rightColumnBranch)
        rootContainer.setPruneWhenEmpty(false)
        rootContainer.setDividerPositions(0.1, 0.85)

        stageSelectorLeaf.addDockable(stageSelectorDockable)
        stageSelectorLeaf.selectDockable(stageSelectorDockable)

        midiEventsLeaf.addDockable(midiEventsDockable)
        midiEventsLeaf.selectDockable(midiEventsDockable)

        licenseLeaf.addDockable(licenseDockable)
        licenseLeaf.selectDockable(licenseDockable)

        stageSelectorLeaf.prefWidth = 260.0
        rightColumnBranch.prefWidth = 430.0
        visualizerTabs.minWidth = 1800.0
        visualizerTabs.prefWidth = 1800.0
        rootContainer.prefHeight = 900.0
    }

    private fun createFixedDockable(
        id: String,
        title: String,
        content: Node
    ): Dockable {
        return bento.dockBuilding().dockable(id).apply {
            this.titleProperty().set(title)
            this.nodeProperty().set(content)
            this.isClosable = false
            this.isCanBeDragged = false
            this.isCanBeDroppedToNewWindow = false
        }
    }

    fun shutdown() {
        activeVisualizers.values.toList().forEach { active ->
            disposeVisualizer(active)
        }
        activeVisualizers.clear()
        midiSimulator.stop()
        midiSimulator.scheduler.shutdownNow()
    }

    private fun toggleVisualizer(label: String, selected: Boolean) {
        val option = optionsByLabel[label] ?: return
        if (selected) {
            openVisualizer(option)
        } else {
            closeVisualizer(option)
        }
    }

    private fun openVisualizer(option: StageOption) {
        if (activeVisualizers.containsKey(option.id)) {
            return
        }

        ensureVisualizerLeafAttached()

        val stage = option.stageSupplier()
        val content = extractStageContent(stage)
        val closeHandler = stage.onCloseRequest

        val dockable: Dockable = bento.dockBuilding().dockable(option.id).apply {
            titleProperty().set(option.label)
            nodeProperty().set(content)
            isCanBeDroppedToNewWindow = false
            addCloseListener { _, _ ->
                val removed = activeVisualizers.remove(option.id)
                if (removed != null) {
                    disposeVisualizer(removed)
                    stageSelector.setSelected(option.label, false)
                }
            }
        }

        midiSimulator.registerListener(stage)
        visualizerTabs.addDockable(dockable)
        visualizerTabs.selectDockable(dockable)

        activeVisualizers[option.id] = ActiveVisualizer(
            stage = stage,
            dockable = dockable,
            closeHandler = closeHandler
        )
    }

    private fun ensureVisualizerLeafAttached() {
        if (visualizerTabs.parentContainer != null) {
            return
        }

        // If Bento pruned the center leaf after the last tab closed, restore it in the middle slot.
        rootContainer.addContainer(1, visualizerTabs)
        rootContainer.setDividerPositions(0.1, 0.85)
    }

    private fun closeVisualizer(option: StageOption) {
        val active = activeVisualizers[option.id] ?: return
        val closed = visualizerTabs.closeDockable(active.dockable)
        if (!closed) {
            visualizerTabs.removeDockable(active.dockable)
            activeVisualizers.remove(option.id)
            disposeVisualizer(active)
        }
    }

    private fun extractStageContent(stage: VisualizerStage): Node {
        val scene = stage.scene
            ?: throw IllegalStateException("Visualizer stage ${stage::class.simpleName} did not initialize a scene")
        val naturalW = scene.width
        val naturalH = scene.height
        val content = scene.root
        scene.root = Pane() // Detach original content so it can be embedded in Bento tabs.
        return ScaledContentPane(content, naturalW, naturalH)
    }

    private fun disposeVisualizer(active: ActiveVisualizer) {
        midiSimulator.removeListener(active.stage)
        active.closeHandler?.handle(WindowEvent(active.stage, WindowEvent.WINDOW_CLOSE_REQUEST))
    }

    private data class StageOption(
        val label: String,
        val id: String,
        val stageSupplier: () -> VisualizerStage
    )

    private data class ActiveVisualizer(
        val stage: VisualizerStage,
        val dockable: Dockable,
        val closeHandler: EventHandler<WindowEvent>?
    )
}