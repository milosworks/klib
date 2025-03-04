package xyz.milosworks.klib.ui

import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.snapshots.Snapshot
import com.mojang.blaze3d.platform.InputConstants
import kotlinx.coroutines.*
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu
import xyz.milosworks.klib.ui.layout.Alignment
import xyz.milosworks.klib.ui.layout.Box
import xyz.milosworks.klib.ui.layout.LayoutNode
import xyz.milosworks.klib.ui.modifiers.Constraints
import xyz.milosworks.klib.ui.modifiers.Modifier
import xyz.milosworks.klib.ui.modifiers.fillMaxSize
import xyz.milosworks.klib.ui.modifiers.input.*
import xyz.milosworks.klib.ui.nodes.UINodeApplier
import kotlin.coroutines.CoroutineContext

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
			Box(
				modifier = Modifier.fillMaxSize(),
				contentAlignment = Alignment.Center
			) {
				content()
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

	private fun <T : InputEvent> processInputEvent(
		node: LayoutNode,
		event: T,
		condition: (LayoutNode) -> Boolean = { true },
		process: (LayoutNode, T) -> Unit
	) {
		for (child in node.children.asReversed()) {
			if (!event.isConsumed) processInputEvent(child, event, condition, process)
			else break
		}

		if (!event.isConsumed && condition(node)) process(node, event)
	}

	@Suppress("NOTHING_TO_INLINE")
	private inline fun processPointerEvent(
		node: LayoutNode,
		mouseX: Double,
		mouseY: Double,
		eventType: PointerEventType,
		noinline condition: (LayoutNode) -> Boolean = { it.isBounded(mouseX.toInt(), mouseY.toInt()) }
	): PointerEvent {
		val event = PointerEvent(eventType, mouseX, mouseY)

		processInputEvent(node, event, condition) { currentNode, currentEvent ->
			currentNode.modifier.foldIn(Unit) { acc, el ->
				if (el is OnPointerEventModifier && el.eventType == eventType && !currentEvent.isConsumed)
					el.onEvent(currentNode, event)
			}
		}

		return event
	}

	@Suppress("NOTHING_TO_INLINE")
	private inline fun processGlobalPressEvent(
		node: LayoutNode,
		mouseX: Double,
		mouseY: Double,
	) {
		val event = PointerEvent(PointerEventType.GLOBAL_PRESS, mouseX, mouseY)

		processInputEvent(node, event) { currentNode, currentEvent ->
			currentNode.modifier.foldIn(Unit) { acc, el ->
				if (el is OnPointerEventModifier && el.eventType == PointerEventType.GLOBAL_PRESS)
					el.onEvent(currentNode, event)
			}
		}
	}

	@Suppress("NOTHING_TO_INLINE")
	private inline fun processKeyEvent(
		node: LayoutNode,
		keyCode: Int,
		scanCode: Int,
		modifiers: Int
	): KeyEvent {
		val event = KeyEvent(keyCode, scanCode, modifiers)

		processInputEvent(node, event) { currentNode, currentEvent ->
			currentNode.modifier.foldIn(Unit) { acc, el ->
				if (el is OnKeyEventModifier && !currentEvent.isConsumed) el.onEvent(currentNode, currentEvent)
			}
		}

		return event
	}

	@Suppress("NOTHING_TO_INLINE")
	private inline fun processCharEvent(
		node: LayoutNode,
		codePoint: Char,
		modifiers: Int
	): CharEvent {
		val event = CharEvent(codePoint, modifiers)

		processInputEvent(node, event) { currentNode, currentEvent ->
			currentNode.modifier.foldIn(Unit) { acc, el ->
				if (el is OnCharTypedModifier && !currentEvent.isConsumed) el.onEvent(currentNode, currentEvent)
			}
		}

		return event
	}

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		processGlobalPressEvent(rootNode, mouseX, mouseY)

		val event = processPointerEvent(rootNode, mouseX, mouseY, PointerEventType.PRESS)

		return event.bypassSuper || super.mouseClicked(mouseX, mouseY, button)
	}

	override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
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

	// TODO: Add scrollX and scrollY to event data
	override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
		val event = processPointerEvent(rootNode, mouseX, mouseY, PointerEventType.SCROLL)

		return event.bypassSuper || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
	}

	override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
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

//	override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
//		// CTRL + SHIFT
//		// CTRL is detected as modifier 3
//		// SHIFT is the detected key
//		if (keyCode == InputConstants.KEY_LSHIFT && modifiers == 3) rootNode.debug = (rootNode.debug == false)
//		if (rootNode.debug && keyCode == InputConstants.KEY_LSHIFT) rootNode.extraDebug = true
//
//		processEventKey(rootNode, keyCode, scanCode, modifiers)
//
//		return super.keyPressed(keyCode, scanCode, modifiers)
//	}

	// -- OLD PROCESSING

//	private fun oldprocessEvent(
//		node: LayoutNode,
//		mouseX: Double,
//		mouseY: Double,
//		eventType: PointerEventType,
//		condition: (LayoutNode) -> Boolean = { it.isBounded(mouseX.toInt(), mouseY.toInt()) }
//	): Boolean {
//		var handled = false
//
//		for (child in node.children) {
//			handled = oldprocessEvent(child, mouseX, mouseY, eventType, condition) || handled
//		}
//
//		if (condition(node)) {
//			node.modifier.foldIn(Unit) { acc, el ->
//				if (handled == true) return@foldIn
//
//				if (el is OnPointerEventModifier && el.eventType == eventType) handled =
//					el.onEvent(node, mouseX, mouseY)
//			}
//		}
//
//		return handled
//	}
//
//	private fun oldprocessGlobalClickEvent(
//		node: LayoutNode,
//		mouseX: Double,
//		mouseY: Double,
//	) {
//		for (child in node.children) {
//			oldprocessGlobalClickEvent(child, mouseX, mouseY)
//		}
//
//		node.modifier.foldIn(Unit) { acc, el ->
//			if (el is OnPointerEventModifier && el.eventType == PointerEventType.GLOBAL_PRESS) el.onEvent(
//				node,
//				mouseX,
//				mouseY
//			)
//		}
//	}

//	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
//		oldprocessEvent(rootNode, mouseX, mouseY, PointerEventType.PRESS)
//		oldprocessGlobalClickEvent(rootNode, mouseX, mouseY)
//
//		return super.mouseClicked(mouseX, mouseY, button)
//	}
//
//	override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
//		oldprocessEvent(rootNode, mouseX, mouseY, PointerEventType.RELEASE)
//
//		return super.mouseReleased(mouseX, mouseY, button)
//	}
//
//	override fun mouseMoved(mouseX: Double, mouseY: Double) {
//		oldprocessEvent(rootNode, mouseX, mouseY, PointerEventType.MOVE)
//
//		oldprocessEvent(rootNode, mouseX, mouseY, PointerEventType.ENTER, {
//			it.isBounded(
//				mouseX.toInt(),
//				mouseY.toInt()
//			) && !it.isBounded(lastMouseX.toInt(), lastMouseY.toInt())
//		})
//
//		oldprocessEvent(rootNode, mouseX, mouseY, PointerEventType.EXIT, {
//			!it.isBounded(
//				mouseX.toInt(),
//				mouseY.toInt()
//			) && it.isBounded(lastMouseX.toInt(), lastMouseY.toInt())
//		})
//
//		lastMouseX = mouseX
//		lastMouseY = mouseY
//	}
//
//	override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
//		oldprocessEvent(rootNode, mouseX, mouseY, PointerEventType.SCROLL)
//
//		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
//	}

//	private fun processEventKey(node: LayoutNode, keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
//		var handled = false
//
//		for (child in node.children) {
//			handled = processEventKey(child, keyCode, scanCode, modifiers) || handled
//		}
//
//		node.modifier.foldIn(Unit) { acc, el ->
//			if (handled == true) return@foldIn
//
//			if (el is OnKeyEventModifier) handled = el.onEvent(node, keyCode, scanCode, modifiers)
//		}
//
//		return handled
//	}

//	private fun processEventChar(node: LayoutNode, codePoint: Char, modifiers: Int): Boolean {
//		var handled = false
//
//		for (child in node.children) {
//			handled = processEventChar(child, codePoint, modifiers) || handled
//		}
//
//		node.modifier.foldIn(Unit) { acc, el ->
//			if (handled == true) return@foldIn
//
//			if (el is OnCharTypedModifier) handled = el.onEvent(node, codePoint, modifiers)
//		}
//
//		return handled
//	}

//	override fun charTyped(codePoint: Char, modifiers: Int): Boolean {
//		processEventChar(rootNode, codePoint, modifiers)
//
//		return super.charTyped(codePoint, modifiers)
//	}
}