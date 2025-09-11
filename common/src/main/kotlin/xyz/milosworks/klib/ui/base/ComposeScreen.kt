package xyz.milosworks.klib.ui.base

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.Snapshot
import com.mojang.blaze3d.platform.InputConstants
import kotlinx.coroutines.*
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import xyz.milosworks.klib.ui.layer.LayerStackManager
import xyz.milosworks.klib.ui.layer.LocalLayerManager
import xyz.milosworks.klib.ui.layout.LayoutNode
import xyz.milosworks.klib.ui.layout.containers.Box
import xyz.milosworks.klib.ui.layout.primitive.Alignment
import xyz.milosworks.klib.ui.modifiers.core.Constraints
import xyz.milosworks.klib.ui.modifiers.core.Modifier
import xyz.milosworks.klib.ui.modifiers.input.PointerEventType
import xyz.milosworks.klib.ui.modifiers.layout.fillMaxSize
import xyz.milosworks.klib.ui.utils.extensions.*
import kotlin.coroutines.CoroutineContext

val LocalScreen: ProvidableCompositionLocal<ComposeScreen> =
    compositionLocalOf { throw IllegalStateException("Screen has not been provided") }

abstract class ComposeScreen(title: Component) : Screen(title), CoroutineScope {
    private var hasFrameWaiters = false
    private val clock = BroadcastFrameClock { hasFrameWaiters = true }

    private val composeScope = CoroutineScope(Dispatchers.Default) + clock
    final override val coroutineContext: CoroutineContext = composeScope.coroutineContext

    private lateinit var layerManager: LayerStackManager
    private lateinit var recomposer: Recomposer

    private var applyScheduled = false
    private val snapshotHandle = Snapshot.registerGlobalWriteObserver {
        if (!applyScheduled) {
            applyScheduled = true
            composeScope.launch {
                applyScheduled = false
                Snapshot.sendApplyNotifications()
            }
        }
    }

    private var lastMouseX = 0.0
    private var lastMouseY = 0.0

    protected fun start(content: @Composable () -> Unit) {
        recomposer = Recomposer(coroutineContext)
        layerManager = LayerStackManager(recomposer)

        UIScopeManager.scopes += composeScope
        launch {
            recomposer.runRecomposeAndApplyChanges()
        }

        layerManager.push { dismiss ->
            CompositionLocalProvider(
                LocalScreen provides this,
                LocalLayerManager provides layerManager
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    content()
                }
            }
        }
    }

    open fun renderNodes(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (hasFrameWaiters) {
            hasFrameWaiters = false
            clock.sendFrame(System.nanoTime())
        }

        var zOffset = 0f
        for (layer in layerManager.layers) {
            val rootNode = layer.rootNode
            rootNode.measure(Constraints(maxWidth = width, maxHeight = height))
            rootNode.render(0, 0, guiGraphics, mouseX, mouseY, partialTick, zOffset)
            zOffset = rootNode.getMaxZ(zOffset) + 10.0f // Add a buffer between layers
        }
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderNodes(guiGraphics, mouseX, mouseY, partialTick)
        super.render(guiGraphics, mouseX, mouseY, partialTick)
    }

    override fun onClose() {
        super.onClose()
        recomposer.close()
        snapshotHandle.dispose()
        layerManager.layers.forEach { it.dispose() }
        composeScope.cancel()
    }

    private fun getTopNode(): LayoutNode? = layerManager.top?.rootNode

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val topNode = getTopNode() ?: return super.mouseClicked(mouseX, mouseY, button)
        processPointerEvent(topNode, mouseX, mouseY, PointerEventType.GLOBAL_PRESS, true)
        val event = processPointerEvent(topNode, mouseX, mouseY, PointerEventType.PRESS)
        return event.bypassSuper || super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val topNode = getTopNode() ?: return super.mouseReleased(mouseX, mouseY, button)
        processPointerEvent(topNode, mouseX, mouseY, PointerEventType.GLOBAL_RELEASE, true)
        val event = processPointerEvent(topNode, mouseX, mouseY, PointerEventType.RELEASE)
        return event.bypassSuper || super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        val topNode = getTopNode() ?: return super.mouseMoved(mouseX, mouseY)
        processPointerEvent(topNode, mouseX, mouseY, PointerEventType.MOVE)

        processPointerEvent(
            topNode,
            mouseX,
            mouseY,
            PointerEventType.ENTER
        ) {
            it.isBounded(mouseX.toInt(), mouseY.toInt()) && !it.isBounded(
                lastMouseX.toInt(),
                lastMouseY.toInt()
            )
        }

        processPointerEvent(
            topNode,
            mouseX,
            mouseY,
            PointerEventType.EXIT
        ) {
            !it.isBounded(mouseX.toInt(), mouseY.toInt()) && it.isBounded(
                lastMouseX.toInt(),
                lastMouseY.toInt()
            )
        }

        lastMouseX = mouseX
        lastMouseY = mouseY
        super.mouseMoved(mouseX, mouseY)
    }

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        scrollX: Double,
        scrollY: Double
    ): Boolean {
        val topNode = getTopNode() ?: return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
        val event =
            processScrollEvent(topNode, mouseX, mouseY, scrollX, scrollY, PointerEventType.SCROLL)
        return event.bypassSuper || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    override fun mouseDragged(
        mouseX: Double,
        mouseY: Double,
        button: Int,
        dragX: Double,
        dragY: Double
    ): Boolean {
        val topNode =
            getTopNode() ?: return super.mouseDragged(mouseX, mouseY, button, dragX, dragY)
        val event =
            processDragEvent(topNode, mouseX, mouseY, button, dragX, dragY, PointerEventType.DRAG)
        return event.bypassSuper || super.mouseDragged(mouseX, mouseY, button, dragX, dragY)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        val topNode = getTopNode() ?: return super.keyPressed(keyCode, scanCode, modifiers)
        val baseNode = layerManager.layers.firstOrNull()?.rootNode
        if (baseNode != null) {
            // CTRL + SHIFT
            // CTRL is detected as modifier 3
            // SHIFT is the detected key
            if (keyCode == InputConstants.KEY_LSHIFT && modifiers == 3) baseNode.debug =
                (!baseNode.debug)
            if (baseNode.debug && keyCode == InputConstants.KEY_LSHIFT) baseNode.extraDebug = true
        }

        val event = processKeyEvent(topNode, keyCode, scanCode, modifiers)
        return event.bypassSuper || super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun charTyped(codePoint: Char, modifiers: Int): Boolean {
        val topNode = getTopNode() ?: return super.charTyped(codePoint, modifiers)
        val event = processCharEvent(topNode, codePoint, modifiers)
        return event.bypassSuper || super.charTyped(codePoint, modifiers)
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        val baseNode = layerManager.layers.firstOrNull()?.rootNode
        if (baseNode != null && baseNode.debug && keyCode == InputConstants.KEY_LSHIFT) {
            baseNode.extraDebug = false
        }
        return super.keyReleased(keyCode, scanCode, modifiers)
    }
}