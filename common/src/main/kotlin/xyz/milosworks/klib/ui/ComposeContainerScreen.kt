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
import xyz.milosworks.klib.ui.modifiers.*
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

	private fun processEvent(
		node: LayoutNode,
		mouseX: Int,
		mouseY: Int,
		eventType: PointerEventType,
		condition: (LayoutNode) -> Boolean = { it.isBounded(mouseX.toInt(), mouseY.toInt()) }
	): Boolean {
		var handled = false

		for (child in node.children) {
			handled = processEvent(child, mouseX, mouseY, eventType, condition) || handled
		}

		if (condition(node)) {
			node.modifier.foldIn(Unit) { acc, el ->
				if (handled == true) return@foldIn

				if (el is OnPointerEventModifier && el.eventType == eventType) handled = el.onEvent(node)
			}
		}

		return handled
	}

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		fun find(node: LayoutNode): List<Pair<LayoutNode, List<OnPointerEventModifier>>> =
			node.children.flatMap { find(it) } + node.takeIf { node.isBounded(mouseX.toInt(), mouseY.toInt()) }?.let {
				buildList {
					node.modifier.foldIn(Unit) { _, element ->
						if (element is OnPointerEventModifier && element.eventType == PointerEventType.PRESS)
							add(element)
					}
				}.takeIf { it.isNotEmpty() }?.let { listOf(node to it) }
			}.orEmpty()

		find(rootNode)
			.takeIf { it.isNotEmpty() }
			?.any { (child, mods) -> mods.any { it.onEvent(child) } }

		return super.mouseClicked(mouseX, mouseY, button)
	}

	override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
		fun find(node: LayoutNode): List<Pair<LayoutNode, List<OnPointerEventModifier>>> =
			node.children.flatMap { find(it) } + node.takeIf { node.isBounded(mouseX.toInt(), mouseY.toInt()) }?.let {
				buildList {
					node.modifier.foldIn(Unit) { _, element ->
						if (element is OnPointerEventModifier && element.eventType == PointerEventType.RELEASE)
							add(element)
					}
				}.takeIf { it.isNotEmpty() }?.let { listOf(node to it) }
			}.orEmpty()

		find(rootNode)
			.takeIf { it.isNotEmpty() }
			?.any { (child, mods) -> mods.any { it.onEvent(child) } }

		return super.mouseReleased(mouseX, mouseY, button)
	}

	var lastMouseX = 0.0
	var lastMouseY = 0.0

	override fun mouseMoved(mouseX: Double, mouseY: Double) {
		fun findMoved(node: LayoutNode): List<Pair<LayoutNode, List<OnPointerEventModifier>>> =
			node.children.flatMap { findMoved(it) } + node.takeIf { node.isBounded(mouseX.toInt(), mouseY.toInt()) }
				?.let {
					buildList {
						node.modifier.foldIn(Unit) { _, element ->
							if (element is OnPointerEventModifier && element.eventType == PointerEventType.MOVE)
								add(element)
						}
					}.takeIf { it.isNotEmpty() }?.let { listOf(node to it) }
				}.orEmpty()

		findMoved(rootNode)
			.takeIf { it.isNotEmpty() }
			?.any { (child, mods) -> mods.any { it.onEvent(child) } }

		fun findEnter(node: LayoutNode): List<Pair<LayoutNode, List<OnPointerEventModifier>>> =
			node.children.flatMap { findEnter(it) } + node.takeIf {
				node.isBounded(
					mouseX.toInt(),
					mouseY.toInt()
				) && !node.isBounded(lastMouseX.toInt(), lastMouseY.toInt())
			}
				?.let {
					buildList {
						node.modifier.foldIn(Unit) { _, element ->
							if (element is OnPointerEventModifier && element.eventType == PointerEventType.ENTER)
								add(element)
						}
					}.takeIf { it.isNotEmpty() }?.let { listOf(node to it) }
				}.orEmpty()

		findEnter(rootNode)
			.takeIf { it.isNotEmpty() }
			?.any { (child, mods) -> mods.any { it.onEvent(child) } }

		fun findExit(node: LayoutNode): List<Pair<LayoutNode, List<OnPointerEventModifier>>> =
			node.children.flatMap { findExit(it) } + node.takeIf {
				node.isBounded(lastMouseX.toInt(), lastMouseY.toInt()) &&
						!node.isBounded(
							mouseX.toInt(),
							mouseY.toInt()
						)
			}
				?.let {
					buildList {
						node.modifier.foldIn(Unit) { _, element ->
							if (element is OnPointerEventModifier && element.eventType == PointerEventType.EXIT)
								add(element)
						}
					}.takeIf { it.isNotEmpty() }?.let { listOf(node to it) }
				}.orEmpty()

		findExit(rootNode)
			.takeIf { it.isNotEmpty() }
			?.any { (child, mods) -> mods.any { it.onEvent(child) } }

		lastMouseX = mouseX
		lastMouseY = mouseY
	}

	override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
		fun find(node: LayoutNode): List<Pair<LayoutNode, List<OnPointerEventModifier>>> =
			node.children.flatMap { find(it) } + node.takeIf { node.isBounded(mouseX.toInt(), mouseY.toInt()) }
				?.let {
					buildList {
						node.modifier.foldIn(Unit) { _, element ->
							if (element is OnPointerEventModifier && element.eventType == PointerEventType.SCROLL)
								add(element)
						}
					}.takeIf { it.isNotEmpty() }?.let { listOf(node to it) }
				}.orEmpty()

		find(rootNode)
			.takeIf { it.isNotEmpty() }
			?.any { (child, mods) -> mods.any { it.onEvent(child) } }

		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
	}

//	override fun mouseMoved(mouseX: Double, mouseY: Double) {
//		fun findHovered(node: LayoutNode): List<Pair<LayoutNode, OnHoverModifier>> {
//			return node.children
//				.flatMap { findHovered(it) } +
//					node.takeIf { it.isBounded(mouseX.toInt(), mouseY.toInt()) }
//						?.modifier?.get<OnHoverModifier>()
//						?.let { listOf(node to it) }
//						.orEmpty()
//		}
//
//		findHovered(rootNode)
//			.takeIf { it.isNotEmpty() }
//			?.any { (child, mod) -> mod.onHover(child) }
//	}

	override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		// CTRL + SHIFT
		// CTRL is detected as modifier 3
		// SHIFT is the detected key
		if (keyCode == InputConstants.KEY_LSHIFT && modifiers == 3) rootNode.debug = (rootNode.debug == false)
		if (rootNode.debug && keyCode == InputConstants.KEY_LSHIFT) rootNode.extraDebug = true

		return super.keyPressed(keyCode, scanCode, modifiers)
	}

	override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		if (rootNode.debug && keyCode == InputConstants.KEY_LSHIFT) rootNode.extraDebug = false

		return super.keyReleased(keyCode, scanCode, modifiers)
	}
}