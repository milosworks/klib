package xyz.milosworks.klib.ui.modifiers.position

import androidx.compose.runtime.Stable
import xyz.milosworks.klib.ui.layout.primitive.IntCoordinates
import xyz.milosworks.klib.ui.layout.primitive.IntOffset
import xyz.milosworks.klib.ui.modifiers.core.LayoutChangingModifier
import xyz.milosworks.klib.ui.modifiers.core.Modifier

data class OffsetModifier(
    val offset: IntOffset
) : Modifier.Element<OffsetModifier>, LayoutChangingModifier {
    override fun mergeWith(other: OffsetModifier) = other

    override fun modifyPosition(offset: IntOffset): IntOffset = offset + this.offset
}

@Stable
fun Modifier.offset(x: Int, y: Int) = then(OffsetModifier(IntCoordinates(x, y)))