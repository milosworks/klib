package xyz.milosworks.klib.ui.layout.containers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import xyz.milosworks.klib.ui.layout.DefaultRenderer
import xyz.milosworks.klib.ui.layout.Layout
import xyz.milosworks.klib.ui.layout.LayoutNode
import xyz.milosworks.klib.ui.layout.measure.*
import xyz.milosworks.klib.ui.layout.primitive.Alignment
import xyz.milosworks.klib.ui.layout.primitive.Arrangement
import xyz.milosworks.klib.ui.layout.primitive.LayoutDirection
import xyz.milosworks.klib.ui.modifiers.core.Modifier
import xyz.milosworks.klib.ui.modifiers.core.get
import xyz.milosworks.klib.ui.modifiers.position.inset.InsetModifier
import xyz.milosworks.klib.ui.modifiers.position.inset.InsetValues
import xyz.milosworks.klib.ui.modifiers.position.outset.OutsetModifier

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
    override fun placeChildren(
        scope: MeasureScope,
        measurables: List<Measurable>,
        placeables: List<Placeable>,
        width: Int,
        height: Int
    ): MeasureResult {
        // Get the widths of all children for arrangement calculation
        val sizes = placeables.map { it.width }.toIntArray()
        val positions = IntArray(placeables.size)

        // Calculate base positions according to arrangement
        horizontalArrangement.arrange(
            totalSize = width,
            sizes = sizes,
            layoutDirection = LayoutDirection.Ltr,
            outPositions = positions
        )

        return MeasureResult(width, height) {
            val inset = (scope as? LayoutNode)?.modifier?.get<InsetModifier>()?.inset ?: InsetValues()
            // We need to adjust positions for each element based on previous elements' outsets
            var accumulatedOutset = 0

            placeables.forEachIndexed { index, placeable ->
                val x = positions[index] + accumulatedOutset + inset.left
                val y = verticalAlignment.align(placeable.height, height) + inset.top

                // Place the child at the adjusted position
                placeable.placeAt(x, y)

                // After placing, the total horizontal outset of this child affects the next ones.
                (measurables[index] as? LayoutNode)?.get<OutsetModifier>()?.let { outset ->
                    accumulatedOutset += outset.horizontal
                }
            }
        }
    }
}