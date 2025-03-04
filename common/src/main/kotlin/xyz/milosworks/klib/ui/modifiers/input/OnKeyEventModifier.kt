package xyz.milosworks.klib.ui.modifiers.input

import xyz.milosworks.klib.ui.modifiers.Modifier
import xyz.milosworks.klib.ui.nodes.UINode

data class OnKeyEventModifier(
	val onEvent: (UINode, KeyEvent) -> Unit
) : Modifier.Element<OnKeyEventModifier> {
	override fun mergeWith(other: OnKeyEventModifier): OnKeyEventModifier =
		OnKeyEventModifier { node, event ->
			onEvent(node, event)
			if (!event.isConsumed) other.onEvent(node, event)
		}
}

fun Modifier.onKeyEvent(onEvent: (UINode, KeyEvent) -> Unit): Modifier =
	this then OnKeyEventModifier(onEvent)