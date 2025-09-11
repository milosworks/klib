package xyz.milosworks.klib.ui.modifiers.position.padding

import androidx.compose.runtime.Stable
import xyz.milosworks.klib.ui.modifiers.core.Constraints
import xyz.milosworks.klib.ui.modifiers.core.LayoutChangingModifier
import xyz.milosworks.klib.ui.modifiers.core.Modifier
import xyz.milosworks.klib.ui.modifiers.core.offset

data class PaddingModifier(
    val padding: PaddingValues
) : Modifier.Element<PaddingModifier>, LayoutChangingModifier {
    override fun mergeWith(other: PaddingModifier) = PaddingModifier(padding + other.padding)

    val horizontal get() = padding.left + padding.right
    val vertical get() = padding.top + padding.bottom

    override fun modifyInnerConstraints(constraints: Constraints): Constraints {
        return constraints.offset(-horizontal, -vertical)
    }

    override fun toString(): String = buildString {
        append("InsetModifier(")

        val paddings = buildList {
            if (padding.left != 0) add("left=${padding.left}")
            if (padding.right != 0) add("right=${padding.right}")
            if (padding.top != 0) add("top=${padding.top}")
            if (padding.bottom != 0) add("bottom=${padding.bottom}")
        }

        append(paddings.joinToString(", "))
        append(")")
    }
}

@Stable
fun Modifier.padding(
    left: Int = 0,
    right: Int = 0,
    top: Int = 0,
    bottom: Int = 0,
) = then(PaddingModifier(PaddingValues(left, right, top, bottom)))

@Stable
fun Modifier.padding(horizontal: Int = 0, vertical: Int = 0) =
    padding(horizontal, horizontal, vertical, vertical)

@Stable
fun Modifier.padding(all: Int = 0) =
    padding(all, all, all, all)