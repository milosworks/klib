package xyz.milosworks.klib.ui.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import xyz.milosworks.klib.ui.modifiers.Modifier

/**
 * A layout that arranges its children in a horizontal row from left to right.
 *
 * The `Row` composable places its children one beside the other, based on the provided
 * [horizontalArrangement] and [verticalAlignment]. It allows for spacing between
 * elements and alignment control for the content within each column.
 *
 * Example usage:
 * ```
 * Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
 *     Text("Centered content")
 * }
 * ```
 *
 * @param modifier The modifier to be applied to the layout.
 * @param horizontalArrangement Defines how the children are arranged horizontally. Default is [Arrangement.Start].
 * @param verticalAlignment Defines how the children are aligned vertically. Default is [Alignment.Top].
 * @param content The composable content to be placed inside the row.
 */
@Composable
fun Row(
	modifier: Modifier = Modifier,
	horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
	verticalAlignment: Alignment.Vertical = Alignment.Top,
	content: @Composable () -> Unit
) {
	val measurePolicy = remember(horizontalArrangement, verticalAlignment) {
		RowMeasurePolicy(
			horizontalArrangement,
			verticalAlignment
		)
	}
	Layout(
		measurePolicy,
		renderer = DefaultRenderer(),
		modifier = modifier,
		content = content,
	)
}

private data class RowMeasurePolicy(
	private val horizontalArrangement: Arrangement.Horizontal,
	private val verticalAlignment: Alignment.Vertical,
) : RowColumnMeasurePolicy(
	sumWidth = true,
	arrangementSpacing = horizontalArrangement.spacing
) {
	override fun placeChildren(placeables: List<Placeable>, width: Int, height: Int): MeasureResult {
		val positions = IntArray(placeables.size)
		horizontalArrangement.arrange(
			totalSize = width,
			sizes = placeables.map { it.width }.toIntArray(),
			layoutDirection = LayoutDirection.Ltr,
			outPositions = positions
		)
		return MeasureResult(width, height) {
			placeables.forEachIndexed { index, child ->
				child.placeAt(positions[index], verticalAlignment.align(child.height, height))
			}
		}
	}
}