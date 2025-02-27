package xyz.milosworks.klib.ui.modifiers

import xyz.milosworks.klib.ui.nodes.UINode

data class OnCharTypedModifier(
	val onEvent: (UINode, codePoint: Char, modifiers: Int) -> Boolean
) : Modifier.Element<OnCharTypedModifier> {
	override fun mergeWith(other: OnCharTypedModifier): OnCharTypedModifier =
		OnCharTypedModifier { n, p, m -> onEvent(n, p, m) || other.onEvent(n, p, m) }
}

fun Modifier.onCharTyped(onEvent: (UINode, codePoint: Char, modifiers: Int) -> Boolean): Modifier =
	this then OnCharTypedModifier(onEvent)