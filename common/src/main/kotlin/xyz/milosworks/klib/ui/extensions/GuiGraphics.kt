package xyz.milosworks.klib.ui.extensions

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderType

fun GuiGraphics.fillGradient(
	x: Int,
	y: Int,
	width: Int,
	height: Int,
	topLeftColor: Int,
	topRightColor: Int,
	bottomLeftColor: Int,
	bottomRightColor: Int
) = fillGradient(RenderType.gui(), x, y, width, height, topLeftColor, topRightColor, bottomLeftColor, bottomRightColor)

fun GuiGraphics.fillGradient(
	type: RenderType,
	x: Int,
	y: Int,
	width: Int,
	height: Int,
	topLeftColor: Int,
	topRightColor: Int,
	bottomLeftColor: Int,
	bottomRightColor: Int
) {
	val buffer = bufferSource().getBuffer(type)
	val matrix = pose().last().pose()

	buffer.addVertex(matrix, x + width, y, 0).setColor(topRightColor)
	buffer.addVertex(matrix, x, y, 0).setColor(topLeftColor)
	buffer.addVertex(matrix, x, y + height, 0).setColor(bottomLeftColor)
	buffer.addVertex(matrix, x + width, y + height, 0).setColor(bottomRightColor)
}