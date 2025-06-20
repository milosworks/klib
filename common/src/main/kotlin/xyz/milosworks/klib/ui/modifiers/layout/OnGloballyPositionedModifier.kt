package xyz.milosworks.klib.ui.modifiers.layout

import xyz.milosworks.klib.ui.layout.primitive.IntCoordinates
import xyz.milosworks.klib.ui.modifiers.core.Modifier

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

    override fun toString(): String = "OnGloballyPositionedModifier(merged=$merged)"
}

fun Modifier.onGloballyPositioned(onGloballyPositioned: (IntCoordinates) -> Unit) =
    this then OnGloballyPositionedModifier(onGloballyPositioned = onGloballyPositioned)

