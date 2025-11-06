package xyz.milosworks.klib.ui.modifiers.position.margin

import androidx.compose.runtime.Stable
import xyz.milosworks.klib.ui.layout.primitive.IntOffset
import xyz.milosworks.klib.ui.modifiers.core.LayoutChangingModifier
import xyz.milosworks.klib.ui.modifiers.core.Modifier

data class MarginModifier(
    val margin: MarginValues
) : Modifier.Element<MarginModifier>, LayoutChangingModifier {
    override fun mergeWith(other: MarginModifier) = MarginModifier(margin + other.margin)

    val horizontal get() = margin.left + margin.right
    val vertical get() = margin.top + margin.bottom

    override fun modifyPosition(offset: IntOffset): IntOffset = offset + margin.getOffset()

    override fun toString(): String = buildString {
        append("MarginModifier(")

        val margins = buildList {
            if (margin.left != 0) add("left=${margin.left}")
            if (margin.right != 0) add("right=${margin.right}")
            if (margin.top != 0) add("top=${margin.top}")
            if (margin.bottom != 0) add("bottom=${margin.bottom}")
        }

        append(margins.joinToString(", "))
        append(")")
    }
}

@Stable
fun Modifier.margin(
    left: Int = 0,
    right: Int = 0,
    top: Int = 0,
    bottom: Int = 0,
) = then(MarginModifier(MarginValues(left, right, top, bottom)))

@Stable
fun Modifier.margin(horizontal: Int = 0, vertical: Int = 0) =
    margin(horizontal, horizontal, vertical, vertical)

@Stable
fun Modifier.margin(all: Int = 0) =
    margin(all, all, all, all)