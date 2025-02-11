package xyz.milosworks.klib.ui.extensions

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation
import xyz.milosworks.klib.KLib
import xyz.milosworks.klib.ui.util.NinePatchTexture

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
 * @param type The render type to use for rendering (e.g., [RenderType.gui]).
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

// REMEMBER: blit() requires a renderType on 1.21.4
/**
 * Draws a [NinePatchTexture] on the GUI.
 * All NinePatchTextures need to be under "assets/mod_id/nine_patch_textures".
 *
 * For more information check [NinePatchTexture]
 *
 * @param x The x-coordinate of the top-left corner of the texture.
 * @param y The y-coordinate of the top-left corner of the texture.
 * @param width The width of the texture in pixels.
 * @param height The height of the texture in pixels.
 * @param loc The [ResourceLocation] of the texture.
 */
fun GuiGraphics.ninePatchTexture(x: Int, y: Int, width: Int, height: Int, loc: ResourceLocation) {
	val npt = NinePatchTexture.of(loc)
		?: return KLib.LOGGER.warn("Nine patch texture couldn't be found: $loc. Did you forget to add it under \"nine_patch_textures\"?")

	val rightEdge = npt.cornersSize.width + npt.centerSize.width
	val bottomEdge = npt.cornersSize.height + npt.centerSize.height

	blit(
		npt.texture,
		x,
		y,
		npt.u.toFloat(),
		npt.v.toFloat(),
		npt.cornersSize.width,
		npt.cornersSize.height,
		npt.textureSize.width,
		npt.textureSize.height
	)
	blit(
		npt.texture,
		x + width - npt.cornersSize.width,
		y,
		npt.u.toFloat() + rightEdge,
		npt.v.toFloat(),
		npt.cornersSize.width,
		npt.cornersSize.height,
		npt.textureSize.width,
		npt.textureSize.height
	)
	blit(
		npt.texture,
		x,
		y + height - npt.cornersSize.height,
		npt.u.toFloat(),
		npt.v.toFloat() + bottomEdge,
		npt.cornersSize.width,
		npt.cornersSize.height,
		npt.textureSize.width,
		npt.textureSize.height
	)
	blit(
		npt.texture,
		x + width - npt.cornersSize.width,
		y + height - npt.cornersSize.height,
		npt.u.toFloat() + rightEdge,
		npt.v.toFloat() + bottomEdge,
		npt.cornersSize.width,
		npt.cornersSize.height,
		npt.textureSize.width,
		npt.textureSize.height
	)

	val cornerHeight = npt.cornersSize.height * 2
	val cornerWidth = npt.cornersSize.width * 2

	if (npt.repeat) {
		if (width > cornerWidth && height > cornerHeight) {
			var leftoverHeight: Int = height - cornerHeight

			while (leftoverHeight > 0) {
				val drawHeight = npt.centerSize.height.coerceAtMost(leftoverHeight)

				var leftoverWidth: Int = width - cornerWidth
				while (leftoverWidth > 0) {
					val drawWidth = npt.centerSize.width.coerceAtMost(leftoverWidth)
					blit(
						npt.texture,
						x + npt.cornersSize.width + leftoverWidth - drawWidth,
						y + npt.cornersSize.height + leftoverHeight - drawHeight,
						drawWidth,
						drawHeight,
						npt.u.toFloat() + npt.cornersSize.width + npt.centerSize.width - drawWidth,
						npt.v.toFloat() + npt.cornersSize.height + npt.centerSize.height - drawHeight,
						drawWidth,
						drawHeight,
						npt.textureSize.width,
						npt.textureSize.height
					)

					leftoverWidth -= npt.centerSize.width
				}
				leftoverHeight -= npt.centerSize.height
			}
		}

		if (width > cornerWidth) {
			var leftoverWidth: Int = width - cornerWidth
			while (leftoverWidth > 0) {
				val drawWidth = npt.centerSize.width.coerceAtMost(leftoverWidth)

				blit(
					npt.texture,
					x + npt.cornersSize.width + leftoverWidth - drawWidth,
					y,
					drawWidth,
					npt.cornersSize.height,
					npt.u.toFloat() + npt.cornersSize.width + npt.centerSize.width - drawWidth,
					npt.v.toFloat(),
					drawWidth,
					npt.cornersSize.height,
					npt.textureSize.width,
					npt.textureSize.height
				)
				blit(
					npt.texture,
					x + npt.cornersSize.width + leftoverWidth - drawWidth,
					y + height - npt.cornersSize.height,
					drawWidth,
					npt.cornersSize.height,
					npt.u.toFloat() + npt.cornersSize.width + npt.centerSize.width - drawWidth,
					npt.v.toFloat() + bottomEdge,
					drawWidth,
					npt.cornersSize.height,
					npt.textureSize.width,
					npt.textureSize.height
				)

				leftoverWidth -= npt.centerSize.width
			}
		}

		if (height > cornerHeight) {
			var leftoverHeight: Int = height - cornerHeight
			while (leftoverHeight > 0) {
				val drawHeight = npt.centerSize.height.coerceAtMost(leftoverHeight)

				blit(
					npt.texture,
					x,
					y + npt.cornersSize.height + leftoverHeight - drawHeight,
					npt.cornersSize.width,
					drawHeight,
					npt.u.toFloat(),
					npt.v.toFloat() + npt.cornersSize.height + npt.centerSize.height - drawHeight,
					npt.cornersSize.width,
					drawHeight,
					npt.textureSize.width,
					npt.textureSize.height
				)
				blit(
					npt.texture,
					x + width - npt.cornersSize.width,
					y + npt.cornersSize.height + leftoverHeight - drawHeight,
					npt.cornersSize.width,
					drawHeight,
					npt.u.toFloat() + rightEdge,
					npt.v.toFloat() + npt.cornersSize.height + npt.centerSize.height - drawHeight,
					npt.cornersSize.width,
					drawHeight,
					npt.textureSize.width,
					npt.textureSize.height
				)

				leftoverHeight -= npt.centerSize.height
			}
		}
	} else {
		if (width > cornerWidth && height > cornerHeight) {
			blit(
				npt.texture,
				x + npt.cornersSize.width,
				y + npt.cornersSize.height,
				width - cornerWidth,
				height - cornerHeight,
				npt.u.toFloat() + npt.cornersSize.width,
				npt.v.toFloat() + npt.cornersSize.height,
				npt.centerSize.width,
				npt.centerSize.height,
				npt.textureSize.width,
				npt.textureSize.height
			);
		}

		if (width > cornerWidth) {
			blit(
				npt.texture,
				x + npt.cornersSize.width,
				y,
				width - cornerWidth,
				npt.cornersSize.height,
				npt.u.toFloat() + npt.cornersSize.width,
				npt.v.toFloat(),
				npt.centerSize.width,
				npt.cornersSize.height,
				npt.textureSize.width,
				npt.textureSize.height
			);
			blit(
				npt.texture,
				x + npt.cornersSize.width,
				y + height - npt.cornersSize.height,
				width - cornerWidth,
				npt.cornersSize.height,
				npt.u.toFloat() + npt.cornersSize.width,
				npt.v.toFloat() + bottomEdge,
				npt.centerSize.width,
				npt.cornersSize.height,
				npt.textureSize.width,
				npt.textureSize.height
			);
		}

		if (height > cornerHeight) {
			blit(
				npt.texture,
				x,
				y + npt.cornersSize.height,
				npt.cornersSize.width,
				height - cornerHeight,
				npt.u.toFloat(),
				npt.v.toFloat() + npt.cornersSize.height,
				npt.cornersSize.width,
				npt.centerSize.height,
				npt.textureSize.width,
				npt.textureSize.height
			);
			blit(
				npt.texture,
				x + width - npt.cornersSize.width,
				y + npt.cornersSize.height,
				npt.cornersSize.width,
				height - cornerHeight,
				npt.u.toFloat() + rightEdge,
				npt.v.toFloat() + npt.cornersSize.height,
				npt.cornersSize.width,
				npt.centerSize.height,
				npt.textureSize.width,
				npt.textureSize.height
			);
		}
	}
}
