package xyz.milosworks.klib.ui.components

import androidx.compose.runtime.Composable
import net.minecraft.client.gui.GuiGraphics
import xyz.milosworks.klib.ui.extensions.drawRectOutline
import xyz.milosworks.klib.ui.extensions.fillGradient
import xyz.milosworks.klib.ui.layout.Layout
import xyz.milosworks.klib.ui.layout.MeasureResult
import xyz.milosworks.klib.ui.layout.Renderer
import xyz.milosworks.klib.ui.modifiers.*
import xyz.milosworks.klib.ui.nodes.UINode

@Composable
fun Spacer(modifier: Modifier = Modifier) {
	var background: BackgroundModifier? = null
	var outline: OutlineModifier? = null

	modifier.foldIn(Unit) { _, element ->
		when (element) {
			is BackgroundModifier -> background = element
			is OutlineModifier -> outline = element
		}
	}

	Layout(
		measurePolicy = { _, constraints -> MeasureResult(constraints.minWidth, constraints.minHeight) {} },
		renderer = object : Renderer {
			override fun render(
				node: UINode,
				x: Int,
				y: Int,
				guiGraphics: GuiGraphics,
				mouseX: Int,
				mouseY: Int,
				partialTick: Float
			) {
				if (background != null) {
					when (background.gradientDirection) {
						GradientDirection.TOP_TO_BOTTOM -> guiGraphics.fillGradient(
							x,
							y,
							node.width,
							node.height,
							background.startColor,
							background.startColor,
							background.endColor,
							background.endColor,
						)

						GradientDirection.LEFT_TO_RIGHT -> guiGraphics.fillGradient(
							x,
							y,
							node.width,
							node.height,
							background.startColor,
							background.endColor,
							background.endColor,
							background.startColor
						)

						GradientDirection.RIGHT_TO_LEFT -> guiGraphics.fillGradient(
							x,
							y,
							node.width,
							node.height,
							background.endColor,
							background.startColor,
							background.startColor,
							background.endColor
						)

						GradientDirection.BOTTOM_TO_TOP -> guiGraphics.fillGradient(
							x,
							y,
							node.width,
							node.height,
							background.endColor,
							background.endColor,
							background.startColor,
							background.startColor
						)
					}
				}

				if (outline != null) {
					guiGraphics.drawRectOutline(
						x, y, node.width, node.height, outline.color
					)
				}
			}
		},
		modifier = modifier.fillMaxSize(),
	)
}