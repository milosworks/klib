package xyz.milosworks.klib.ui.modifiers

import xyz.milosworks.klib.ui.nodes.UINode

data class OnKeyEventModifier(
	val onEvent: (UINode, keyCode: Int, scanCode: Int, modifiers: Int) -> Boolean
) : Modifier.Element<OnKeyEventModifier> {
	override fun mergeWith(other: OnKeyEventModifier): OnKeyEventModifier =
		OnKeyEventModifier { n, k, s, m -> onEvent(n, k, s, m) || other.onEvent(n, k, s, m) }
}

fun Modifier.onKeyEvent(onEvent: (UINode, keyCode: Int, scanCode: Int, modifiers: Int) -> Boolean): Modifier =
	this then OnKeyEventModifier(onEvent)