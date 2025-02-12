package xyz.milosworks.klib.ui.modifiers

import androidx.compose.runtime.Stable
import xyz.milosworks.klib.ui.nodes.UINode

/**
 * A modifier that handles hover events on a composable.
 *
 * This modifier allows you to attach an `onHover` listener to a composable. The provided listener function
 * will be invoked when the composable is hovered over. The listener receives a [UINode] as a parameter,
 * allowing you to access the node that is being hovered.
 *
 * @param onHover A function that is triggered when the composable is hovered over. It takes a [UINode] as a
 * parameter and returns a Boolean value indicating whether the hover was handled.
 */
@Deprecated("deprecated")
data class OnHoverModifier(var onHover: (UINode) -> Boolean) : Modifier.Element<OnHoverModifier> {
	override fun mergeWith(other: OnHoverModifier): OnHoverModifier =
		OnHoverModifier { node -> (onHover(node) || other.onHover(node)) }
}

/**
 * Adds an onHover listener to a composable.
 *
 * This modifier attaches a hover listener to the composable. When the composable is hovered over, the provided
 * `onHover` function will be executed, receiving a [UINode] as a parameter.
 *
 * @param onHover A function that is triggered when the composable is hovered over. It takes a [UINode] as a
 * parameter and returns a Boolean indicating whether the hover event was handled.
 */
@Stable
@Deprecated("deprecated")
fun Modifier.onHover(onHover: (UINode) -> Boolean): Modifier = this then OnHoverModifier(onHover)