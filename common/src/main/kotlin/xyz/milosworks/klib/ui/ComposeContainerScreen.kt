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

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		fun findClick(node: LayoutNode): List<Pair<LayoutNode, OnClickModifier>> {
			return node.children
				.flatMap { findClick(it) } +
					node.takeIf { it.isBounded(mouseX.toInt(), mouseY.toInt()) }
						?.modifier?.get<OnClickModifier>()
						?.let { listOf(node to it) }
						.orEmpty()
		}

		findClick(rootNode)
			.takeIf { it.isNotEmpty() }
			?.any { (child, mod) -> mod.onClick(child) }

		return super.mouseClicked(mouseX, mouseY, button)
	}

	override fun mouseMoved(mouseX: Double, mouseY: Double) {
		fun findHovered(node: LayoutNode): List<Pair<LayoutNode, OnHoverModifier>> {
			return node.children
				.flatMap { findHovered(it) } +
					node.takeIf { it.isBounded(mouseX.toInt(), mouseY.toInt()) }
						?.modifier?.get<OnHoverModifier>()
						?.let { listOf(node to it) }
						.orEmpty()
		}

		findHovered(rootNode)
			.takeIf { it.isNotEmpty() }
			?.any { (child, mod) -> mod.onHover(child) }
	}

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