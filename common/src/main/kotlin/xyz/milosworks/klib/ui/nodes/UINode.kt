package xyz.milosworks.klib.ui.nodes

import net.minecraft.client.gui.GuiGraphics
import xyz.milosworks.klib.ui.layout.LayoutNode
import xyz.milosworks.klib.ui.layout.MeasurePolicy
import xyz.milosworks.klib.ui.layout.Renderer
import xyz.milosworks.klib.ui.modifiers.Modifier

interface UINode {
	var measurePolicy: MeasurePolicy
	var renderer: Renderer
	var modifier: Modifier
	var width: Int
	var height: Int
	var x: Int
	var y: Int

	fun render(x: Int, y: Int, guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float)

	companion object {
		fun Constructor(nodeName: String): () -> UINode = { LayoutNode(nodeName) }
	}
}