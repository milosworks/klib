package xyz.milosworks.klib.ui.modifiers

import androidx.compose.runtime.Stable
import xyz.milosworks.klib.ui.util.KColor

data class OutlineModifier(val color: Int) : Modifier.Element<OutlineModifier> {
	override fun mergeWith(other: OutlineModifier): OutlineModifier =
		throw UnsupportedOperationException("not implemented")
}

@Stable
fun Modifier.outline(color: KColor) = this then OutlineModifier(color.argb)

@Stable
fun Modifier.outline(color: Int): Modifier = this then OutlineModifier(color)