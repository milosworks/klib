package xyz.milosworks.klib.ui.modifiers.position.inset

import androidx.compose.runtime.Stable
import xyz.milosworks.klib.ui.modifiers.core.Constraints
import xyz.milosworks.klib.ui.modifiers.core.LayoutChangingModifier
import xyz.milosworks.klib.ui.modifiers.core.Modifier
import xyz.milosworks.klib.ui.modifiers.core.offset

data class InsetModifier(
    val inset: InsetValues
) : Modifier.Element<InsetModifier>, LayoutChangingModifier {
    override fun mergeWith(other: InsetModifier) = InsetModifier(inset + other.inset)

    val horizontal get() = inset.left + inset.right
    val vertical get() = inset.top + inset.bottom

    override fun modifyInnerConstraints(constraints: Constraints): Constraints {
        return constraints.offset(-horizontal, -vertical)
    }

    override fun toString(): String = buildString {
        append("InsetModifier(")

        val insets = buildList {
            if (inset.left != 0) add("left=${inset.left}")
            if (inset.right != 0) add("right=${inset.right}")
            if (inset.top != 0) add("top=${inset.top}")
            if (inset.bottom != 0) add("bottom=${inset.bottom}")
        }

        append(insets.joinToString(", "))
        append(")")
    }
}

@Stable
fun Modifier.inset(
    left: Int = 0,
    right: Int = 0,
    top: Int = 0,
    bottom: Int = 0,
) = then(InsetModifier(InsetValues(left, right, top, bottom)))

@Stable
fun Modifier.inset(horizontal: Int = 0, vertical: Int = 0) =
    inset(horizontal, horizontal, vertical, vertical)

@Stable
fun Modifier.inset(all: Int = 0) =
    inset(all, all, all, all)