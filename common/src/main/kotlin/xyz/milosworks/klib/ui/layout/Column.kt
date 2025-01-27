package xyz.milosworks.klib.ui.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import net.minecraft.client.gui.GuiGraphics
import xyz.milosworks.klib.ui.extensions.drawRectOutline
import xyz.milosworks.klib.ui.extensions.fillGradient
import xyz.milosworks.klib.ui.modifiers.BackgroundModifier
import xyz.milosworks.klib.ui.modifiers.GradientDirection
import xyz.milosworks.klib.ui.modifiers.Modifier
import xyz.milosworks.klib.ui.modifiers.OutlineModifier
import xyz.milosworks.klib.ui.nodes.UINode

/**
 * A layout component that places contents in a column top-to-bottom.
 */
@Composable
fun Column(
	modifier: Modifier = Modifier,
	verticalArrangement: Arrangement.Vertical = Arrangement.Top,
	horizontalAlignment: Alignment.Horizontal = Alignment.Start,
	content: @Composable () -> Unit
) {
	val measurePolicy = remember(verticalArrangement, horizontalAlignment) {
		ColumnMeasurePolicy(
			verticalArrangement,
			horizontalAlignment
		)
	}
	var background: BackgroundModifier? = null
	var outline: OutlineModifier? = null

	modifier.foldIn(Unit) { _, element ->
		when (element) {
			is BackgroundModifier -> background = element
			is OutlineModifier -> outline = element
		}
	}

	Layout(
		measurePolicy,
		modifier = modifier,
		content = content,
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
		}
	)
}

private data class ColumnMeasurePolicy(
	private val verticalArrangement: Arrangement.Vertical,
	private val horizontalAlignment: Alignment.Horizontal,
) : RowColumnMeasurePolicy(
	sumHeight = true,
	arrangementSpacing = verticalArrangement.spacing
) {
	override fun placeChildren(placeables: List<Placeable>, width: Int, height: Int): MeasureResult {
		val positions = IntArray(placeables.size)
		verticalArrangement.arrange(
			totalSize = height,
			sizes = placeables.map { it.height }.toIntArray(),
			outPositions = positions
		)
		return MeasureResult(width, height) {
			var childY = 0
			placeables.forEachIndexed { index, child ->
				child.placeAt(horizontalAlignment.align(child.height, height, LayoutDirection.Ltr), positions[index])
				childY += child.height
			}
		}
	}
}