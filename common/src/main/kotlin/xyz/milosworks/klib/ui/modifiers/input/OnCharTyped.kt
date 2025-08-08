package xyz.milosworks.klib.ui.modifiers.input

import xyz.milosworks.klib.ui.base.UINode
import xyz.milosworks.klib.ui.modifiers.core.Modifier

data class OnCharTypedModifier(
    val onEvent: (UINode, CharEvent) -> Unit
) : Modifier.Element<OnCharTypedModifier> {
    override fun mergeWith(other: OnCharTypedModifier): OnCharTypedModifier =
        OnCharTypedModifier { node, event ->
            onEvent(node, event)
            if (!event.isConsumed) other.onEvent(node, event)
        }
}

fun Modifier.onCharTyped(onEvent: (UINode, CharEvent) -> Unit): Modifier =
    this then OnCharTypedModifier(onEvent)