package xyz.milosworks.klib.ui.modifiers.margin

import androidx.compose.runtime.Stable
import xyz.milosworks.klib.ui.layout.IntOffset
import xyz.milosworks.klib.ui.modifiers.LayoutChangingModifier
import xyz.milosworks.klib.ui.modifiers.Modifier
import kotlin.math.max

data class OutsetModifier(
    val outset: OutsetValues
) : Modifier.Element<OutsetModifier>, LayoutChangingModifier {
    override fun mergeWith(other: OutsetModifier) = OutsetModifier(
        OutsetValues(
            max(outset.left, other.outset.left),
            max(outset.right, other.outset.right),
            max(outset.top, other.outset.top),
            max(outset.bottom, other.outset.bottom),
        )
    )

    val horizontal get() = outset.left + outset.right
    val vertical get() = outset.top + outset.bottom

    override fun modifyPosition(offset: IntOffset): IntOffset = offset + outset.getOffset()

    override fun toString(): String = buildString {
        append("OutsetModifier(")

        val outsets = buildList {
            if (outset.left != 0) add("left=${outset.left}")
            if (outset.right != 0) add("right=${outset.right}")
            if (outset.top != 0) add("top=${outset.top}")
            if (outset.bottom != 0) add("bottom=${outset.bottom}")
        }

        append(outsets.joinToString(", "))
        append(")")
    }
}

@Stable
fun Modifier.outset(
    left: Int = 0,
    right: Int = 0,
    top: Int = 0,
    bottom: Int = 0,
) = then(OutsetModifier(OutsetValues(left, right, top, bottom)))

@Stable
fun Modifier.outset(horizontal: Int = 0, vertical: Int = 0) =
    outset(horizontal, horizontal, vertical, vertical)

@Stable
fun Modifier.outset(all: Int = 0) =
    outset(all, all, all, all)