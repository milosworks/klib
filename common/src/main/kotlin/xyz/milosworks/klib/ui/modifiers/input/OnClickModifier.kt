package xyz.milosworks.klib.ui.modifiers.input

import androidx.compose.runtime.Stable
import xyz.milosworks.klib.ui.base.ui1.nodes.UINode
import xyz.milosworks.klib.ui.modifiers.core.Modifier

/**
 * A modifier that handles click events on a composable.
 *
 * This modifier allows you to attach an `onClick` listener to a composable. The provided listener function
 * will be invoked when the composable is clicked. The listener receives a [UINode] as a parameter, allowing
 * you to access the node that was clicked.
 *
 * @param onClick A function that is triggered when the composable is clicked. It takes a [UINode] as a parameter
 * and returns a Boolean value indicating whether the click was handled.
 */
@Deprecated("deprecated")
data class OnClickModifier(var onClick: (UINode) -> Boolean) : Modifier.Element<OnClickModifier> {
    override fun mergeWith(other: OnClickModifier): OnClickModifier =
        OnClickModifier { node -> (onClick(node) || other.onClick(node)) }
}

/**
 * Adds an onClick listener to a composable.
 *
 * This modifier attaches a click listener to the composable. When the composable is clicked, the provided
 * `onClick` function will be executed, receiving a [UINode] as a parameter.
 *
 * @param onClick A function that is triggered when the composable is clicked. It takes a [UINode] as a parameter
 * and returns a Boolean indicating whether the click was handled.
 */
@Stable
@Deprecated("deprecated")
fun Modifier.onClick(onClick: (UINode) -> Boolean): Modifier = this then OnClickModifier(onClick)
