package xyz.milosworks.klib.ui.layout.containers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import xyz.milosworks.klib.ui.layout.DefaultRenderer
import xyz.milosworks.klib.ui.layout.Layout
import xyz.milosworks.klib.ui.layout.LayoutNode
import xyz.milosworks.klib.ui.layout.measure.*
import xyz.milosworks.klib.ui.layout.primitive.Alignment
import xyz.milosworks.klib.ui.layout.primitive.IntSize
import xyz.milosworks.klib.ui.layout.primitive.LayoutDirection
import xyz.milosworks.klib.ui.modifiers.core.Modifier
import xyz.milosworks.klib.ui.modifiers.core.get
import xyz.milosworks.klib.ui.modifiers.position.inset.InsetModifier
import xyz.milosworks.klib.ui.modifiers.position.inset.InsetValues

/**
 * A composable that arranges its children on top of each other.
 *
 * The `Box` composable positions its children relative to each other, with each child stacked
 * on top of the previous one. The position of each child can be controlled using the
 * [contentAlignment] parameter.
 *
 * Example usage:
 * ```
 * Box(contentAlignment = Alignment.Center) {
 *     Text("Centered content")
 * }
 * ```
 *
 * @param contentAlignment Defines how the content is aligned inside the box. Default is [Alignment.TopStart].
 * @param content The composable content to be placed inside the box.
 */
@Composable
fun Box(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable () -> Unit
) {
    val measurePolicy = remember(contentAlignment) { BoxMeasurePolicy(contentAlignment) }
    Layout(
        measurePolicy,
        renderer = DefaultRenderer(),
        modifier = modifier,
        content = content,
    )
}

internal data class BoxMeasurePolicy(
    private val alignment: Alignment,
) : RowColumnMeasurePolicy() {
    override fun placeChildren(
        scope: MeasureScope,
        measurables: List<Measurable>,
        placeables: List<Placeable>,
        width: Int,
        height: Int
    ): MeasureResult {
        return MeasureResult(width, height) {
            val inset = (scope as? LayoutNode)?.modifier?.get<InsetModifier>()?.inset ?: InsetValues()
            for (child in placeables) {
                child.placeAt(
                    alignment.align(
                        child.size,
                        IntSize(width, height),
                        LayoutDirection.Ltr
                    ) + inset.getOffset()
                )
            }
        }
    }
}