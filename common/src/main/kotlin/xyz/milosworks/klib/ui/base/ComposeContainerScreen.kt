package xyz.milosworks.klib.ui.base

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.Snapshot
import com.mojang.blaze3d.platform.InputConstants
import kotlinx.coroutines.*
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu
import xyz.milosworks.klib.ui.base.ui1.nodes.UINodeApplier
import xyz.milosworks.klib.ui.layout.LayoutNode
import xyz.milosworks.klib.ui.layout.containers.Box
import xyz.milosworks.klib.ui.layout.primitive.Alignment
import xyz.milosworks.klib.ui.modifiers.core.Constraints
import xyz.milosworks.klib.ui.modifiers.core.Modifier
import xyz.milosworks.klib.ui.modifiers.input.PointerEventType
import xyz.milosworks.klib.ui.modifiers.layout.fillMaxSize
import xyz.milosworks.klib.ui.utils.extensions.*
import kotlin.coroutines.CoroutineContext

val LocalContainer: ProvidableCompositionLocal<ComposeContainerScreen<*>> =
    compositionLocalOf { throw IllegalStateException("Container has not been provided") }

abstract class ComposeContainerScreen<T : AbstractContainerMenu>(
    menu: T, playerInventory: Inventory, title: Component
) : AbstractContainerScreen<T>(menu, playerInventory, title), CoroutineScope {
    private var hasFrameWaiters = false
    private val clock = BroadcastFrameClock { hasFrameWaiters = true }

    private val composeScope = CoroutineScope(Dispatchers.Default) + clock
    final override val coroutineContext: CoroutineContext = composeScope.coroutineContext

    private val rootNode = LayoutNode()

    private val recomposer = Recomposer(coroutineContext)
    private val composition = Composition(UINodeApplier(rootNode), recomposer)

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
        UIScopeManager.scopes += composeScope
        launch {
            recomposer.runRecomposeAndApplyChanges()
        }

        setContent {
            CompositionLocalProvider(LocalContainer provides this) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    content()
                }
            }
        }
    }

    private fun setContent(content: @Composable () -> Unit) {
        composition.setContent {
            content()
        }
    }

    open fun renderNodes(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (hasFrameWaiters) {
            hasFrameWaiters = false
            clock.sendFrame(System.nanoTime()) // Frame time value is not used by Compose runtime.
        }
        rootNode.measure(Constraints(maxWidth = width, maxHeight = height))
        rootNode.render(0, 0, guiGraphics, mouseX, mouseY, partialTick)
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)
        renderTooltip(guiGraphics, mouseX, mouseY)
    }

    override fun renderBg(guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        renderNodes(guiGraphics, mouseX, mouseY, partialTick)
    }

    override fun onClose() {
        super.onClose()
        recomposer.close()
        snapshotHandle.dispose()
        composition.dispose()
        composeScope.cancel()
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        processPointerEvent(rootNode, mouseX, mouseY, PointerEventType.GLOBAL_PRESS, true)

        val event = processPointerEvent(rootNode, mouseX, mouseY, PointerEventType.PRESS)

        return event.bypassSuper || super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        processPointerEvent(rootNode, mouseX, mouseY, PointerEventType.GLOBAL_RELEASE, true)

        val event = processPointerEvent(rootNode, mouseX, mouseY, PointerEventType.RELEASE)

        return event.bypassSuper || super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        processPointerEvent(rootNode, mouseX, mouseY, PointerEventType.MOVE)

        processPointerEvent(
            rootNode,
            mouseX,
            mouseY,
            PointerEventType.ENTER
        ) { it.isBounded(mouseX.toInt(), mouseY.toInt()) && !it.isBounded(lastMouseX.toInt(), lastMouseY.toInt()) }

        processPointerEvent(
            rootNode,
            mouseX,
            mouseY,
            PointerEventType.EXIT
        ) { !it.isBounded(mouseX.toInt(), mouseY.toInt()) && it.isBounded(lastMouseX.toInt(), lastMouseY.toInt()) }

        lastMouseX = mouseX
        lastMouseY = mouseY
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        val event = processScrollEvent(rootNode, mouseX, mouseY, scrollX, scrollY)
        return event.bypassSuper || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        val event = processDragEvent(rootNode, mouseX, mouseY, button, dragX, dragY)
        return event.bypassSuper || super.mouseDragged(mouseX, mouseY, button, dragX, dragY)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        // CTRL + SHIFT
        // CTRL is detected as modifier 3
        // SHIFT is the detected key
        if (keyCode == InputConstants.KEY_LSHIFT && modifiers == 3) rootNode.debug = (rootNode.debug == false)
        if (rootNode.debug && keyCode == InputConstants.KEY_LSHIFT) rootNode.extraDebug = true

        val event = processKeyEvent(rootNode, keyCode, scanCode, modifiers)

        return event.bypassSuper || super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun charTyped(codePoint: Char, modifiers: Int): Boolean {
        val event = processCharEvent(rootNode, codePoint, modifiers)

        return event.bypassSuper || super.charTyped(codePoint, modifiers)
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (rootNode.debug && keyCode == InputConstants.KEY_LSHIFT) rootNode.extraDebug = false

        return super.keyReleased(keyCode, scanCode, modifiers)
    }
}