package xyz.milosworks.klib.ui.modifiers

import xyz.milosworks.klib.ui.layout.IntCoordinates

class OnGloballyPositionedModifier(
	val merged: Boolean = false,
	val onGloballyPositioned: (IntCoordinates) -> Unit
) : Modifier.Element<OnGloballyPositionedModifier> {
	override fun mergeWith(other: OnGloballyPositionedModifier): OnGloballyPositionedModifier =
		OnGloballyPositionedModifier(merged = true) { position ->
			if (!other.merged)
				onGloballyPositioned(position)
			other.onGloballyPositioned(position)
		}

}

fun Modifier.onGloballyPositioned(onGloballyPositioned: (IntCoordinates) -> Unit) =
	this then OnGloballyPositionedModifier(onGloballyPositioned = onGloballyPositioned)

