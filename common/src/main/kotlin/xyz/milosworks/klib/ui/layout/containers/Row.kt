package xyz.milosworks.klib.ui.layout.containers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import xyz.milosworks.klib.ui.layout.Layout
import xyz.milosworks.klib.ui.layout.LayoutNode
import xyz.milosworks.klib.ui.layout.measure.*
import xyz.milosworks.klib.ui.layout.primitive.Alignment
import xyz.milosworks.klib.ui.layout.primitive.Arrangement
import xyz.milosworks.klib.ui.layout.primitive.LayoutDirection
import xyz.milosworks.klib.ui.modifiers.core.Modifier
import xyz.milosworks.klib.ui.modifiers.core.get
import xyz.milosworks.klib.ui.modifiers.position.margin.MarginModifier
import xyz.milosworks.klib.ui.modifiers.position.padding.PaddingModifier
import xyz.milosworks.klib.ui.modifiers.position.padding.PaddingValues

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
    override fun placeChildren(
        scope: MeasureScope,
        measurables: List<Measurable>,
        placeables: List<Placeable>,
        width: Int,
        height: Int
    ): MeasureResult {
        val sizes = placeables.map { it.width }.toIntArray()
        val positions = IntArray(placeables.size)

        horizontalArrangement.arrange(
            totalSize = width,
            sizes = sizes,
            layoutDirection = LayoutDirection.Ltr,
            outPositions = positions
        )

        return MeasureResult(width, height) {
            val inset =
                (scope as? LayoutNode)?.modifier?.get<PaddingModifier>()?.padding ?: PaddingValues()
            var accumulatedOutset = 0

            placeables.forEachIndexed { index, placeable ->
                val x = positions[index] + accumulatedOutset + inset.left
                val y = verticalAlignment.align(placeable.height, height) + inset.top

                placeable.placeAt(x, y)

                (measurables[index] as? LayoutNode)?.get<MarginModifier>()?.let { outset ->
                    accumulatedOutset += outset.horizontal
                }
            }
        }
    }
}