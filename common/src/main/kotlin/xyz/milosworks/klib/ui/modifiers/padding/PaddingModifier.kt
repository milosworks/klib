package xyz.milosworks.klib.ui.modifiers.padding

import androidx.compose.runtime.Stable
import xyz.milosworks.klib.ui.layout.IntOffset
import xyz.milosworks.klib.ui.layout.IntSize
import xyz.milosworks.klib.ui.modifiers.Constraints
import xyz.milosworks.klib.ui.modifiers.LayoutChangingModifier
import xyz.milosworks.klib.ui.modifiers.Modifier
import xyz.milosworks.klib.ui.modifiers.offset
import kotlin.math.max

data class PaddingModifier(
    val padding: PaddingValues
) : Modifier.Element<PaddingModifier>, LayoutChangingModifier {
    override fun mergeWith(other: PaddingModifier) = PaddingModifier(
        PaddingValues(
            max(padding.start, other.padding.start),
            max(padding.end, other.padding.end),
            max(padding.top, other.padding.top),
            max(padding.bottom, other.padding.bottom),
        )
    )

    val horizontal get() = padding.start + padding.end
    val vertical get() = padding.top + padding.bottom

    override fun modifyPosition(offset: IntOffset): IntOffset = offset + padding.getOffset()

    override fun modifyInnerConstraints(constraints: Constraints) = constraints.offset(
        horizontal = -horizontal,
        vertical = -vertical,
    )

    override fun modifyLayoutConstraints(measuredSize: IntSize, constraints: Constraints): Constraints {
        val width = (measuredSize.width + horizontal).coerceIn(constraints.minWidth, constraints.maxWidth)
        val height = (measuredSize.height + vertical).coerceIn(constraints.minHeight, constraints.maxHeight)
        return constraints.copy(
            minWidth = width,
            maxWidth = width,
            minHeight = height,
            maxHeight = height,
        )
    }
}

@Stable
fun Modifier.padding(
    start: Int = 0,
    end: Int = 0,
    top: Int = 0,
    bottom: Int = 0,
) = then(PaddingModifier(PaddingValues(start, end, top, bottom)))

@Stable
fun Modifier.padding(horizontal: Int = 0, vertical: Int = 0) =
    padding(horizontal, horizontal, vertical, vertical)

@Stable
fun Modifier.padding(all: Int = 0) =
    padding(all, all, all, all)