package xyz.milosworks.klib.ui.components

import androidx.compose.runtime.Composable
import net.minecraft.client.gui.GuiGraphics
import xyz.milosworks.klib.ui.extensions.fillGradient
import xyz.milosworks.klib.ui.layout.Layout
import xyz.milosworks.klib.ui.layout.MeasureResult
import xyz.milosworks.klib.ui.layout.Renderer
import xyz.milosworks.klib.ui.modifiers.BackgroundModifier
import xyz.milosworks.klib.ui.modifiers.GradientDirection
import xyz.milosworks.klib.ui.modifiers.Modifier
import xyz.milosworks.klib.ui.modifiers.SizeModifier
import xyz.milosworks.klib.ui.nodes.UINode

@Composable
fun Spacer(modifier: Modifier = Modifier) {
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
				var background: BackgroundModifier? = null
				var size: SizeModifier? = null

				modifier.foldIn(Unit) { _, element ->
					when (element) {
						is BackgroundModifier -> background = element
						is SizeModifier -> size = element
					}
				}
				if (size == null) throw IllegalStateException("Spacer component needs to have a size modifier")

				if (background != null) {
					when (background.gradientDirection) {
						GradientDirection.TOP_TO_BOTTOM -> guiGraphics.fillGradient(
							x,
							y,
							size.constraints.minWidth,
							size.constraints.minHeight,
							background.startColor,
							background.startColor,
							background.endColor,
							background.endColor,
						)

						GradientDirection.LEFT_TO_RIGHT -> guiGraphics.fillGradient(
							x,
							y,
							size.constraints.minWidth,
							size.constraints.minHeight,
							background.startColor,
							background.endColor,
							background.endColor,
							background.startColor
						)

						GradientDirection.RIGHT_TO_LEFT -> guiGraphics.fillGradient(
							x,
							y,
							size.constraints.minWidth,
							size.constraints.minHeight,
							background.endColor,
							background.startColor,
							background.startColor,
							background.endColor
						)

						GradientDirection.BOTTOM_TO_TOP -> guiGraphics.fillGradient(
							x,
							y,
							size.constraints.minWidth,
							size.constraints.minHeight,
							background.endColor,
							background.endColor,
							background.startColor,
							background.startColor
						)
					}
				}
			}
		},
		modifier = modifier,
	)
}