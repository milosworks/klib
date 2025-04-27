package xyz.milosworks.klib.ui.modifiers.offset

import androidx.compose.runtime.Stable
import xyz.milosworks.klib.ui.layout.IntOffset
import xyz.milosworks.klib.ui.modifiers.LayoutChangingModifier
import xyz.milosworks.klib.ui.modifiers.Modifier

data class OffsetModifier(
    val offset: IntOffset
) : Modifier.Element<OffsetModifier>, LayoutChangingModifier {
    override fun mergeWith(other: OffsetModifier) = other

    override fun modifyPosition(offset: IntOffset): IntOffset = offset + this.offset
}

@Stable
fun Modifier.offset(x: Int, y: Int) = then(OffsetModifier(IntOffset(x, y)))