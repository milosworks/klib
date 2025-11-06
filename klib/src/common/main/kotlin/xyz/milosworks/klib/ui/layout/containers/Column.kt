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
    override fun placeChildren(
        scope: MeasureScope,
        measurables: List<Measurable>,
        placeables: List<Placeable>,
        width: Int,
        height: Int
    ): MeasureResult {
        val positions = IntArray(placeables.size)
        verticalArrangement.arrange(
            totalSize = height,
            sizes = placeables.map { it.height }.toIntArray(),
            outPositions = positions
        )
        return MeasureResult(width, height) {
            val inset =
                (scope as? LayoutNode)?.modifier?.get<PaddingModifier>()?.padding ?: PaddingValues()
            var accumulatedOutset = 0
            placeables.forEachIndexed { index, placeable ->
                val x = horizontalAlignment.align(
                    placeable.width,
                    width,
                    LayoutDirection.Ltr
                ) + inset.left
                val y = positions[index] + accumulatedOutset + inset.top

                placeable.placeAt(x, y)

                (measurables[index] as? LayoutNode)?.get<MarginModifier>()?.let { outset ->
                    accumulatedOutset += outset.vertical
                }
            }
        }
    }
}