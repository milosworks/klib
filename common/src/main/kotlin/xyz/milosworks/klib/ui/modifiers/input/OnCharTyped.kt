package xyz.milosworks.klib.ui.modifiers.input

import xyz.milosworks.klib.ui.modifiers.Modifier
import xyz.milosworks.klib.ui.nodes.UINode

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