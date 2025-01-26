package xyz.milosworks.klib.ui.extensions

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderType

/**
 * Adapted from Wisp Forest's owo-lib.
 * Original source: https://github.com/wisp-forest/owo-lib/blob/1.21.4/src/main/java/io/wispforest/owo/ui/core/OwoUIDrawContext.java
 *
 * License: MIT License
 * Author(s): The Wisp Forest Team
 */

/**
 * Draws a rectangle gradient on the GUI.
 *
 * @param x The x-coordinate of the top-left corner of the rectangle.
 * @param y The y-coordinate of the top-left corner of the rectangle.
 * @param width The width of the rectangle in pixels.
 * @param height The height of the rectangle in pixels.
 * @param topLeftColor The ARGB colour of the top-left corner.
 * @param topRightColor The ARGB colour of the top-right corner.
 * @param bottomLeftColor The ARGB colour of the bottom-left corner.
 * @param bottomRightColor The ARGB colour of the bottom-right corner.
 */
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

/**
 * Draws a rectangle gradient on the GUI.
 *
 * @param type The render type to use for rendering (e.g., {@link RenderType.gui()}).
 * @param x The x-coordinate of the top-left corner of the rectangle.
 * @param y The y-coordinate of the top-left corner of the rectangle.
 * @param width The width of the rectangle in pixels.
 * @param height The height of the rectangle in pixels.
 * @param topLeftColor The ARGB colour of the top-left corner.
 * @param topRightColor The ARGB colour of the top-right corner.
 * @param bottomLeftColor The ARGB colour of the bottom-left corner.
 * @param bottomRightColor The ARGB colour of the bottom-right corner.
 */
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

/**
 * Draws a rectangle outline on the GUI.
 *
 * @param x The x-coordinate of the top-left corner of the rectangle.
 * @param y The y-coordinate of the top-left corner of the rectangle.
 * @param width The width of the rectangle in pixels.
 * @param height The height of the rectangle in pixels.
 * @param color The ARGB colour of the rectangle's outline.
 */
fun GuiGraphics.drawRectOutline(x: Int, y: Int, width: Int, height: Int, color: Int) =
	drawRectOutline(RenderType.gui(), x, y, width, height, color)

/**
 * Draws a rectangle outline on the GUI with a specified render type.
 *
 * @param type The render type to use for rendering (e.g., {@link RenderType.gui()}).
 * @param x The x-coordinate of the top-left corner of the rectangle.
 * @param y The y-coordinate of the top-left corner of the rectangle.
 * @param width The width of the rectangle in pixels.
 * @param height The height of the rectangle in pixels.
 * @param color The ARGB colour of the rectangle's outline.
 */
fun GuiGraphics.drawRectOutline(type: RenderType, x: Int, y: Int, width: Int, height: Int, color: Int) {
	fill(type, x, y, x + width, y + 1, color)
	fill(type, x, y + height - 1, x + width, y + height, color)

	fill(type, x, y + 1, x + 1, y + height - 1, color)
	fill(type, x + width - 1, y + 1, x + width, y + height - 1, color)
}