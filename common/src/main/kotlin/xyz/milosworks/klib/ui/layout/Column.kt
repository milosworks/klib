package xyz.milosworks.klib.ui.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import xyz.milosworks.klib.ui.modifiers.Modifier

/**
 * A layout that arranges its children in a vertical column from top to bottom.
 *
 * The `Column` composable places its children one below the other, based on the provided
 * [verticalArrangement] and [horizontalAlignment]. It allows for spacing between
 * elements and alignment control for the content within each row.
 *
 * Example usage:
 * ```
 * Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
 *     Text("Centered content")
 * }
 * ```
 *
 * @param verticalArrangement Defines how the children are vertically arranged. Default is [Arrangement.Top].
 * @param horizontalAlignment Defines how the children are aligned horizontally. Default is [Alignment.Start].
 * @param content The composable content to be placed inside the column.
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

	Layout(
		measurePolicy,
		renderer = DefaultRenderer(),
		modifier = modifier,
		content = content,
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